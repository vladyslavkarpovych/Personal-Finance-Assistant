package s25601.pjwstk.personalfinanceassistant.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import s25601.pjwstk.personalfinanceassistant.model.Account;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.AccountRepository;
import s25601.pjwstk.personalfinanceassistant.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping
    public String viewAccounts(Model model) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        List<Account> accounts = accountRepository.findByUserId(user.getId());
        model.addAttribute("accounts", accounts);
        return "account_list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("account", new Account());
        return "account_form";
    }

    @PostMapping("/create")
    public String createAccount(@Valid @ModelAttribute("account") Account account,
                                BindingResult result) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        if (result.hasErrors()) {
            return "account_form";
        }

        account.setUser(user);
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        accountRepository.save(account);
        return "redirect:/accounts";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = getCurrentUser();
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isEmpty() || !optionalAccount.get().getUser().equals(user)) {
            return "redirect:/accounts";
        }

        model.addAttribute("account", optionalAccount.get());
        return "account_form";
    }

    @PostMapping("/edit/{id}")
    public String updateAccount(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("account") Account updatedAccount,
                                BindingResult result) {
        User user = getCurrentUser();
        if (user == null) return "redirect:/login";

        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isEmpty() || !optionalAccount.get().getUser().equals(user)) {
            return "redirect:/accounts";
        }

        if (result.hasErrors()) {
            return "account_form";
        }

        Account existingAccount = optionalAccount.get();
        existingAccount.setName(updatedAccount.getName());
        existingAccount.setCurrency(updatedAccount.getCurrency());
        existingAccount.setBalance(updatedAccount.getBalance());

        accountRepository.save(existingAccount);
        return "redirect:/accounts";
    }

    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable("id") Long id) {
        User user = getCurrentUser();
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isPresent() && optionalAccount.get().getUser().equals(user)) {
            accountRepository.delete(optionalAccount.get());
        }
        return "redirect:/accounts";
    }
}