package calendar.service;

import calendar.data.dao.UserDao;
import calendar.data.model.User;

public class UserSession {
    private static UserSession instance;
    private int userId;

    private UserSession() {
        userId = -1;
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public User getUser() {
        UserDao userDao = new UserDao();
        User user = userDao.getUserById(userId);
        return user;
    }
}
