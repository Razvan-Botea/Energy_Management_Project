FROM maven:3.9 AS buildenv

ENV TZ=UTC

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV CATALINA_OPTS="-Duser.timezone=UTC"

WORKDIR /demo

COPY ./pom.xml /demo/pom.xml

RUN mvn dependency:go-offline

COPY ./src/ /demo/src

RUN mvn clean install -DskipTests


FROM eclipse-temurin:17-jdk

WORKDIR /demo


COPY --from=buildenv /demo/target/*.jar /demo/demo.jar


EXPOSE 8080

CMD ["java", "-jar", "demo.jar"]
