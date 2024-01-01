package calendar.gui.view;

import java.util.Calendar;
import javax.swing.JPanel;

import calendar.gui.listener.CalendarUpdateListener;

public interface CalendarView {
    public abstract String getTimeFrame();
    public abstract void setTimeFrame(Calendar date);
    public abstract JPanel getHeaderView();
    public abstract void setHeaderView();
    public abstract JPanel getView();
    public void addUpdateListener(CalendarUpdateListener updateListener);
}
