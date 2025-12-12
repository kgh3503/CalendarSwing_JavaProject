package com.example.finance_server.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.example.finance_server.client.dto.Transaction;
import com.example.finance_server.client.dto.User;
import com.example.finance_server.util.ApiService;
import com.example.finance_server.util.ExcelExporter;

public class AnalysisView extends JDialog implements ActionListener { 

    private final User currentUser;
    private final int year;
    private final int month;
    
    private final ApiService apiService; 
    
    private JButton exportExcelBtn; 
    private JButton compareMonthBtn; 
    
    // 버튼 변수 선언
    private JButton compareUsersBtn;

    public AnalysisView(JFrame owner, User user, ApiService apiService, int year, int month) {
        super(owner, String.format("%d년 분석", year), true); 
        this.currentUser = user;
        this.apiService = apiService; 
        this.year = year;
        this.month = month;

        setSize(800, 550); 
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);
        
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel monthlyTabPanel = createMonthlyTabPanel();
        tabbedPane.addTab(String.format("%d월 분석", month), monthlyTabPanel);

        JPanel yearlyTabPanel = createYearlyTabPanel();
        tabbedPane.addTab(String.format("%d년 전체 분석", year), yearlyTabPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 하단 버튼 패널
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        compareMonthBtn = new JButton("지난 달과 비교"); 
        compareMonthBtn.addActionListener(this);
        
        // 버튼 생성
        compareUsersBtn = new JButton("사용자 평균과 비교"); 
        compareUsersBtn.addActionListener(this);
        
        exportExcelBtn = new JButton("월별 내역 Excel로 출력");
        exportExcelBtn.addActionListener(this);
        
        southPanel.add(compareMonthBtn);
        southPanel.add(compareUsersBtn); 
        southPanel.add(exportExcelBtn);
        
        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
    
    // 버튼 눌렀을 때 비교창 띄우기
    private void handleCompareUsers() {
        String comparisonType = "지출"; 
        
        try {
            Map<String, Double> myData = apiService.getCategorySummary(
                currentUser.getUserId(), year, month, comparisonType
            );

            // [수정됨] userId 인수 제거 (전체 사용자 평균 요청)
            Map<String, Double> averageData = apiService.getAverageCategorySummary(
                year, month, comparisonType
            );
            
            // ComparisonView 창 열기
            new ComparisonView(this, "사용자 지출 비교", myData, averageData);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "사용자 비교 데이터를 서버에서 불러오는 데 실패했습니다: " + e.getMessage(), 
                "서버 연결 오류", JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel createMonthlyTabPanel() {
        Map<String, Double> monthlySummary = new HashMap<>();
        Map<String, Double> expenseCategorySummary = new HashMap<>();

        try {
            monthlySummary = apiService.getMonthlySummary(currentUser.getUserId(), year, month);
            expenseCategorySummary = apiService.getCategorySummary(currentUser.getUserId(), year, month, "지출");
        } catch (Exception e) {
            e.printStackTrace();
            monthlySummary.put("수입", 0.0);
            monthlySummary.put("지출", 0.0);
        }
        
        JPanel summaryPanel = createSummaryPanel(monthlySummary);
        
        JSplitPane monthlyChartPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new PieChartPanel("지출 카테고리 분석 (원그래프)", expenseCategorySummary, monthlySummary.getOrDefault("지출", 0.0)),
            new BarChartPanel("수입/지출 비교 (막대 차트)", monthlySummary)
        );
        monthlyChartPane.setDividerLocation(480);
        monthlyChartPane.setOneTouchExpandable(true);
        monthlyChartPane.setResizeWeight(0.6); 

        JPanel monthlyTabPanel = new JPanel(new BorderLayout());
        monthlyTabPanel.add(summaryPanel, BorderLayout.NORTH);
        monthlyTabPanel.add(monthlyChartPane, BorderLayout.CENTER);
        
        return monthlyTabPanel;
    }
    
    private JPanel createYearlyTabPanel() {
        Map<String, double[]> yearlyData = new HashMap<>();
        
        try {
            yearlyData = apiService.getYearlySummary(currentUser.getUserId(), year);
        } catch (Exception e) {
            e.printStackTrace();
            yearlyData.put("수입", new double[12]);
            yearlyData.put("지출", new double[12]);
        }
        
        YearlyLineChartPanel yearlyChartPanel = new YearlyLineChartPanel(
            String.format("%d년 수입/지출 추이 (선 그래프)", year), yearlyData
        );
        
        return yearlyChartPanel;
    }

    private JPanel createSummaryPanel(Map<String, Double> summary) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        panel.setBackground(new Color(240, 240, 255));
        panel.setPreferredSize(new Dimension(800, 40)); 

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        double income = summary.getOrDefault("수입", 0.0);
        double expense = summary.getOrDefault("지출", 0.0);
        double net = income - expense;
        
        JLabel incomeLabel = new JLabel("총 수입: " + nf.format(income) + "원");
        JLabel expenseLabel = new JLabel("총 지출: " + nf.format(expense) + "원");
        JLabel netLabel = new JLabel("순자산: " + nf.format(net) + "원");

        incomeLabel.setForeground(Color.BLUE);
        expenseLabel.setForeground(Color.RED);
        netLabel.setForeground(net >= 0 ? new Color(0, 150, 0) : Color.RED);
        
        panel.add(incomeLabel);
        panel.add(expenseLabel);
        panel.add(netLabel);
        
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exportExcelBtn) {
            handleExportExcel();
        } else if (e.getSource() == compareMonthBtn) {
            handleCompareMonth();
        } else if (e.getSource() == compareUsersBtn) { // 버튼 이벤트 연결
            handleCompareUsers();
        }
    }
    
    private void handleCompareMonth() {
        Map<String, Double> currentSummary = new HashMap<>();
        Map<String, Double> prevSummary = new HashMap<>();
        int prevYear = year;
        int prevMonth = month - 1;

        try {
            currentSummary = apiService.getMonthlySummary(currentUser.getUserId(), year, month);

            if (prevMonth == 0) {
                prevMonth = 12;
                prevYear--;
            }
            prevSummary = apiService.getMonthlySummary(currentUser.getUserId(), prevYear, prevMonth);
        } catch (Exception e) {
            e.printStackTrace();
            return; 
        }

        double currentIncome = currentSummary.getOrDefault("수입", 0.0);
        double currentExpense = currentSummary.getOrDefault("지출", 0.0);
        double prevIncome = prevSummary.getOrDefault("수입", 0.0);
        double prevExpense = prevSummary.getOrDefault("지출", 0.0);

        String message = String.format(
            "<html><h3>%d년 %d월 (지난달) vs %d년 %d월 (이번달)</h3>" +
            "<hr>" +
            "<b>총 수입:</b><br>" +
            " - 이번 달: %,.0f 원<br>" +
            " - 지난 달: %,.0f 원<br>" +
            " - 차이: <font color='%s'>%,.0f 원</font><br>" +
            "<hr>" +
            "<b>총 지출:</b><br>" +
            " - 이번 달: %,.0f 원<br>" +
            " - 지난 달: %,.0f 원<br>" +
            " - 차이: <font color='%s'>%,.0f 원</font><br>" +
            "<hr>" +
            "<b>순자산 (수입-지출):</b><br>" +
            " - 이번 달: %,.0f 원<br>" +
            " - 지난 달: %,.0f 원</html>",
            prevYear, prevMonth, year, month,
            currentIncome, prevIncome,
            (currentIncome >= prevIncome ? "blue" : "red"), (currentIncome - prevIncome),
            currentExpense, prevExpense,
            (currentExpense <= prevExpense ? "blue" : "red"), (currentExpense - prevExpense), 
            (currentIncome - currentExpense),
            (prevIncome - prevExpense)
        );

        JOptionPane.showMessageDialog(this, message, "월별 비교", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void handleExportExcel() {
        List<Transaction> transactions = new ArrayList<>();
        
        try {
            transactions = apiService.getMonthlyTransactions(
                currentUser.getUserId(), this.year, this.month
            );
        } catch (Exception e) {
             e.printStackTrace();
            return; 
        }
        
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "출력할 거래 내역이 없습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(String.format("%d년 %d월 내역 저장", year, month));
        fileChooser.setSelectedFile(new File(String.format("가계부_%d년_%d월_내역.xlsx", year, month)));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            ExcelExporter exporter = new ExcelExporter();
            if (exporter.exportMonth(transactions, this.year, this.month, filePath)) {
                JOptionPane.showMessageDialog(this, "Excel 파일이 성공적으로 저장되었습니다.", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}

class PieChartPanel extends JPanel {
    private final String title;
    private final Map<String, Double> data;
    private final double total;
    private final String[] colors = {"#FF6347", "#4682B4", "#3CB371", "#FFD700", "#9370DB", "#FFA07A", "#6A5ACD", "#8FBC8F"};

    public PieChartPanel(String title, Map<String, Double> data, double total) {
        this.title = title;
        this.data = data;
        this.total = total;
        setBorder(BorderFactory.createTitledBorder(title)); 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (total <= 0 || data.isEmpty()) {
            g.drawString("지출 내역이 없습니다.", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        int legendWidth = (int)(getWidth() * 0.45); 
        int chartAreaWidth = getWidth() - legendWidth;
        int size = Math.min(chartAreaWidth, getHeight()) - 40; 
        int x = (chartAreaWidth - size) / 2;
        int y = getHeight() / 2 - size / 2;

        double currentAngle = 0;
        int colorIndex = 0;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        int legendX = chartAreaWidth + 10;
        int legendY = 30; 

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            double value = entry.getValue();
            double percent = value / total;
            int angle = (int) Math.round(percent * 360);

            Color color = Color.decode(colors[colorIndex % colors.length]);
            g2d.setColor(color);
            g2d.fillArc(x, y, size, size, (int) currentAngle, angle);
            
            int currentLegendY = legendY + colorIndex * 20; 
            
            g2d.fillRect(legendX, currentLegendY, 10, 10);
            g2d.setColor(Color.BLACK);
            
            g2d.drawString(entry.getKey() + ": " + nf.format(value) + String.format("원 (%.1f%%)", percent * 100), legendX + 15, currentLegendY + 10);

            currentAngle += angle;
            colorIndex++;
        }
    }
}

class BarChartPanel extends JPanel {
    private final String title;
    private final Map<String, Double> data;

    public BarChartPanel(String title, Map<String, Double> data) {
        this.title = title;
        this.data = data;
        setBorder(BorderFactory.createTitledBorder(title));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int padding = 60; 
        int barWidth = 40;
        int chartHeight = getHeight() - 2 * padding;
        int chartWidth = getWidth() - 2 * padding;

        double income = data.getOrDefault("수입", 0.0);
        double expense = data.getOrDefault("지출", 0.0);
        double max = Math.max(income, expense);

        if (max <= 0) {
            g.drawString("내역이 없습니다.", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }
        
        g2d.drawLine(padding, padding + chartHeight, padding + chartWidth, padding + chartHeight);
        
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        
        // 수입 막대
        double incomeRatio = (income / max);
        int incomeBarHeight = (int) (incomeRatio * chartHeight);
        g2d.setColor(Color.BLUE);
        int incomeX = padding + chartWidth / 4 - barWidth / 2;
        g2d.fillRect(incomeX, padding + chartHeight - incomeBarHeight, barWidth, incomeBarHeight);
        g2d.setColor(Color.BLACK);
        
        String incomeStr = nf.format(income);
        g2d.drawString(incomeStr, incomeX + barWidth/2 - g2d.getFontMetrics().stringWidth(incomeStr)/2, padding + chartHeight - incomeBarHeight - 5);
        g2d.drawString("수입", incomeX + barWidth/2 - g2d.getFontMetrics().stringWidth("수입")/2, padding + chartHeight + 15);

        // 지출 막대
        double expenseRatio = (expense / max);
        int expenseBarHeight = (int) (expenseRatio * chartHeight);
        g2d.setColor(Color.RED);
        int expenseX = padding + chartWidth * 3 / 4 - barWidth / 2;
        g2d.fillRect(expenseX, padding + chartHeight - expenseBarHeight, barWidth, expenseBarHeight);
        g2d.setColor(Color.BLACK);
        
        String expenseStr = nf.format(expense);
        g2d.drawString(expenseStr, expenseX + barWidth/2 - g2d.getFontMetrics().stringWidth(expenseStr)/2, padding + chartHeight - expenseBarHeight - 5);
        g2d.drawString("지출", expenseX + barWidth/2 - g2d.getFontMetrics().stringWidth("지출")/2, padding + chartHeight + 15);
    }
}