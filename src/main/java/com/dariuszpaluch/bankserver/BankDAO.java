package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.models.User;

import javax.validation.constraints.AssertFalse;
import java.sql.*;
import java.util.Random;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class BankDAO {
  private static BankDAO instance = new BankDAO();
  private Connection databaseConnection = null;

  public static BankDAO getInstance() {
    return instance;
  }

  private BankDAO() {
    try {
      Class.forName("org.h2.Driver");
      databaseConnection = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
      databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS ACCOUNT(ACCOUNT_NO VARCHAR PRIMARY KEY, BALANCE DOUBLE)").execute();
      databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS USER(ID BIGINT AUTO_INCREMENT, LOGIN VARCHAR, PASSWORD VARCHAR)").execute();
      databaseConnection.commit();


    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public String createAccound() {
    Random rand = new Random();
    int value = rand.nextInt(50);
    String accountNo = String.valueOf(value);
    double balance = 0.0;

    try {
      PreparedStatement ps = databaseConnection.prepareStatement("INSERT INTO ACCOUNT(ACCOUNT_NO,BALANCE) VALUES(?,?)");
      ps.setString(1, accountNo);
      ps.setDouble(2, balance);
      ps.execute();

      databaseConnection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return accountNo;
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
      if(rs.next()) {
        System.out.println(rs.getString("login"));

        User user = new User(
                rs.getInt("id"),
                rs.getString("login"),
                rs.getString("password")
        );

        if(user.getPassword().equals(password)) {
          return user.generateToken();
        }

        throw new Exception("Wrong authenticate data");
      }
      else {
        throw new Exception("Wrong authenticate data");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }
}
