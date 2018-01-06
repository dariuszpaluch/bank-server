package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.models.Account;
import com.dariuszpaluch.bankserver.models.User;
import com.dariuszpaluch.bankserver.utils.BankAccountUtils;

import javax.validation.constraints.AssertFalse;
import java.sql.*;
import java.util.*;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class BankDAO {
  private static final Map<String, User> availableUsers = new HashMap<>(); //TODO replace this
  private static BankDAO instance = new BankDAO();
  private Connection databaseConnection = null;

  public static BankDAO getInstance() {
    return instance;
  }

  private BankDAO() {
    try {
      Class.forName("org.h2.Driver");
      databaseConnection = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
      databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS USER(ID INT AUTO_INCREMENT, LOGIN VARCHAR, PASSWORD VARCHAR)").execute();
      databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS ACCOUNT(ACCOUNT_NO VARCHAR PRIMARY KEY, USER_ID INT, BALANCE DOUBLE, FOREIGN KEY (USER_ID) REFERENCES USER(ID), )").execute();
      databaseConnection.commit();


    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean checkIfAccountNoExistInBank(String accountNo) {
    PreparedStatement ps = null;
    try {
      ps = databaseConnection.prepareStatement("SELECT COUNT(*) FROM ACCOUNT WHERE ACCOUNT_NO IS ?");
      ps.setString(1, accountNo);
      ResultSet rs = ps.executeQuery();
      rs.next();
      int count = rs.getInt(1);
      if (count == 0) {
        return true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

  public String createBankAccount(String token) throws Exception {
    User user = getUserByToken(token);
    String accountNo = null;
    boolean generatedNewBankAccount = false;
    while (!generatedNewBankAccount) {
      accountNo = BankAccountUtils.generateIban();
      generatedNewBankAccount = checkIfAccountNoExistInBank(accountNo);
    }

    double balance = 0.0;

    try {
      PreparedStatement ps = databaseConnection.prepareStatement("INSERT INTO ACCOUNT(ACCOUNT_NO, USER_ID, BALANCE) VALUES(?,?,?)");
      ps.setString(1, accountNo);
      ps.setInt(2, user.getId());
      ps.setDouble(3, balance);
      ps.execute();

      databaseConnection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return accountNo;
  }

  public User getUserByToken(String token) throws Exception {
    User user = availableUsers.get(token);
    if (user == null) {
      throw new Exception("Wrong token");
    }

    return user;
  }

  public void addUser(String login, String password) {
    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("INSERT INTO USER(LOGIN, PASSWORD) VALUES(?,?)");
      ps.setString(1, login);
      ps.setString(2, password);
      ps.execute();

      this.databaseConnection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public String authenticate(String login, String password) throws Exception {
    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("SELECT * FROM USER WHERE LOGIN IS ?");
      ps.setString(1, login);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        User user = new User(
                rs.getInt("id"),
                rs.getString("login"),
                rs.getString("password")
        );

        if (user.getPassword().equals(password)) {
          String userToken = user.generateToken();
          availableUsers.put(userToken, user);
          return userToken;
        }

        throw new Exception("Wrong authenticate data");
      } else {
        throw new Exception("Wrong authenticate data");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public boolean depositMoney(int userId, String accountNo, double amount) {
    try {
      Account account = getAccount(accountNo);

      PreparedStatement ps = this.databaseConnection.prepareStatement("UPDATE ACCOUNT SET BALANCE = ? WHERE ACCOUNT_NO = ?");
      ps.setDouble(1, account.getBalance() + amount);
      ps.setString(2, accountNo);
      if(ps.executeUpdate() > 0) {
        return true;
      };
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

  public Account getAccount(String accountNo) {
    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("SELECT * FROM ACCOUNT WHERE ACCOUNT_NO IS ?");
      ps.setString(1, accountNo);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        Account account = new Account(
                rs.getString("account_no"),
                rs.getInt("user_id"),
                rs.getDouble("balance")
        );
        return account;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public boolean withdrawMoney(int userId, String accountNo, double amount) {
    try {
      Account account = getAccount(accountNo);
      PreparedStatement ps = this.databaseConnection.prepareStatement("UPDATE ACCOUNT SET BALANCE = ? WHERE ACCOUNT_NO = ?");
      ps.setDouble(1, account.getBalance() - amount);
      ps.setString(2, accountNo);
      if(ps.executeUpdate() > 0) {
        return true;
      };
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }
}
