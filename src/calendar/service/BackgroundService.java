package calendar.service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import calendar.data.dao.EventDao;
import calendar.data.dao.RsvpDao;
import calendar.data.model.Event;
import calendar.data.model.Rsvp;
import calendar.gui.popup.ReminderDialog;
import calendar.gui.popup.RsvpDialog;

public class BackgroundService {
    private ScheduledExecutorService scheduler;

    RsvpDao rsvpDao;
    EventDao eventDao;

    public BackgroundService() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        rsvpDao = new RsvpDao();
        eventDao = new EventDao();
    }

    public void startBackgroundScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            checkRSVPRequests();
            checkReminders();
        }, 0, 30, TimeUnit.SECONDS); // 매 30초마다 실행
    }
    public void stopBackgroundScheduler() {
        scheduler.shutdown();
    }

    private void checkRSVPRequests() {
        List<Rsvp> rsvpList = rsvpDao.getRsvpRequests();
        if (rsvpList != null && !rsvpList.isEmpty()) {
            for (Rsvp rsvp: rsvpList) {
                RsvpDialog rsvpDialog = new RsvpDialog(rsvp);
                rsvpDialog.setVisible(true);
            }
        }
    }

    private void checkReminders() {
        List<Event> eventList = eventDao.getReminderEvents();
        if (eventList != null && !eventList.isEmpty()) {
            for (Event event: eventList) {
                ReminderDialog reminderDialog = new ReminderDialog(event);
                reminderDialog.setVisible(true);
            }
        }
    }
}

