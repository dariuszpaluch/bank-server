package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.*;
import com.dariuszpaluch.bankserver.models.*;
import com.dariuszpaluch.bankserver.utils.BankAccountUtils;
import com.dariuszpaluch.services.bank.Transfer;

import javax.wsdl.OperationType;
import java.sql.*;
import java.util.*;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class BankDAO {
  private static final Map<String, User> availableUsers = new HashMap<>(); //TODO Add spring authorization by token
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
      databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS OPERATION(ID INT AUTO_INCREMENT, SOURCE_ACCOUNT VARCHAR, DESTINATION_ACCOUNT VARCHAR , AMOUNT INT, TITLE VARCHAR , OPERATION_TYPE VARCHAR)").execute();
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

  public User getUserByToken(String token) throws WrongUserTokenException {
    User user = availableUsers.get(token);
    if (user == null) {
      throw new WrongUserTokenException();
    }

    return user;
  }

  public boolean checkIfUserExistWithThisLogin(String login) throws DatabaseException {
    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("SELECT * FROM USER WHERE LOGIN IS ?");
      ps.setString(1, login);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        return true;
      }

    } catch (SQLException e) {
      throw new DatabaseException();
    }

    return false;
  }

  public void addUser(String login, String password) throws UserLoginIsBusyException, DatabaseException {
    try {
      if (this.checkIfUserExistWithThisLogin(login)) {
        throw new UserLoginIsBusyException();
      } else {
        PreparedStatement ps = this.databaseConnection.prepareStatement("INSERT INTO USER(LOGIN, PASSWORD) VALUES(?,?)");
        ps.setString(1, login);
        ps.setString(2, password);
        ps.execute();
      }
      this.databaseConnection.commit();
    } catch (SQLException e) {
      throw new DatabaseException();
    }
  }

  public String authenticate(String login, String password) throws WrongAuthenticateData, DatabaseException {
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

        throw new WrongAuthenticateData();
      } else {
        throw new WrongAuthenticateData();
      }
    } catch (SQLException e) {
      throw new DatabaseException();
    }
  }

  public void depositMoney(String accountNo, int amount) throws DatabaseException, AccountNumberDoesNotExist {
    BankOperation bankOperation = new BankOperation(
            null,
            accountNo,
            amount,
            null,
            BankOperationType.DEPOSIT
    );
    this.updateDestinationAccount(bankOperation);
  }

  public Account getAccount(String accountNo) throws AccountNumberDoesNotExist {
    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("SELECT * FROM ACCOUNT WHERE ACCOUNT_NO IS ?");
      ps.setString(1, accountNo);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        Account account = new Account(
                rs.getString("account_no"),
                rs.getInt("user_id"),
                rs.getInt("balance")
        );
        return account;
      } else {
        throw new AccountNumberDoesNotExist();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public void withdrawMoney(String accountNo, int amount) throws DatabaseException, AccountNumberDoesNotExist {
    BankOperation bankOperation = new BankOperation(
            accountNo,
            null,
            amount,
            null,
            BankOperationType.WITHDRAW
    );
    this.updateOperationSourceAccount(bankOperation);
  }

  public void externalIncomingTransfer(Transfer externalTransfer) throws DatabaseException, AccountNumberDoesNotExist {
    BankOperation bankOperation = new BankOperation(
            externalTransfer.getSourceAccount(),
            externalTransfer.getDestinationAccount(),
            externalTransfer.getAmount(),
            externalTransfer.getTitle(),
            BankOperationType.EXTERNAL_INPUT_TRANSFER
    );
    this.updateDestinationAccount(bankOperation);
  }

  public void updateOperationSourceAccount(BankOperation bankOperation) throws AccountNumberDoesNotExist, DatabaseException {
    Account sourceAccount = getAccount(bankOperation.getSourceAccount());

    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("UPDATE ACCOUNT SET BALANCE = ? WHERE ACCOUNT_NO = ?");
      ps.setDouble(1, sourceAccount.getBalance() - bankOperation.getAmount());
      ps.setString(2, sourceAccount.getAccountNO());

      if (ps.executeUpdate() <= 0) {
        throw new DatabaseException();
      }
      ;

      this.addOperationToHistory(bankOperation);
    } catch (SQLException e) {
      throw new DatabaseException();
    }
  }

  public void updateDestinationAccount(BankOperation bankOperation) throws AccountNumberDoesNotExist, DatabaseException {
    Account destinationAccount = getAccount(bankOperation.getDestinationAccount());

    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("UPDATE ACCOUNT SET BALANCE = ? WHERE ACCOUNT_NO = ?");
      ps.setDouble(1, destinationAccount.getBalance() + bankOperation.getAmount());
      ps.setString(2, destinationAccount.getAccountNO());

      if (ps.executeUpdate() <= 0) {
        throw new DatabaseException();
      }
      ;

      this.addOperationToHistory(bankOperation);
    } catch (SQLException e) {
      throw new DatabaseException();
    }
  }


  public void addOperationToHistory(BankOperation bankOperation) {
    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("INSERT INTO OPERATION(SOURCE_ACCOUNT, DESTINATION_ACCOUNT, AMOUNT, TITLE, OPERATION_TYPE) VALUES(?,?,?,?,?)");
      ps.setString(1, bankOperation.getSourceAccount());
      ps.setString(2, bankOperation.getDestinationAccount());
      ps.setInt(3, bankOperation.getAmount());
      ps.setString(4, bankOperation.getTitle());
      ps.setString(5, bankOperation.getType().toString());
      ps.execute();

      this.databaseConnection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<String> getUserAccounts(User user) throws DatabaseException {
    List<String> accounts = new ArrayList<>();
    try {
      PreparedStatement ps = this.databaseConnection.prepareStatement("SELECT ACCOUNT_NO FROM ACCOUNT WHERE USER_ID IS ?");
      ps.setInt(1, user.getId());
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        accounts.add(rs.getString("account_no"));
      }

    } catch (SQLException e) {
      throw new DatabaseException();
    }

    return accounts;
  }

  public void executeTransferToAnotherBank(Transfer transfer) throws DatabaseException, AccountNumberDoesNotExist {
    BankOperation bankOperation = new BankOperation(
            transfer.getSourceAccount(),
            transfer.getDestinationAccount(),
            transfer.getAmount(),
            transfer.getTitle(),
            BankOperationType.EXTERNAL_OUTPUT_TRANSFER
    );

    this.updateOperationSourceAccount(bankOperation);
  }

  public void makeInternalTransfer(Transfer transfer) throws DatabaseException, AccountNumberDoesNotExist {
    BankOperation bankOperation = new BankOperation(
            transfer.getSourceAccount(),
            transfer.getDestinationAccount(),
            transfer.getAmount(),
            transfer.getTitle(),
            BankOperationType.TRANSFER
    );

    this.updateOperationSourceAccount(bankOperation);
    this.updateDestinationAccount(bankOperation);
  }
}
