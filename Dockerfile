FROM openjdk:17-slim
WORKDIR /app
COPY . .
EXPOSE 8080
CMD ["./gradlew", "bootRun"] 