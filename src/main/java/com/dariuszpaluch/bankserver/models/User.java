package com.dariuszpaluch.bankserver.models;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class User {
  private int id;
  private String login;
  private String password;

  public User() {
  }

  public User(int id, String login, String password) {
    this.id = id;
    this.login = login;
    this.password = password;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getId() {
    return id;
  }

  public String generateToken() {
    String subject = "register";
    String key = "bsrToken";


    Map<String,Object> myMap = new HashMap<>();
    myMap.put("login", this.login);
    myMap.put("password", this.password);

    String token = Jwts.builder()
            .setClaims(myMap)
            .setSubject(subject)
            .signWith(SignatureAlgorithm.HS512, key)
            .compact();

    return token;
  }
}
