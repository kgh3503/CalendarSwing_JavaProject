// 클라이언트용 DTO
public class RecurringTransaction {
    private int id;
    private int userId;
    private String type;
    private double amount;
    private String category;
    private String content;
    private int dayOfMonth;

    public RecurringTransaction(int userId, String type, double amount, String category, String content, int dayOfMonth) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
        this.dayOfMonth = dayOfMonth;
    }

    // Getter/Setter 추가 (기존 코드 유지하거나 새로 작성)
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getContent() { return content; }
    public int getDayOfMonth() { return dayOfMonth; }
}