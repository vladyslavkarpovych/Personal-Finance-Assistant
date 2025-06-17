package s25601.pjwstk.personalfinanceassistant.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import s25601.pjwstk.personalfinanceassistant.model.*;
import s25601.pjwstk.personalfinanceassistant.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import s25601.pjwstk.personalfinanceassistant.service.NotificationService;
import s25601.pjwstk.personalfinanceassistant.util.BudgetPeriodUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CashflowRepository cashflowRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("")
    public String showUserProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        List<Cashflow> cashflows = cashflowRepository.findByUserId(user.getId());
        model.addAttribute("cashflows", cashflows);

        List<Account> accounts = accountRepository.findByUserId(user.getId());

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

            // Check if exceeded and notify
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

        model.addAttribute("budgets", budgets);

        return "profile_view";
    }


    @GetMapping("/create")
    public String showCreateProfileForm(Model model) {
        model.addAttribute("profile", new Profile());
        return "profile_form";
    }

    @PostMapping("/create")
    public String createProfile(@Valid @ModelAttribute("profile") Profile profile,
                                BindingResult result,
                                Model model) {
        User user = getCurrentUser();
        if (user == null) {
            result.reject("user", "No logged-in user found.");
            return "profile_form";
        }

        profile.setUser(user);

        if (result.hasErrors()) {
            return "profile_form";
        }

        profileRepository.save(profile);
        model.addAttribute("message", "Profile created successfully!");
        return "profile_success";
    }

    // Helper method to get the currently logged-in user entity
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        }
        return null;
    }
}