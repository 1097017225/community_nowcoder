package wlz.com.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import wlz.com.domain.Account;

import java.util.List;

public interface AccountDao {

    @Select("select * from account")
    public List<Account> findAll();

    @Insert("insert into account(name,money) values(#{name}, #{money})")
    public void saveAccount(Account account);

    @Update("update account set money=#{money} where name = #{name}")
    public void updateAccount(Account account);

    @Select("select * from account where name = #{accountName}")
    public Account findAccountByName(String accountName);
}
