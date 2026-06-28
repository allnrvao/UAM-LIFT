# === PASO 1: COMPILAR LA APLICACIÓN ===
# Descarga una imagen oficial de Maven con Java 21 para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia los archivos de configuración de Maven (pom.xml) y el código fuente
COPY pom.xml .
COPY src ./src

# Compila el proyecto generando el archivo .jar (y se salta los tests para agilizar)
RUN mvn clean package -DskipTests

# === PASO 2: EJECUTAR LA APLICACIÓN ===
# Descarga una imagen ultra ligera que solo tiene el entorno de ejecución de Java 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Toma el archivo .jar compilado en el Paso 1 y lo copia aquí dentro
COPY --from=build /app/target/*.jar app.jar

# Le avisa a Render que la app escuchará en el puerto 8080
EXPOSE 8080

# Comando final para arrancar tu API de Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]