import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        try {
            // Test Database Connection
            Connection con = DBConnection.getConnection();
            if (con != null) {
                System.out.println("Database connected successfully!");
                con.close();
            }
        } catch (Exception e) {
            System.out.println("DB connection warning: " + e.getMessage());
            System.out.println("Server will still start...");
        }

        try {
            FoodDAO dao = new FoodDAO();
            dao.addFood("Apple",   "🍎", 95,  0.5f, 0.3f,  25.0f, 4.4f, 19.0f, 1.0f,  8.4f, "Fresh apple",     "Fruit");
            dao.addFood("Chicken", "🍗", 200, 25.0f, 10.0f, 0.0f,  0.0f, 0.0f,  70.0f, 0.0f, "Grilled chicken", "Protein");
            System.out.println("Foods added successfully!");

            int newUserId = dao.registerUser("Test User", "test@gmail.com", "1234");
            if (newUserId > 0) {
                System.out.println("User Registered! ID: " + newUserId);
            } else {
                System.out.println("User already exists, proceeding to login...");
            }

            int userId = dao.validateLogin("test@gmail.com", "1234");
            if (userId != -1) {
                System.out.println("Login Successful! User ID: " + userId);
            } else {
                System.out.println("Login Failed - User not found!");
            }
        } catch (Exception e) {
            System.out.println("DB setup warning: " + e.getMessage());
        }

        // Always start server regardless of DB status
        try {
            System.out.println("Starting server...");
            Server.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}