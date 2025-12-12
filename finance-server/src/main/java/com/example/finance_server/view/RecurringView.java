package com.example.finance_server.view;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.example.finance_server.client.dto.RecurringTransaction;
import com.example.finance_server.client.dto.User;
import com.example.finance_server.util.ApiService;

public class RecurringView extends JDialog implements ActionListener {

    private final CalendarSwing parent;
    private final User currentUser;
    private final ApiService apiService;
    
    private DefaultTableModel tableModel;
    private JTable transactionsTable;
    
    // 입력 필드
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField contentField;
    private JTextField amountField;
    
    // [변경] 날짜 선택용 콤보박스 (시작일, 종료일)
    private JComboBox<Integer> sYear, sMonth, sDay; 
    private JComboBox<Integer> eYear, eMonth, eDay;
    
    private JButton addButton;
    private JButton deleteButton;

    private final Map<String, String[]> categories = new HashMap<>();

    public RecurringView(CalendarSwing owner, User user, ApiService apiService) {
        super(owner, "정기 거래 관리", true);
        this.parent = owner;
        this.currentUser = user;
        this.apiService = apiService;

        initCategories();
        setSize(900, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        
        // 1. 목록 테이블 (컬럼: 매월 -> 기간)
        String[] columnNames = {"ID", "유형", "카테고리", "내용", "금액", "기간"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        transactionsTable = new JTable(tableModel);
        
        // ID 컬럼 숨김
        transactionsTable.getColumn("ID").setMinWidth(0);
        transactionsTable.getColumn("ID").setMaxWidth(0);
        transactionsTable.getColumn("ID").setWidth(0);
        transactionsTable.getColumn("기간").setPreferredWidth(180);

        add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

        // 2. 입력 패널
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(createInputPanel(), BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("등록");
        deleteButton = new JButton("삭제");
        
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(southPanel, BorderLayout.SOUTH);

        loadRecurringTransactions();
    }

    private void initCategories() {
        categories.put("지출", new String[]{"식비", "교통", "생활/쇼핑", "문화/여가", "건강/의료", "경조사/모임", "교육/자기개발", "기타"});
        categories.put("수입", new String[]{"근로 소득", "부가 소득", "금융 소득", "기타 소득"});
    }
    
    // [핵심] 입력창 디자인 변경: 시작일 ~ 종료일 선택 (매월 며칠 선택 삭제)
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("정기 거래 설정 (기간 입력)"));

        typeCombo = new JComboBox<>(new String[]{"지출", "수입"});
        categoryCombo = new JComboBox<>();
        contentField = new JTextField();
        amountField = new JTextField();

        // 날짜 콤보박스 초기화
        sYear = new JComboBox<>(); sMonth = new JComboBox<>(); sDay = new JComboBox<>();
        eYear = new JComboBox<>(); eMonth = new JComboBox<>(); eDay = new JComboBox<>();
        
        int curYear = LocalDate.now().getYear();
        for(int i=curYear; i<=curYear+10; i++) { sYear.addItem(i); eYear.addItem(i); }
        for(int i=1; i<=12; i++) { sMonth.addItem(i); eMonth.addItem(i); }
        for(int i=1; i<=31; i++) { sDay.addItem(i); eDay.addItem(i); }
        
        // 기본값 설정
        sMonth.setSelectedItem(LocalDate.now().getMonthValue());
        sDay.setSelectedItem(LocalDate.now().getDayOfMonth());
        eYear.setSelectedItem(curYear + 1);

        // 날짜 패널 조립
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        startPanel.add(sYear); startPanel.add(new JLabel("/"));
        startPanel.add(sMonth); startPanel.add(new JLabel("/"));
        startPanel.add(sDay); 
        
        JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        endPanel.add(eYear); endPanel.add(new JLabel("/"));
        endPanel.add(eMonth); endPanel.add(new JLabel("/"));
        endPanel.add(eDay);

        typeCombo.addActionListener(e -> updateCategoryCombo());
        updateCategoryCombo();

        // 화면 배치
        inputPanel.add(new JLabel("유형 / 카테고리:")); 
        JPanel typeCatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typeCatPanel.add(typeCombo); typeCatPanel.add(new JLabel(" ")); typeCatPanel.add(categoryCombo);
        inputPanel.add(typeCatPanel);

        inputPanel.add(new JLabel("금액 / 내용:"));
        JPanel amountContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        amountField.setPreferredSize(new Dimension(100, 25));
        contentField.setPreferredSize(new Dimension(150, 25));
        amountContentPanel.add(amountField); amountContentPanel.add(new JLabel("원  ")); amountContentPanel.add(contentField);
        inputPanel.add(amountContentPanel);

        inputPanel.add(new JLabel("시작일 (이 날부터 매월 반복):"));
        inputPanel.add(startPanel);

        inputPanel.add(new JLabel("종료일 (이 날까지만 반복):"));
        inputPanel.add(endPanel);

        return inputPanel;
    }

    private void updateCategoryCombo() {
        String selectedType = (String) typeCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        if (selectedType != null) {
            String[] cats = categories.get(selectedType);
            if (cats != null) for (String cat : cats) categoryCombo.addItem(cat);
        }
    }
    
    private void loadRecurringTransactions() {
        tableModel.setRowCount(0); 
        List<RecurringTransaction> list = apiService.getRecurringTransactions(currentUser.getUserId());
        for (RecurringTransaction rt : list) {
            // [수정] 기간이 null이면 빈 문자열로 표시
            String start = rt.getStartDate() == null ? "" : rt.getStartDate();
            String end = rt.getEndDate() == null ? "" : rt.getEndDate();
            String period = start + " ~ " + end;
            
            tableModel.addRow(new Object[]{
                rt.getId(), rt.getType(), rt.getCategory(), rt.getContent(), 
                String.format("%,.0f", rt.getAmount()), period
            });
        }
    }

    private void addRecurringTransaction() {
        try {
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            String content = contentField.getText();
            double amount = Double.parseDouble(amountField.getText());

            // 1. 날짜를 "yyyy-MM-dd" 문자열로 정확히 변환
            String startDate = String.format("%04d-%02d-%02d", 
                sYear.getSelectedItem(), sMonth.getSelectedItem(), sDay.getSelectedItem());
            
            String endDate = String.format("%04d-%02d-%02d", 
                eYear.getSelectedItem(), eMonth.getSelectedItem(), eDay.getSelectedItem());

            if (startDate.compareTo(endDate) > 0) {
                JOptionPane.showMessageDialog(this, "종료일이 시작일보다 빨라야 합니다.");
                return;
            }

            RecurringTransaction newRt = new RecurringTransaction(
                currentUser.getUserId(), type, amount, category, content, startDate, endDate
            );

            if (apiService.addRecurringTransaction(newRt)) {
                JOptionPane.showMessageDialog(this, "등록되었습니다.\n매월 " + sDay.getSelectedItem() + "일에 내역이 생성됩니다.");
                loadRecurringTransactions();
                contentField.setText(""); amountField.setText("");
                
                // 즉시 반영 여부 확인
                int confirm = JOptionPane.showConfirmDialog(this, "이번 달 달력에도 바로 반영하시겠습니까?", "즉시 반영", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    apiService.applyRecurringTransactions(currentUser.getUserId(), parent.year, parent.month);
                    parent.loadMonthData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "등록 실패 (서버 로그를 확인하세요)");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "금액은 숫자여야 합니다.");
        }
    }

    // [삭제] 기간 외 내역 삭제 로직 (기존 유지)
    private void deleteRecurringTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int id = (int) tableModel.getValueAt(transactionsTable.convertRowIndexToModel(selectedRow), 0); 

        if (JOptionPane.showConfirmDialog(this, "해지(삭제)하시겠습니까?\n(이번 달 이후의 미래 내역은 자동으로 정리됩니다)", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // 이번 달 1일을 기준으로 미래 데이터 삭제 (종료일 따로 안 물어봄)
            if (apiService.deleteRecurringTransaction(id, parent.year, parent.month, 1, 9999, 12, 31)) { 
                JOptionPane.showMessageDialog(this, "해지되었습니다.");
                loadRecurringTransactions();
                parent.loadMonthData();
            } else {
                JOptionPane.showMessageDialog(this, "삭제 실패");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) addRecurringTransaction();
        else if (e.getSource() == deleteButton) deleteRecurringTransaction();
    }
}