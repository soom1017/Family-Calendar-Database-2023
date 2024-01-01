package calendar.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import calendar.data.connection.DatabaseManager;
import calendar.data.model.Rsvp;
import calendar.data.model.RsvpResponseResult;
import calendar.service.UserSession;


public class RsvpDao {
    private final String GET_RSVP_REQUESTS = "SELECT DISTINCT rf.*\n" +
                                                "FROM rsvp r\n" +
                                                "INNER JOIN rsvp_form rf ON r.form_id = rf.form_id\n" +
                                                "WHERE r.recipient_id = ? AND rf.expires_at > CURRENT_TIMESTAMP AND r.status = 0";
    private final String SELECT_MY_LATEST_RSVP = "SELECT DISTINCT * FROM rsvp_form WHERE sender_id = ? ORDER BY expires_at DESC LIMIT 1";
    private final String GET_RSVP_RESPONSES = "SELECT cu.user_name, cu.email,\n" +
                                                "CASE \n" +
                                                " WHEN r.status = 0 AND rf.expires_at < NOW() THEN 2\n" +
                                                " ELSE r.status\n" +
                                                "END AS status\n" +
                                               "FROM rsvp r\n" +
                                               "INNER JOIN caluser cu ON r.recipient_id = cu.user_id\n" +
                                               "INNER JOIN rsvp_form rf ON r.form_id = rf.form_id\n" +
                                               "WHERE r.form_id = ?";
    private final String INSERT_RSVP = "INSERT INTO rsvp_form (sender_id, event_name, event_start_at, event_end_at, expires_at) VALUES (?, ?, ?, ?, ?)";
    private final String SEND_RSVP_TO_USER = "INSERT INTO rsvp VALUES (?, ?)";
    private final String UPDATE_RSVP_STATUS = "UPDATE rsvp SET status = ? WHERE form_id = ? AND recipient_id = ?";

    public List<Rsvp> getRsvpRequests() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<Rsvp> rsvpList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(GET_RSVP_REQUESTS)) {
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Rsvp rsvp = new Rsvp(
                    resultSet.getInt("form_id"),
                    resultSet.getInt("sender_id"),
                    resultSet.getString("event_name"),
                    resultSet.getTimestamp("event_start_at"),
                    resultSet.getTimestamp("event_end_at"),
                    resultSet.getTimestamp("expires_at")
                );
                rsvpList.add(rsvp);
            }
            return rsvpList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public Rsvp getMyRsvp() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_MY_LATEST_RSVP)) {
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Rsvp rsvp = new Rsvp(
                    resultSet.getInt("form_id"),
                    resultSet.getInt("sender_id"),
                    resultSet.getString("event_name"),
                    resultSet.getTimestamp("event_start_at"),
                    resultSet.getTimestamp("event_end_at"),
                    resultSet.getTimestamp("expires_at")
                );
                return rsvp;
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public List<RsvpResponseResult> getRsvpResponseStatus(int rsvpFormId) {
        Connection connection = DatabaseManager.getConnection();
        List<RsvpResponseResult> resultList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(GET_RSVP_RESPONSES)) {
            statement.setInt(1, rsvpFormId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                RsvpResponseResult responseResult = new RsvpResponseResult(
                    resultSet.getString("user_name"),
                    resultSet.getString("email"),
                    resultSet.getInt("status")
                );
                resultList.add(responseResult);
            }
            return resultList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public boolean insertRsvp(Rsvp rsvpForm, List<Integer> recipientIdList) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return false;

        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement1 = connection.prepareStatement(INSERT_RSVP, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement statement2 = connection.prepareStatement(SEND_RSVP_TO_USER)) {
            statement1.setInt(1, userId);
            statement1.setString(2, rsvpForm.getEventName());
            statement1.setTimestamp(3, rsvpForm.getEventStartAt());
            statement1.setTimestamp(4, rsvpForm.getEventEndAt());
            statement1.setTimestamp(5, rsvpForm.getExpiresAt());

            statement1.executeUpdate();
            ResultSet resultSet = statement1.getGeneratedKeys();
            if (resultSet.next()) {
                int rsvpFormId = resultSet.getInt(1);
                statement2.setInt(1, rsvpFormId);
                for (int participantId: recipientIdList) {
                    statement2.setInt(2, participantId);
                    statement2.addBatch();
                }
                statement2.executeBatch();
                connection.commit();
                System.out.println("Rsvp record inserted successfully");
                    
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

    public boolean updateRSVPStatus(int rsvpFormId, boolean accept) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return false;

        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_RSVP_STATUS)) {
            statement.setInt(1, accept? 1 : 2);
            statement.setInt(2, rsvpFormId);
            statement.setInt(3, userId);

            int affected = statement.executeUpdate();
            if (affected > 0) {
                connection.commit();
                System.out.println("Rsvp response is saved successfully");
                    
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
}

