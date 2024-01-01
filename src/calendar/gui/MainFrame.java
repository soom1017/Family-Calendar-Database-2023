package calendar.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import calendar.data.connection.DatabaseManager;
import calendar.gui.listener.LoginListener;
import calendar.gui.listener.RegisterListener;
import calendar.gui.popup.WelcomeDialog;
import calendar.service.BackgroundService;
import calendar.service.UserSession;


public class MainFrame {
    public static void main(String[] args) {
        DatabaseManager.getConnection();

        BackgroundService backgroundService = new BackgroundService();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            backgroundService.stopBackgroundScheduler();
            DatabaseManager.closeAllConnections();
        }));

        SwingUtilities.invokeLater(() -> {
            // Main Calendar Frame
            JFrame frame = new JFrame("________'s Family Calendar App");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 900);
            frame.setLocationRelativeTo(null);

            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            CalendarPanel calendarPanel = new CalendarPanel();
            frame.add(calendarPanel, BorderLayout.CENTER);
            frame.setVisible(true);

            // Login First - Login Frame is over the Main Calendar Frame.
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.addLoginListener(new LoginListener() {
                @Override
                public void onLogin(boolean successful) {
                    if (successful) {
                        String username = UserSession.getInstance().getUser().getUserName();
                        frame.setTitle(username + "'s Calendar App");
                        calendarPanel.changeView(0);
                        backgroundService.startBackgroundScheduler();
                    } else {
                        System.exit(0);
                    }
                }
            });
            welcomeDialog.addRegisterListener(new RegisterListener() {
                @Override
                public void onRegister(boolean successful) {
                    if (successful) {
                        String username = UserSession.getInstance().getUser().getUserName();
                        frame.setTitle(username + "'s Calendar App");
                        calendarPanel.changeView(0);
                        backgroundService.startBackgroundScheduler();
                    } else {
                        System.exit(0);
                    }
                }
            });
            welcomeDialog.setVisible(true);
            
        });
    }
}



