import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;
import java.util.Locale;
import java.text.NumberFormat;

/**
 * 1년치 수입/지출 추이를 보여주는 선 그래프 패널
 * (50만원 단위 Y축 눈금 추가)
 */
public class YearlyLineChartPanel extends JPanel {
    private final String title;
    private final Map<String, double[]> data; // "수입" / "지출" 키로 12개월치 배열
    private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);

    public YearlyLineChartPanel(String title, Map<String, double[]> data) {
        this.title = title;
        this.data = data;
        setBorder(BorderFactory.createTitledBorder(title));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double[] incomes = data.getOrDefault("수입", new double[12]);
        double[] expenses = data.getOrDefault("지출", new double[12]);

        // 1. 최대값 찾기 (Y축 스케일용)
        double maxAmount = 0;
        for (int i = 0; i < 12; i++) {
            maxAmount = Math.max(maxAmount, incomes[i]);
            maxAmount = Math.max(maxAmount, expenses[i]);
        }

        // 최대값을 50만의 배수로 올림 (그래프가 꽉 차게)
        if (maxAmount > 0) {
            maxAmount = Math.ceil(maxAmount / 500000) * 500000;
        }

        if (maxAmount <= 0) {
            g.drawString("연간 데이터가 없습니다.", getWidth() / 2 - 50, getHeight() / 2);
            return;
        }

        // 차트 영역 및 패딩 설정
        int padding = 50;
        int chartWidth = getWidth() - 2 * padding;
        int chartHeight = getHeight() - 2 * padding;
        int x0 = padding;
        int y0 = padding + chartHeight; // Y=0 (바닥) 지점

        // X축 (월) 그리기
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(x0, y0, x0 + chartWidth, y0); // X축

        // X축 레이블 (1월 ~ 12월) 그리기
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < 12; i++) {
            int x = x0 + (i * chartWidth) / 11; // 11개 구간
            g2d.drawString((i + 1) + "월", x - 10, y0 + 20);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(x, y0, x, padding); // 월별 세로선
            g2d.setColor(Color.BLACK);
        }

        // Y축 레이블 및 눈금선 그리기 (50만원 단위)
        g2d.setColor(Color.BLACK);
        g2d.drawString("0", x0 - padding + 5, y0); // 0원
        
        double tickAmount = 500000; // 50만원
        
        for (double currentTick = tickAmount; currentTick <= maxAmount; currentTick += tickAmount) {
            int y = y0 - (int) ((currentTick / maxAmount) * chartHeight);
            
            // Y축 레이블 (500,000, 1,000,000 ...)
            g2d.setColor(Color.BLACK);
            // y좌표에 +5를 하여 선에 겹치지 않게 살짝 아래로 내림
            g2d.drawString(nf.format(currentTick), x0 - padding + 5, y + 5); 

            // Y축 눈금선 (가로선)
            g2d.setColor(new Color(230, 230, 230)); // 기존 LIGHT_GRAY보다 연하게
            g2d.drawLine(x0, y, x0 + chartWidth, y);
        }
        
        // Y축 (세로선) - 맨 위에 그리도록 순서 변경 (눈금선이 덮어쓰도록)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(x0, y0, x0, padding);

        // 데이터 포인트 그리기 (수입 - 파란색)
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2)); // 선 굵기
        int[] incomeXPoints = new int[12];
        int[] incomeYPoints = new int[12];
        for (int i = 0; i < 12; i++) {
            incomeXPoints[i] = x0 + (i * chartWidth) / 11;
            incomeYPoints[i] = y0 - (int) ((incomes[i] / maxAmount) * chartHeight);
        }
        g2d.drawPolyline(incomeXPoints, incomeYPoints, 12);

        // 데이터 포인트 그리기 (지출 - 빨간색)
        g2d.setColor(Color.RED);
        int[] expenseXPoints = new int[12];
        int[] expenseYPoints = new int[12];
        for (int i = 0; i < 12; i++) {
            expenseXPoints[i] = x0 + (i * chartWidth) / 11;
            expenseYPoints[i] = y0 - (int) ((expenses[i] / maxAmount) * chartHeight);
        }
        g2d.drawPolyline(expenseXPoints, expenseYPoints, 12);
        
        // 범례
        g2d.setColor(Color.BLUE);
        g2d.fillRect(getWidth() - padding - 60, padding, 10, 10);
        g2d.drawString("수입", getWidth() - padding - 45, padding + 10);
        g2d.setColor(Color.RED);
        g2d.fillRect(getWidth() - padding - 60, padding + 20, 10, 10);
        g2d.drawString("지출", getWidth() - padding - 45, padding + 30);
    }
}