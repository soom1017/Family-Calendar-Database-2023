package calendar.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import calendar.data.dao.UserDao;
import calendar.data.model.User;

public class UserAuthService {
    private static final SecureRandom secureRandom = new SecureRandom();
    private String _familyCode;

    public String getFamilyCode() {
        return _familyCode;
    }

    private static String generateFamilyCode() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        String familyCode = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return familyCode;
    }

    public boolean register(String id, String userName, String email, String password) {
        String hashedPassword = hashPassword(password);

        UserDao userDao = new UserDao();
        do {
            _familyCode = generateFamilyCode();
            // System.out.println(_familyCode);
        } while (userDao.checkFamilyCodeDuplicate(_familyCode));
        
        return userDao.createUserAndFamily(new User(id, userName, email, hashedPassword, 0), _familyCode);
    }

    public boolean register(String id, String userName, String email, String password, String familyCode) {
        String hashedPassword = hashPassword(password);

        UserDao userDao = new UserDao();
        return userDao.createUser(new User(id, userName, email, hashedPassword, 0), familyCode); // notification channel: pop-ups only.
    }

    public boolean login(String id, String password) {
        String hashedPassword = hashPassword(password);

        UserDao userDao = new UserDao();
        return userDao.validateUserLogin(id, hashedPassword);
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public boolean isEmailExists(String userEmail) {
        UserDao userDao = new UserDao();
        
        return userDao.checkEmailDuplicate(userEmail);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte hashedByte: hashedBytes) {
                String hex = Integer.toHexString(0xff & hashedByte);
                if (hex.length() == 1)
                    hexString.append('o');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUserAccount(User user) {
        UserDao userDao = new UserDao();
        user.setPassword(hashPassword(user.getPassword()));
        return userDao.updateUser(user);
    }
}
