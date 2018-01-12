package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.WrongBankIdInExternalTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class Settings {
  public static String EXTERNAL_BANK_AUTHORIZATION_LOGIN = "admin";
  public static String EXTERNAL_BANK_AUTHORIZATION_PASSWORD = "admin";
  public static String MY_BANK_ID = "00117225";
  private static String csvFile = "banks.csv";
  private static Map<String, String> BANKS = null;


  private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

  private static Map<String, String> getAddressOfBanks() {
    Map<String, String> banks = new HashMap<>();
    BufferedReader br = null;
    String line = "";
    String cvsSplitBy = ";";

    try {
      br = new BufferedReader(new FileReader(csvFile));
      while ((line = br.readLine()) != null) {
        String[] data = line.split(cvsSplitBy);
        banks.put(data[0], data[1]);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    LOGGER.info("Wczytano " + String.valueOf(banks.size()) + " banków zewnętrznych");

    return banks;
  }

  public static String getBankUrl(String bankId) throws WrongBankIdInExternalTransfer {
    BANKS = getAddressOfBanks();

    String bankUrl = BANKS.get(bankId);


    if(bankUrl.isEmpty()) {
      throw new WrongBankIdInExternalTransfer();
    }

    return bankUrl;
  }
}
