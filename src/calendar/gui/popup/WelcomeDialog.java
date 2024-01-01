package calendar.gui.popup;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import calendar.gui.listener.LoginListener;
import calendar.gui.listener.RegisterListener;
import calendar.gui.resources.font.CustomFont;
import calendar.service.UserAuthService;

public class WelcomeDialog extends JDialog {
    private JLabel noticeLabel, idLabel, passwordLabel, emailLabel, usernameLabel, familyLabel, familyCodeLabel;
    private JTextField idField, emailField, usernameField, familyCodeField;
    private JPasswordField passwordField;
    private JCheckBox createFamilyCheckBox;
    private JButton loginButton, registerButton, goLoginButton, goRegisterButton;

    private UserAuthService authService;
    private LoginListener loginListener;
    private RegisterListener registerListener;

    public WelcomeDialog() {
        setTitle("환영합니다!");
        setLayout(new GridBagLayout());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;

        noticeLabel = new JLabel("계속 진행하려면 로그인하거나 '회원가입' 버튼을 눌러 새 계정을 만드세요.");
        noticeLabel.setFont(CustomFont.HEADER_FONT);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 5;
        add(noticeLabel, constraints);

        idLabel = new JLabel("아이디: ");
        idLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridy++;
        constraints.gridwidth = 1;
        add(idLabel, constraints);
        
        idField = new JTextField(20);
        idField.setFont(CustomFont.DEFAULT_FONT);
        idField.setMinimumSize(idField.getPreferredSize());
        constraints.gridx = 1;
        add(idField, constraints);
        
        passwordLabel = new JLabel("비밀번호: ");
        passwordLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 0;
        constraints.gridy++;
        add(passwordLabel, constraints);

        passwordField = new JPasswordField(20);
        passwordField.setFont(CustomFont.DEFAULT_FONT);
        passwordField.setMinimumSize(passwordField.getPreferredSize());
        constraints.gridx = 1;
        add(passwordField, constraints);

        // additional fields for register.
        usernameLabel = new JLabel("사용자명: ");
        usernameLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 0;
        constraints.gridy++;
        add(usernameLabel, constraints);

        usernameField = new JTextField(20);
        usernameField.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 1;
        add(usernameField, constraints);

        emailLabel = new JLabel("이메일: ");
        emailLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 0;
        constraints.gridy++;
        add(emailLabel, constraints);
        
        emailField = new JTextField(20);
        emailField.setFont(CustomFont.DEFAULT_FONT);
        emailField.setMinimumSize(emailField.getPreferredSize());
        constraints.gridx = 1;
        add(emailField, constraints);

        familyLabel = new JLabel("가족 코드가 있으신가요? 또는, 새로운 가족 코드를 생성하고 이 코드를 입력하여 다른 구성원이 가족 계정에 접속할 수 있습니다.");
        familyLabel.setFont(CustomFont.DETAIL_FONT);
        constraints.gridx = 0;
        constraints.gridy += 2;
        constraints.gridwidth = 5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(familyLabel, constraints);

        familyCodeLabel = new JLabel("가족코드: ");
        familyCodeLabel.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        add(familyCodeLabel, constraints);

        familyCodeField = new JTextField(20);
        familyCodeField.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 1;
        constraints.gridwidth = 1;
        add(familyCodeField, constraints);

        createFamilyCheckBox = new JCheckBox("새로운 가족입니다.");
        createFamilyCheckBox.addActionListener(e -> {
            boolean selected = createFamilyCheckBox.isSelected();
            familyCodeField.setEnabled(!selected);
        });
        createFamilyCheckBox.setFont(CustomFont.DEFAULT_FONT);
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        add(createFamilyCheckBox, constraints);

        usernameLabel.setVisible(false);
        usernameField.setVisible(false);
        emailLabel.setVisible(false);
        emailField.setVisible(false);
        familyLabel.setVisible(false);
        familyCodeLabel.setVisible(false);
        familyCodeField.setVisible(false);
        createFamilyCheckBox.setVisible(false);

        authService = new UserAuthService();
        
        loginButton = new JButton("로그인");
        loginButton.addActionListener(e -> processLogin());
        CustomFont.applyHighlightedButtonStyles(loginButton);
        constraints.gridx = 3;
        constraints.gridy++;
        constraints.gridwidth = 1;
        add(loginButton, constraints);

        goLoginButton = new JButton("뒤로가기");
        goLoginButton.addActionListener(e -> goLogin());
        CustomFont.applyButtonStyles(goLoginButton);
        add(goLoginButton, constraints);

        goRegisterButton = new JButton("회원가입");
        goRegisterButton.addActionListener(e -> goRegister());
        CustomFont.applyButtonStyles(goRegisterButton);
        constraints.gridx = 4;
        add(goRegisterButton, constraints);

        registerButton = new JButton("제출하기");
        registerButton.addActionListener(e -> processRegister());
        CustomFont.applyHighlightedButtonStyles(registerButton);
        add(registerButton, constraints);

        goLoginButton.setVisible(false);
        registerButton.setVisible(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // When dialog closed by user, alert main to exit the program.
                if (loginListener != null) {
                    loginListener.onLogin(false);
                }
            }
        });
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 400);
        setModal(true);
        setLocationRelativeTo(null);
    }

    public void addLoginListener(LoginListener listener) {
        this.loginListener = listener;
    }
    public void addRegisterListener(RegisterListener listener) {
        this.registerListener = listener;
    }

    private void processLogin() {
        String uid = idField.getText();
        String password = new String(passwordField.getPassword());

        if (uid.isEmpty()) {
            MessageDialog.alert("아이디를 입력해주세요.");
            idField.setText("");
        } else if (password.isEmpty()) {
            MessageDialog.alert("패스워드를 입력해주세요.");
            idField.setText("");
        } else if (!authService.login(uid, password)) {
            MessageDialog.alert("로그인에 실패했습니다. 다시 시도해주세요.");
            idField.setText("");
            passwordField.setText("");
        } else {
            if (loginListener != null)
                loginListener.onLogin(true);
            MessageDialog.alert("로그인에 성공했습니다.");
            setVisible(false);
        }
    }
    private void processRegister() {
        String uid = idField.getText();
        String useremail = emailField.getText();
        String password = new String(passwordField.getPassword());
        String username = usernameField.getText();
        String familyCode = familyCodeField.getText();

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
        } else if (!createFamilyCheckBox.isSelected()) {
            if (familyCode.isEmpty() || !authService.register(uid, username, useremail, password, familyCode)) {
                MessageDialog.alert("유효하지 않은 가족코드입니다. 다시 시도해 주십시오.");
                familyCodeField.setText("");
            } else {
                if (registerListener != null)
                    registerListener.onRegister(true);
                MessageDialog.alert("성공적으로 등록되었습니다.");
                setVisible(false);
            }
        } else {
            if (!authService.register(uid, username, useremail, password)) {
                MessageDialog.alert("등록에 실패했습니다. 나중에 다시 시도해주세요.");
            } else {
                if (registerListener != null)
                    registerListener.onRegister(true);
                MessageDialog.alert("성공적으로 등록되었습니다. 생성된 가족코드는: '" + authService.getFamilyCode() + "' 입니다. 이 코드를 안전한 곳에 기록해두세요. 다른 가족 구성원이 계정에 접속할 때 필요합니다.");
                setVisible(false);
            }
        }
    }
    private void goLogin() {
        noticeLabel.setText("계속 진행하려면 로그인하거나 '회원가입' 버튼을 눌러 새 계정을 만드세요.");
        usernameLabel.setVisible(false);
        usernameField.setVisible(false);
        emailLabel.setVisible(false);
        emailField.setVisible(false);
        familyLabel.setVisible(false);
        familyCodeLabel.setVisible(false);
        familyCodeField.setVisible(false);
        createFamilyCheckBox.setVisible(false);
        
        goLoginButton.setVisible(false);
        registerButton.setVisible(false);

        loginButton.setVisible(true);
        goRegisterButton.setVisible(true);
        
        revalidate();
        repaint();
    }
    private void goRegister() {
        noticeLabel.setText("회원가입 정보를 입력해주세요.");
        usernameLabel.setVisible(true);
        usernameField.setVisible(true);
        emailLabel.setVisible(true);
        emailField.setVisible(true);
        familyLabel.setVisible(true);
        familyCodeLabel.setVisible(true);
        familyCodeField.setVisible(true);
        createFamilyCheckBox.setVisible(true);

        loginButton.setVisible(false);
        goRegisterButton.setVisible(false);

        goLoginButton.setVisible(true);
        registerButton.setVisible(true);
        
        revalidate();
        repaint();
    }
}