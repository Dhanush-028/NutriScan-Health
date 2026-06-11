FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY lib/ lib/
COPY src/ src/
COPY frontened/ frontened/

RUN mkdir -p out && javac -cp "lib/mysql-connector-j-9.6.0.jar" -d out src/*.java

EXPOSE 8081

CMD ["java", "-cp", "out:lib/mysql-connector-j-9.6.0.jar", "Main"]