package models;

import java.util.Date;

public class ConnectionHistory {
    private Integer id;
    private Integer userId;
    private Date connectionDate;
    
    // User object for reference (not stored in database)
    private User user;

    public ConnectionHistory() {
    }

    public ConnectionHistory(Integer id, Integer userId, Date connectionDate) {
        this.id = id;
        this.userId = userId;
        this.connectionDate = connectionDate;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getConnectionDate() {
        return connectionDate;
    }

    public void setConnectionDate(Date connectionDate) {
        this.connectionDate = connectionDate;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public String toString() {
        return "ConnectionHistory{" +
                "id=" + id +
                ", userId=" + userId +
                ", connectionDate=" + connectionDate +
                '}';
    }
}
