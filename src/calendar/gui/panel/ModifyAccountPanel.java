package calendar.gui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import calendar.data.model.User;
import calendar.gui.popup.MessageDialog;
import calendar.gui.resources.font.CustomFont;
import calendar.service.UserAuthService;
import calendar.service.UserSession;

public class ModifyAccountPanel extends JPanel {
    private JTextField idField, usernameField, emailField;
    private JPasswordField passwordField;

    private UserAuthService authService;
    private User user;

    public ModifyAccountPanel() {
        this.user = UserSession.getInstance().getUser();
        this.authService = new UserAuthService();
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;

        JLabel noticeLabel = new JLabel("변경된 계정 정보를 입력하고 반영할 수 있습니다.");
        noticeLabel.setFont(CustomFont.HEADER_FONT);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 5;
        add(noticeLabel, constraints);

        JLabel idLabel = new JLabel("아이디: ");
        idLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridy++;
        constraints.gridwidth = 1;
        add(idLabel, constraints);
        
        idField = new JTextField(20);
        idField.setText(user.getUid());
        idField.setFont(CustomFont.DEFAULT_FONT);
        idField.setMinimumSize(idField.getPreferredSize());
        constraints.gridx = 1;
        add(idField, constraints);
        
        JLabel passwordLabel = new JLabel("비밀번호: ");
        passwordLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 0;
        constraints.gridy++;
        add(passwordLabel, constraints);

        passwordField = new JPasswordField(20);
        passwordField.setFont(CustomFont.DEFAULT_FONT);
        passwordField.setMinimumSize(passwordField.getPreferredSize());
        constraints.gridx = 1;
        add(passwordField, constraints);

        JLabel usernameLabel = new JLabel("사용자명: ");
        usernameLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 0;
        constraints.gridy++;
        add(usernameLabel, constraints);

        usernameField = new JTextField(20);
        usernameField.setText(user.getUserName());
        usernameField.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 1;
        add(usernameField, constraints);

        JLabel emailLabel = new JLabel("이메일: ");
        emailLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 0;
        constraints.gridy++;
        add(emailLabel, constraints);
        
        emailField = new JTextField(20);
        emailField.setText(user.getEmail());
        emailField.setFont(CustomFont.DEFAULT_FONT);
        emailField.setMinimumSize(emailField.getPreferredSize());
        constraints.gridx = 1;
        add(emailField, constraints);

        JButton updateUserAccountButton = new JButton("정보 저장");
        updateUserAccountButton.addActionListener(e -> updateUserAccount());
        CustomFont.applyHighlightedButtonStyles(updateUserAccountButton);
        constraints.gridx = 3;
        constraints.gridy++;
        constraints.gridwidth = 1;
        add(updateUserAccountButton, constraints);
    }

    private void updateUserAccount() {
        String uid = idField.getText();
        String useremail = emailField.getText();
        String password = new String(passwordField.getPassword());
        String username = usernameField.getText();

        if (uid.isEmpty()) {
            MessageDialog.alert("아이디를 입력해주세요.");
            idField.setText("");
        } else if (password.isEmpty()) {
            MessageDialog.alert("패스워드를 입력해주세요.");
            idField.setText("");
        } if (useremail.isEmpty() || !authService.isValidEmail(useremail)) {
            MessageDialog.alert("올바르지 않은 이메일 주소입니다. 다시 시도해 주십시오.");
            emailField.setText("");
        } else if (authService.isEmailExists(useremail)) {
            MessageDialog.alert("등록에 실패했습니다. 해당 이메일 계정은 이미 존재합니다.");
            emailField.setText("");
            passwordField.setText("");
        } else if (username.isEmpty()) {
            MessageDialog.alert("사용자명을 입력해주세요.");
        } else {
            if (authService.updateUserAccount(new User(user.getUserId(), uid, username, useremail, password)))
                MessageDialog.alert("계정정보가 업데이트되었습니다.");
            else
                MessageDialog.alert("계정정보 업데이트에 문제가 생겼습니다.");
        }
    }
}
