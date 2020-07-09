package wlz.com.service;

import wlz.com.domain.Account;
import java.util.List;

public interface AccountService {

    public List<Account> findAll();

    public void saveAccount(Account account);

    public void transfer(String sourceName, String targetName, Double money);
}
