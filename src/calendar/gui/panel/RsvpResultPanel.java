package calendar.gui.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import calendar.data.model.Rsvp;
import calendar.data.model.RsvpResponseResult;
import calendar.gui.popup.MessageDialog;
import calendar.gui.resources.font.CustomFont;
import calendar.data.dao.RsvpDao;

public class RsvpResultPanel extends JPanel {
    public RsvpResultPanel() {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel notificationLabel = new JLabel("가장 최근에 보낸 RSVP에 대한 응답 결과입니다.");
        notificationLabel.setFont(CustomFont.DEFAULT_FONT);
        add(notificationLabel);

        add(Box.createVerticalStrut(10));

        RsvpDao rsvpDao = new RsvpDao();
        Rsvp rsvp = rsvpDao.getMyRsvp();
        if (rsvp == null) {
            JLabel detailNotificationLabel = new JLabel("전송한 RSVP가 없습니다.");
            detailNotificationLabel.setFont(CustomFont.DETAIL_FONT);
            detailNotificationLabel.setForeground(Color.GRAY);
            add(detailNotificationLabel);
        }

        JLabel eventNameLabel = new JLabel("제목: " + rsvp.getEventName());
        eventNameLabel.setFont(CustomFont.DEFAULT_FONT);
        add(eventNameLabel);
        
        add(Box.createVerticalStrut(5));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateInfo = "시간: " + dateFormat.format(rsvp.getEventStartAt()) + " -- " + dateFormat.format(rsvp.getEventEndAt());
        JLabel dateInfoLabel = new JLabel(dateInfo);
        dateInfoLabel.setFont(CustomFont.DEFAULT_FONT);
        add(dateInfoLabel);

        add(Box.createVerticalStrut(10));

        List<RsvpResponseResult> resultList = rsvpDao.getRsvpResponseStatus(rsvp.getFormId());
        if (resultList == null || resultList.isEmpty()) {
            MessageDialog.alert("RSVP 응답 결과를 찾지 못했습니다.");
            return;
        }
        
        String[] columnNames = {"구성원 정보", "회신 결과"};
        Object[][] rowData = new Object[resultList.size()][2];

        for (int i = 0; i < resultList.size(); i++) {
            RsvpResponseResult result = resultList.get(i);
            rowData[i][0] = result.getRecipientName() + " (" + result.getRecipientEmail() + ")";
            rowData[i][1] = result.getStatus();
        }

        DefaultTableModel model = new DefaultTableModel(rowData, columnNames);
        JTable resultTable = new JTable(model);

        DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer();
        resultTable.getColumnModel().getColumn(1).setCellRenderer(colorRenderer);
        resultTable.getColumnModel().getColumn(1).setCellRenderer(new RsvpStatusRenderer());
        resultTable.setFont(CustomFont.DEFAULT_FONT);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane);

        setPreferredSize(new Dimension(400, Integer.MAX_VALUE));
    }
}

class RsvpStatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if ("불가".equals(value))
            cellComponent.setForeground(Color.RED);
        return cellComponent;
    }
}
