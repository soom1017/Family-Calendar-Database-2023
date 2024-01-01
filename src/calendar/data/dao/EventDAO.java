package calendar.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import calendar.data.connection.DatabaseManager;
import calendar.data.model.Event;
import calendar.service.UserSession;


public class EventDao {
    private final String SELECT_EVENT_OVERLAP_COUNT = "SELECT COUNT(DISTINCT e.event_id) AS num_dup FROM event_user eu " 
                                                        + "INNER JOIN event e ON eu.event_id = e.event_id " 
                                                        + "WHERE e.end_at > ? AND e.start_at < ? AND eu.user_id = ? AND e.event_id != ?";
    private final String SELECT_EVENT_BY_DATE = "SELECT DISTINCT e.*, (DATE(e.start_at) = ?) AS first_day, (DATE(e.end_at) = ?) AS last_day FROM event_user eu " 
                                                    + "INNER JOIN event e ON eu.event_id = e.event_id " 
                                                    + "WHERE eu.user_id = ? AND (? BETWEEN DATE(e.start_at) AND (DATE(e.end_at - INTERVAL '1 second'))) " 
                                                    + "ORDER BY e.start_at, e.end_at";
    private final String SELECT_EVENT_BY_WEEK = "SELECT DISTINCT e.*, EXTRACT(DOW FROM e.start_at) AS start_day, EXTRACT(DOW FROM e.end_at) AS end_day\n" +
                                                "    FROM event e\n" +
                                                "    INNER JOIN event_user eu ON eu.event_id = e.event_id \n" +
                                                "    WHERE eu.user_id = ? \n" +
                                                "    AND e.event_id NOT IN (\n" +
                                                "        SELECT event_id FROM event \n" +
                                                "        WHERE DATE(end_at - INTERVAL '1 second') < ? OR DATE(start_at) > DATE(?) + INTERVAL '6 day'\n" +
                                                "    )\n" +
                                                "ORDER BY e.start_at, e.end_at";
    private final String SELECT_EVENT_BY_EVENTNAME = "SELECT DISTINCT e.* FROM event e "
                                                    + "INNER JOIN event_user eu ON eu.event_id = e.event_id " 
                                                    + "WHERE eu.user_id = ? AND event_name LIKE ? "
                                                    + "ORDER BY e.start_at, e.end_at";
    private final String SELECT_EVENT_BY_EVENTNAME_AND_DATE = "SELECT e.* FROM event e "
                                                        + "WHERE e.event_id IN ( "
                                                            + "SELECT DISTINCT eu1.event_id FROM event_user eu1 "
                                                            + "INNER JOIN event_user eu2 ON eu1.event_id = eu2.event_id "
                                                            + "WHERE eu1.user_id = ? "
                                                            + "AND e.event_name LIKE ? "
                                                            + "AND ? BETWEEN DATE(e.start_at) AND DATE(e.end_at - INTERVAL '1 second')"
                                                        + ") ORDER BY e.start_at, e.end_at";
    private final String SELECT_EVENT_BY_DETAILS_ALL = "SELECT DISTINCT e.* FROM event e "
                                                + "WHERE e.event_id IN ( "
                                                    + "SELECT eu1.event_id FROM event_user eu1 "
                                                    + "INNER JOIN event_user eu2 ON eu1.event_id = eu2.event_id "
                                                    + "WHERE eu1.user_id = ? "
                                                    + "AND e.event_name LIKE ? "
                                                    + "AND eu2.user_id = ? "
                                                    + "AND ? BETWEEN DATE(e.start_at) AND DATE(e.end_at - INTERVAL '1 second')"
                                                + ") ORDER BY e.start_at, e.end_at";
    private final String SELECT_REMINDER_EVENTS = "SELECT DISTINCT e.*, (CURRENT_TIMESTAMP - e.start_at) AS time_left\n" +
                                                    "FROM event e\n" +
                                                    "INNER JOIN event_user eu ON e.event_id = eu.event_id\n" +
                                                    "WHERE eu.user_id =?\n" +
                                                    "  AND e.start_at > CURRENT_TIMESTAMP\n" +
                                                    "  AND e.start_at - (INTERVAL '1 minute' * e.time_frame) <= CURRENT_TIMESTAMP\n" +
                                                    "  AND e.interval != 0\n" +
                                                    "  AND MOD(EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - (e.start_at - INTERVAL '1 minute' * e.time_frame)))::INT / 60, e.interval) = 0";
                                                
    private final String INSERT_EVENT = "INSERT INTO event (event_name, host_id, description, start_at, end_at, all_day, interval, time_frame) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private final String INSERT_PARTICIPANT = "INSERT INTO event_user VALUES (?, ?)";
    private final String MODIFY_EVENT = "UPDATE event SET event_name = ?, description = ?, start_at = ?, end_at = ?, all_day = ? WHERE event_id = ?";
    private final String DELETE_EVENT = "DELETE FROM event WHERE event_id = ?";
    private final String DELETE_PARTICIPANT = "DELETE FROM event_user WHERE event_id = ? AND user_id = ?";

    public List<Event> getEventsByDate(Calendar date) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<Event> eventList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_EVENT_BY_DATE)) {
            Date sqlDate = new Date(date.getTimeInMillis());
            statement.setDate(1, sqlDate);
            statement.setDate(2, sqlDate);
            statement.setInt(3, userId);
            statement.setDate(4, sqlDate);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = new Event(
                    resultSet.getInt("event_id"),
                    resultSet.getString("event_name"),
                    resultSet.getInt("host_id"),
                    resultSet.getString("description"),
                    resultSet.getTimestamp("start_at").getTime(),
                    resultSet.getTimestamp("end_at").getTime(),
                    resultSet.getBoolean("all_day"),
                    resultSet.getInt("interval"),
                    resultSet.getInt("time_frame")
                );
                event.setFirstDay(resultSet.getBoolean("first_day"));
                event.setLastDay(resultSet.getBoolean("last_day"));
                eventList.add(event);
            }
            return eventList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public List<Event> getEventsByWeek(Calendar startDate) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<Event> eventList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_EVENT_BY_WEEK)) {
            Date sqlDate = new Date(startDate.getTimeInMillis());
            statement.setInt(1, userId);
            statement.setDate(2, sqlDate);
            statement.setDate(3, sqlDate);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = new Event(
                    resultSet.getInt("event_id"),
                    resultSet.getString("event_name"),
                    resultSet.getInt("host_id"),
                    resultSet.getString("description"),
                    resultSet.getTimestamp("start_at").getTime(),
                    resultSet.getTimestamp("end_at").getTime(),
                    resultSet.getBoolean("all_day"),
                    resultSet.getInt("interval"),
                    resultSet.getInt("time_frame")
                );
                event.setStartDay(resultSet.getInt("start_day"));
                event.setEndDay(resultSet.getInt("end_day"));
                eventList.add(event);
            }
            return eventList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public List<Event> getEventsByEventName(String eventName) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<Event> eventList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_EVENT_BY_EVENTNAME)) {
            statement.setInt(1, userId);
            statement.setString(2, "%" + eventName + "%");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = new Event(
                    resultSet.getInt("event_id"),
                    resultSet.getString("event_name"),
                    resultSet.getInt("host_id"),
                    resultSet.getString("description"),
                    resultSet.getTimestamp("start_at").getTime(),
                    resultSet.getTimestamp("end_at").getTime(),
                    resultSet.getBoolean("all_day"),
                    resultSet.getInt("interval"),
                    resultSet.getInt("time_frame")
                );
                eventList.add(event);
            }
            return eventList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public List<Event> getEventsByEventDetails(String eventName, int participantId, java.util.Date date) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<Event> eventList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(
            (participantId == -1) ? SELECT_EVENT_BY_EVENTNAME_AND_DATE : SELECT_EVENT_BY_DETAILS_ALL)) {
            Date sqlDate = new Date(date.getTime());
            int pos = 1;

            statement.setInt(pos++, userId);
            statement.setString(pos++, "%" + eventName + "%");
            if (participantId != -1)
                statement.setInt(pos++, participantId);
            statement.setDate(pos++, sqlDate);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = new Event(
                    resultSet.getInt("event_id"),
                    resultSet.getString("event_name"),
                    resultSet.getInt("host_id"),
                    resultSet.getString("description"),
                    resultSet.getTimestamp("start_at").getTime(),
                    resultSet.getTimestamp("end_at").getTime(),
                    resultSet.getBoolean("all_day"),
                    resultSet.getInt("interval"),
                    resultSet.getInt("time_frame")
                );
                eventList.add(event);
            }
            return eventList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public List<Event> getReminderEvents() {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return null;

        Connection connection = DatabaseManager.getConnection();
        List<Event> eventList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SELECT_REMINDER_EVENTS)) {
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Event event = new Event(
                    resultSet.getInt("event_id"),
                    resultSet.getString("event_name"),
                    resultSet.getInt("host_id"),
                    resultSet.getString("description"),
                    resultSet.getTimestamp("start_at").getTime(),
                    resultSet.getTimestamp("end_at").getTime(),
                    resultSet.getBoolean("all_day"),
                    resultSet.getInt("interval"),
                    resultSet.getInt("time_frame")
                );
                event.setTimeLeft(resultSet.getInt("time_left"));
                eventList.add(event);
            }
            return eventList;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public boolean isEventOverlapped(long eventStartAt, long eventEndAt, int eventId) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return true;

        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_EVENT_OVERLAP_COUNT)) {
            statement.setTimestamp(1, new Timestamp(eventStartAt));
            statement.setTimestamp(2, new Timestamp(eventEndAt));
            statement.setInt(3, userId);
            statement.setInt(4, eventId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int overlapEventCount = resultSet.getInt("num_dup");
                if (overlapEventCount == 0)
                    return false;
            }
            return true;

        } catch (SQLException e) {
            DatabaseManager.rollback(connection);
            e.printStackTrace();

            return true;

        } finally {
            DatabaseManager.releaseConnection(connection);
        }
    }

    public boolean createEvent(Event event, List<Integer> participantIdList) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return false;

        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement1 = connection.prepareStatement(INSERT_EVENT, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement statement2 = connection.prepareStatement(INSERT_PARTICIPANT)) {
            statement1.setString(1, event.getEventName());
            statement1.setInt(2, userId);
            statement1.setString(3, event.getDescription());
            statement1.setTimestamp(4, new Timestamp(event.getStartAt()));
            statement1.setTimestamp(5, new Timestamp(event.getEndAt()));
            statement1.setBoolean(6, event.isAllDay());
            statement1.setInt(7, event.getInterval());
            statement1.setInt(8, event.getTimeframe());

            statement1.executeUpdate();
            ResultSet resultSet = statement1.getGeneratedKeys();
            if (resultSet.next()) {
                int eventId = resultSet.getInt(1);
                statement2.setInt(1, eventId);
                statement2.setInt(2, userId);
                statement2.addBatch();
                for (int participantId: participantIdList) {
                    statement2.setInt(2, participantId);
                    statement2.addBatch();
                }
                statement2.executeBatch();
                connection.commit();
                System.out.println("Event record inserted successfully");
                    
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

    public boolean updateEvent(Event event) {
        Connection connection = DatabaseManager.getConnection();

        try (PreparedStatement statement = connection.prepareStatement(MODIFY_EVENT)) {
            statement.setString(1, event.getEventName());
            statement.setString(2, event.getDescription());
            statement.setTimestamp(3, new Timestamp(event.getStartAt()));
            statement.setTimestamp(4, new Timestamp(event.getEndAt()));
            statement.setBoolean(5, event.isAllDay());
            statement.setInt(6, event.getEventId());

            int affected = statement.executeUpdate();
            if (affected > 0) {
                connection.commit();
                System.out.println("Event record updated successfully");

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

    public boolean deleteEvent(Event event) {
        int userId = UserSession.getInstance().getUserId();
        if (userId == -1)
            return false;

        Connection connection = DatabaseManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement((event.getHostId() == userId) ? DELETE_EVENT : DELETE_PARTICIPANT)) {
            statement.setInt(1, event.getEventId());
            if (event.getHostId() != userId)
                statement.setInt(2, userId);

            int affected = statement.executeUpdate();
            if (affected > 0) {
                connection.commit();
                System.out.println("Event record deleted successfully");

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
