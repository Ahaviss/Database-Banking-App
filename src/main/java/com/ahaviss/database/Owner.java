package com.ahaviss.database;
import com.ahaviss.utilities.SecurityUtils;

public class Owner {
    //Private fields
    public Owner () {}
    private String username = "tempUsername@123";
    private String password = SecurityUtils.hashPassword("tempPassword@123", SecurityUtils.generateSalt());
    public void setPassword (String password) {this.password = password;}
    public void setPasswordFromUser (String password) {this.password = SecurityUtils.hashPassword(password, SecurityUtils.generateSalt());}
    public String getPassword () {return password;}
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
}
