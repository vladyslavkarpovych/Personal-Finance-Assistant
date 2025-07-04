package s25601.pjwstk.personalfinanceassistant.service;

import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import s25601.pjwstk.personalfinanceassistant.model.Account;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.AccountRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    public List<Account> getAccessibleAccounts(User user) {
        List<Account> ownedAccounts = accountRepository.findByUserId(user.getId());
        List<Account> sharedAccounts = accountRepository.findBySharedUsersId(user.getId());
        return Stream.concat(ownedAccounts.stream(), sharedAccounts.stream())
                .distinct()
                .toList();
    }

    public boolean isAccountOwner(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getUser().getId().equals(userService.getCurrentUser().getId());
    }

    public void addAccountDetailsToModel(Long id, Model model) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Set<User> accountSharedUsers = account.getSharedUsers();
        model.addAttribute("account", account);
        model.addAttribute("accountSharedUsers", accountSharedUsers);
    }
}

