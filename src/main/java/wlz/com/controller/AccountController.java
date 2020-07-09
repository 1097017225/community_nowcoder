package wlz.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import wlz.com.domain.Account;
import wlz.com.service.AccountService;
import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @RequestMapping("/findAll")
    public String findAll(Model model){

        List<Account> accounts =  accountService.findAll();
        model.addAttribute("accounts", accounts);
        System.out.println();
        return "list";
    }

    @RequestMapping("/save")
    public String saveAccount(Account account){

        accountService.saveAccount(account);
        return "list";
    }

    @RequestMapping("/transfer")
    public String transfer(String name1, String name2, Double money){

        accountService.transfer(name1, name2, money);
        return "success";
    }
}
