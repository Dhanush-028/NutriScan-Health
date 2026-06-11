import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDAO {

    // ================================================================
    //  USER
    // ================================================================

    public int registerUser(String name, String email, String password) {
        String check = "SELECT id FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(check)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Email already registered: " + email);
                return -1;
            }
        } catch (Exception e) { e.printStackTrace(); }

        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public int validateLogin(String email, String password) {
        String sql = "SELECT id FROM users WHERE email = ? AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public boolean updateDailyGoal(int userId, int goal) {
        String sql = "UPDATE users SET daily_goal = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, goal);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public int getDailyGoal(int userId) {
        String sql = "SELECT daily_goal FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("daily_goal");
        } catch (Exception e) { e.printStackTrace(); }
        return 2000;
    }

    // ================================================================
    //  FOOD MASTER
    // ================================================================

    public boolean addFood(String name, String icon, int calories,
                           float protein, float fat, float carbs,
                           float fiber, float sugar, float sodium,
                           float vitaminC, String description, String category) {
        String sql = "INSERT IGNORE INTO food " +
                     "(name, icon, calories, protein, fat, carbs, fiber, sugar, sodium, vitamin_c, description, category) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, icon);
            ps.setInt(3, calories);
            ps.setFloat(4, protein);
            ps.setFloat(5, fat);
            ps.setFloat(6, carbs);
            ps.setFloat(7, fiber);
            ps.setFloat(8, sugar);
            ps.setFloat(9, sodium);
            ps.setFloat(10, vitaminC);
            ps.setString(11, description);
            ps.setString(12, category);
            ps.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String getAllFoodsAsJson() {
        StringBuilder sb = new StringBuilder("[");
        String sql = "SELECT * FROM food ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (sb.length() > 1) sb.append(",");
                sb.append("{")
                  .append("\"id\":").append(rs.getInt("id")).append(",")
                  .append("\"name\":\"").append(rs.getString("name")).append("\",")
                  .append("\"icon\":\"").append(rs.getString("icon")).append("\",")
                  .append("\"calories\":").append(rs.getInt("calories")).append(",")
                  .append("\"protein\":").append(rs.getFloat("protein")).append(",")
                  .append("\"fat\":").append(rs.getFloat("fat")).append(",")
                  .append("\"carbs\":").append(rs.getFloat("carbs")).append(",")
                  .append("\"fiber\":").append(rs.getFloat("fiber")).append(",")
                  .append("\"sugar\":").append(rs.getFloat("sugar")).append(",")
                  .append("\"sodium\":").append(rs.getFloat("sodium")).append(",")
                  .append("\"vitamin_c\":").append(rs.getFloat("vitamin_c")).append(",")
                  .append("\"description\":\"").append(rs.getString("description")).append("\",")
                  .append("\"category\":\"").append(rs.getString("category")).append("\"")
                  .append("}");
            }
        } catch (Exception e) { e.printStackTrace(); }
        sb.append("]");
        return sb.toString();
    }

    // ================================================================
    //  FOOD LOG
    // ================================================================

    public boolean addFoodLog(int userId, String foodName, String icon,
                              int calories, float protein, float fat,
                              float carbs, String logTime) {
        String sql = "INSERT INTO food_log " +
                     "(user_id, food_name, icon, calories, protein, fat, carbs, log_date, log_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE(), ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, foodName);
            ps.setString(3, icon);
            ps.setInt(4, calories);
            ps.setFloat(5, protein);
            ps.setFloat(6, fat);
            ps.setFloat(7, carbs);
            ps.setString(8, logTime);
            ps.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String getTodayLogAsJson(int userId) {
        StringBuilder sb = new StringBuilder("[");
        String sql = "SELECT * FROM food_log WHERE user_id = ? AND log_date = CURDATE() ORDER BY logged_at";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (sb.length() > 1) sb.append(",");
                sb.append("{")
                  .append("\"id\":").append(rs.getInt("id")).append(",")
                  .append("\"food_name\":\"").append(rs.getString("food_name")).append("\",")
                  .append("\"icon\":\"").append(rs.getString("icon")).append("\",")
                  .append("\"calories\":").append(rs.getInt("calories")).append(",")
                  .append("\"protein\":").append(rs.getFloat("protein")).append(",")
                  .append("\"fat\":").append(rs.getFloat("fat")).append(",")
                  .append("\"carbs\":").append(rs.getFloat("carbs")).append(",")
                  .append("\"log_time\":\"").append(rs.getString("log_time")).append("\"")
                  .append("}");
            }
        } catch (Exception e) { e.printStackTrace(); }
        sb.append("]");
        return sb.toString();
    }

    public boolean deleteFoodLog(int logId, int userId) {
        String sql = "DELETE FROM food_log WHERE id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, logId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean clearTodayLog(int userId) {
        String sql = "DELETE FROM food_log WHERE user_id = ? AND log_date = CURDATE()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public int getTodayCalories(int userId) {
        String sql = "SELECT COALESCE(SUM(calories), 0) AS total FROM food_log WHERE user_id = ? AND log_date = CURDATE()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("total");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ================================================================
    //  BMI RECORDS
    // ================================================================

    public boolean saveBMI(int userId, float heightCm, float weightKg, int age,
                           String gender, float activity, float bmi,
                           String category, int bmr, int tdee, float idealWeight) {
        String sql = "INSERT INTO bmi_records " +
                     "(user_id, height_cm, weight_kg, age, gender, activity, bmi, category, bmr, tdee, ideal_weight) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setFloat(2, heightCm);
            ps.setFloat(3, weightKg);
            ps.setInt(4, age);
            ps.setString(5, gender);
            ps.setFloat(6, activity);
            ps.setFloat(7, bmi);
            ps.setString(8, category);
            ps.setInt(9, bmr);
            ps.setInt(10, tdee);
            ps.setFloat(11, idealWeight);
            ps.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public String getLatestBMIAsJson(int userId) {
        String sql = "SELECT * FROM bmi_records WHERE user_id = ? ORDER BY recorded_at DESC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "{" +
                    "\"bmi\":"          + rs.getFloat("bmi")          + "," +
                    "\"category\":\""   + rs.getString("category")    + "\"," +
                    "\"height\":"       + rs.getFloat("height_cm")    + "," +
                    "\"weight\":"       + rs.getFloat("weight_kg")    + "," +
                    "\"age\":"          + rs.getInt("age")            + "," +
                    "\"gender\":\""     + rs.getString("gender")      + "\"," +
                    "\"bmr\":"          + rs.getInt("bmr")            + "," +
                    "\"tdee\":"         + rs.getInt("tdee")           + "," +
                    "\"idealWeight\":"  + rs.getFloat("ideal_weight") + "," +
                    "\"recordedAt\":\"" + rs.getTimestamp("recorded_at") + "\"" +
                    "}";
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "{}";
    }
}
