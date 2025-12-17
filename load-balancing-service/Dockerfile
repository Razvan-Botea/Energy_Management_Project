# Folosim o imagine de bază cu Java 17 (sau 21 dacă folosești 21)
FROM eclipse-temurin:17-jdk-alpine

# Setăm directorul de lucru
WORKDIR /app

# Copiem fișierele necesare pentru Maven build
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Descărcăm dependențele (pentru cache)
RUN ./mvnw dependency:go-offline

# Copiem sursele
COPY src ./src

# Construim aplicația (skip tests pentru viteză la deploy)
RUN ./mvnw package -DskipTests

# Comanda de start
# Ajustează numele jar-ului dacă e diferit (verifică target/)
CMD ["java", "-jar", "target/load-balancing-service-0.0.1-SNAPSHOT.jar"]
