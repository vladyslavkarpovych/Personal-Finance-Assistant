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
        return owned.stream().distinct().toList();
    }

    @GetMapping
    public String listCashflows(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String account,
            Model model) {

        User user = userService.getAuthenticatedUser();

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

        if (dateError != null) {
            model.addAttribute("dateError", dateError);
            model.addAttribute("cashflows", List.<Cashflow>of()); // empty list
            model.addAttribute("filteredTotal", BigDecimal.ZERO);
            model.addAttribute("incomeTotal", BigDecimal.ZERO);
            model.addAttribute("expenseTotal", BigDecimal.ZERO);
            model.addAttribute("netTotal", BigDecimal.ZERO);
            model.addAttribute("incomeCategories", IncomeCategory.values());
            model.addAttribute("expenseCategories", ExpenseCategory.values());

            model.addAttribute("accounts", getAccessibleAccounts(user));

            model.addAttribute("selectedType", type);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedDateFrom", dateFrom);
            model.addAttribute("selectedDateTo", dateTo);
            model.addAttribute("selectedAccount", account);
            return "cashflow_list";
        }

        final LocalDate fromDateFinal = fromDate;
        final LocalDate toDateFinal = toDate;

        List<Cashflow> allCashflows = cashflowRepository.findByUserId(user.getId());

        List<Cashflow> filtered = allCashflows.stream().filter(cf -> {
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
                match &= !cf.getDate().isBefore(fromDateFinal);  // cf.date >= dateFrom
            }

            if (toDateFinal != null) {
                match &= !cf.getDate().isAfter(toDateFinal);  // cf.date <= dateTo
            }

            if (account != null && !account.isEmpty()) {
                match &= cf.getAccount() != null &&
                        cf.getAccount().getName().equalsIgnoreCase(account);
            }

            return match;
        }).toList();

        BigDecimal filteredTotal = filtered.stream()
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal incomeTotal = filtered.stream()
                .filter(c -> c.getType() == CashflowType.INCOME)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenseTotal = filtered.stream()
                .filter(c -> c.getType() == CashflowType.EXPENSE)
                .map(Cashflow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netTotal = incomeTotal.subtract(expenseTotal);

        model.addAttribute("cashflows", filtered);
        model.addAttribute("filteredTotal", filteredTotal);
        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("expenseTotal", expenseTotal);
        model.addAttribute("netTotal", netTotal);

        model.addAttribute("incomeCategories", IncomeCategory.values());
        model.addAttribute("expenseCategories", ExpenseCategory.values());

        // UPDATED: use accessible accounts (owned + shared)
        model.addAttribute("accounts", getAccessibleAccounts(user));

        // Pass back selected filters for form inputs
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDateFrom", dateFrom);
        model.addAttribute("selectedDateTo", dateTo);
        model.addAttribute("selectedAccount", account);

        return "cashflow_list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {

        User user = userService.getAuthenticatedUser();

        Cashflow cashflow = new Cashflow();
        cashflow.setDate(java.time.LocalDate.now());
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

        User user = userService.getAuthenticatedUser();

        if (result.hasErrors()) {
            model.addAttribute("types", CashflowType.values());

            model.addAttribute("accounts", getAccessibleAccounts(user));

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

        if (cashflow.getAccount() != null && cashflow.getAmount() != null) {
            Account account = cashflow.getAccount();
            if (cashflow.getType() == CashflowType.INCOME) {
                account.setBalance(account.getBalance().add(cashflow.getAmount()));
                cashflow.setExpenseCategory(null);
                accountRepository.save(account);
            } else if (cashflow.getType() == CashflowType.EXPENSE) {
                cashflow.setIncomeCategory(null);
            }
        }

        cashflowRepository.save(cashflow);

        return "redirect:/profile";
    }

    @PostMapping("/delete/{id}")
    public String deleteCashflow(@PathVariable Long id) {

        User user = userService.getAuthenticatedUser();

        Cashflow cashflow = cashflowRepository.findById(id).orElse(null);
        if (cashflow == null || !cashflow.getUser().getId().equals(user.getId())) {
            return "redirect:/cashflow";
        }

        Account account = cashflow.getAccount();
        if (account != null && cashflow.getAmount() != null) {
            if (cashflow.getType() == CashflowType.INCOME) {
                account.setBalance(account.getBalance().subtract(cashflow.getAmount()));
                accountRepository.save(account);
            }
        }

        cashflowRepository.delete(cashflow);

        return "redirect:/profile";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {

        User user = userService.getAuthenticatedUser();

        Cashflow cashflow = cashflowRepository.findById(id).orElse(null);
        if (cashflow == null || !cashflow.getUser().getId().equals(user.getId())) {
            return "redirect:/cashflow";
        }

        model.addAttribute("cashflow", cashflow);
        model.addAttribute("types", CashflowType.values());

        // UPDATED: use accessible accounts (owned + shared)
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

        User user = userService.getAuthenticatedUser();

        if (result.hasErrors()) {
            model.addAttribute("types", CashflowType.values());

            // UPDATED: use accessible accounts (owned + shared)
            model.addAttribute("accounts", getAccessibleAccounts(user));

            model.addAttribute("incomeCategories", IncomeCategory.values());
            model.addAttribute("expenseCategories", ExpenseCategory.values());
            return "cashflow_form";
        }

        Cashflow existingCashflow = cashflowRepository.findById(id).orElse(null);
        if (existingCashflow == null || !existingCashflow.getUser().getId().equals(user.getId())) {
            return "redirect:/profile";
        }

        Account oldAccount = existingCashflow.getAccount();
        if (oldAccount != null && existingCashflow.getType() == CashflowType.INCOME && existingCashflow.getAmount() != null) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(existingCashflow.getAmount()));
            accountRepository.save(oldAccount);
        }

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

        Account newAccount = existingCashflow.getAccount();
        if (newAccount != null && existingCashflow.getType() == CashflowType.INCOME && existingCashflow.getAmount() != null) {
            newAccount.setBalance(newAccount.getBalance().add(existingCashflow.getAmount()));
            accountRepository.save(newAccount);
        }

        return "redirect:/profile";
    }
}