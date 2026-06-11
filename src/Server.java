import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        FoodDAO dao = new FoodDAO();

        // API routes
        server.createContext("/register", ex -> handleRegister(ex, dao));
        server.createContext("/login",    ex -> handleLogin(ex, dao));
        server.createContext("/foods",    ex -> handleFoods(ex, dao));
        server.createContext("/log",      ex -> handleLog(ex, dao));
        server.createContext("/bmi",      ex -> handleBMI(ex, dao));
        server.createContext("/report",   ex -> handleReport(ex, dao));

        // Serve all static files from frontened/
        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File("frontened" + path);
            if (!file.exists() || file.isDirectory()) {
                ex.sendResponseHeaders(404, -1);
                return;
            }

            String mime = "text/plain";
            if (path.endsWith(".html")) mime = "text/html; charset=UTF-8";
            else if (path.endsWith(".css"))  mime = "text/css";
            else if (path.endsWith(".js"))   mime = "application/javascript";
            else if (path.endsWith(".png"))  mime = "image/png";
            else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mime = "image/jpeg";
            else if (path.endsWith(".ico"))  mime = "image/x-icon";

            byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().set("Content-Type", mime);
            ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(data); }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("✅ NutriScan Server running at http://localhost:8081");
    }

    // ============================================================
    //  /register   GET → serve HTML | POST → create user
    // ============================================================
    static void handleRegister(HttpExchange ex, FoodDAO dao) throws IOException {
        if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
            serveFile(ex, "frontened/register.html"); return;
        }
        addCORS(ex);
        if (preflight(ex)) return;

        String body     = readBody(ex);
        String name     = extract(body, "name");
        String email    = extract(body, "email");
        String password = extract(body, "password");

        int userId = dao.registerUser(name, email, password);
        String response = userId > 0
            ? "{\"success\":true,\"userId\":" + userId + "}"
            : "{\"success\":false,\"error\":\"Email already registered\"}";
        send(ex, response);
    }

    // ============================================================
    //  /login   GET → serve HTML | POST → validate credentials
    // ============================================================
    static void handleLogin(HttpExchange ex, FoodDAO dao) throws IOException {
        if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
            serveFile(ex, "frontened/login.html"); return;
        }
        addCORS(ex);
        if (preflight(ex)) return;

        String body     = readBody(ex);
        String email    = extract(body, "email");
        String password = extract(body, "password");

        int userId = dao.validateLogin(email, password);
        String response = userId > 0
            ? "{\"success\":true,\"userId\":" + userId + "}"
            : "{\"success\":false,\"error\":\"Invalid credentials\"}";
        send(ex, response);
    }

    // ============================================================
    //  /foods   GET → all foods | POST → add custom food
    // ============================================================
    static void handleFoods(HttpExchange ex, FoodDAO dao) throws IOException {
        addCORS(ex);
        if (preflight(ex)) return;

        if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, dao.getAllFoodsAsJson());

        } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            String body = readBody(ex);
            boolean ok = dao.addFood(
                extract(body, "name"),
                extract(body, "icon"),
                parseInt(extract(body, "calories")),
                parseFloat(extract(body, "protein")),
                parseFloat(extract(body, "fat")),
                parseFloat(extract(body, "carbs")),
                parseFloat(extract(body, "fiber")),
                parseFloat(extract(body, "sugar")),
                parseFloat(extract(body, "sodium")),
                parseFloat(extract(body, "vitamin_c")),
                extract(body, "description"),
                extract(body, "category")
            );
            send(ex, "{\"success\":" + ok + "}");
        }
    }

    // ============================================================
    //  /log
    // ============================================================
    static void handleLog(HttpExchange ex, FoodDAO dao) throws IOException {
        addCORS(ex);
        if (preflight(ex)) return;

        String path   = ex.getRequestURI().getPath();
        String method = ex.getRequestMethod().toUpperCase();

        if (path.endsWith("/clear") && "POST".equals(method)) {
            String body   = readBody(ex);
            int    userId = parseInt(extract(body, "userId"));
            boolean ok    = dao.clearTodayLog(userId);
            send(ex, "{\"success\":" + ok + "}");
            return;
        }

        if ("GET".equals(method)) {
            String query  = ex.getRequestURI().getQuery();
            int    userId = parseInt(parseParam(query, "userId"));
            send(ex, dao.getTodayLogAsJson(userId));

        } else if ("POST".equals(method)) {
            String body = readBody(ex);
            boolean ok = dao.addFoodLog(
                parseInt(extract(body, "userId")),
                extract(body, "foodName"),
                extract(body, "icon"),
                parseInt(extract(body, "calories")),
                parseFloat(extract(body, "protein")),
                parseFloat(extract(body, "fat")),
                parseFloat(extract(body, "carbs")),
                extract(body, "logTime")
            );
            send(ex, "{\"success\":" + ok + "}");

        } else if ("DELETE".equals(method)) {
            String body   = readBody(ex);
            int    logId  = parseInt(extract(body, "logId"));
            int    userId = parseInt(extract(body, "userId"));
            boolean ok    = dao.deleteFoodLog(logId, userId);
            send(ex, "{\"success\":" + ok + "}");
        }
    }

    // ============================================================
    //  /bmi
    // ============================================================
    static void handleBMI(HttpExchange ex, FoodDAO dao) throws IOException {
        addCORS(ex);
        if (preflight(ex)) return;

        if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
            String query  = ex.getRequestURI().getQuery();
            int    userId = parseInt(parseParam(query, "userId"));
            send(ex, dao.getLatestBMIAsJson(userId));

        } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
            String body = readBody(ex);
            boolean ok = dao.saveBMI(
                parseInt(extract(body, "userId")),
                parseFloat(extract(body, "height")),
                parseFloat(extract(body, "weight")),
                parseInt(extract(body, "age")),
                extract(body, "gender"),
                parseFloat(extract(body, "activity")),
                parseFloat(extract(body, "bmi")),
                extract(body, "category"),
                parseInt(extract(body, "bmr")),
                parseInt(extract(body, "tdee")),
                parseFloat(extract(body, "idealWeight"))
            );
            send(ex, "{\"success\":" + ok + "}");
        }
    }

    // ============================================================
    //  /report
    // ============================================================
    static void handleReport(HttpExchange ex, FoodDAO dao) throws IOException {
        addCORS(ex);
        if (preflight(ex)) return;

        String query    = ex.getRequestURI().getQuery();
        int    userId   = parseInt(parseParam(query, "userId"));
        int    totalCal = dao.getTodayCalories(userId);
        int    goal     = dao.getDailyGoal(userId);
        String bmiJson  = dao.getLatestBMIAsJson(userId);
        String logJson  = dao.getTodayLogAsJson(userId);

        int foodCount = 0, idx = 0;
        while ((idx = logJson.indexOf("\"id\":", idx)) != -1) { foodCount++; idx++; }

        String response =
            "{" +
            "\"totalCalories\":"  + totalCal  + "," +
            "\"dailyGoal\":"      + goal       + "," +
            "\"goalPercent\":"    + Math.round(totalCal * 100.0 / goal) + "," +
            "\"foodsLogged\":"    + foodCount  + "," +
            "\"bmi\":"            + bmiJson    + "," +
            "\"log\":"            + logJson    +
            "}";
        send(ex, response);
    }

    // ============================================================
    //  HELPERS
    // ============================================================

    static void serveFile(HttpExchange ex, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) { ex.sendResponseHeaders(404, -1); return; }
        byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes());
    }

    static String extract(String body, String key) {
        try {
            String after = body.split("\"" + key + "\"\\s*:\\s*")[1];
            if (after.trim().startsWith("\"")) {
                return after.trim().substring(1, after.trim().indexOf("\"", 1));
            }
            return after.split("[,}\\]]")[0].trim();
        } catch (Exception e) { return "0"; }
    }

    static String parseParam(String query, String key) {
        if (query == null) return "0";
        for (String part : query.split("&")) {
            String[] kv = part.split("=");
            if (kv.length == 2 && kv[0].equals(key)) return kv[1];
        }
        return "0";
    }

    static int   parseInt(String s)   { try { return Integer.parseInt(s.trim()); }  catch (Exception e) { return 0; } }
    static float parseFloat(String s) { try { return Float.parseFloat(s.trim()); }  catch (Exception e) { return 0f; } }

    static boolean preflight(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    static void addCORS(HttpExchange ex) {
        Headers h = ex.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
    }

    static void send(HttpExchange ex, String res) throws IOException {
        byte[] b = res.getBytes("UTF-8");
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(200, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}