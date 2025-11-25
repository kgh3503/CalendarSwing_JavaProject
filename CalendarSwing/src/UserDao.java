//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//public class UserDao {
//
//    /**
//     * 사용자 아이디와 비밀번호를 DB에 저장 (회원가입)
//     */
//    public boolean registerUser(String username, String password) {
//        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
//        
//        // DB와 로그인 로직을 일치시키기 위한 임시 해시 
//        String passwordHash = password + "_hashed"; 
//
//        try (Connection conn = DatabaseManager.connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            
//            pstmt.setString(1, username);
//            pstmt.setString(2, passwordHash);
//            pstmt.executeUpdate();
//            return true;
//            
//        } catch (SQLException e) {
//            if (e.getSQLState().equals("23000")) {
//                 System.err.println("DB 회원가입 오류: 아이디 '" + username + "'이(가) 이미 존재합니다.");
//            } else {
//                 System.err.println("DB 회원가입 오류: " + e.getMessage());
//            }
//            return false;
//        }
//    }
//
//    /**
//     * 사용자 인증을 시도
//     */
//    public User authenticate(String username, String password) {
//        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
//        
//        try (Connection conn = DatabaseManager.connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            
//            pstmt.setString(1, username);
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//                    String storedHash = rs.getString("password_hash");
//                    
//                    // 입력 비밀번호를 해시 처리하여 DB 값과 비교합니다.
//                    String inputHash = password + "_hashed";
//
//                    if (storedHash.equals(inputHash)) { 
//                        return new User(rs.getInt("id"), rs.getString("username"), storedHash);
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            System.err.println("DB 인증 오류: " + e.getMessage());
//        }
//        return null; 
//    }
//}