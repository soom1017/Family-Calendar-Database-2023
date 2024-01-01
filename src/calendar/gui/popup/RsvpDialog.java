package calendar.gui.popup;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import calendar.data.dao.RsvpDao;
import calendar.data.dao.UserDao;
import calendar.data.model.Rsvp;
import calendar.gui.resources.font.CustomFont;

public class RsvpDialog extends JDialog {
    public RsvpDialog(Rsvp rsvp) {
        setTitle("RSVP");
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 6;
        JLabel notificationLabel = new JLabel("다음 이벤트를 생성하고자 하니, 가능한지 알려주세요.");
        notificationLabel.setFont(CustomFont.HEADER_FONT);
        add(notificationLabel, constraints);

        constraints.gridy++;
        JLabel detailNotificationLabel = new JLabel("겹치는 시간대의 일정이 있다면, 이벤트에 초대될 경우 기존 일정은 취소됩니다.");
        detailNotificationLabel.setFont(CustomFont.DETAIL_FONT);
        detailNotificationLabel.setForeground(Color.RED);
        add(detailNotificationLabel, constraints);

        constraints.gridy++;
        JLabel eventNameLabel = new JLabel("제목: " + rsvp.getEventName());
        eventNameLabel.setFont(CustomFont.DEFAULT_FONT);
        add(eventNameLabel, constraints);
        

        constraints.gridy++;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateInfo = "시간: " + dateFormat.format(rsvp.getEventStartAt()) + " -- " + dateFormat.format(rsvp.getEventEndAt());
        JLabel dateInfoLabel = new JLabel(dateInfo);
        dateInfoLabel.setFont(CustomFont.DEFAULT_FONT);
        add(dateInfoLabel, constraints);

        constraints.gridx = 6;
        constraints.gridy++;
        constraints.gridwidth = 2;
        UserDao userDao = new UserDao();
        String hostName = userDao.getUserById(rsvp.getSenderId()).getUserName();
        JLabel hostNameLabel = new JLabel("- " + hostName + " 보냄");
        hostNameLabel.setFont(CustomFont.DEFAULT_FONT);
        add(hostNameLabel, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        JButton acceptButton = new JButton("수락");
        acceptButton.addActionListener(e -> accept(rsvp.getFormId()));
        CustomFont.applyButtonStyles(acceptButton);
        add(acceptButton, constraints);

        constraints.gridx++;
        JButton rejectButton = new JButton("거절");
        rejectButton.addActionListener(e -> reject(rsvp.getFormId()));
        CustomFont.applyHighlightedButtonStyles(rejectButton);
        add(rejectButton, constraints);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(900, 500);
        setModal(true);
        setLocationRelativeTo(null);
    }

    private void accept(int rsvpFormId) {
        RsvpDao rsvpDao = new RsvpDao();
        if (rsvpDao.updateRSVPStatus(rsvpFormId, true)) {
            MessageDialog.alert("RSVP에 수락하였습니다.");
            dispose();
        }
    }
    private void reject(int rsvpFormId) {
        RsvpDao rsvpDao = new RsvpDao();
        if (rsvpDao.updateRSVPStatus(rsvpFormId, false)) {
            MessageDialog.alert("RSVP에 거절하였습니다.");
            dispose();
        }
    }
}
