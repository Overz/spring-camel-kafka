FROM ghcr.io/graalvm/graalvm-ce:21.3.0-java21 AS build

RUN gu install native-image

WORKDIR /app

COPY . .

RUN mvn -Pnative clean install package -DskipTests

FROM ghcr.io/graalvm/graalvm-ce:21.3.0-java21

WORKDIR /app

COPY --from=build /app/target/*-native-image /app/app

EXPOSE "3000"

ENTRYPOINT ["./app"]

# ENTRYPOINT ["java", "-jar", "target/your-spring-boot-app.jar"]