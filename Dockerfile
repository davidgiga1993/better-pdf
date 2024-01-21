FROM eclipse-temurin:17-jdk-focal as build
# RUN apk update && apk add gcompat binutils
RUN jlink --strip-debug --no-man-pages --no-header-files --add-modules java.base,java.xml,java.desktop --output /java

FROM ubuntu:focal
COPY --from=build /java /java
COPY build/distributions/better-pdf.tar /app/


ARG APPLICATION_USER=app
RUN useradd -ms /bin/bash $APPLICATION_USER && \
    chown -R $APPLICATION_USER /app && \
    cd /app && tar -xf /app/better-pdf.tar && \
    rm /app/*.tar && \
    ls -la /app/better-pdf

USER app
WORKDIR /app/better-pdf
ENV JAVA_HOME=/java

ENTRYPOINT ["/app/better-pdf/bin/better-pdf"]