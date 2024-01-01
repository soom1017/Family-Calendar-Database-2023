package calendar.gui.panel;

import java.util.List;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import calendar.data.dao.UserDao;
import calendar.data.model.User;
import calendar.gui.elements.DateSpinner;
import calendar.gui.elements.TimeComboBox;
import calendar.gui.popup.MessageDialog;
import calendar.gui.resources.font.CustomFont;

public class CheckAvailabilityPanel extends JPanel {
    private DateSpinner startDateSpinner, endDateSpinner;
    private TimeComboBox startTimeComboBox, endTimeComboBox;
    private JCheckBox allDayCheckBox;

    private List<User> familyMemberList;
    private JPanel memberInfoPanel;
    JTable memberTable;

    public CheckAvailabilityPanel() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 6;
        JLabel notificationLabel = new JLabel("시간대를 입력하고 가족 구성원들의 가능 여부를 확인하세요.");
        notificationLabel.setFont(CustomFont.DEFAULT_FONT);
        add(notificationLabel, constraints);

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
                // repaint();
            }
        });
        add(allDayCheckBox, constraints);

        constraints.gridx = 6;
        constraints.gridy++;
        JButton checkRequestButton = new JButton("시간 가능여부 확인");
        checkRequestButton.addActionListener(e -> requestAvailability());
        CustomFont.applyButtonStyles(checkRequestButton);
        add(checkRequestButton, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 7;
        memberInfoPanel = new JPanel();
        add(memberInfoPanel, constraints);

        setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    private void requestAvailability() {
        UserDao userDao = new UserDao();

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

        familyMemberList = userDao.getFamilyMembersAvailability(startAt, endAt);
        
        memberInfoPanel.removeAll();
        if (familyMemberList == null || familyMemberList.isEmpty()) {
            MessageDialog.alert("가족 구성원을 찾지 못했습니다.");
            return;
        }
        String[] columnNames = {"구성원 정보", "가능 여부"};
        Object[][] rowData = new Object[familyMemberList.size()][2];

        for (int i = 0; i < familyMemberList.size(); i++) {
            User member = familyMemberList.get(i);
            rowData[i][0] = member.getUserName() + " (" + member.getEmail() + ")";
            rowData[i][1] = member.isAvailable() ? "가능" : "불가";
        }

        DefaultTableModel model = new DefaultTableModel(rowData, columnNames);
        memberTable = new JTable(model);

        DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer();
        memberTable.getColumnModel().getColumn(1).setCellRenderer(colorRenderer);
        memberTable.getColumnModel().getColumn(1).setCellRenderer(new StatusColumnCellRenderer());
        memberTable.setFont(CustomFont.DEFAULT_FONT);

        JScrollPane scrollPane = new JScrollPane(memberTable);
        memberInfoPanel.add(scrollPane);

        memberInfoPanel.revalidate();
        memberInfoPanel.repaint();
    }
}

class StatusColumnCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if ("불가".equals(value))
            cellComponent.setForeground(Color.RED);
        return cellComponent;
    }
}
