# Stage 1: build
# Start with a Maven image that includes JDK 21
FROM maven:3.9.8-amazoncorretto-21 AS build

# Copy source code and pom.xml file to /app folder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build source code with maven
RUN mvn spotless:apply && mvn package -DskipTests


#Stage 2: create image
# Start with Amazon Correto JDK 21
FROM amazoncorretto:21.0.4

# ✅ Install tzdata and set timezone to Asia/Ho_Chi_Minh (GMT+7)
RUN yum install -y tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    echo "Asia/Ho_Chi_Minh" > /etc/timezone && \
    yum clean all

# Set working folder to App and copy compiled file from above step
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8089

# Command to run the application with timezone setting
ENTRYPOINT ["java", \
    "-Duser.timezone=Asia/Ho_Chi_Minh", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]