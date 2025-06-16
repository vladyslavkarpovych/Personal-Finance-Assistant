package s25601.pjwstk.personalfinanceassistant.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import s25601.pjwstk.personalfinanceassistant.model.*;
import s25601.pjwstk.personalfinanceassistant.repository.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cashflow")
public class CashflowController {

    @Autowired
    private CashflowRepository cashflowRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

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

    @GetMapping
    public String listCashflows(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<Cashflow> cashflows = cashflowRepository.findByUserId(user.getId());
        model.addAttribute("cashflows", cashflows);

        BigDecimal incomeTotal = cashflows.stream()
                .filter(c -> c.getType() == CashflowType.INCOME)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenseTotal = cashflows.stream()
                .filter(c -> c.getType() == CashflowType.EXPENSE)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netTotal = incomeTotal.subtract(expenseTotal);

        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("expenseTotal", expenseTotal);
        model.addAttribute("netTotal", netTotal);

        return "cashflow_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Cashflow cashflow = new Cashflow();
        cashflow.setDate(java.time.LocalDate.now());
        cashflow.setType(CashflowType.INCOME);  // default to INCOME for better UX
        model.addAttribute("cashflow", cashflow);

        model.addAttribute("types", CashflowType.values());

        List<Account> accounts = accountRepository.findByUserId(user.getId());
        model.addAttribute("accounts", accounts);

        // Add income and expense categories
        model.addAttribute("incomeCategories", IncomeCategory.values());
        model.addAttribute("expenseCategories", ExpenseCategory.values());

        return "cashflow_form";
    }

    @PostMapping("/add")
    public String addCashflow(@Valid @ModelAttribute("cashflow") Cashflow cashflow,
                              BindingResult result,
                              Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("types", CashflowType.values());
            model.addAttribute("accounts", accountRepository.findByUserId(user.getId()));
            model.addAttribute("incomeCategories", IncomeCategory.values());
            model.addAttribute("expenseCategories", ExpenseCategory.values());
            return "cashflow_form";
        }

        if (cashflow.getAccount() != null && cashflow.getAccount().getId() != null) {
            Account account = accountRepository.findById(cashflow.getAccount().getId()).orElse(null);
            cashflow.setAccount(account);
        } else {
            cashflow.setAccount(null);
        }

        cashflow.setUser(user);

        // Update account balance only for INCOME, NOT for EXPENSE
        if (cashflow.getAccount() != null && cashflow.getAmount() != null) {
            Account account = cashflow.getAccount();
            if (cashflow.getType() == CashflowType.INCOME) {
                account.setBalance(account.getBalance().add(cashflow.getAmount()));
                cashflow.setExpenseCategory(null);  // clear unused
                accountRepository.save(account);
            } else if (cashflow.getType() == CashflowType.EXPENSE) {
                // Do NOT update account balance for expenses
                cashflow.setIncomeCategory(null);  // clear unused
            }
        }

        cashflowRepository.save(cashflow);

        return "redirect:/profile";
    }

    @PostMapping("/delete/{id}")
    public String deleteCashflow(@PathVariable Long id) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Cashflow cashflow = cashflowRepository.findById(id).orElse(null);
        if (cashflow == null || !cashflow.getUser().getId().equals(user.getId())) {
            // cashflow not found or does not belong to current user
            return "redirect:/cashflow";
        }

        Account account = cashflow.getAccount();
        if (account != null && cashflow.getAmount() != null) {
            // Reverse only INCOME cashflow effect on account balance before deleting
            if (cashflow.getType() == CashflowType.INCOME) {
                account.setBalance(account.getBalance().subtract(cashflow.getAmount()));
                accountRepository.save(account);
            }
            // Do NOT reverse expenses on account balance
        }

        cashflowRepository.delete(cashflow);

        return "redirect:/profile";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        Cashflow cashflow = cashflowRepository.findById(id).orElse(null);
        if (cashflow == null || !cashflow.getUser().getId().equals(user.getId())) {
            return "redirect:/cashflow";
        }

        model.addAttribute("cashflow", cashflow);
        model.addAttribute("types", CashflowType.values());

        List<Account> accounts = accountRepository.findByUserId(user.getId());
        model.addAttribute("accounts", accounts);

        model.addAttribute("incomeCategories", IncomeCategory.values());
        model.addAttribute("expenseCategories", ExpenseCategory.values());

        return "cashflow_form";
    }

    @PostMapping("/edit/{id}")
    public String updateCashflow(@PathVariable Long id,
                                 @Valid @ModelAttribute("cashflow") Cashflow updatedCashflow,
                                 BindingResult result,
                                 Model model) {
        User user = getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("types", CashflowType.values());
            model.addAttribute("accounts", accountRepository.findByUserId(user.getId()));
            model.addAttribute("incomeCategories", IncomeCategory.values());
            model.addAttribute("expenseCategories", ExpenseCategory.values());
            return "cashflow_form";
        }

        Cashflow existingCashflow = cashflowRepository.findById(id).orElse(null);
        if (existingCashflow == null || !existingCashflow.getUser().getId().equals(user.getId())) {
            return "redirect:/profile";
        }

        // Reverse old income effect if needed
        Account oldAccount = existingCashflow.getAccount();
        if (oldAccount != null && existingCashflow.getType() == CashflowType.INCOME && existingCashflow.getAmount() != null) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(existingCashflow.getAmount()));
            accountRepository.save(oldAccount);
        }

        // Update existingCashflow fields with updatedCashflow values
        existingCashflow.setDescription(updatedCashflow.getDescription());
        existingCashflow.setType(updatedCashflow.getType());
        existingCashflow.setAmount(updatedCashflow.getAmount());
        existingCashflow.setDate(updatedCashflow.getDate());

        if (updatedCashflow.getAccount() != null && updatedCashflow.getAccount().getId() != null) {
            Account newAccount = accountRepository.findById(updatedCashflow.getAccount().getId()).orElse(null);
            existingCashflow.setAccount(newAccount);
        } else {
            existingCashflow.setAccount(null);
        }

        existingCashflow.setIncomeCategory(updatedCashflow.getIncomeCategory());
        existingCashflow.setExpenseCategory(updatedCashflow.getExpenseCategory());

        cashflowRepository.save(existingCashflow);

        // Apply new income effect if applicable
        Account newAccount = existingCashflow.getAccount();
        if (newAccount != null && existingCashflow.getType() == CashflowType.INCOME && existingCashflow.getAmount() != null) {
            newAccount.setBalance(newAccount.getBalance().add(existingCashflow.getAmount()));
            accountRepository.save(newAccount);
        }

        return "redirect:/profile";
    }
}
