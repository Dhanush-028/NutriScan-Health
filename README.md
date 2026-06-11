# 🥗 NutriScan Health

A food nutrition analysis and health tracking web app built with Java and MySQL.

## 🌐 Live Demo

[Click here to view the live app](https://nutriscan-health.onrender.com)

## ✨ Features

- Search any food and get instant calorie & nutrient breakdown
- Log daily meals and track calorie goals
- BMI calculator with personalized health recommendations
- Personal health report summary
- User register & login system

## 🛠️ Tech Stack

- **Backend:** Java (HTTP Server)
- **Database:** MySQL (Aiven Cloud)
- **Frontend:** HTML, CSS, JavaScript
- **Deployment:** Render (Docker)

## 🚀 Run Locally

```bash
git clone https://github.com/Dhanush-028/NutriScan-Health.git
cd NutriScan-Health
javac -cp "lib/mysql-connector-j-9.6.0.jar" -d out src/*.java
java -cp "out;lib/mysql-connector-j-9.6.0.jar" Main
```

Open `http://localhost:8081` in your browser.