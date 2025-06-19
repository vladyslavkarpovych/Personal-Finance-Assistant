package s25601.pjwstk.personalfinanceassistant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import s25601.pjwstk.personalfinanceassistant.model.Account;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.AccountRepository;

import java.util.List;
import java.util.stream.Stream;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public List<Account> getAccessibleAccounts(User user) {
        List<Account> ownedAccounts = accountRepository.findByUserId(user.getId());
        List<Account> sharedAccounts = accountRepository.findBySharedUsersId(user.getId());
        return Stream.concat(ownedAccounts.stream(), sharedAccounts.stream())
                .distinct()
                .toList();
    }

    public boolean canShareWithUser(User user) {
        long totalAccounts = getAccessibleAccounts(user).size();
        return totalAccounts < User.MAX_PROFILES;
    }
}