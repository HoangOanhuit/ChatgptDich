package com.mycompany.quanlytruyen.model;

import java.util.Objects;

/**
 *
 * @author Administrator
 * Domain model đại diện cho một tài khoản đăng truyện.
 */
public class Account {
    

    private Long id;
    private String username;
    private String email;
    private String password;
    private String note;
    private String userId;
    private String uuid;
    private String registed;
    private String activationKey;
    private String auth2;
    private String versionIos;

    public Account() {
    }

    public Account(Long id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRegisted() {
        return registed;
    }

    public void setRegisted(String registed) {
        this.registed = registed;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getAuth2() {
        return auth2;
    }

    public void setAuth2(String auth2) {
        this.auth2 = auth2;
    }

    public String getVersionIos() {
        return versionIos;
    }

    public void setVersionIos(String versionIos) {
        this.versionIos = versionIos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Account{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            '}';
    }
}