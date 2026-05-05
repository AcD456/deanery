FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jdk
WORKDIR /app

# Копируем JAR
COPY --from=build /app/target/deanery-0.0.1-SNAPSHOT.jar app.jar

# Копируем статические файлы
COPY --from=build /app/src/main/resources/static /app/static

# Устанавливаем кодировку UTF-8
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_OPTS="-Dfile.encoding=UTF-8"

EXPOSE 8080

# Запускаем с UTF-8
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
