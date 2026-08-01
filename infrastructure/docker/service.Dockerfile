# Shared multi-stage build for every OneStop backend module.
#
# The build context is the repository root so Maven can see the parent POM and
# resolve the reactor. Pass the module (== directory == artifactId) as a build
# arg, e.g. in docker-compose:
#
#   build:
#     context: .
#     dockerfile: infrastructure/docker/service.Dockerfile
#     args:
#       SERVICE: catalog-service
#
# ---- build stage -----------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy the whole monorepo, then build only the requested module (+ its parent).
COPY . .
ARG SERVICE
RUN chmod +x mvnw && ./mvnw -B -pl ${SERVICE} -am package -DskipTests

# ---- runtime stage ---------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
ARG SERVICE
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
