package calendar.gui.popup;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import calendar.data.model.Event;
import calendar.gui.resources.font.CustomFont;

public class ReminderDialog extends JDialog {
    public ReminderDialog(Event event) {
        setTitle("이벤트 리마인더");
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 6;
        JLabel notificationLabel = new JLabel("아래 이벤트의 시작까지 " + String.valueOf(event.getTimeLeft()) + "분 남았습니다.");
        notificationLabel.setFont(CustomFont.HEADER_FONT);
        add(notificationLabel, constraints);

        constraints.gridy++;
        JLabel eventNameLabel = new JLabel("제목: " + event.getEventName());
        eventNameLabel.setFont(CustomFont.DEFAULT_FONT);
        add(eventNameLabel, constraints);
        

        constraints.gridy++;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateInfo = "시간: " + dateFormat.format(event.getStartAt()) + " -- " + dateFormat.format(event.getEndAt());
        JLabel dateInfoLabel = new JLabel(dateInfo);
        dateInfoLabel.setFont(CustomFont.DEFAULT_FONT);
        add(dateInfoLabel, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        JButton confirmButton = new JButton("확인");
        confirmButton.addActionListener(e -> dispose());
        CustomFont.applyButtonStyles(confirmButton);
        add(confirmButton, constraints);

        this.addWindowListener(null);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(900, 500);
        setModal(true);
        setLocationRelativeTo(null);
    }
}
