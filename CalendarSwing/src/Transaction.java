public class Transaction {
    
    private int id;        // 거래 고유 번호
    private int userId;    // 사용자 ID
    private String date;   // 날짜
    private String type;   // 수입/지출
    private double amount; // 금액
    private String category; // 카테고리
    private String content;  // 내용

    // 1. 기본 생성자 
    public Transaction() {
    }

    // 2. 데이터 생성자
    public Transaction(int userId, String date, String type, double amount, String category, String content) {
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }
    
    // 3. 전체 생성자
    public Transaction(int id, int userId, String date, String type, double amount, String category, String content) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.content = content;
    }


    // 화면에서 삭제 버튼이 찾는 메서드
    public int getTransactionId() { return id; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}