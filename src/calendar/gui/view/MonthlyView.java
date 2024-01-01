package calendar.gui.view;

import java.util.Calendar;
import java.util.List;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import calendar.data.dao.EventDao;
import calendar.data.model.Event;
import calendar.gui.listener.CalendarUpdateListener;
import calendar.gui.popup.CreateEventDialog;
import calendar.gui.popup.ShowEventDialog;
import calendar.gui.resources.font.CustomFont;

public class MonthlyView implements CalendarView {
    private String timeFrame;
    private JPanel headerPanel;
    private String[] daysOfWeek = {"일", "월", "화", "수", "목", "금", "토"};

    private Calendar date;
    private CalendarUpdateListener updateListener;

    public MonthlyView(Calendar date) {
        setTimeFrame(date);
        setHeaderView();
    }

    @Override
    public String getTimeFrame() {
        return this.timeFrame;
    }

    @Override
    public void setTimeFrame(Calendar date) {
        this.timeFrame = "   " + String.valueOf(date.get(Calendar.YEAR)) + " 년   " + String.valueOf(date.get(Calendar.MONTH) + 1) + " 월   ";
        this.date = (Calendar) date.clone();
    }

    @Override
    public JPanel getHeaderView() {
        return this.headerPanel;
    }

    @Override
    public void setHeaderView() {
        headerPanel = new JPanel(new GridLayout(1, 7));
        
        for (int i = 0; i < 7; i++) {
            JLabel label = new JLabel(daysOfWeek[i], JLabel.CENTER);
            label.setFont(CustomFont.DEFAULT_FONT);
            if (i == 0)
                label.setForeground(Color.RED);
            else if (i == 6)
                label.setForeground(Color.BLUE);
            headerPanel.add(label);
        }
    }

    @Override
    public JPanel getView() {
        date.set(Calendar.DATE, 1);
        int firstDay = date.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = date.getActualMaximum(Calendar.DATE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel gridPanel = new JPanel(new GridLayout(0, 7));
        for (int i = 0; i < firstDay; i++) {
            gridPanel.add(new JLabel(""));
        }
        JPanel[] panelGrid = new JPanel[daysInMonth];

        int continuingEventIndex = -1;
        for (int i = 0; i < daysInMonth; i++) {
            Calendar currentDate = (Calendar) date.clone();
            currentDate.set(Calendar.DATE, i+1);

            panelGrid[i] = new JPanel();
            panelGrid[i].setBorder(new LineBorder(Color.GRAY, 1));
            panelGrid[i].setLayout(new BoxLayout(panelGrid[i], BoxLayout.Y_AXIS));

            JLabel dateLabel = new JLabel(String.format("%02d", i+1));
            dateLabel.setFont(CustomFont.DEFAULT_FONT);
            dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            dateLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    createEventInDate(currentDate);
                }
            });
            panelGrid[i].add(dateLabel);

            panelGrid[i].add(Box.createRigidArea(new Dimension(0, 1)));

            EventDao eventDAO = new EventDao();
            List<Event> events = eventDAO.getEventsByDate(currentDate);

            if(events != null && !events.isEmpty()) {
                int numEvents = events.size();
                boolean continuingEventExists = !events.get(numEvents - 1).isLastDay();

                if (continuingEventIndex == -1) {
                    for (Event event: events) {
                        JPanel eventCard = createEventCard(event);
                        panelGrid[i].add(eventCard);
                    }
                    if (continuingEventExists)
                        continuingEventIndex = numEvents - 1;
                } else if (continuingEventIndex < numEvents) {
                    for (int j = 0; j < continuingEventIndex; j++) {
                        JPanel eventCard = createEventCard(events.get(j+1));
                        panelGrid[i].add(eventCard);
                    }
                    JPanel eventCard = createEventCard(events.get(0));
                    panelGrid[i].add(eventCard);
                    for (int j = continuingEventIndex+1; j < numEvents; j++) {
                        JPanel _eventCard = createEventCard(events.get(j));
                        panelGrid[i].add(_eventCard);
                    }
                    if (continuingEventExists) {
                        if (continuingEventIndex == numEvents - 1)
                            continuingEventIndex = numEvents - 2;
                        else
                            continuingEventIndex = numEvents - 1;
                    }

                } else {
                    JPanel sampleEventCard = createEventCard(events.get(0));
                    for (int j = 1; j < numEvents; j++) {
                        JPanel eventCard = createEventCard(events.get(j));
                        panelGrid[i].add(eventCard);
                    }
                    for (int j = numEvents-1; j < continuingEventIndex; j++) {
                        panelGrid[i].add(Box.createVerticalStrut(sampleEventCard.getPreferredSize().height));
                    }
                    panelGrid[i].add(sampleEventCard);
                    if (continuingEventExists)
                        continuingEventIndex = numEvents - 2;
                }
            }
            gridPanel.add(panelGrid[i]);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        mainPanel.add(scrollPane);

        return mainPanel;
    }

    public JPanel createEventCard(Event event) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JLabel eventNameLabel = new JLabel(event.getEventName());
        eventNameLabel.setFont(CustomFont.DETAIL_FONT);
        panel.add(eventNameLabel);

        if (event.isAllDay()) {
            panel.setBackground(Color.WHITE);
        } else if (event.isFirstDay()) {
            JLabel startTimeLabel = new JLabel(event.getStartTime());
            startTimeLabel.setFont(CustomFont.DETAIL_FONT);
            panel.add(Box.createHorizontalGlue());
            panel.add(startTimeLabel);
        } else {
            eventNameLabel.setForeground(Color.GRAY);
            eventNameLabel.setOpaque(true);
        }

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        panel.setEnabled(true);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showEvent(event);
            }
        });

        return panel;
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




