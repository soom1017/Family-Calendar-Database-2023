package calendar.data.dao;

import calendar.data.connection.DatabaseManager;
import calendar.data.model.User;
import calendar.service.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final String CHECK_EMAIL_DUPLICATE = "SELECT user_id FROM caluser WHERE email = ? AND user_id != ?";
    private final String VALIDATE_USER = "SELECT user_id FROM caluser WHERE user_uid = ? AND password = ?";
    private final String SELECT_USER_BY_ID = "SELECT * FROM caluser WHERE user_id = ?";
    private final String INSERT_USER = "INSERT INTO caluser (user_uid, user_name, email, password, notification_channel_id, family_id) VALUES (?, ?, ?, ?, ?, ?)";

    private final String SELECT_FAMILY_BY_FAMILY_CODE = "SELECT family_id FROM family WHERE family_code = ?";
    private final String INSERT_FAMILY = "INSERT INTO family (family_code) VALUES (?)";

    private final String SELECT_FAMILY_MEMBERS = "SELECT DISTINCT u2.* " 
                                                    + "FROM caluser u1 "
                                                    + "JOIN caluser u2 ON u1.family_id = u2.family_id "
                                                    + "WHERE u1.user_id = ? AND u2.user_id != ?";
    private final String SELECT_FAMILY_AVAILABILITY = "SELECT u.*, CASE WHEN EXISTS (SELECT 1 FROM event e "
                                                        + "INNER JOIN event_user eu ON e.event_id = eu.event_id AND eu.user_id = u.user_id " 
                                                        + "WHERE e.end_at > ? AND e.start_at < ?) THEN false ELSE true END AS available "
                                                        + "FROM caluser u INNER JOIN caluser selfu ON u.family_id = selfu.family_id " 
                                                        + "WHERE selfu.user_id = ? AND u.user_id != ?"
                                                        + "ORDER BY available";

    private final String MODIFY_USER_ACCOUNT = "UPDATE caluser SET user_uid = ?, user_name = ?, email = ?, password = ? WHERE user_id = ?";

    // user-register
    public boolean checkEmailDuplicate(String email) {
        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(CHECK_EMAIL_DUPLICATE)) {
            statement.setString(1, email);
            statement.setInt(2, UserSession.getInstance().getUserId());

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public boolean checkFamilyCodeDuplicate(String secretCode) {
        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_FAMILY_BY_FAMILY_CODE)) {
            statement.setString(1, secretCode);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public boolean createUserAndFamily(User user, String familyCode) {
        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement1 = connection.prepareStatement(INSERT_FAMILY, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement statement2 = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            statement1.setString(1, familyCode);
            statement1.executeUpdate();

            ResultSet resultSet = statement1.getGeneratedKeys();
            if (resultSet.next()) {
                int familyId = resultSet.getInt(1);
                statement2.setString(1, user.getUid());
                statement2.setString(2, user.getUserName());
                statement2.setString(3, user.getEmail());
                statement2.setString(4, user.getPassword());
                statement2.setInt(5, user.getNotificationChannel());
                statement2.setInt(6, familyId);
                statement2.executeUpdate();
                
                ResultSet resultSet2 = statement2.getGeneratedKeys();
                if (resultSet2.next()) {
                    connection.commit();
                    System.out.println("User record inserted successfully");
                    
                    UserSession.getInstance().setUserId(resultSet2.getInt("user_id"));
                    
                    return true;
                }
            }
            return false;

        } catch (SQLException e) {
            DatabaseManager.rollback(connection);
            e.printStackTrace();

            return false;

        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public boolean createUser(User user, String familyCode) {
        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement1 = connection.prepareStatement(SELECT_FAMILY_BY_FAMILY_CODE);
             PreparedStatement statement2 = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            
            statement1.setString(1, familyCode);
            ResultSet resultSet = statement1.executeQuery();
            if (resultSet.next()) {
                int familyId = resultSet.getInt(1);
                statement2.setString(1, user.getUid());
                statement2.setString(2, user.getUserName());
                statement2.setString(3, user.getEmail());
                statement2.setString(4, user.getPassword());
                statement2.setInt(5, user.getNotificationChannel());
                statement2.setInt(6, familyId);
                statement2.executeUpdate();
                
                ResultSet resultSet2 = statement2.getGeneratedKeys();
                if (resultSet2.next()) {
                    connection.commit();
                    System.out.println("User record inserted successfully");

                    UserSession.getInstance().setUserId(resultSet2.getInt("user_id"));

                    return true;
                }
            }
            return false;

        } catch (SQLException e) {
            DatabaseManager.rollback(connection);
            e.printStackTrace();

            return false;

        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public boolean updateUser(User user) {
        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(MODIFY_USER_ACCOUNT)) {
            statement.setString(1, user.getUid());
            statement.setString(2, user.getUserName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setInt(5, user.getUserId());
            int affected = statement.executeUpdate();
            if (affected > 0) {
                connection.commit();
                System.out.println("User record updated successfully");

                return true;
            }
            return false;

        } catch (SQLException e) {
            DatabaseManager.rollback(connection);
            e.printStackTrace();

            return false;

        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    // user-authentication
    public boolean validateUserLogin(String id, String hashedPassword) {
        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(VALIDATE_USER)) {
            statement.setString(1, id);
            statement.setString(2, hashedPassword);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                UserSession.getInstance().setUserId(resultSet.getInt("user_id"));
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public User getUserById(int id) {
        Connection connection = DatabaseManager.getConnection();
        User user = null;
        try (PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID)) {
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("user_uid"),
                    resultSet.getString("user_name"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getInt("notification_channel_id"),
                    resultSet.getInt("family_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
        return user;
    }

    // Prepare family member dropdown
    public List<User> getFamilyMembers() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<User> familyMemberList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_FAMILY_MEMBERS)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User user = new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("user_name"),
                    resultSet.getString("email")
                );
                familyMemberList.add(user);
            }
            return familyMemberList;

        } catch (SQLException e) {
            DatabaseManager.rollback(connection);
            e.printStackTrace();

            return null;

        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public List<User> getFamilyMembersOnlyAvaliable(long startDatetime, long endDatetime) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<User> familyMemberList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_FAMILY_AVAILABILITY)) {
            statement.setTimestamp(1, new Timestamp(startDatetime));
            statement.setTimestamp(2, new Timestamp(endDatetime));
            statement.setInt(3, userId);
            statement.setInt(4, userId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (!resultSet.getBoolean("available"))
                    continue;
                
                User user = new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("user_name"),
                    resultSet.getString("email")
                );
                familyMemberList.add(user);
            }
            return familyMemberList;

        } catch (SQLException e) {
            DatabaseManager.rollback(connection);
            e.printStackTrace();

            return null;

        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    // Check available user & Send RSVP
    public List<User> getFamilyMembersAvailability(long startDatetime, long endDatetime) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<User> familyMemberList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_FAMILY_AVAILABILITY)) {
            statement.setTimestamp(1, new Timestamp(startDatetime));
            statement.setTimestamp(2, new Timestamp(endDatetime));
            statement.setInt(3, userId);
            statement.setInt(4, userId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User user = new User(
                    resultSet.getInt("user_id"),
                    resultSet.getString("user_name"),
                    resultSet.getString("email")
                );
                user.setAvailable(resultSet.getBoolean("available"));
                familyMemberList.add(user);
            }
            return familyMemberList;

        } catch (SQLException e) {
            DatabaseManager.rollback(connection);
            e.printStackTrace();

            return null;

        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

}