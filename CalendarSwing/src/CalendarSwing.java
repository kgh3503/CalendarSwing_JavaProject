import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class CalendarSwing extends JFrame implements ItemListener, ActionListener {

    Font fnt = new Font("굴림체", Font.BOLD, 18);

    // 상단 선택 패널
    JPanel selectPane = new JPanel();
    JButton prevBtn = new JButton("◀");
    JButton nextBtn = new JButton("▶");
    JComboBox<Integer> yearCombo = new JComboBox<Integer>();
    JComboBox<Integer> monthCombo = new JComboBox<Integer>();
    JLabel yearLBl = new JLabel("년");
    JLabel monthLBl = new JLabel("월");
    JButton analysisBtn = new JButton("분석");
    JButton calculatorBtn = new JButton("계산기");
    JButton goalBtn = new JButton("목표 관리");
    
    // 테마 변경 버튼
    JButton themeToggleBtn = new JButton("다크 모드");
    private boolean isDarkMode = false; // 현재 상태 기억

    // 중앙 캘린더 패널
    JPanel centerPane = new JPanel(new BorderLayout());
    JPanel titlePane = new JPanel(new GridLayout(1, 7));
    String[] title = {"일", "월", "화", "수", "목", "금", "토"};
    JPanel dayPane = new JPanel(new GridLayout(0, 7));

    // 달력 데이터
    Calendar date;
    int year;
    int month;

    // ApiService 및 User 필드
    private final User currentUser;
    private final ApiService apiService = new ApiService(); 
    
    private List<Transaction> currentMonthTransactions;

    // 오른쪽 상세 패널 컴포넌트
    private JPanel detailsPanel;
    private JLabel selectedDateLabel;
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    
    // 입력 필드 대신 버튼
    private JButton openInputBtn;
    private JButton deleteButton;

    // 선택 상태 관리
    private JButton previouslySelectedDayButton = null;
    private String currentSelectedDate = null;
    
    public CalendarSwing(User user) {
        super("가계부 달력 - " + user.getUsername() + "님");
        this.currentUser = user;
        
        date = Calendar.getInstance();
        year = date.get(Calendar.YEAR);
        month = date.get(Calendar.MONTH) + 1;

        // 1. 상단 패널 (NORTH)
        prevBtn.setFont(fnt); selectPane.add(prevBtn);
        yearCombo.setFont(fnt); selectPane.add(yearCombo);
        yearLBl.setFont(fnt); selectPane.add(yearLBl);
        monthCombo.setFont(fnt); selectPane.add(monthCombo);
        monthLBl.setFont(fnt); selectPane.add(monthLBl);
        nextBtn.setFont(fnt); selectPane.add(nextBtn);
        analysisBtn.setFont(fnt); selectPane.add(analysisBtn);
        calculatorBtn.setFont(fnt); selectPane.add(calculatorBtn);
        
        goalBtn.setFont(fnt);
        goalBtn.addActionListener(this);
        selectPane.add(goalBtn);
        
        // 테마 버튼 달기
        themeToggleBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        themeToggleBtn.setBackground(new Color(220, 220, 220));
        themeToggleBtn.addActionListener(this);
        selectPane.add(themeToggleBtn);
        
        add(BorderLayout.NORTH, selectPane);

        // 2. 캘린더 패널 (CENTER) 
        setYear();
        setMonth();
        setCalendarTitle();
        centerPane.add(BorderLayout.NORTH, titlePane);
        centerPane.add(dayPane);
        add(centerPane, BorderLayout.CENTER);

        // 3. 상세 정보 패널 (EAST) 
        this.detailsPanel = createDetailsPanel();
        add(detailsPanel, BorderLayout.EAST);

        // 4. 이벤트 리스너 
        prevBtn.addActionListener(this);
        nextBtn.addActionListener(this);
        yearCombo.addItemListener(this);
        monthCombo.addItemListener(this);
        analysisBtn.addActionListener(this);
        calculatorBtn.addActionListener(this);
        
        // 5. JFrame 설정
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setMinimumSize(new Dimension(1024, 768));	
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        loadMonthData();
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setMinimumSize(new Dimension(300, 0));	

        selectedDateLabel = new JLabel("날짜를 선택하세요");
        selectedDateLabel.setFont(fnt);
        selectedDateLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        panel.add(selectedDateLabel);
        
        panel.add(javax.swing.Box.createVerticalStrut(10));

        String[] columnNames = {"유형", "카테고리", "내용", "금액", "ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        transactionsTable = new JTable(tableModel);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        hideIdColumn(transactionsTable);

        JScrollPane tableScrollPane = new JScrollPane(transactionsTable);
        panel.add(tableScrollPane);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        openInputBtn = new JButton("가계부 작성하기");
        openInputBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        deleteButton = new JButton("선택 삭제");
        
        openInputBtn.addActionListener(this);
        deleteButton.addActionListener(this);

        buttonPanel.add(openInputBtn);
        buttonPanel.add(deleteButton);
        
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        panel.add(buttonPanel);

        return panel;
    }
    
    private void hideIdColumn(JTable table) {
        TableColumn idColumn = table.getColumn("ID");
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setWidth(0);
        idColumn.setPreferredWidth(0);
        idColumn.setResizable(false);
    }
    
    public void updateCalendarUI() {
        dayPane.removeAll();

        date.set(year, month - 1, 1);
        int week = date.get(Calendar.DAY_OF_WEEK);
        int lastDay = date.getActualMaximum(Calendar.DATE);

        Map<Integer, Map<String, Double>> dailyIncomeMaps = new HashMap<>();
        Map<Integer, Map<String, Double>> dailyExpenseMaps = new HashMap<>();

        if (currentMonthTransactions != null) {
            for (Transaction t : currentMonthTransactions) {
                int dayOfMonth = Integer.parseInt(t.getDate().substring(8));
                if (t.getType().equals("수입")) {
                    dailyIncomeMaps.computeIfAbsent(dayOfMonth, k -> new HashMap<>())
                                 .merge(t.getCategory(), t.getAmount(), Double::sum);
                } else {
                    dailyExpenseMaps.computeIfAbsent(dayOfMonth, k -> new HashMap<>())
                                  .merge(t.getCategory(), t.getAmount(), Double::sum);
                }
            }
        }

        for (int s = 1; s < week; s++) {
            dayPane.add(new JLabel(" "));
        }

        for (int day = 1; day <= lastDay; day++) {
            JPanel dayCell = new JPanel(new BorderLayout());
            dayCell.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("굴림체", Font.BOLD, 14));
            dayBtn.setPreferredSize(new Dimension(50, 20));
            dayBtn.setMargin(new Insets(0, 0, 0, 0));

            String dateString = String.format("%d-%02d-%02d", year, month, day);
            dayBtn.setActionCommand(dateString);
            dayBtn.addActionListener(this);
            
            if (dateString.equals(currentSelectedDate)) {
                dayBtn.setBackground(Color.YELLOW);
                dayBtn.setOpaque(true); // FlatLaf에서 배경색 보이게
                previouslySelectedDayButton = dayBtn;
            }

            date.set(Calendar.DATE, day);
            int w = date.get(Calendar.DAY_OF_WEEK);
            
            // 다크모드 대응 색상
            if (w == Calendar.SUNDAY) dayBtn.setForeground(new Color(255, 80, 80));
            if (w == Calendar.SATURDAY) dayBtn.setForeground(new Color(80, 120, 255));

            dayCell.add(dayBtn, BorderLayout.NORTH);

            Map<String, Double> incomes = dailyIncomeMaps.get(day);
            Map<String, Double> expenses = dailyExpenseMaps.get(day);
            StringBuilder incomeStr = new StringBuilder();
            StringBuilder expenseStr = new StringBuilder();
            if (incomes != null && !incomes.isEmpty()) {
                for (Map.Entry<String, Double> entry : incomes.entrySet()) {
                    incomeStr.append(String.format(Locale.KOREA, "%s: +%,.0f<br>",	
                                      entry.getKey(), entry.getValue()));
                }
            }
            if (expenses != null && !expenses.isEmpty()) {
                for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                    expenseStr.append(String.format(Locale.KOREA, "%s: -%,.0f<br>",	
                                      entry.getKey(), entry.getValue()));
                }
            }
            JLabel summary = new JLabel("", SwingConstants.CENTER);	
            summary.setFont(new Font("맑은 고딕", Font.PLAIN, 12));	
            
            // 다크/라이트 모드 모두 잘 보이는 색상 태그
            if (incomeStr.length() > 0 || expenseStr.length() > 0) {
                summary.setText("<html><font color='#3388ff'>" + incomeStr.toString() + "</font>" +	
                                "<font color='#ff4444'>" + expenseStr.toString() + "</font></html>");
            }
            dayCell.add(summary, BorderLayout.CENTER);
            
            dayPane.add(dayCell);
        }

        dayPane.revalidate();
        dayPane.repaint();
    }

    public void loadMonthData() {
        try {
            this.currentMonthTransactions = apiService.getMonthlyTransactions(
                currentUser.getUserId(), this.year, this.month
            );
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "데이터 로드 실패: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            this.currentMonthTransactions = new ArrayList<>(); 
        }
        updateCalendarUI();
    }

    private void setDayReset() {
        yearCombo.removeItemListener(this);
        monthCombo.removeItemListener(this);
        yearCombo.setSelectedItem(year);
        monthCombo.setSelectedItem(month);
        
        currentSelectedDate = null;
        previouslySelectedDayButton = null;
        updateDetailsPanel(null); 
        
        dayPane.setVisible(false);
        dayPane.removeAll();
        yearCombo.addItemListener(this);
        monthCombo.addItemListener(this);
        loadMonthData(); 
        dayPane.setVisible(true);
    }
    
    private void updateDetailsPanel(String dateString) {
        if (dateString == null) {
            selectedDateLabel.setText("날짜를 선택하세요");
            tableModel.setRowCount(0); 
            return;
        }
        
        try {
            LocalDate date = LocalDate.parse(dateString);
            String formattedDate = String.format("%d월 %d일 소비 내역",
                date.getMonthValue(), date.getDayOfMonth());
            selectedDateLabel.setText(formattedDate);
        } catch (Exception e) {
            selectedDateLabel.setText(dateString);
        }

        tableModel.setRowCount(0); 

        List<Transaction> txList = new ArrayList<>(); 
        try {
            txList = apiService.getDailyTransactions(currentUser.getUserId(), dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (txList != null) {
            for (Transaction t : txList) {
                Object[] row = {
                    t.getType(),
                    t.getCategory(),
                    t.getContent(),
                    String.format(Locale.KOREA, "%,.0f", t.getAmount()),
                    t.getTransactionId()
                };
                tableModel.addRow(row);
            }
        }
    }
    
    private void deleteTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요.");
            return;
        }
        int modelRow = transactionsTable.convertRowIndexToModel(selectedRow);
        int transactionId = (int) tableModel.getValueAt(modelRow, 4); 

        int confirm = JOptionPane.showConfirmDialog(this,
            "정말로 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                apiService.deleteTransaction(transactionId);
                updateDetailsPanel(currentSelectedDate); 
                loadMonthData(); 
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "삭제 오류: " + e.getMessage());
            }
        }
    }

    public void setCalendarTitle() {
        for (String s : title) {
            JLabel lbl = new JLabel(s, JLabel.CENTER);
            lbl.setFont(fnt);
            if (s.equals("일")) lbl.setForeground(new Color(255, 80, 80));
            if (s.equals("토")) lbl.setForeground(new Color(80, 120, 255));
            titlePane.add(lbl);
        }
    }
    
    public void setYear() {
        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        for (int i = currentYear - 10; i <= currentYear + 10; i++) {
            yearCombo.addItem(i);
        }
        yearCombo.setSelectedItem(year);
    }
    
    public void setMonth() {
        for (int i = 1; i <= 12; i++) {
            monthCombo.addItem(i);
        }
        monthCombo.setSelectedItem(month);
    }
    
    public void prevMonth() {
        if (month == 1) { year--; month = 12; } else { month--; }
    }
    
    public void nextMonth() {
        if (month == 12) { year++; month = 1; } else { month++; }
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            year = (int) yearCombo.getSelectedItem();
            month = (int) monthCombo.getSelectedItem();
            
            currentSelectedDate = null;
            previouslySelectedDayButton = null;
            updateDetailsPanel(null); 
            
            loadMonthData(); 
        }
    }
    
    // 테마 변경 로직
    private void toggleTheme() {
        try {
            if (isDarkMode) {
                // 라이트 모드로 변경
                UIManager.setLookAndFeel(new FlatLightLaf());
                themeToggleBtn.setText("🌙 다크 모드");
                isDarkMode = false;
            } else {
                // 다크 모드로 변경
                UIManager.setLookAndFeel(new FlatDarkLaf());
                themeToggleBtn.setText("☀️ 라이트 모드");
                isDarkMode = true;
            }
            // 전체 UI 새로고침 (화면 깜빡이며 적용됨)
            SwingUtilities.updateComponentTreeUI(this);
            // 다이얼로그나 팝업 등 다른 창들도 업데이트가 필요할 수 있음
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object obj = ae.getSource();
        String command = ae.getActionCommand();

        if (obj == prevBtn) {
            prevMonth(); setDayReset();
        } else if (obj == nextBtn) {
            nextMonth(); setDayReset();
        } else if (obj == analysisBtn) {
            new AnalysisView(this, currentUser, apiService, year, month);
        } else if (obj == calculatorBtn) {
            SwingUtilities.invokeLater(() -> new Calculator());
        } else if (obj == goalBtn) {
        	new GoalView(this, currentUser, apiService, year, month).setVisible(true);
        } else if (obj == themeToggleBtn) { 
            // 테마 변경 버튼 클릭 시
            toggleTheme();
        } else if (command != null && command.matches("\\d{4}-\\d{2}-\\d{2}")) {
            this.currentSelectedDate = command;
            JButton clickedButton = (JButton) obj;
            if (previouslySelectedDayButton != null) {
                previouslySelectedDayButton.setBackground(null);
                previouslySelectedDayButton.setOpaque(false);
            }
            clickedButton.setBackground(Color.YELLOW);
            clickedButton.setOpaque(true);
            previouslySelectedDayButton = clickedButton;
            updateDetailsPanel(command); 
        } else if (obj == openInputBtn) {
            if (currentSelectedDate == null) {
                JOptionPane.showMessageDialog(this, "먼저 캘린더에서 날짜를 선택하세요.");
                return;
            }
            new DailyInputView(this, currentSelectedDate, currentUser, apiService);
        } else if (obj == deleteButton) {
            deleteTransaction(); 
        }
    }

    public static void main(String[] args) {
        // 시작할 때는 라이트 모드로 시작 (사용자가 버튼으로 바꾸도록)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
        	
        }

        SwingUtilities.invokeLater(() -> {
            JFrame initialFrame = new JFrame();
            initialFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            initialFrame.setVisible(false);

            LoginView loginDialog = new LoginView(initialFrame); 
            User loggedInUser = loginDialog.showDialog();

            if (loggedInUser != null) {
                initialFrame.dispose();
                new CalendarSwing(loggedInUser);
            } else {
                initialFrame.dispose();
                System.exit(0);
            }
        });
    }
}