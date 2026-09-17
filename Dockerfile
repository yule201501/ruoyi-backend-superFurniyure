FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN mvn -B -pl ruoyi-admin -am package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app app && mkdir -p /app/data/uploads /app/logs && chown -R app:app /app
COPY --from=build --chown=app:app /build/ruoyi-admin/target/ruoyi-admin.jar /app/app.jar
USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -Dfile.encoding=UTF-8 -Duser.timezone=UTC"
ENTRYPOINT ["java","-jar","/app/app.jar"]
