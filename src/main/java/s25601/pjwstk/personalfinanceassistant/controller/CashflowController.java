package s25601.pjwstk.personalfinanceassistant.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import s25601.pjwstk.personalfinanceassistant.model.*;
import s25601.pjwstk.personalfinanceassistant.repository.*;
import s25601.pjwstk.personalfinanceassistant.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cashflow")
public class CashflowController {

    @Autowired
    private CashflowRepository cashflowRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private List<Account> getAccessibleAccounts(User user) {
        List<Account> owned = accountRepository.findByUserId(user.getId());
        List<Account> shared = accountRepository.findBySharedUsersId(user.getId());
        owned.addAll(shared);
        return owned.stream().distinct().collect(Collectors.toList());
    }

    private void recalculateAccountBalance(Account account) {
        // Sum of all incomes for this account
        BigDecimal totalIncome = cashflowRepository.findByAccount(account).stream()
                .filter(cf -> cf.getType() == CashflowType.INCOME)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum of all expenses for this account
        BigDecimal totalExpense = cashflowRepository.findByAccount(account).stream()
                .filter(cf -> cf.getType() == CashflowType.EXPENSE)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        account.setBalance(totalIncome.subtract(totalExpense));
        accountRepository.save(account);
    }

    @GetMapping
    public String listCashflows(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String account,
            Model model) {

        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        LocalDate fromDate = null;
        LocalDate toDate = null;
        String dateError = null;

        try {
            if (dateFrom != null && !dateFrom.isEmpty()) {
                fromDate = LocalDate.parse(dateFrom);
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                toDate = LocalDate.parse(dateTo);
            }
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                dateError = "Date From cannot be after Date To";
            }
        } catch (Exception e) {
            dateError = "Invalid date format";
        }

        List<Account> accessibleAccounts = getAccessibleAccounts(user);
        List<Long> accessibleAccountIds = accessibleAccounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        List<Cashflow> cashflows = List.of();

        if (dateError == null) {
            // <-- IMPORTANT: fetch cashflows by accessible account IDs, NOT by user
            cashflows = cashflowRepository.findByAccountIdIn(accessibleAccountIds);

            final LocalDate fromDateFinal = fromDate;
            final LocalDate toDateFinal = toDate;

            // Filter cashflows by optional parameters
            cashflows = cashflows.stream()
                    .filter(cf -> {
                        boolean match = true;
                        if (type != null && !type.isEmpty()) {
                            match &= cf.getType().name().equalsIgnoreCase(type);
                        }
                        if (category != null && !category.isEmpty()) {
                            String cat = cf.getType() == CashflowType.INCOME ?
                                    (cf.getIncomeCategory() != null ? cf.getIncomeCategory().name() : "") :
                                    (cf.getExpenseCategory() != null ? cf.getExpenseCategory().name() : "");
                            match &= cat.equalsIgnoreCase(category);
                        }
                        if (fromDateFinal != null) {
                            match &= !cf.getDate().isBefore(fromDateFinal);
                        }
                        if (toDateFinal != null) {
                            match &= !cf.getDate().isAfter(toDateFinal);
                        }
                        if (account != null && !account.isEmpty()) {
                            match &= cf.getAccount() != null && cf.getAccount().getName().equalsIgnoreCase(account);
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
        }

        if (dateError != null) {
            model.addAttribute("dateError", dateError);
        }

        BigDecimal filteredTotal = cashflows.stream()
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal incomeTotal = cashflows.stream()
                .filter(c -> c.getType() == CashflowType.INCOME)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenseTotal = cashflows.stream()
                .filter(c -> c.getType() == CashflowType.EXPENSE)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netTotal = incomeTotal.subtract(expenseTotal);

        model.addAttribute("cashflows", cashflows);
        model.addAttribute("filteredTotal", filteredTotal);
        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("expenseTotal", expenseTotal);
        model.addAttribute("netTotal", netTotal);

        model.addAttribute("incomeCategories", IncomeCategory.values());
        model.addAttribute("expenseCategories", ExpenseCategory.values());

        model.addAttribute("accounts", accessibleAccounts);

        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        model.addAttribute("selectedAccount", account);

        return "cashflow_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) return "redirect:/login";

        Cashflow cashflow = new Cashflow();
        cashflow.setDate(LocalDate.now());
        cashflow.setType(CashflowType.INCOME);
        model.addAttribute("cashflow", cashflow);

        model.addAttribute("types", CashflowType.values());
        model.addAttribute("accounts", getAccessibleAccounts(user));
        model.addAttribute("incomeCategories", IncomeCategory.values());
        model.addAttribute("expenseCategories", ExpenseCategory.values());

        return "cashflow_form";
    }

    @PostMapping("/add")
    public String addCashflow(@Valid @ModelAttribute("cashflow") Cashflow cashflow,
                              BindingResult result,
                              Model model) {
        User user = userService.getCurrentUser();
        if (user == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("types", CashflowType.values());
            model.addAttribute("accounts", getAccessibleAccounts(user));
            model.addAttribute("incomeCategories", IncomeCategory.values());
            model.addAttribute("expenseCategories", ExpenseCategory.values());
            return "cashflow_form";
        }

        if (cashflow.getAccount() != null && cashflow.getAccount().getId() != null) {
            Account account = accountRepository.findById(cashflow.getAccount().getId()).orElse(null);
            if (account == null || !getAccessibleAccounts(user).contains(account)) {
                result.rejectValue("account", "error.cashflow", "Selected account not accessible.");
                model.addAttribute("types", CashflowType.values());
                model.addAttribute("accounts", getAccessibleAccounts(user));
                model.addAttribute("incomeCategories", IncomeCategory.values());
                model.addAttribute("expenseCategories", ExpenseCategory.values());
                return "cashflow_form";
            }
            cashflow.setAccount(account);
        } else {
            cashflow.setAccount(null);
        }

        cashflow.setUser(user);

        // Fix category XOR
        if (cashflow.getType() == CashflowType.INCOME) {
            cashflow.setExpenseCategory(null);
        } else if (cashflow.getType() == CashflowType.EXPENSE) {
            cashflow.setIncomeCategory(null);
        }

        cashflowRepository.save(cashflow);

        // Recalculate balance after adding cashflow
        if (cashflow.getAccount() != null) {
            recalculateAccountBalance(cashflow.getAccount());
        }

        return "redirect:/profile";
    }

    @PostMapping("/delete/{id}")
    public String deleteCashflow(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        if (user == null) return "redirect:/login";

        Cashflow cashflow = cashflowRepository.findById(id).orElse(null);
        if (cashflow == null || !getAccessibleAccounts(user).contains(cashflow.getAccount())) {
            return "redirect:/cashflow";
        }

        Account account = cashflow.getAccount();

        cashflowRepository.delete(cashflow);

        // Recalculate balance after deletion
        if (account != null) {
            recalculateAccountBalance(account);
        }

        return "redirect:/profile";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getCurrentUser();
        if (user == null) return "redirect:/login";

        Cashflow cashflow = cashflowRepository.findById(id).orElse(null);
        if (cashflow == null || !getAccessibleAccounts(user).contains(cashflow.getAccount())) {
            return "redirect:/cashflow";
        }

        model.addAttribute("cashflow", cashflow);
        model.addAttribute("types", CashflowType.values());
        model.addAttribute("accounts", getAccessibleAccounts(user));
        model.addAttribute("incomeCategories", IncomeCategory.values());
        model.addAttribute("expenseCategories", ExpenseCategory.values());

        return "cashflow_form";
    }

    @PostMapping("/edit/{id}")
    public String updateCashflow(@PathVariable Long id,
                                 @Valid @ModelAttribute("cashflow") Cashflow updatedCashflow,
                                 BindingResult result,
                                 Model model) {
        User user = userService.getCurrentUser();
        if (user == null) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("types", CashflowType.values());
            model.addAttribute("accounts", getAccessibleAccounts(user));
            model.addAttribute("incomeCategories", IncomeCategory.values());
            model.addAttribute("expenseCategories", ExpenseCategory.values());
            return "cashflow_form";
        }

        Cashflow existingCashflow = cashflowRepository.findById(id).orElse(null);
        if (existingCashflow == null || !getAccessibleAccounts(user).contains(existingCashflow.getAccount())) {
            return "redirect:/cashflow";
        }

        List<Account> accessibleAccounts = getAccessibleAccounts(user);

        Account newAccount = null;
        if (updatedCashflow.getAccount() != null && updatedCashflow.getAccount().getId() != null) {
            newAccount = accountRepository.findById(updatedCashflow.getAccount().getId()).orElse(null);
            if (newAccount == null || !accessibleAccounts.contains(newAccount)) {
                result.rejectValue("account", "error.cashflow", "Selected account not accessible.");
                model.addAttribute("types", CashflowType.values());
                model.addAttribute("accounts", accessibleAccounts);
                model.addAttribute("incomeCategories", IncomeCategory.values());
                model.addAttribute("expenseCategories", ExpenseCategory.values());
                return "cashflow_form";
            }
        }

        Account oldAccount = existingCashflow.getAccount();

        // Update fields
        existingCashflow.setAccount(newAccount);
        existingCashflow.setType(updatedCashflow.getType());
        existingCashflow.setAmount(updatedCashflow.getAmount());
        existingCashflow.setDate(updatedCashflow.getDate());
        existingCashflow.setDescription(updatedCashflow.getDescription());

        if (existingCashflow.getType() == CashflowType.INCOME) {
            existingCashflow.setIncomeCategory(updatedCashflow.getIncomeCategory());
            existingCashflow.setExpenseCategory(null);
        } else if (existingCashflow.getType() == CashflowType.EXPENSE) {
            existingCashflow.setExpenseCategory(updatedCashflow.getExpenseCategory());
            existingCashflow.setIncomeCategory(null);
        }

        cashflowRepository.save(existingCashflow);

        // Recalculate balance for both old and new accounts if they differ
        if (oldAccount != null) {
            recalculateAccountBalance(oldAccount);
        }
        if (newAccount != null && !newAccount.equals(oldAccount)) {
            recalculateAccountBalance(newAccount);
        }

        return "redirect:/profile";
    }
}