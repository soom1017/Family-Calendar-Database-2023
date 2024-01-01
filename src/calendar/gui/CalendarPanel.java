package calendar.gui;

import calendar.gui.view.CalendarView;
import calendar.gui.view.DailyView;
import calendar.gui.view.MonthlyView;
import calendar.gui.view.WeeklyView;
import calendar.gui.panel.CheckAvailabilityPanel;
import calendar.gui.panel.ModifyAccountPanel;
import calendar.gui.panel.RsvpFormPanel;
import calendar.gui.panel.RsvpResultPanel;
import calendar.gui.panel.SearchPanel;
import calendar.gui.resources.font.CustomFont;
import calendar.gui.elements.SearchEventTextField;
import calendar.gui.listener.CalendarUpdateListener;
import calendar.gui.listener.RsvpViewUpdateListener;
import calendar.gui.listener.SearchListener;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CalendarPanel extends JPanel {
    private Calendar date;
    private List<CalendarView> calendarViews;
    private int viewType;

    private JPanel viewPanel;
    private JPanel selectionPanel;

    private JButton prevButton;
    private JButton nextButton;
    private JButton todayButton;
    private JLabel timeFrameLabel;

    private SearchEventTextField searchField;

    private JComboBox<String> calendarViewDropdown;
    private final String[] viewOptions = {"월   (M)", "주   (W)", "일   (D)"};

    private JButton checkAvailabilityButton;
    private JButton RSVPButton;
    private JButton modifyAccountButton;

    private final Calendar STDDATE = Calendar.getInstance();

    public CalendarPanel() {
        // Initialize Calendar data
        date = Calendar.getInstance();

        // Initialize View
        viewType = 0;
        calendarViews = new ArrayList<>();
        calendarViews.add(new MonthlyView(STDDATE));
        calendarViews.add(new WeeklyView(STDDATE));
        calendarViews.add(new DailyView(STDDATE));
        for (CalendarView calendarView: calendarViews) {
            calendarView.addUpdateListener(new CalendarUpdateListener() {

                @Override
                public void onUpdate(boolean hasChanges) {
                    changeView(viewType);
                }
            });
        }

        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);

        // 1. ViewPanel
        viewPanel = new JPanel();
        add(viewPanel, BorderLayout.CENTER);

        // 2. SelectionPanel
        selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.X_AXIS));

        // - Buttons (today / prev / next)
        todayButton = new JButton("오늘");
        prevButton = new JButton(new ImageIcon("calendar/gui/resources/icon/backward.png"));
        nextButton = new JButton(new ImageIcon("calendar/gui/resources/icon/forward.png"));
        todayButton.addActionListener(e -> showToday());
        prevButton.addActionListener(e -> showPrevious());
        nextButton.addActionListener(e -> showNext());

        CustomFont.applyButtonStyles(todayButton);
        CustomFont.applyButtonStyles(prevButton);
        CustomFont.applyButtonStyles(nextButton);

        selectionPanel.add(todayButton);
        selectionPanel.add(Box.createHorizontalStrut(10));
        selectionPanel.add(prevButton);
        selectionPanel.add(Box.createHorizontalStrut(5));
        selectionPanel.add(nextButton);

        // - Labels ( _ 년 _ 월)
        timeFrameLabel = new JLabel(calendarViews.get(viewType).getTimeFrame());
        timeFrameLabel.setFont(CustomFont.DEFAULT_FONT);
        selectionPanel.add(timeFrameLabel);

        selectionPanel.add(Box.createHorizontalGlue());

        // - Search box
        searchField = new SearchEventTextField(25);
        searchField.addSearchListener(new SearchListener() {

            @Override
            public void onSearch(String text) {
                getSpecificView("Search", text);
            }
        });
        selectionPanel.add(searchField);

        selectionPanel.add(Box.createHorizontalStrut(5));

        // - View Dropdown
        calendarViewDropdown = new JComboBox<>(viewOptions);
        calendarViewDropdown.setFont(CustomFont.DEFAULT_FONT);
        calendarViewDropdown.addActionListener(e -> {
            viewType = calendarViewDropdown.getSelectedIndex();
            changeView(viewType);
        });
        selectionPanel.add(calendarViewDropdown);

        selectionPanel.add(Box.createHorizontalStrut(5));

        // - Check availability & RSVP form button
        checkAvailabilityButton = new JButton("가능시간대확인");
        checkAvailabilityButton.addActionListener(e -> getSpecificView("CheckAvailability"));
        CustomFont.applyButtonStyles(checkAvailabilityButton);
        selectionPanel.add(checkAvailabilityButton);

        selectionPanel.add(Box.createHorizontalStrut(5));

        RSVPButton = new JButton("RSVP");
        RSVPButton.addActionListener(e -> getSpecificView("RSVP"));
        CustomFont.applyButtonStyles(RSVPButton);
        selectionPanel.add(RSVPButton);

        selectionPanel.add(Box.createHorizontalStrut(5));

        modifyAccountButton = new JButton("계정 설정");
        modifyAccountButton.addActionListener(e -> getSpecificView("ModifyAccount"));
        CustomFont.applyButtonStyles(modifyAccountButton);
        selectionPanel.add(modifyAccountButton);

        add(selectionPanel, BorderLayout.NORTH);

        // Initalize view panel
        changeView(viewType);
    }

    private void showToday() {
        Calendar today = Calendar.getInstance();
        if (!date.equals(today)) {
            date = today;
            changeView(viewType);
        }
    }
    private void showPrevious() {
        switch (viewType) {
            case 0:
                date.add(Calendar.MONTH, -1);
                break;
            case 1:
                date.add(Calendar.WEEK_OF_MONTH, -1);
                break; 
            case 2:
                date.add(Calendar.DATE, -1);
                break; 
            default:
                break;
        }
        changeView(viewType);
    }

    private void showNext() {
        switch (viewType) {
            case 0:
                date.add(Calendar.MONTH, 1);
                break;
            case 1:
                date.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case 2:
                date.add(Calendar.DATE, 1);
                break;  
            default:
                break;
        }
        changeView(viewType);
    }

    public void changeView(int viewType) {
        CalendarView view = calendarViews.get(viewType);
        view.setTimeFrame(date);
        timeFrameLabel.setText(view.getTimeFrame());

        viewPanel.removeAll();
        viewPanel.setLayout(new BorderLayout());

        viewPanel.add(view.getHeaderView(), BorderLayout.NORTH);
        viewPanel.add(view.getView(), BorderLayout.CENTER);

        viewPanel.revalidate();
        viewPanel.repaint();
    }

    private void getSpecificView(String type) {
        getSpecificView(type, "");
    }
    private void getSpecificView(String type, String inputText) {
        viewPanel.removeAll();
        viewPanel.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));

        EmptyBorder headerPanelMargin = new EmptyBorder(10, 10, 0, 0);
        headerPanel.setBorder(headerPanelMargin);

        JButton returnButton = new JButton("<");
        CustomFont.applyHighlightedButtonStyles(returnButton);
        headerPanel.add(returnButton);

        returnButton.addActionListener(e -> changeView(viewType));
        viewPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel specificViewPanel;
        switch (type) {
            case "Search":
                specificViewPanel = new SearchPanel(inputText);
                break;
            case "RSVP":
                RsvpFormPanel formPanel = new RsvpFormPanel();
                formPanel.addUpdateListener(new RsvpViewUpdateListener() {
                    @Override
                    public void onUpdate() {
                        getSpecificView("RSVP");
                    }
                });
                specificViewPanel = formPanel;
                viewPanel.add(new RsvpResultPanel(), BorderLayout.EAST);
                break;
            case "CheckAvailability":
                specificViewPanel = new CheckAvailabilityPanel();
                break;
            case "ModifyAccount":
                specificViewPanel = new ModifyAccountPanel();
                break;
            default:
                specificViewPanel = new JPanel();
                break;
        }
        viewPanel.add(specificViewPanel, BorderLayout.CENTER);

        viewPanel.revalidate();
        viewPanel.repaint();
    }
}
