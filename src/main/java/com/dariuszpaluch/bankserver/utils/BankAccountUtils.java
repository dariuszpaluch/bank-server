package com.dariuszpaluch.bankserver.utils;

import org.iban4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class BankAccountUtils {
  public static String generateIban() {
    String account = "00117225" + randomBankAccountNumber();

    try {
      String resultAccount = calculateNRBControlSum(account) + account;
      System.out.println(validateIBAN(resultAccount));
      System.out.println(resultAccount);

      return resultAccount;
    } catch (Exception e) {

      e.printStackTrace();
    }
    return null;
  }

  public static String calculateNRBControlSum(String bban) throws Exception {
    if (bban.isEmpty())
      throw new Exception("Nie podano numeru rachunku.");

    bban = bban.replaceAll(" ", "");
    bban = bban.replaceAll("-", "");
    bban = bban.replaceAll("_", "");

    if(!bban.matches("[0-9]+") || bban.length() != 24) {
      throw new Exception("Podany numer rachunku jest nieprawidłowy.");
    }
    String nr2 = bban + "252100"; // A=10, B=11, ..., L=21, ..., P=25 oraz 2 zera
    int modulo = 0;
    for(char bbanChar : nr2.toCharArray()) {
      modulo = (10 * modulo + Integer.parseInt(Character.toString(bbanChar))) % 97;
    }
    modulo = 98 - modulo;

    return String.format("%02d", modulo);
  }

  public static String randomBankAccountNumber() {
    Random rand = new Random();

    StringBuilder accountNumber = new StringBuilder();
    for(int i = 0; i < 16; i++) {
      accountNumber.append(Integer.toString(rand.nextInt(10)));
    }

    return accountNumber.toString();
  }

  public static boolean validateIBAN(String iban) {
    try {
      IbanUtil.validate("PL" + iban);
      return true;
    } catch (IbanFormatException |
            InvalidCheckDigitException |
            UnsupportedCountryException e) {
      return false;
      // invalid
    }
  }
}
