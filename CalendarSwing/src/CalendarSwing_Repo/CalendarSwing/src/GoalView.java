import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList; 
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;     
import java.util.HashMap;  


public class GoalView extends JDialog implements ActionListener {

    private final User currentUser;
    
    // GoalManager -> ApiService
    // private final GoalManager goalManager; 
    private final ApiService apiService; 
    
    private final int currentYear;
    private final int currentMonth;
    private final CalendarSwing parent;

    // UI 컴포넌트
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JTextField amountField;
    private JButton saveBtn;
    private JPanel listPanel; // 목표 목록을 표시할 패널

    // GoalManager에 있던 카테고리/진행률 계산 로직이 이쪽으로 와야 함
    private final Map<String, String[]> categories = new HashMap<>();

    private void initCategories() {
        categories.put("지출", new String[]{"식비", "교통", "생활/쇼핑", "문화/여가", "건강/의료", "경조사/모임", "교육/자기개발", "기타"});
        categories.put("수입", new String[]{"근로 소득", "부가 소득", "금융 소득", "기타 소득"});
    }

    private void updateCategoryCombo() {
        String selectedType = (String) typeCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        
        categoryCombo.addItem("전체"); 
        
        if (selectedType != null) {
            String[] cats = categories.get(selectedType);
            if (cats != null) {
                for (String cat : cats) {
                    categoryCombo.addItem(cat);
                }
            }
        }
    }

    public GoalView(CalendarSwing owner, User user, ApiService apiService, int year, int month) {
        super(owner, String.format("%d년 %d월 목표 관리", year, month), true);
        this.parent = owner;
        this.currentUser = user;
        this.apiService = apiService; 
        // this.goalManager = manager; 
        this.currentYear = year;
        this.currentMonth = month;
        
        initCategories(); // 카테고리 맵 초기화

        setSize(650, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        
        // 1. 목표 설정 입력부 
        add(createInputPanel(), BorderLayout.NORTH);
        
        // 2. 목표 목록 및 현황 표시부 
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(BorderFactory.createTitledBorder("목표 달성 현황"));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        add(scrollPane, BorderLayout.CENTER);

        loadGoalData(); // loadGoalData() 호출
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("신규 목표 설정"));
        
        typeCombo = new JComboBox<>(new String[]{"지출", "수입"});
        
        categoryCombo = new JComboBox<>(); 
        
        amountField = new JTextField(10);
        saveBtn = new JButton("목표 저장");
        saveBtn.addActionListener(this);

        typeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCategoryCombo(); 
            }
        });
        updateCategoryCombo(); // 초기 카테고리 목록 설정
     
        inputPanel.add(new JLabel("유형:"));
        inputPanel.add(typeCombo);
        inputPanel.add(new JLabel("카테고리:"));
        inputPanel.add(categoryCombo);
        inputPanel.add(new JLabel("금액:"));
        inputPanel.add(amountField);
        inputPanel.add(saveBtn);
        
        return inputPanel;
    }

    /**
     * DB -> 서버 API 호출로 변경
     * 1. List<Goal> -> List<GoalProgressDTO>로 변경
     * 2. progress = 0.0 / rate = 0.0 삭제
     * 3. DTO에서 실제 서버 계산값을 가져오도록 변경
     */
    private void loadGoalData() {
        listPanel.removeAll(); 
        
        //  Goal -> GoalProgressDTO
        List<GoalProgressDTO> progressDTOs = new ArrayList<>();
        
        try {
            // ApiService 호출 (반환 타입이 DTO 리스트임)
            progressDTOs = apiService.getMonthlyGoals(currentUser.getUserId(), currentYear, currentMonth);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "목표 데이터를 서버에서 불러오는 데 실패했습니다: " + e.getMessage(), 
                "서버 연결 오류", JOptionPane.ERROR_MESSAGE);
        }

        if (progressDTOs.isEmpty()) {
            listPanel.add(new JLabel("현재 설정된 목표가 없습니다."));
        } else {
            // DTO 리스트를 순회
            for (GoalProgressDTO dto : progressDTOs) {
                
                // 서버가 계산한 실제 값을 사용!
                Goal goal = dto.getGoal(); // 원본 목표
                double progress = dto.getCurrentProgress(); // (예: 400,000)
                double rate = dto.getAchievementRate();     // (예: 133.3)
                
                listPanel.add(createGoalProgressComponent(goal, progress, rate));
            }
        }
        
        listPanel.revalidate();
        listPanel.repaint();
    }
    

    private JPanel createGoalProgressComponent(Goal goal, double progress, double rate) {
        JPanel goalPane = new JPanel(new BorderLayout());
        goalPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        String catInfo = (goal.getCategory() == null) ? "전체" : goal.getCategory();
        JLabel infoLabel = new JLabel(String.format(" %s - %s 목표: %,.0f원", goal.getType(), catInfo, goal.getTargetAmount()));
        goalPane.add(infoLabel, BorderLayout.NORTH);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        
        int percentage = (int) Math.min(100.0, rate); 
        
        progressBar.setValue(percentage);
        progressBar.setStringPainted(true);
        // 진행률이 (400,000 / 300,000 (133.3%))로 표시됨
        progressBar.setString(String.format("진행: %,.0f원 / 목표: %,.0f원 (%.1f%%)", progress, goal.getTargetAmount(), rate));

        if (goal.getType().equals("지출") && rate > 100.0) {
            progressBar.setForeground(Color.RED);
        } else if (goal.getType().equals("지출")) {
            progressBar.setForeground(Color.ORANGE); 
        } else if (goal.getType().equals("수입") && rate >= 100.0) {
            progressBar.setForeground(Color.BLUE); 
        } else if (goal.getType().equals("수입")) {
             progressBar.setForeground(new Color(0, 150, 255)); 
        }
        
        goalPane.add(progressBar, BorderLayout.CENTER);
        return goalPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveBtn) {
            handleSaveGoal();
        }
    }
    
    private void handleSaveGoal() {
        String type = (String) typeCombo.getSelectedItem();
        String category = (String) categoryCombo.getSelectedItem();
        String amountText = amountField.getText();

        if (amountText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "목표 금액을 입력해주세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText.replaceAll(",", ""));
            
            String finalCategory = (category == null || "전체".equals(category) || category.trim().isEmpty()) ? null : category;
            
            // Goal.java 수정에 맞게 생성자 변경 (ID가 없는 생성자 사용)
            Goal newGoal = new Goal(
                currentUser.getUserId(),
                type,
                finalCategory,
                amount,
                currentYear,
                currentMonth
            );
            
            // ApiService 호출
            Goal savedGoal = apiService.addGoal(newGoal);

            if (savedGoal != null) {
                JOptionPane.showMessageDialog(this, "목표가 성공적으로 저장되었습니다!", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
                loadGoalData(); // loadGoalData() 호출 (목록 새로고침)
            } else {
                // (apiService.addGoal이 실패/null을 반환한 경우)
                JOptionPane.showMessageDialog(this, "목표 저장 실패: 동일한 목표가 이미 존재하거나 서버 오류입니다.", "저장 실패", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "금액은 유효한 숫자 형식이어야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { // apiService가 던진 Exception 처리
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 저장 중 오류 발생: " + e.getMessage(), "서버 오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}