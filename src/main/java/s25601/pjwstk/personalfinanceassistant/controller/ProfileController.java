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
import s25601.pjwstk.personalfinanceassistant.repository.AccountRepository;
import s25601.pjwstk.personalfinanceassistant.repository.ProfileRepository;
import s25601.pjwstk.personalfinanceassistant.repository.UserRepository;
import s25601.pjwstk.personalfinanceassistant.repository.CashflowRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
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

    @GetMapping("")
    public String showUserProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        // Fetch user's cashflows
        List<Cashflow> cashflows = cashflowRepository.findByUserId(user.getId());
        model.addAttribute("cashflows", cashflows);

        // Fetch user's accounts
        List<Account> accounts = accountRepository.findByUserId(user.getId());

        // Total income = total account balances
        BigDecimal totalIncome = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total expense = sum of expense-type cashflows
        BigDecimal totalExpense = cashflows.stream()
                .filter(cf -> cf.getType() == CashflowType.EXPENSE)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Net total = income - expense
        BigDecimal netTotal = totalIncome.subtract(totalExpense);

        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("totalExpense", totalExpense);
        model.addAttribute("netTotal", netTotal);

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