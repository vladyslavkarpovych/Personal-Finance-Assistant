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
import s25601.pjwstk.personalfinanceassistant.service.AccountService;
import s25601.pjwstk.personalfinanceassistant.service.UserService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    // Limiting the total accounts to User.MAX_PROFILES
    private boolean canShareWithUser(User user) {
        long totalAccounts = accountService.getAccessibleAccounts(user).size();
        return totalAccounts < User.MAX_PROFILES;
    }

    // List accounts accessible by current user
    @GetMapping
    public String viewAccounts(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) return "redirect:/login";

        List<Account> accounts = accountService.getAccessibleAccounts(currentUser);

        Map<Long, String> filteredSharedUsernames = new HashMap<>();
        for (Account account : accounts) {
            Set<User> allRelatedUsers = new HashSet<>(account.getSharedUsers());
            allRelatedUsers.add(account.getUser());

            String sharedNames = allRelatedUsers.stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .map(User::getUsername)
                    .sorted()
                    .collect(Collectors.joining(", "));

            filteredSharedUsernames.put(account.getId(), sharedNames);
        }

        model.addAttribute("accounts", accounts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("filteredSharedUsernames", filteredSharedUsernames);

        return "account_list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        User user = userService.getCurrentUser();
        List<Account> accessibleAccounts = accountService.getAccessibleAccounts(user);
        long totalAccounts = accessibleAccounts.size();

        model.addAttribute("maxAccountsReached", totalAccounts >= User.MAX_PROFILES);
        model.addAttribute("userAccountCount", totalAccounts);
        model.addAttribute("account", new Account());
        model.addAttribute("sharedUsernames", "");
        return "account_form";
    }

    @PostMapping("/create")
    public String createAccount(@Valid @ModelAttribute("account") Account account,
                                BindingResult result,
                                @RequestParam(name = "sharedUsernames", required = false) String sharedUsernames,
                                Model model) {
        User user = userService.getCurrentUser();
        if (user == null) return "redirect:/login";

        // Enforce max accounts limit per user
        long totalAccounts = accountService.getAccessibleAccounts(user).size();
        if (totalAccounts >= User.MAX_PROFILES) {
            result.reject("maxAccounts", "You cannot have more than " + User.MAX_PROFILES + " accounts (owned + shared).");
            return "account_form";
        }

        if (result.hasErrors()) {
            return "account_form";
        }

        account.setUser(user);
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        if (sharedUsernames != null && !sharedUsernames.trim().isEmpty()) {
            for (String username : sharedUsernames.split(",")) {
                username = username.trim();
                if (username.isEmpty()) continue;

                Optional<User> userToShareWith = userRepository.findByUsername(username);
                if (userToShareWith.isEmpty()) {
                    model.addAttribute("account", account);
                    model.addAttribute("sharedUsernames", sharedUsernames);
                    model.addAttribute("error", "User '" + username + "' not found.");
                    return "account_form";
                }

                User shareUser = userToShareWith.get();
                if (shareUser.equals(user)) {
                    model.addAttribute("account", account);
                    model.addAttribute("sharedUsernames", sharedUsernames);
                    model.addAttribute("error", "Cannot share account with yourself.");
                    return "account_form";
                }

                // Prevent sharing with self
                if (!canShareWithUser(shareUser)) {
                    model.addAttribute("account", account);
                    model.addAttribute("sharedUsernames", sharedUsernames);
                    model.addAttribute("error", "User '" + username + "' already has the maximum number of accounts.");
                    return "account_form";
                }

                account.getSharedUsers().add(shareUser);
            }
        }

        accountRepository.save(account);
        return "redirect:/accounts";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getCurrentUser();
        Optional<Account> optionalAccount = accountRepository.findById(id);

        // Only allow owner to edit the account
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
        User user = userService.getCurrentUser();
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
        User user = userService.getCurrentUser();
        Optional<Account> optionalAccount = accountRepository.findById(id);

        // Delete only if current user owns the account
        if (optionalAccount.isPresent() && optionalAccount.get().getUser().equals(user)) {
            accountRepository.delete(optionalAccount.get());
        }

        return "redirect:/accounts";
    }

    // Show form to share account with another user
    @GetMapping("/share/{id}")
    public String showShareForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getCurrentUser();
        Optional<Account> optionalAccount = accountRepository.findById(id);

        // Only owner can access the share form
        if (optionalAccount.isEmpty() || !optionalAccount.get().getUser().equals(user)) {
            return "redirect:/accounts";
        }

        model.addAttribute("account", optionalAccount.get());
        return "account_share_form";
    }

    // Handle sharing account with another user
    @PostMapping("/share/{id}")
    public String shareAccount(@PathVariable("id") Long id,
                               @RequestParam("username") String username,
                               Model model) {
        User user = userService.getCurrentUser();
        Optional<Account> optionalAccount = accountRepository.findById(id);

        // Only owner can share the account
        if (optionalAccount.isEmpty() || !optionalAccount.get().getUser().equals(user)) {
            return "redirect:/accounts";
        }

        Account account = optionalAccount.get();
        Optional<User> userToShareWith = userRepository.findByUsername(username);

        // Validate that user to share with exists
        if (userToShareWith.isEmpty()) {
            model.addAttribute("account", account);
            model.addAttribute("error", "User '" + username + "' not found.");
            return "account_share_form";
        }

        User shareUser = userToShareWith.get();

        // Prevent sharing with self
        if (shareUser.equals(user)) {
            model.addAttribute("account", account);
            model.addAttribute("error", "Cannot share account with yourself.");
            return "account_share_form";
        }

        // Check if the user can still have more accounts shared with
        if (!canShareWithUser(shareUser)) {
            model.addAttribute("account", account);
            model.addAttribute("error", "User '" + username + "' already has the maximum number of accounts.");
            return "account_share_form";
        }

        if (!account.getSharedUsers().contains(shareUser)) {
            account.getSharedUsers().add(shareUser);
            accountRepository.save(account);
        }

        return "redirect:/accounts";
    }

    @PostMapping("/share/{id}/remove")
    public String removeSharedUser(@PathVariable("id") Long id,
                                   @RequestParam("userId") Long userId) {
        User user = userService.getCurrentUser();
        Optional<Account> optionalAccount = accountRepository.findById(id);

        // Only owner can remove shared users
        if (optionalAccount.isEmpty() || !optionalAccount.get().getUser().equals(user)) {
            return "redirect:/accounts";
        }

        // Remove user with the given ID from shared users
        Account account = optionalAccount.get();
        account.getSharedUsers().removeIf(u -> u.getId().equals(userId));
        accountRepository.save(account);

        return "redirect:/accounts/share/" + id;
    }
}