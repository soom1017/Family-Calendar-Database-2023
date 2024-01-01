package calendar.gui.popup;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import calendar.gui.panel.UpdateEventPanel;

public class CreateEventDialog extends JDialog {
    public CreateEventDialog(Calendar date) {
        setTitle("이벤트 생성");

        UpdateEventPanel createEventPanel = new UpdateEventPanel(date);
        createEventPanel.saveButton.addActionListener(e -> {
            int result = createEventPanel.updateEvent(true);
            if (result == 0) {
                MessageDialog.alert("성공적으로 생성되었습니다.");
                dispose();
            }
        });
        add(createEventPanel);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(CreateEventDialog.this,
                        "이벤트를 생성하지 않고 창을 닫으시겠습니까?", "경고", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });

        setSize(900, 500);
        setModal(true);
        setLocationRelativeTo(null);
        
    }
}
