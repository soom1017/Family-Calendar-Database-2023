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

public class WeeklyView implements CalendarView {
    private String timeFrame;
    private JPanel headerPanel;
    private String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};

    private Calendar firstOfWeek, lastOfWeek;
    private CalendarUpdateListener updateListener;

    public WeeklyView(Calendar date) {
        setTimeFrame(date);
        setHeaderView();
    }

    @Override
    public String getTimeFrame() {
        return this.timeFrame;
    }

    @Override
    public void setTimeFrame(Calendar _date) {
        Calendar date = (Calendar) _date.clone();
        while (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            date.add(Calendar.DATE, -1);

        firstOfWeek = (Calendar) date.clone();
        date.add(Calendar.DATE, 6);
        lastOfWeek = date;

        int startYear = firstOfWeek.get(Calendar.YEAR);
        int startMonth = firstOfWeek.get(Calendar.MONTH) + 1;
        String startString = startYear + " 년   " + startMonth + " 월   ";

        int endYear = date.get(Calendar.YEAR);
        int endMonth = date.get(Calendar.MONTH) + 1;

        if (startYear != endYear) {
            this.timeFrame = "   " + startString + "-   " + endYear + " 년   " + endMonth + " 월   ";
        } else if (startMonth != endMonth) {
            this.timeFrame = "   " + startString + "-   " + endMonth + " 월   ";
        } else {
            this.timeFrame = "   " + startString;
        }
    }

    @Override
    public JPanel getHeaderView() {
        setHeaderView();
        return this.headerPanel;
    }

    @Override
    public void setHeaderView() {
        Calendar date = (Calendar) firstOfWeek.clone();

        headerPanel = new JPanel(new GridLayout(1, 8));
        headerPanel.add(new JLabel(""));

        for (int i = 0; i < 7; i++) {
            Calendar currentDate = (Calendar) date.clone();
            String day = String.valueOf(currentDate.get(Calendar.DATE)) + "일 (" + daysOfWeek[i] + ")";
            JLabel label = new JLabel(day, JLabel.CENTER);
            label.setFont(CustomFont.DEFAULT_FONT);
            if (i == 0)
                label.setForeground(Color.RED);
            else if (i == 6)
                label.setForeground(Color.BLUE);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    createEventInDate(currentDate);
                }
            });
            headerPanel.add(label);

            date.add(Calendar.DATE, 1);
        }
    }

    @Override
    public JPanel getView() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel gridPanel = new JPanel(new GridLayout(96, 8));
        JPanel[][] panelGrid = new JPanel[96][7];
        for (int i = 0; i < 96; i++)
            for (int j = 0; j < 7; j++) {
                panelGrid[i][j] = new JPanel();
                panelGrid[i][j].setLayout(new BoxLayout(panelGrid[i][j], BoxLayout.X_AXIS));
            }

        EventDao eventDao = new EventDao();
        List<Event> eventList = eventDao.getEventsByWeek(firstOfWeek);
        if(eventList != null && !eventList.isEmpty()) {
            
            int startRow, endRow, startCol, endCol;

            long startTimeOfWeek = firstOfWeek.getTimeInMillis();
            startTimeOfWeek = startTimeOfWeek - startTimeOfWeek % 86400000;

            long endTimeOfWeek = lastOfWeek.getTimeInMillis();
            endTimeOfWeek = endTimeOfWeek - endTimeOfWeek % 86400000 + 86400000;

            for (int i = 0; i < eventList.size(); i++) {
                Event event = eventList.get(i);

                if ((i == 0) && (event.getStartAt() < startTimeOfWeek)) {
                    startCol = 0;
                    startRow = 0;
                } else {
                    startCol = event.getStartDay();
                    startRow = calculateStartRow(event.getStartAt());
                }
                
                if ((i == eventList.size()-1) && (event.getEndAt() >= endTimeOfWeek)) {
                    endCol = 6;
                    endRow = 96;
                } else {
                    endCol = event.getEndDay();
                    endRow = calculateEndRow(event.getEndAt());
                }
                // 그리기 시작
                if (startCol == endCol) {
                    for (int row = startRow; row < endRow; row++) {
                        JLabel label = new JLabel((row == startRow)? " | " + event.getEventName() : " | ");
                        label.setFont(CustomFont.DETAIL_FONT);
                        panelGrid[row][startCol].add(label);
                        panelGrid[row][startCol].addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                showEvent(event);
                            }
                        });
                    }
                } else {
                    for (int col = startCol; col < endCol; col++) {
                        for (int row = startRow; row < 96; row++) {
                            JLabel label = new JLabel((row == startRow)? " | " + event.getEventName() : " | ");
                            label.setFont(CustomFont.DETAIL_FONT);
                            panelGrid[row][col].add(label);
                            panelGrid[row][col].addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                showEvent(event);
                            }
                        });
                        }
                    }
                    for (int row = 0; row < endRow; row++) {
                        JLabel label = new JLabel(" | ");
                        label.setFont(CustomFont.DETAIL_FONT);
                        panelGrid[row][endCol].add(label);
                        panelGrid[row][endCol].addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                showEvent(event);
                            }
                        });
                    }
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
            for (int j=1; j<8; j++) {
                if (i % 4 == 0) {
                    panelGrid[i][j-1].setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
                } else {
                    panelGrid[i][j-1].setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.GRAY));
                }
                
                gridPanel.add(panelGrid[i][j-1]);
            }
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
