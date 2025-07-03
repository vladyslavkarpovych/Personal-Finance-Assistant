package s25601.pjwstk.personalfinanceassistant.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import s25601.pjwstk.personalfinanceassistant.model.*;
import s25601.pjwstk.personalfinanceassistant.repository.*;
import s25601.pjwstk.personalfinanceassistant.service.AccountService;
import s25601.pjwstk.personalfinanceassistant.service.NotificationService;
import s25601.pjwstk.personalfinanceassistant.service.UserService;
import s25601.pjwstk.personalfinanceassistant.util.BudgetPeriodUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private CashflowRepository cashflowRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;


    @GetMapping("")
    public String showUserProfile(Model model) {

        User user = userService.getAuthenticatedUser();

        model.addAttribute("user", user);

        List<Account> accounts = accountService.getAccessibleAccounts(user);
        model.addAttribute("accounts", accounts);

        List<Cashflow> cashflows = cashflowRepository.findByAccountIn(accounts);
        model.addAttribute("cashflows", cashflows);

        BigDecimal totalIncome = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = cashflows.stream()
                .filter(cf -> cf.getType() == CashflowType.EXPENSE)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netTotal = totalIncome.subtract(totalExpense);

        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("netTotal", netTotal);

        List<Budget> budgets = budgetRepository.findByUser(user);
        LocalDate today = LocalDate.now();

        for (Budget budget : budgets) {
            LocalDate[] periodRange = BudgetPeriodUtil.getPeriodStartEnd(budget.getPeriod(), today);
            LocalDate startDate = periodRange[0];
            LocalDate endDate = periodRange[1];

            BigDecimal spent = cashflowRepository.findByUserId(user.getId()).stream()
                    .filter(cf -> cf.getType() == CashflowType.EXPENSE)
                    .filter(cf -> cf.getExpenseCategory() == budget.getCategory())
                    .filter(cf -> {
                        LocalDate date = cf.getDate();
                        return (date != null) && (!date.isBefore(startDate)) && (!date.isAfter(endDate));
                    })
                    .map(Cashflow::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            budget.setRemaining(budget.getLimitAmount().subtract(spent));

            if (budget.getRemaining().compareTo(BigDecimal.ZERO) < 0) {
                notificationService.notifyBudgetExceeded(
                        user,
                        budget.getCategory().name(),
                        budget.getPeriod().name(),
                        budget.getLimitAmount().toString(),
                        spent.toString()
                );
            }
        }

        List<Notification> notifications = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentUser", user);
        model.addAttribute("budgets", budgets);

        List<Goal> goals = goalRepository.findByUser(user);
        // Sort using Comparable in Goal
        Collections.sort(goals);
        model.addAttribute("goals", goals);

        Map<Long, BigDecimal> goalProgressMap = new HashMap<>();
        for (Goal goal : goals) {
            BigDecimal targetAmount = goal.getTargetAmount() != null ? goal.getTargetAmount() : BigDecimal.ZERO;
            BigDecimal progressAmount = netTotal.min(targetAmount);
            goalProgressMap.put(goal.getId(), progressAmount);
        }

        model.addAttribute("goals", goals);
        model.addAttribute("goalProgressMap", goalProgressMap);

        return "profile_view";
    }

    @GetMapping("/goals/add")
    public String showAddGoalForm(Model model) {
        model.addAttribute("financialGoal", new Goal());
        return "goal_form";
    }

    @PostMapping("/goals/add")
    public String addGoal(@Valid @ModelAttribute Goal financialGoal,
                          BindingResult result,
                          Model model) {
        User user = userService.getAuthenticatedUser();
        financialGoal.setUser(user);

        if (result.hasErrors()) {
            return "goal_form";
        }

        goalRepository.save(financialGoal);
        return "redirect:/profile";
    }

    // Show edit form
    @GetMapping("/goals/edit/{id}")
    public String showEditGoalForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid goal Id:" + id));
        model.addAttribute("financialGoal", goal);
        return "goal_form";
    }

    // Update goal
    @PostMapping("/goals/edit/{id}")
    public String updateGoal(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("financialGoal") Goal financialGoal,
                             BindingResult result, Model model) {
        User user = userService.getAuthenticatedUser();

        if (result.hasErrors()) {
            return "goal_form";
        }

        Goal existingGoal = goalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid goal Id:" + id));

        // Update fields manually to avoid changing user and id
        existingGoal.setName(financialGoal.getName());
        existingGoal.setTargetAmount(financialGoal.getTargetAmount());
        existingGoal.setDueDate(financialGoal.getDueDate());
        existingGoal.setCurrentAmount(financialGoal.getCurrentAmount());

        goalRepository.save(existingGoal);

        return "redirect:/profile";
    }

    // Delete goal
    @GetMapping("/goals/delete/{id}")
    public String deleteGoal(@PathVariable("id") Long id) {
        User user = userService.getAuthenticatedUser();
        Goal goal = goalRepository.findById(id)
                .filter(g -> g.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid goal Id:" + id));
        goalRepository.delete(goal);
        return "redirect:/profile";
    }
}