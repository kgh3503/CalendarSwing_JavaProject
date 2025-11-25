//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//public class DatabaseManager {
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/FinanceAppDB?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true"; 
//    private static final String DB_USER = "root";          
//    private static final String DB_PASSWORD = "han0226";
//    
//    /**
//     * MySQL DB 연결을 가져옴
//     */
//    public static Connection connect() {
//        Connection conn = null;
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver"); 
//            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//        } catch (ClassNotFoundException e) {
//            System.err.println("MySQL 드라이버(Connector/J)를 찾을 수 없습니다. JAR 파일을 추가했는지 확인하세요.");
//            e.printStackTrace();
//        } catch (SQLException e) {
//            System.err.println("DB 연결 오류: URL, ID, 비밀번호를 확인하세요: " + e.getMessage());
//        }
//        return conn;
//    }
//
//    /**
//     * User 및 Transaction 테이블을 생성 (LoginView에서 호출됨)
//     */
//    public static void createTables() {
//        String sqlUser = "CREATE TABLE IF NOT EXISTS users (\n"
//                + " id INT AUTO_INCREMENT PRIMARY KEY,\n"
//                + " username VARCHAR(50) NOT NULL UNIQUE,\n"
//                + " password_hash VARCHAR(255) NOT NULL,\n"
//                + " created_at DATETIME DEFAULT CURRENT_TIMESTAMP\n"
//                + ");";
//
//        // transactions 테이블 생성 SQL 추가
//        String sqlTransaction = "CREATE TABLE IF NOT EXISTS transactions (\n"
//                + " id INT AUTO_INCREMENT PRIMARY KEY,\n"
//                + " user_id INT NOT NULL,\n"
//                + " date DATE NOT NULL, \n"
//                + " type VARCHAR(10) NOT NULL, \n"
//                + " amount DOUBLE NOT NULL,\n"
//                + " category VARCHAR(50),\n"
//                + " content VARCHAR(255),\n"
//                + " FOREIGN KEY (user_id) REFERENCES users(id)\n"
//                + ");";
//        
//        // Goal 테이블 생성 SQL 추가
//        String sqlGoal = "CREATE TABLE IF NOT EXISTS goals (\n"
//                + " id INT AUTO_INCREMENT PRIMARY KEY,\n"
//                + " user_id INT NOT NULL,\n"
//                + " type VARCHAR(10) NOT NULL,\n"       // 수입 또는 지출
//                + " category VARCHAR(50),\n"            // 카테고리 (NULL이면 전체)
//                + " target_amount DOUBLE NOT NULL,\n"   // 목표 금액
//                + " year INT NOT NULL,\n"               // 목표 연도
//                + " month INT NOT NULL,\n"              // 목표 월
//                + " FOREIGN KEY (user_id) REFERENCES users(id)\n"
//                + ", UNIQUE KEY unique_goal (user_id, year, month, type, category)" // 중복 목표 방지
//                + ");";
//
//        try (Connection conn = connect();
//             Statement stmt = conn.createStatement()) {
//            
//            if (conn != null) {
//                stmt.execute(sqlUser);
//                stmt.execute(sqlTransaction); // 테이블 생성 실행
//                stmt.execute(sqlGoal);	//목표 테이블 생성 실행
//            }ㄴ
//
//        } catch (SQLException e) {
//            System.err.println("테이블 생성 중 오류 발생: " + e.getMessage());
//        }
//    }
//}