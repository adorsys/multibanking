package de.adorsys.multibanking.web.base.entity;

/**
 * Created by peter on 07.05.18 at 08:58.
 */
public class UserPasswordTuple {
    public final String user;
    public final String password;

    public UserPasswordTuple(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
