FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY . .

RUN chmod +x build.sh && ./build.sh

EXPOSE 8080

CMD ["java", "-cp", "backend/bin:backend/lib/*", "com.expensesplit.App"]
