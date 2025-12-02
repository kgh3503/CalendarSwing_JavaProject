import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RecurringView extends JDialog implements ActionListener {

    private final CalendarSwing parent;
    private final User currentUser;
    
    // DAO 제거 -> ApiService 사용
    private final ApiService apiService;
    
    private DefaultTableModel tableModel;
    private JTable transactionsTable;
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField contentField;
    private JTextField amountField;
    private JComboBox<Integer> dayOfMonthCombo;
    private JButton addButton;
    private JButton deleteButton;

    private final Map<String, String[]> categories = new HashMap<>();

    // 생성자 파라미터 변경 (Dao -> ApiService)
    public RecurringView(CalendarSwing owner, User user, ApiService apiService) {
        super(owner, "정기 거래 관리", true);
        this.parent = owner;
        this.currentUser = user;
        this.apiService = apiService; // 변경됨

        initCategories();
        
        setSize(800, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        
        // 테이블 설정
        String[] columnNames = {"ID", "유형", "카테고리", "내용", "금액", "반복일"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        transactionsTable = new JTable(tableModel);
        
        // ID 컬럼 숨기기
        transactionsTable.getColumn("ID").setMinWidth(0);
        transactionsTable.getColumn("ID").setMaxWidth(0);
        transactionsTable.getColumn("ID").setWidth(0);

        add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

        // 하단 패널
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(createInputPanel(), BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("정기 거래 추가");
        deleteButton = new JButton("선택 항목 삭제");
        
        addButton.addActionListener(this);
        deleteButton.addActionListener(this);
        
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(southPanel, BorderLayout.SOUTH);

        loadRecurringTransactions(); // 데이터 로드
    }

    private void initCategories() {
        categories.put("지출", new String[]{"식비", "교통", "생활/쇼핑", "문화/여가", "건강/의료", "경조사/모임", "교육/자기개발", "기타"});
        categories.put("수입", new String[]{"근로 소득", "부가 소득", "금융 소득", "기타 소득"});
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(3, 4, 10, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("새 정기 거래 입력"));

        typeCombo = new JComboBox<>(new String[]{"지출", "수입"});
        categoryCombo = new JComboBox<>();
        contentField = new JTextField();
        amountField = new JTextField();
        dayOfMonthCombo = new JComboBox<>();
        for (int i = 1; i <= 31; i++) dayOfMonthCombo.addItem(i);

        typeCombo.addActionListener(e -> updateCategoryCombo());
        updateCategoryCombo();

        inputPanel.add(new JLabel("유형:")); inputPanel.add(typeCombo);
        inputPanel.add(new JLabel("카테고리:")); inputPanel.add(categoryCombo);
        inputPanel.add(new JLabel("반복일 (매월):")); inputPanel.add(dayOfMonthCombo);
        inputPanel.add(new JLabel("금액:")); inputPanel.add(amountField);
        inputPanel.add(new JLabel("내용:")); inputPanel.add(contentField);
        inputPanel.add(new JLabel("")); inputPanel.add(new JLabel(""));

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
    
    // [변경] ApiService를 사용하여 데이터 로드
    private void loadRecurringTransactions() {
        tableModel.setRowCount(0); 
        List<RecurringTransaction> list = apiService.getRecurringTransactions(currentUser.getUserId());
        
        for (RecurringTransaction rt : list) {
            Object[] row = {
                rt.getId(),
                rt.getType(),
                rt.getCategory(),
                rt.getContent(),
                String.format("%,.0f", rt.getAmount()),
                rt.getDayOfMonth() + "일"
            };
            tableModel.addRow(row);
        }
    }

    private void clearInputFields() {
        contentField.setText("");
        amountField.setText("");
        dayOfMonthCombo.setSelectedIndex(0);
    }
    
    // [변경] ApiService를 사용하여 추가
    private void addRecurringTransaction() {
        try {
            String type = (String) typeCombo.getSelectedItem();
            String category = (String) categoryCombo.getSelectedItem();
            String content = contentField.getText();
            int dayOfMonth = (int) dayOfMonthCombo.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText());

            if (content.isEmpty() || amount <= 0) {
                 JOptionPane.showMessageDialog(this, "내용과 유효한 금액을 입력하세요.");
                 return;
            }

            RecurringTransaction newRt = new RecurringTransaction(
                currentUser.getUserId(), type, amount, category, content, dayOfMonth
            );

            // API 호출
            if (apiService.addRecurringTransaction(newRt)) {
                JOptionPane.showMessageDialog(this, "저장되었습니다.");
                loadRecurringTransactions();
                clearInputFields();
            } else {
                JOptionPane.showMessageDialog(this, "저장 실패 (서버 오류)", "오류", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "금액은 숫자여야 합니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // [변경] ApiService를 사용하여 삭제
    private void deleteRecurringTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0); 

        if (JOptionPane.showConfirmDialog(this, "삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (apiService.deleteRecurringTransaction(id)) {
                JOptionPane.showMessageDialog(this, "삭제되었습니다.");
                loadRecurringTransactions();
            } else {
                JOptionPane.showMessageDialog(this, "삭제 실패", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) addRecurringTransaction();
        else if (e.getSource() == deleteButton) deleteRecurringTransaction();
    }
}