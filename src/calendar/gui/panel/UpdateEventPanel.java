package calendar.gui.panel;

import calendar.data.dao.EventDao;
import calendar.data.dao.UserDao;
import calendar.data.model.Event;
import calendar.data.model.User;
import calendar.gui.elements.DateSpinner;
import calendar.gui.elements.TimeComboBox;
import calendar.gui.popup.MessageDialog;
import calendar.gui.resources.font.CustomFont;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class UpdateEventPanel extends JPanel {
    private JLabel titleLabel, timeLabel, toLabel, descriptionLabel, allDayLabel, attendLabel, reminderLabel, timeFrameLabel;
    private JTextField eventNameField;
    private DateSpinner startDateSpinner, endDateSpinner;
    private TimeComboBox startTimeComboBox, endTimeComboBox;
    private JCheckBox allDayCheckBox;
    private JTextArea descriptionArea;
    private JComboBox<String> intervalDropdown;
    private JSpinner timeFrameSpinner;
    private JTable attendeeListTable;

    private List<User> familyMemberList;
    private int eventId;
    public JButton saveButton;

    private final String TYPE_TITLE = "(제목 없음)";
    private final String TYPE_DESCRIPTION = "설명 추가";

    public UpdateEventPanel(Calendar date) {
        this(new Event("(제목 없음)", "설명 추가", date.getTimeInMillis(), date.getTimeInMillis(), true, 0, 0));
    }

    public UpdateEventPanel(Event event) {
        this.eventId = event.getEventId();
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        // Set Event name
        constraints.gridx = 0;
        constraints.gridy = 0;
        titleLabel = new JLabel("제목");
        titleLabel.setFont(CustomFont.HEADER_FONT);
        add(titleLabel, constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        eventNameField = new JTextField(event.getEventName());
        eventNameField.setFont(CustomFont.HEADER_FONT);
        eventNameField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (eventNameField.getText().equals(TYPE_TITLE))
                    eventNameField.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (eventNameField.getText().isEmpty())
                    eventNameField.setText(TYPE_TITLE);
            }
        });
        add(eventNameField, constraints);

        // Set Event datetime
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 1;
        timeLabel = new JLabel("시간");
        timeLabel.setFont(CustomFont.DEFAULT_FONT);
        add(timeLabel, constraints);

        constraints.gridx = 1;
        startDateSpinner = new DateSpinner(event.getStartAt());
        startDateSpinner.addChangeListener(e -> updateAttendeeListTable());
        add(startDateSpinner, constraints);

        constraints.gridx = 2;
        startTimeComboBox = new TimeComboBox(calculateTimeComboboxIndex(event.getStartAt()));
        startTimeComboBox.addActionListener(e -> updateAttendeeListTable());
        add(startTimeComboBox, constraints);

        constraints.gridx = 3;
        toLabel = new JLabel("-----");
        toLabel.setFont(CustomFont.DEFAULT_FONT);
        add(toLabel, constraints);

        constraints.gridx = 4;
        endDateSpinner = new DateSpinner(event.getEndAt());
        endDateSpinner.addChangeListener(e -> updateAttendeeListTable());
        add(endDateSpinner, constraints);

        constraints.gridx = 5;
        endTimeComboBox = new TimeComboBox(calculateTimeComboboxIndex(event.getEndAt()));
        endTimeComboBox.addActionListener(e -> updateAttendeeListTable());
        add(endTimeComboBox, constraints);

        startTimeComboBox.setVisible(!event.isAllDay());
        endTimeComboBox.setVisible(!event.isAllDay());

        // - Allday
        constraints.gridx = 0;
        constraints.gridy++;
        allDayLabel = new JLabel("종일", JLabel.LEFT);
        allDayLabel.setFont(CustomFont.DEFAULT_FONT);
        add(allDayLabel, constraints);

        constraints.gridx = 1;
        allDayCheckBox = new JCheckBox();
        allDayCheckBox.setSelected(event.isAllDay());
        allDayCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    startTimeComboBox.setVisible(false);
                    endTimeComboBox.setVisible(false);
                } else {
                    startTimeComboBox.setVisible(true);
                    endTimeComboBox.setVisible(true);
                }
                revalidate();
                repaint();

                updateAttendeeListTable();
            }
        });
        add(allDayCheckBox, constraints);

        // Attendee
        constraints.gridx = 0;
        constraints.gridy++;
        attendLabel = new JLabel("참석");
        attendLabel.setFont(CustomFont.DEFAULT_FONT);
        add(attendLabel, constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        constraints.gridheight = 3;

        attendeeListTable = new JTable();
        attendeeListTable.setFont(CustomFont.DEFAULT_FONT);
        updateAttendeeListTable();

        JScrollPane scrollPane = new JScrollPane(attendeeListTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(scrollPane.getSize().width, 80));
        add(scrollPane, constraints);
        
        // Set Event Description
        constraints.gridx = 0;
        constraints.gridy += 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        descriptionLabel = new JLabel("설명");
        descriptionLabel.setFont(CustomFont.DEFAULT_FONT);
        add(descriptionLabel, constraints);

        constraints.gridx = 1;
        constraints.gridwidth = 5;
        constraints.gridheight = 3;
        descriptionArea = new JTextArea(event.getDescription());
        descriptionArea.setFont(CustomFont.DEFAULT_FONT);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setPreferredSize(new Dimension(descriptionArea.getSize().width, 80));
        descriptionArea.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (descriptionArea.getText().equals(TYPE_DESCRIPTION))
                    descriptionArea.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (descriptionArea.getText().isEmpty())
                    descriptionArea.setText(TYPE_DESCRIPTION);
            }
        });
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        add(descriptionScrollPane, constraints);

        // Set Reminder
        constraints.gridx = 0;
        constraints.gridy += 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        reminderLabel = new JLabel("알림");
        reminderLabel.setFont(CustomFont.DEFAULT_FONT);
        add(reminderLabel, constraints);

        constraints.gridx = 1;
        String[] intervals = {"0분 간격으로(알림X)", "15분 간격으로", "30분 간격으로", "45분 간격으로", "60분 간격으로"};
        intervalDropdown = new JComboBox<>(intervals);
        intervalDropdown.setSelectedIndex(event.getInterval() / 15);
        intervalDropdown.addActionListener(e -> updateTimeFrameSpinner());
        add(intervalDropdown, constraints);

        constraints.gridx = 2;
        timeFrameSpinner = new JSpinner(new SpinnerNumberModel(60, 0, 60, 1));
        timeFrameSpinner.setEnabled(false);
        add(timeFrameSpinner, constraints);

        constraints.gridx = 3;
        timeFrameLabel = new JLabel("분 전부터");
        timeFrameLabel.setFont(CustomFont.DEFAULT_FONT);
        add(timeFrameLabel, constraints);

        // Save Event
        constraints.gridx = 5;
        constraints.gridy++;
        saveButton = new JButton("저장");
        saveButton.setFont(CustomFont.DEFAULT_FONT);
        add(saveButton, constraints);
    }

    private int calculateTimeComboboxIndex(long startOrEndAt) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(startOrEndAt), ZoneId.systemDefault());
        int idx = time.getHour() * 4 + time.getMinute() / 15;
        return idx;
    }

    private void updateTimeFrameSpinner() {
        int selectedIndex = intervalDropdown.getSelectedIndex();
        if (selectedIndex == 0) {
            timeFrameSpinner.setEnabled(false);
        } else {
            timeFrameSpinner.setModel(new SpinnerNumberModel(60, selectedIndex*15, 60, 1));
            timeFrameSpinner.setEnabled(true);
        }
    }

    private void updateAttendeeListTable() {
        boolean allDay = allDayCheckBox.isSelected();

        UserDao userDao = new UserDao();
        familyMemberList = userDao.getFamilyMembersOnlyAvaliable(getStartDatetime(allDay), getEndDatetime(allDay));
        String[] columnNames = {"가능인원 정보", "이벤트 초대"};
        Object[][] rowData = new Object[familyMemberList.size()][2];

        for (int i = 0; i < familyMemberList.size(); i++) {
            User member = familyMemberList.get(i);
            rowData[i][0] = member.getUserName() + " (" + member.getEmail() + ")";
            rowData[i][1] = false;
        }

        DefaultTableModel model = new DefaultTableModel(rowData, columnNames) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Boolean.class : Object.class;
            }
        };
        attendeeListTable.setModel(model);
        revalidate();
        repaint();
    }

    private List<Integer> getAttendeeIdList() {
        List<Integer> attendeeIdList = new ArrayList<>();
        for (int row = 0; row < attendeeListTable.getRowCount(); row++) {
            boolean isChecked = (boolean) attendeeListTable.getValueAt(row, 1);
            if (isChecked)
                attendeeIdList.add(familyMemberList.get(row).getUserId());
        }
        return attendeeIdList;
    }

    private long getStartDatetime(boolean allDay) {
        if (allDay)
            return startTimeComboBox.getDateStarttime(startDateSpinner.getDate());
        return startTimeComboBox.getDatetime(startDateSpinner.getDate());
    }
    private long getEndDatetime(boolean allDay) {
        if (allDay)
            return endTimeComboBox.getDateEndtime(endDateSpinner.getDate());
        return endTimeComboBox.getDatetime(endDateSpinner.getDate());
    }

    public int updateEvent(boolean isNew) {
        EventDao eventDao = new EventDao();

        boolean allDay = allDayCheckBox.isSelected();
        long startAt = getStartDatetime(allDay);
        long endAt = getEndDatetime(allDay);

        if (startAt >= endAt) {
            MessageDialog.alert("시작시간이 종료시간보다 앞서야 합니다.");
            return -1;
        }
        if (eventDao.isEventOverlapped(startAt, endAt, eventId)) {
            MessageDialog.alert("시간대가 겹치는 기존 일정이 있습니다.");
            return -1;
        }

        String eventName = eventNameField.getText();
        if (eventName.isEmpty())
            eventName = TYPE_TITLE;
        String description = descriptionArea.getText();
        if (description.isEmpty() || description == TYPE_DESCRIPTION)
            description = "";
        int interval = intervalDropdown.getSelectedIndex() * 15;
        int timeframe = (interval == 0) ? 0 : (int) timeFrameSpinner.getValue();

        Event event = new Event(eventName, description, startAt, endAt, allDay, interval, timeframe);
        if(isNew) {
            if (eventDao.createEvent(event, getAttendeeIdList()))
                return 0;
        } else {
            event.setEventId(this.eventId);
            if (eventDao.updateEvent(event))
                return 0;
        }
        MessageDialog.alert("이벤트 저장에 실패했습니다. 다시 시도해주십시오.");
        return -1;
    }
    
    public void setComponentsEnabled(boolean enabled) {
        eventNameField.setEnabled(enabled);
        startDateSpinner.setEnabled(enabled);
        endDateSpinner.setEnabled(enabled);
        startTimeComboBox.setEnabled(enabled);
        endTimeComboBox.setEnabled(enabled);
        allDayCheckBox.setEnabled(enabled);
        descriptionArea.setEnabled(enabled);
        // 생성 시 설정한 값만 유효
        intervalDropdown.setEnabled(false);
        timeFrameSpinner.setEnabled(false);
        attendeeListTable.setEnabled(enabled);
    }
}
