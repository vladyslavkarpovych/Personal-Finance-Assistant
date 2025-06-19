package s25601.pjwstk.personalfinanceassistant.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import s25601.pjwstk.personalfinanceassistant.model.*;
import s25601.pjwstk.personalfinanceassistant.repository.*;
import s25601.pjwstk.personalfinanceassistant.service.NotificationService;
import s25601.pjwstk.personalfinanceassistant.service.UserService;
import s25601.pjwstk.personalfinanceassistant.util.BudgetPeriodUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CashflowRepository cashflowRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    private final UserService userService;

    public BudgetController(UserService userService) {
        this.userService = userService;
    }

    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @GetMapping
    public String listBudgets(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<Budget> budgets = budgetRepository.findByUser(user);
        LocalDate today = LocalDate.now();

        for (Budget b : budgets) {
            // Calculate period start and end dates
            LocalDate[] periodRange = BudgetPeriodUtil.getPeriodStartEnd(b.getPeriod(), today);
            LocalDate startDate = periodRange[0];
            LocalDate endDate = periodRange[1];

            BigDecimal spent = cashflowRepository.findByUserId(user.getId()).stream()
                    .filter(cf -> cf.getType() == CashflowType.EXPENSE)
                    .filter(cf -> cf.getExpenseCategory() == b.getCategory())
                    .filter(cf -> {
                        LocalDate date = cf.getDate();
                        return (date != null) && (!date.isBefore(startDate)) && (!date.isAfter(endDate));
                    })
                    .map(Cashflow::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            b.setRemaining(b.getLimitAmount().subtract(spent));

            // Notify if budget exceeded
            if (b.getRemaining().compareTo(BigDecimal.ZERO) < 0) {
                notificationService.notifyBudgetExceeded(user,
                        b.getCategory().name(),
                        b.getPeriod().name(),
                        b.getLimitAmount().toString(),
                        spent.toString());
            }
        }

        model.addAttribute("budgets", budgets);
        return "budget_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("budget", new Budget());
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("periods", BudgetPeriod.values());
        return "budget_form";
    }

    @PostMapping("/add")
    public String addBudget(@Valid @ModelAttribute Budget budget, BindingResult result, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("periods", BudgetPeriod.values());
            return "budget_form";
        }

        budget.setUser(user);
        budgetRepository.save(budget);
        return "redirect:/budgets";
    }

    @PostMapping("/delete/{id}")
    public String deleteBudget(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Budget budget = budgetRepository.findById(id).orElse(null);
        if (budget != null && budget.getUser().getId().equals(user.getId())) {
            budgetRepository.delete(budget);
        }

        return "redirect:/profile";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Budget budget = budgetRepository.findById(id).orElse(null);
        if (budget == null || !budget.getUser().getId().equals(user.getId())) {
            return "redirect:/budgets"; // or error page
        }

        model.addAttribute("budget", budget);
        model.addAttribute("categories", ExpenseCategory.values());
        model.addAttribute("periods", BudgetPeriod.values());
        return "budget_form";
    }

    @PostMapping("/edit/{id}")
    public String updateBudget(@PathVariable Long id, @Valid @ModelAttribute Budget budget, BindingResult result, Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("categories", ExpenseCategory.values());
            model.addAttribute("periods", BudgetPeriod.values());
            return "budget_form";
        }

        Budget existingBudget = budgetRepository.findById(id).orElse(null);
        if (existingBudget == null || !existingBudget.getUser().getId().equals(user.getId())) {
            return "redirect:/budgets"; // or error page
        }

        // Update fields
        existingBudget.setCategory(budget.getCategory());
        existingBudget.setLimitAmount(budget.getLimitAmount());
        existingBudget.setPeriod(budget.getPeriod());

        budgetRepository.save(existingBudget);

        return "redirect:/budgets";
    }
}