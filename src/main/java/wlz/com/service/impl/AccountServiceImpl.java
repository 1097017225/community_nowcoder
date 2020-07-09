package wlz.com.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wlz.com.dao.AccountDao;
import wlz.com.domain.Account;
import wlz.com.service.AccountService;

import java.util.List;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDao dao;

    @Override
    public List<Account> findAll() {

        return dao.findAll();
    }

    @Override
    public void saveAccount(Account account) {

        dao.saveAccount(account);
    }

    @Override
    public void transfer(String sourceName, String targetName, Double money) {

        Account source = dao.findAccountByName(sourceName);
        Account target = dao.findAccountByName(targetName);
        source.setMoney(source.getMoney()-money);
        target.setMoney(target.getMoney()+money);
        dao.updateAccount(source);
        dao.updateAccount(target);
    }


}
