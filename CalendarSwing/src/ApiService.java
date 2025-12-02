import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; 
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration; 
import java.util.ArrayList; 
import java.util.List;
import java.util.Map;

public class ApiService {

    private static final String SERVER_URL = "http://localhost:8080/api"; 

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private final Gson gson = new Gson();

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteTransaction(int transactionId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/transactions/" + transactionId)) 
                    .DELETE()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return (response.statusCode() == 204 || response.statusCode() == 200);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getDailyTransactions(int userId, String date) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/transactions/user/" + userId + "/date/" + date))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<Transaction>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Transaction> getMonthlyTransactions(int userId, int year, int month) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/transactions/user/" + userId + "/month/" + year + "/" + month))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<Transaction>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<String, Double> getMonthlySummary(int userId, int year, int month) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/transactions/summary/monthly/" + userId + "/" + year + "/" + month))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<Map<String, Double>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("수입", 0.0, "지출", 0.0);
        }
    }
    
    public Map<String, Double> getCategorySummary(int userId, int year, int month, String type) {
        try {
            String url = SERVER_URL + "/transactions/summary/category/" + userId + "/" + year + "/" + month + "?type=" + type;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<Map<String, Double>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }
    
    public Map<String, double[]> getYearlySummary(int userId, int year) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/transactions/summary/yearly/" + userId + "/" + year))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<Map<String, double[]>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    public List<GoalProgressDTO> getMonthlyGoals(int userId, int year, int month) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/goals/user/" + userId + "/month/" + year + "/" + month))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<List<GoalProgressDTO>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Goal addGoal(Goal goal) {
        try {
            String jsonBody = gson.toJson(goal);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL + "/goals")) 
                    .header("Content-Type", "application/json")
                    .POST(BodyPublishers.ofString(jsonBody))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return gson.fromJson(response.body(), Goal.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Double> getAverageCategorySummary(int userId, int year, int month, String type) {
        try {
            String url = SERVER_URL + "/transactions/summary/average/" + userId + "/" + year + "/" + month + "?type=" + type;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), new TypeToken<Map<String, Double>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }
    
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
        return new ArrayList<>(); 
    }
    
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

    // ▼▼▼ [완전 구현] 시작일/종료일 6개 파라미터 전송 ▼▼▼
    public boolean deleteRecurringTransaction(int id, int sYear, int sMonth, int sDay, int eYear, int eMonth, int eDay) {
        try {
            String url = String.format(SERVER_URL + "/recurring/%d?sYear=%d&sMonth=%d&sDay=%d&eYear=%d&eMonth=%d&eDay=%d", 
                                       id, sYear, sMonth, sDay, eYear, eMonth, eDay);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
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
        }
    }
}