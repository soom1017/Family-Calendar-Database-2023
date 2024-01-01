package calendar.gui.view;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import calendar.data.dao.EventDao;
import calendar.data.model.Event;
import calendar.gui.listener.CalendarUpdateListener;
import calendar.gui.popup.CreateEventDialog;
import calendar.gui.popup.ShowEventDialog;
import calendar.gui.resources.font.CustomFont;

public class DailyView implements CalendarView {
    private String timeFrame;
    private JPanel headerPanel;
    private String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};

    private Calendar date;
    private CalendarUpdateListener updateListener;

    public DailyView(Calendar date) {
        setTimeFrame(date);
        setHeaderView();
    }

    @Override
    public String getTimeFrame() {
        return this.timeFrame;
    }

    @Override
    public void setTimeFrame(Calendar date) {
        this.timeFrame = "   " + String.valueOf(date.get(Calendar.YEAR)) + " 년   " 
                               + String.valueOf(date.get(Calendar.MONTH) + 1) + " 월   "
                               + String.valueOf(date.get(Calendar.DATE) + " 일   ");
        this.date = (Calendar) date.clone();
    }

    @Override
    public JPanel getHeaderView() {
        setHeaderView();
        return this.headerPanel;
    }

    @Override
    public void setHeaderView() {
        headerPanel = new JPanel();
        String day = String.valueOf(date.get(Calendar.DATE)) + "일 (" + daysOfWeek[date.get(Calendar.DAY_OF_WEEK) - 1] + ")";
        JLabel dayLabel = new JLabel(day);
        dayLabel.setFont(CustomFont.HEADER_FONT);
        dayLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                createEventInDate(date);
            }
        });
        headerPanel.add(dayLabel);
    }

    @Override
    public JPanel getView() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel gridPanel = new JPanel(new GridLayout(96, 2));
        JPanel[] panelGrid = new JPanel[96];
        for (int i=0; i<96; i++) {
            panelGrid[i] = new JPanel();
            panelGrid[i].setLayout(new BoxLayout(panelGrid[i], BoxLayout.X_AXIS));
        }
        EventDao eventDao = new EventDao();
        List<Event> eventList = eventDao.getEventsByDate(date);
        if(eventList != null && !eventList.isEmpty()) {
            int startRow, endRow;
            for (Event event: eventList) {
                startRow = event.isFirstDay() ? calculateStartRow(event.getStartAt()) : 0;
                endRow = event.isLastDay() ? calculateEndRow(event.getEndAt()) : 96;

                for (int row = startRow; row < endRow; row++) {
                    JLabel label = new JLabel((row == startRow)? " | " + event.getEventName() : " | ");
                    label.setFont(CustomFont.DETAIL_FONT);
                    panelGrid[row].add(label);
                    panelGrid[row].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showEvent(event);
                        }
                    });
                }
            }
        }
        for (int i=0; i<96; i++) {
            JPanel timeCard = new JPanel();
            if (i % 4 == 0) {
                JLabel timeLabel = new JLabel(String.format("%02d시", i / 4), JLabel.CENTER);
                timeLabel.setFont(CustomFont.DETAIL_FONT);
                timeCard.add(timeLabel);
            }
            gridPanel.add(timeCard);

            if (i % 4 == 0) {
                panelGrid[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));
            } else {
                panelGrid[i].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));
            }
            gridPanel.add(panelGrid[i]);
        }
        mainPanel.add(new JScrollPane(gridPanel));

        return mainPanel;
    }
    
    private int calculateStartRow(long startAt) {
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startAt), ZoneId.systemDefault());
        int startRow = startTime.getHour() * 4 + startTime.getMinute() / 15;
        return startRow;
    }
    private int calculateEndRow(long endAt) {
        LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endAt), ZoneId.systemDefault());
        int endRow = endTime.getHour() * 4 + endTime.getMinute() / 15;
        return endRow;
    }

    private void createEventInDate(Calendar date) {
        CreateEventDialog createEventDialog = new CreateEventDialog(date);
        createEventDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                updateListener.onUpdate(true);
            }
        });
        createEventDialog.setVisible(true);
    }

    private void showEvent(Event event) {
        ShowEventDialog showEventDialog = new ShowEventDialog(event);
        showEventDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                updateListener.onUpdate(true);
            }
        });
        showEventDialog.setVisible(true);
    }

    @Override
    public void addUpdateListener(CalendarUpdateListener updateListener) {
        this.updateListener = updateListener;
    }
}
