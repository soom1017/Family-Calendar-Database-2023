package calendar.gui.popup;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import calendar.data.dao.EventDao;
import calendar.data.model.Event;
import calendar.gui.panel.UpdateEventPanel;
import calendar.gui.resources.font.CustomFont;
import calendar.service.UserSession;

public class ShowEventDialog extends JDialog {
    public ShowEventDialog(Event event) {
        setTitle("이벤트 보기");
        setLayout(new BorderLayout());

        UpdateEventPanel createEventPanel = new UpdateEventPanel(event);
        createEventPanel.setComponentsEnabled(false);
        add(createEventPanel, BorderLayout.CENTER);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));

        EmptyBorder headerPanelMargin = new EmptyBorder(10, 10, 0, 0);
        headerPanel.setBorder(headerPanelMargin);

        JButton modifyButton = new JButton("수정");
        CustomFont.applyButtonStyles(modifyButton);
        modifyButton.addActionListener(e -> {
            if (event.getHostId() == UserSession.getInstance().getUserId()) {
                createEventPanel.setComponentsEnabled(true);
                createEventPanel.saveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int result = createEventPanel.updateEvent(false);
                        if (result == 0) {
                            MessageDialog.alert("변경사항이 저장되었습니다.");
                            dispose();
                        }
                    }
                });
            } else {
                MessageDialog.alert("이벤트 생성자만 해당 이벤트를 수정할 수 있습니다.");
            }
        });
        headerPanel.add(modifyButton);

        headerPanel.add(Box.createHorizontalStrut(5));

        JButton deleteButton = new JButton("삭제");
        CustomFont.applyHighlightedButtonStyles(deleteButton);
        deleteButton.addActionListener(e -> {
            EventDao eventDao = new EventDao();
            if (eventDao.deleteEvent(event)) {
                MessageDialog.alert("성공적으로 삭제되었습니다.");
                dispose();
            } else {
                MessageDialog.alert("이벤트 삭제에 실패했습니다.");
            }
        });
        headerPanel.add(deleteButton);

        add(headerPanel, BorderLayout.NORTH);
    
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(900, 500);
        setModal(true);
        setLocationRelativeTo(null);
    }
}
