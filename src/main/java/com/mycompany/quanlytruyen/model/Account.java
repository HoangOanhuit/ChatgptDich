package com.mycompany.quanlytruyen.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author Administrator
 * Domain model đại diện cho một tài khoản đăng truyện.
 */
public class Account {
    
    private Long account_id;
    private String username;
    private String email;
    private String password;
    private String note;
    private String uuid;
    private String registered;
    private String activationKey;
    private String versionIos;
    private Integer totalBook;
    private Integer postedHoan;
    private String createdAt;
    private String updatedAt;
    private String status;
    
    private String authToken;
    private LocalDateTime tokenExpiresAt;

// Getters/Setters
    public Account() {
    }

    public Account(Long id, String username, String email, String password) {
        this.account_id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return account_id;
    }

    public void setId(Long id) {
        this.account_id = id;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String registered) {
        this.registered = registered;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getVersionIos() {
        return versionIos;
    }

    public void setVersionIos(String versionIos) {
        this.versionIos = versionIos;
    }
    public Integer getTotalBook() {
        return totalBook;
    }

    public void setTotalBook(Integer totalBook) {
        this.totalBook = totalBook;
    }

    public Integer getPostedHoan() {
        return postedHoan;
    }

    public void setPostedHoan(Integer postedHoan) {
        this.postedHoan = postedHoan;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAccount_id() {
        return account_id;
    }

    public void setAccount_id(Long account_id) {
        this.account_id = account_id;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public LocalDateTime getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(account_id, account.account_id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(account_id);
    }

    @Override
    public String toString() {
        return "Account{" +
            "account_id=" + account_id +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            '}';
    }
}