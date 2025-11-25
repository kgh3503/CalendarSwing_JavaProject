import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ComparisonView extends JDialog {

    // 생성자: AnalysisView(JDialog)에서도 부를 수 있도록 owner 타입을 Window로 설정
    public ComparisonView(Window owner, String title, Map<String, Double> myData, Map<String, Double> averageData) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        // 1. 데이터를 그릴 차트 패널 생성
        ComparisonChartPanel chartPanel = new ComparisonChartPanel(myData, averageData);
        
        // 2. 스크롤바 추가
        JScrollPane scrollPane = new JScrollPane(chartPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
        
        setVisible(true);
    }
}

// 차트 패널 클래스
class ComparisonChartPanel extends JPanel {
    
    private final Map<String, Double> myData;
    private final Map<String, Double> averageData;
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
    private final Font categoryFont = new Font("맑은 고딕", Font.BOLD, 12);
    private final Font amountFont = new Font("맑은 고딕", Font.PLAIN, 10);
    
    private final Set<String> allCategories = new TreeSet<>();
    private double maxAmount = 0;

    public ComparisonChartPanel(Map<String, Double> myData, Map<String, Double> averageData) {
        this.myData = myData;
        this.averageData = averageData;
        
        allCategories.addAll(myData.keySet());
        allCategories.addAll(averageData.keySet());

        for (String category : allCategories) {
            maxAmount = Math.max(maxAmount, myData.getOrDefault(category, 0.0));
            maxAmount = Math.max(maxAmount, averageData.getOrDefault(category, 0.0));
        }

        if (maxAmount == 0) maxAmount = 100000; 
        
        setBackground(Color.WHITE);
        int preferredHeight = 100 + (allCategories.size() * 60);
        setPreferredSize(new Dimension(750, preferredHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 60; 
        int labelWidth = 100; 
        int chartAreaWidth = getWidth() - labelWidth - padding; 
        
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(labelWidth, padding, labelWidth, getHeight() - padding);
        
        int categoryIndex = 0;
        
        // 범례
        g2d.setColor(Color.BLUE);
        g2d.fillRect(labelWidth, 10, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString("나의 지출", labelWidth + 20, 25);
        
        g2d.setColor(Color.GRAY);
        g2d.fillRect(labelWidth + 100, 10, 15, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString("사용자 평균", labelWidth + 120, 25);

        for (String category : allCategories) {
            int yPos = padding + (categoryIndex * 60); 
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(categoryFont);
            g2d.drawString(category, 10, yPos + 25); 

            double myValue = myData.getOrDefault(category, 0.0);
            int myBarWidth = (int) ((myValue / maxAmount) * chartAreaWidth);
            g2d.setColor(Color.BLUE);
            g2d.fillRect(labelWidth, yPos, myBarWidth, 20); 
            g2d.setColor(Color.BLACK);
            g2d.setFont(amountFont);
            g2d.drawString(nf.format(myValue) + "원", labelWidth + myBarWidth + 5, yPos + 15);

            double avgValue = averageData.getOrDefault(category, 0.0);
            int avgBarWidth = (int) ((avgValue / maxAmount) * chartAreaWidth);
            g2d.setColor(Color.GRAY);
            g2d.fillRect(labelWidth, yPos + 25, avgBarWidth, 20); 
            g2d.setColor(Color.BLACK);
            g2d.setFont(amountFont);
            g2d.drawString(nf.format(avgValue) + "원", labelWidth + avgBarWidth + 5, yPos + 40);

            categoryIndex++;
        }
    }
}