package calendar.gui.panel;

import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Timestamp;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import calendar.data.dao.RsvpDao;
import calendar.data.dao.UserDao;
import calendar.data.model.Rsvp;
import calendar.data.model.User;
import calendar.gui.elements.DateSpinner;
import calendar.gui.elements.TimeComboBox;
import calendar.gui.listener.RsvpViewUpdateListener;
import calendar.gui.popup.MessageDialog;
import calendar.gui.resources.font.CustomFont;

public class RsvpFormPanel extends JPanel {
    private JTextField eventNameField;
    private DateSpinner startDateSpinner, endDateSpinner;
    private TimeComboBox startTimeComboBox, endTimeComboBox;
    private JCheckBox allDayCheckBox;

    private List<User> familyMemberList;
    private JPanel memberInfoPanel;
    private JTable memberTable;

    private RsvpViewUpdateListener updateListener;

    private final String TYPE_TITLE = "(제목 없음)";

    public RsvpFormPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 6;
        JLabel notificationLabel = new JLabel("이후 생성할 이벤트에 대해 입력하고 원하는 인원을 선택하여 RSVP를 보내세요.");
        notificationLabel.setFont(CustomFont.DEFAULT_FONT);
        add(notificationLabel, constraints);

        constraints.gridy++;
        JLabel detailNotificationLabel = new JLabel("10분 내로 답이 오지 않으면 거절한 것으로 처리됩니다.");
        detailNotificationLabel.setFont(CustomFont.DETAIL_FONT);
        detailNotificationLabel.setForeground(Color.RED);
        add(detailNotificationLabel, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        JLabel eventNameLabel = new JLabel("제목");
        eventNameLabel.setFont(CustomFont.DEFAULT_FONT);
        add(eventNameLabel, constraints);

        constraints.gridx++;
        constraints.gridwidth = 2;
        eventNameField = new JTextField(TYPE_TITLE);
        eventNameField.setFont(CustomFont.DEFAULT_FONT);
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

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 1;
        JLabel timeLabel = new JLabel("시간");
        timeLabel.setFont(CustomFont.DEFAULT_FONT);
        add(timeLabel, constraints);

        constraints.gridx++;
        startDateSpinner = new DateSpinner();
        add(startDateSpinner, constraints);

        constraints.gridx++;
        startTimeComboBox = new TimeComboBox();
        add(startTimeComboBox, constraints);

        constraints.gridx++;
        JLabel toLabel = new JLabel("-----");
        toLabel.setFont(CustomFont.DEFAULT_FONT);
        add(toLabel, constraints);

        constraints.gridx++;
        endDateSpinner = new DateSpinner();
        add(endDateSpinner, constraints);

        constraints.gridx++;
        endTimeComboBox = new TimeComboBox();
        add(endTimeComboBox, constraints);

        startTimeComboBox.setEnabled(false);
        endTimeComboBox.setEnabled(false);

        constraints.gridx = 0;
        constraints.gridy++;
        JLabel allDayLabel = new JLabel("종일", JLabel.LEFT);
        allDayLabel.setFont(CustomFont.DEFAULT_FONT);
        add(allDayLabel, constraints);

        constraints.gridx++;
        allDayCheckBox = new JCheckBox();
        allDayCheckBox.setSelected(true);
        allDayCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    startTimeComboBox.setEnabled(false);
                    endTimeComboBox.setEnabled(false);
                } else {
                    startTimeComboBox.setEnabled(true);
                    endTimeComboBox.setEnabled(true);
                }
                revalidate();
            }
        });
        add(allDayCheckBox, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 6;
        setFamilyMemberTable();
        JScrollPane scrollPane = new JScrollPane(memberTable);
        add(scrollPane, constraints);

        constraints.gridx = 6;
        constraints.gridy++;
        constraints.gridwidth = 1;
        JButton checkRequestButton = new JButton("RSVP 전송");
        checkRequestButton.addActionListener(e -> sendRsvp());
        CustomFont.applyButtonStyles(checkRequestButton);
        add(checkRequestButton, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 7;
        memberInfoPanel = new JPanel();
        add(memberInfoPanel, constraints);

        setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    private void sendRsvp() {
        long startAt, endAt;
        boolean allDay = allDayCheckBox.isSelected();
        if (allDay) {
            startAt = startTimeComboBox.getDateStarttime(startDateSpinner.getDate());
            endAt = endTimeComboBox.getDateEndtime(endDateSpinner.getDate());
        } else {
            startAt = startTimeComboBox.getDatetime(startDateSpinner.getDate());
            endAt = endTimeComboBox.getDatetime(endDateSpinner.getDate());
        }
        if (startAt >= endAt) {
            MessageDialog.alert("시작시간이 종료시간보다 앞서야 합니다.");
            return;
        }
        String eventName = eventNameField.getText();
        if (eventName.isEmpty())
            eventName = TYPE_TITLE;

        List<Integer> selectedMemberList = getSelectedMemberList();
        if (selectedMemberList.isEmpty()) {
            MessageDialog.alert("RSVP 보낼 대상을 한 명 이상 선택하세요.");
            return;
        }

        RsvpDao rsvpDao = new RsvpDao();
        boolean success = rsvpDao.insertRsvp(new Rsvp(eventName, 
                                        new Timestamp(startAt), 
                                        new Timestamp(endAt), 
                                        new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000)), // 10분 후 expire 
                                        selectedMemberList
                                    );
        if (success) {
            MessageDialog.alert("RSVP 전송 완료");
            updateListener.onUpdate();
        } else {
            MessageDialog.alert("RSVP 전송에 실패했습니다.");
        }

    }

    private void setFamilyMemberTable() {
        memberTable = new JTable();

        UserDao userDao = new UserDao();
        familyMemberList = userDao.getFamilyMembers();
        
        if (familyMemberList == null) {
            MessageDialog.alert("가족 구성원을 찾지 못했습니다.");
            return;
        }

        String[] columnNames = {"가족구성원 정보", "선택"};
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
        memberTable.setFont(CustomFont.DEFAULT_FONT);
        memberTable.setModel(model);
    }

    private List<Integer> getSelectedMemberList() {
        List<Integer> attendeeIdList = new ArrayList<>();
        for (int row = 0; row < memberTable.getRowCount(); row++) {
            boolean isChecked = (boolean) memberTable.getValueAt(row, 1);
            if (isChecked)
                attendeeIdList.add(familyMemberList.get(row).getUserId());
        }
        return attendeeIdList;
    }

    public void addUpdateListener(RsvpViewUpdateListener listener) {
        this.updateListener = listener;
    }
}
