import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginView extends JDialog implements ActionListener {

    // UserDao 필드가 ApiService 필드로 대체됨
    private final ApiService apiService = new ApiService();
    
    private JTextField idField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JButton signupBtn;
    
    private User loggedInUser = null; 

    // (생성자는 원본과 동일)
    public LoginView(JFrame owner) {
        super(owner, "사용자 로그인", true); 
        
        setSize(300, 200);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // 중앙 입력 패널 (원본과 동일)
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        
        idField = new JTextField(15);
        passwordField = new JPasswordField(15);

        inputPanel.add(new JLabel(" 아이디:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel(" 비밀번호:"));
        inputPanel.add(passwordField);

        // 하단 버튼 패널 (원본과 동일)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        loginBtn = new JButton("로그인");
        signupBtn = new JButton("회원가입");
        
        loginBtn.addActionListener(this);
        signupBtn.addActionListener(this);
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    

    public User showDialog() {
        // DatabaseManager.createTables(); 
        this.setVisible(true);
        return loggedInUser;
    }
    
    /**
     * userDao.authenticate() 대신 apiService.login()을 호출
     */
    private void handleLogin() {
        String id = idField.getText();
        String pw = new String(passwordField.getPassword());
        
        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        //  DAO 호출 대신 ApiService 호출
        User user = apiService.login(id, pw);
        
        if (user != null) {
            loggedInUser = user; // 서버로부터 받은 User 객체 저장
            JOptionPane.showMessageDialog(this, user.getUsername() + "님, 환영합니다!", "로그인 성공", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); 
        } else {
            // 서버가 null을 반환했거나, 네트워크 오류가 발생
            JOptionPane.showMessageDialog(this, "아이디 또는 비밀번호가 일치하지 않거나 서버에 연결할 수 없습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleSignup() {
        String id = idField.getText();
        String pw = new String(passwordField.getPassword());

        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // DAO 호출 대신 ApiService 호출
        User newUser = apiService.register(id, pw);
        
        if (newUser != null) {
            JOptionPane.showMessageDialog(this, "회원가입이 완료되었습니다!\n이제 로그인해주세요.", "회원가입 성공", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 서버가 null을 반환했거나, 네트워크 오류가 발생
            JOptionPane.showMessageDialog(this, "회원가입 실패: 아이디가 이미 존재하거나 서버에 연결할 수 없습니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            handleLogin();
        } else if (e.getSource() == signupBtn) {
            handleSignup();
        }
    }
}