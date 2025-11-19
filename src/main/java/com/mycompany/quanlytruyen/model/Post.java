package com.mycompany.quanlytruyen.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model đại diện cho lịch đăng chương của một tài khoản.
 */
public class Post {
    private Long id;
    private Long accountId;
    private int chapterOrder;
    private int hour;
    private int minute;
    private LocalDateTime dateSchedule;

    public Post() {
    }

    public Post(Long id, Long accountId, int chapterOrder, int hour, int minute, LocalDateTime dateSchedule) {
        this.id = id;
        this.accountId = accountId;
        this.chapterOrder = chapterOrder;
        this.hour = hour;
        this.minute = minute;
        this.dateSchedule = dateSchedule;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public int getChapterOrder() {
        return chapterOrder;
    }

    public void setChapterOrder(int chapterOrder) {
        this.chapterOrder = chapterOrder;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public LocalDateTime getDateSchedule() {
        return dateSchedule;
    }

    public void setDateSchedule(LocalDateTime dateSchedule) {
        this.dateSchedule = dateSchedule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Post post)) return false;
        return chapterOrder == post.chapterOrder
            && hour == post.hour
            && minute == post.minute
            && Objects.equals(id, post.id)
            && Objects.equals(accountId, post.accountId)
            && Objects.equals(dateSchedule, post.dateSchedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, chapterOrder, hour, minute, dateSchedule);
    }

    @Override
    public String toString() {
        return "Post{" +
            "id=" + id +
            ", accountId=" + accountId +
            ", chapterOrder=" + chapterOrder +
            ", hour=" + hour +
            ", minute=" + minute +
            ", dateSchedule=" + dateSchedule +
            '}';
    }
}