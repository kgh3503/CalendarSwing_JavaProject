package com.example.finance_server.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.example.finance_server.client.dto.Transaction;
import com.example.finance_server.client.dto.User;
import com.example.finance_server.util.ApiService;

public class DailyInputView extends JDialog implements ActionListener {

    private final CalendarSwing parent; 
    private String selectedDate; 
    private final User currentUser; 
    
    // DAO 삭제하고 ApiService 추가
    private final ApiService apiService;
    
    // UI 컴포넌트
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JButton categorySelectBtn; 
    private JTextField contentField; 
    private JButton saveBtn;

    // 카테고리 데이터 정의
    private final String[] EXPENSE_CATEGORIES = {"식비", "교통", "생활/쇼핑", "문화/여가", "건강/의료", "경조사/모임", "교육/자기개발", "기타"};
    private final String[] INCOME_CATEGORIES = {"근로 소득", "부가 소득", "금융 소득", "기타 소득"};

    // 생성자에 ApiService 추가
    public DailyInputView(CalendarSwing owner, String date, User user, ApiService apiService) {
        super(owner, user.getUsername() + "님의 " + date + " 입력", true); 
        this.parent = owner;
        this.selectedDate = date;
        this.currentUser = user; 
        this.apiService = apiService; // 전달받은 서비스 저장
        
        // 1. 창 기본 설정
        setSize(400, 350); // 높이 살짝 키움
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner); 

        // 2. 입력 폼 패널 생성
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 3. 컴포넌트 초기화 및 리스너 연결
        typeCombo = new JComboBox<>(new String[]{"지출", "수입"}); 
        amountField = new JTextField(15);
        categorySelectBtn = new JButton("카테고리 선택 ▼");
        contentField = new JTextField(15); 
        saveBtn = new JButton("저장");
        
        saveBtn.addActionListener(this); 
        categorySelectBtn.addActionListener(this);
        
        typeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    categorySelectBtn.setText("카테고리 선택 ▼"); 
                }
            }
        });

        // 4. 입력 폼 패널에 컴포넌트 추가
        inputPanel.add(new JLabel("날짜:"));
        inputPanel.add(new JLabel(date)); 
        inputPanel.add(new JLabel("유형:"));
        inputPanel.add(typeCombo);
        inputPanel.add(new JLabel("금액:"));
        inputPanel.add(amountField);
        inputPanel.add(new JLabel("카테고리:"));
        inputPanel.add(categorySelectBtn); 
        inputPanel.add(new JLabel("내용 (메모):")); 
        inputPanel.add(contentField); 

        // 5. 프레임에 패널 추가
        add(inputPanel, BorderLayout.CENTER);
        add(saveBtn, BorderLayout.SOUTH);

        setVisible(true);
    }
    
    private void showCategoryPopup(JButton sourceButton) {
        JPopupMenu popupMenu = new JPopupMenu();
        String selectedType = (String) typeCombo.getSelectedItem();
        String[] categories = selectedType.equals("수입") ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;
        
        for (String category : categories) {
            JMenuItem item = new JMenuItem(category);
            item.setBackground(Color.WHITE); 
            item.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            item.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { item.setBackground(Color.YELLOW); }
                @Override public void mouseExited(MouseEvent e) { item.setBackground(Color.WHITE); }
            });

            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sourceButton.setText(category); 
                    popupMenu.setVisible(false);
                }
            });
            popupMenu.add(item);
        }
        
        popupMenu.show(sourceButton, 0, sourceButton.getHeight());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveBtn) {
            handleSaveTransaction(); 
        } else if (e.getSource() == categorySelectBtn) {
            showCategoryPopup((JButton) e.getSource());
        }
    }
    
    private void handleSaveTransaction() {
        String type = (String) typeCombo.getSelectedItem();
        String amountText = amountField.getText();
        String category = categorySelectBtn.getText();
        String content = contentField.getText();

        if (amountText.isEmpty() || category.equals("카테고리 선택 ▼")) {
            JOptionPane.showMessageDialog(this, "금액과 카테고리는 필수 입력 항목입니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "금액은 유효한 숫자 형식이어야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Transaction 객체 생성 (ApiService로 보낼 객체)
        Transaction newTransaction = new Transaction(
            currentUser.getUserId(),    
            selectedDate,               
            type,                       
            amount,                     
            category,                   
            content                     
        );
        
        // transactionDao.add() -> apiService.addTransaction() 사용
        // 서버 통신 시도
        Transaction savedTransaction = apiService.addTransaction(newTransaction);
        
        if (savedTransaction != null) {
            JOptionPane.showMessageDialog(this, "거래 내역이 성공적으로 저장되었습니다.", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
            // 부모 창(달력) 새로고침
            parent.loadMonthData(); 
            this.dispose(); // 성공 시 창 닫기
        } else {
            JOptionPane.showMessageDialog(this, "서버 저장 실패: 잠시 후 다시 시도해주세요.", "저장 실패", JOptionPane.ERROR_MESSAGE);
        }
    }
}