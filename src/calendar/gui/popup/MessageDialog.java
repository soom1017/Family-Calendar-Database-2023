package calendar.gui.popup;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

public abstract class MessageDialog {
    public static void alert(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(null, "결과 메시지");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.setVisible(true);
    }
}
