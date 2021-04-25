# Container with application
FROM amazoncorretto:11.0.11
COPY /build/install/tgto /tgto
ENTRYPOINT /tgto/bin/tgto
