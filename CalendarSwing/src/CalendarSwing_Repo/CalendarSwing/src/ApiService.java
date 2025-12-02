import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Map, List 등을 변환하기 위해 필요
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration; // 타임아웃 설정을 위해 추가
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ApiService {

    // 켜져 있는 '서버'의 주소 (http://localhost:8080/api)
    private static final String SERVER_URL = "http://192.168.1.100:8080/api";

    // HTTP 통신을 위한 클라이언트 (타임아웃 10초 설정 추가)
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    // JSON 번역기 
    private final Gson gson = new Gson();

    /**
     * [로그인]
     */
    public User login(String username, String password) {
        try {
            Map<String, String> loginRequest = Map.of("username", username, "password", password);
            String jsonBody = gson.toJson(loginRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/users/login")) 
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), User.class); 
            } else {
                return null; // 로그인 실패
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 서버 오류
        }
    }

    /**
     * [회원가입]
     */
    public User register(String username, String password) {
        try {
            Map<String, String> registerRequest = Map.of("username", username, "password", password);
            String jsonBody = gson.toJson(registerRequest);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/users/register")) 
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), User.class);
            } else {
                return null; 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * [가계부 추가] 
     * DailyInputView에서 호출됨
     */
    public Transaction addTransaction(Transaction transaction) {
        try {
            String jsonBody = gson.toJson(transaction);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/transactions")) 
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return gson.fromJson(response.body(), Transaction.class);
            } else {
                System.out.println("저장 실패 코드: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * [가계부 삭제]
     * CalendarSwing에서 호출됨
     */
    public boolean deleteTransaction(int transactionId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/transactions/" + transactionId)) 
                .DELETE()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return (response.statusCode() == 204 || response.statusCode() == 200);
    }

    /**
     * [일별 조회]
     * CalendarSwing 상세 패널용
     */
    public List<Transaction> getDailyTransactions(int userId, String date) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/transactions/user/" + userId + "/date/" + date))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(response.body(), new TypeToken<List<Transaction>>(){}.getType());
    }

    /**
     * [월별 조회]
     * CalendarSwing 달력 표시용
     */
    public List<Transaction> getMonthlyTransactions(int userId, int year, int month) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/transactions/user/" + userId + "/month/" + year + "/" + month))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return gson.fromJson(response.body(), new TypeToken<List<Transaction>>(){}.getType());
    }

    /**
     * [월별 요약 (총수입/총지출)]
     * AnalysisView용
     */
    public Map<String, Double> getMonthlySummary(int userId, int year, int month) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/transactions/summary/monthly/" + userId + "/" + year + "/" + month))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return gson.fromJson(response.body(), new TypeToken<Map<String, Double>>(){}.getType());
    }
    
    /**
     * [카테고리별 요약]
     * AnalysisView용
     */
    public Map<String, Double> getCategorySummary(int userId, int year, int month, String type) throws Exception {
        String url = SERVER_URL + "/transactions/summary/category/" + userId + "/" + year + "/" + month + "?type=" + type;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url)) 
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return gson.fromJson(response.body(), new TypeToken<Map<String, Double>>(){}.getType());
    }
    
    /**
     * [연간 요약]
     * AnalysisView용
     */
    public Map<String, double[]> getYearlySummary(int userId, int year) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/transactions/summary/yearly/" + userId + "/" + year))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return gson.fromJson(response.body(), new TypeToken<Map<String, double[]>>(){}.getType());
    }

    /**
     * [월별 목표 조회]
     * GoalView용
     */
    public List<GoalProgressDTO> getMonthlyGoals(int userId, int year, int month) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/goals/user/" + userId + "/month/" + year + "/" + month))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return gson.fromJson(response.body(), new TypeToken<List<GoalProgressDTO>>(){}.getType());
    }

    /**
     * [목표 추가]
     * GoalView용
     */
    public Goal addGoal(Goal goal) throws Exception {
        String jsonBody = gson.toJson(goal);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/goals")) 
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(jsonBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return gson.fromJson(response.body(), Goal.class);
        } else {
            throw new RuntimeException("목표 저장 실패: " + response.statusCode());
        }
    }

    /**
     * [다른 사용자와의 비교 데이터 조회]
     * ComparisonView용
     */
    public Map<String, Double> getAverageCategorySummary(int userId, int year, int month, String type) throws Exception {
        String url = SERVER_URL + "/transactions/summary/average/" + userId + "/" + year + "/" + month + "?type=" + type;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return gson.fromJson(response.body(), new TypeToken<Map<String, Double>>(){}.getType());
    }
    
    /**
     * [정기 거래 목록 조회]
     */
    public List<RecurringTransaction> getRecurringTransactions(int userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/recurring/user/" + userId))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return gson.fromJson(response.body(), new TypeToken<List<RecurringTransaction>>(){}.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(); // 실패 시 빈 리스트
    }
    
    /**
     * [정기 거래 추가]
     */
    public boolean addRecurringTransaction(RecurringTransaction rt) {
        try {
            String jsonBody = gson.toJson(rt);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/recurring"))
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * [정기 거래 삭제]
     */
    public boolean deleteRecurringTransaction(int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/recurring/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * [정기 거래 반영 요청]
     * 서버에 "이번 달 정기 거래를 생성해달라"고 요청함
     */
    public void applyRecurringTransactions(int userId, int year, int month) {
        try {
            String url = String.format(SERVER_URL + "/recurring/apply?userId=%d&year=%d&month=%d", userId, year, month);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(BodyPublishers.noBody())
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("정기 거래 반영 중 오류 발생");
        }
    }
}

