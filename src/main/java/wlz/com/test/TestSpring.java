package wlz.com.test;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import wlz.com.dao.AccountDao;
import wlz.com.domain.Account;
import wlz.com.service.AccountService;
import java.io.InputStream;
import java.util.List;


public class TestSpring {

    @Test
    public void run1(){

        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:spring.xml");
        AccountService accountService = ac.getBean("accountService", AccountService.class);
        accountService.findAll();
        System.out.println();
    }

    @Test
    public void run2() throws Exception {

        InputStream in = Resources.getResourceAsStream("mybatis.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        SqlSession session = factory.openSession();
        AccountDao dao = session.getMapper(AccountDao.class);

        List<Account> accounts = dao.findAll();
        for (Account account : accounts) {
            System.out.println(account);
        }

        Account account3 = new Account();
        account3.setName("汪苏泷");
        account3.setMoney(3000.0);
        dao.saveAccount(account3);
        session.commit();
        session.close();
        in.close();
    }

    @Test
    public void run3() throws Exception {

        InputStream in = Resources.getResourceAsStream("mybatis.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        SqlSession session = factory.openSession();
        AccountDao dao = session.getMapper(AccountDao.class);

        Account account = new Account();
        account.setName("汪苏泷");
        account.setMoney(3000.0);
        dao.saveAccount(account);

        session.close();
        in.close();
    }
}
