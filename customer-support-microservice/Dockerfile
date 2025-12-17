# --- Stage 1: Build the Application ---
FROM maven:3.9 AS buildenv

ENV TZ=UTC
# Set timezone to UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /demo

# Copy pom.xml and download dependencies
COPY ./pom.xml /demo/pom.xml
RUN mvn dependency:go-offline

# Copy source code
COPY ./src/ /demo/src

# Build the application
RUN mvn clean package -DskipTests

# --- Stage 2: Create the Runtime Image ---
FROM eclipse-temurin:21-jdk

WORKDIR /demo

# Copy the built jar from the previous stage
COPY --from=buildenv /demo/target/*.jar /demo/demo.jar

# Expose the port (matches application.properties)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "demo.jar"]
