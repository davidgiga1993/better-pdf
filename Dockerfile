FROM eclipse-temurin:17-jdk-focal as build
# RUN apk update && apk add gcompat binutils
RUN jlink --strip-debug --no-man-pages --no-header-files --add-modules java.base --output /java

FROM ubuntu:focal
COPY --from=build /java /java
COPY build/distributions/better-pdf.tar /app/


ARG APPLICATION_USER=app
RUN adduser -u 1000 $APPLICATION_USER && \
     chown -R $APPLICATION_USER /app && \
    tar xf /app/better-pdf.tar && \
    rm /app/*.tar

USER 1000
WORKDIR /app/better-pdf
ENV JAVA_HOME=/java

ENTRYPOINT ["/app/better-pdf/bin/better-pdf"]