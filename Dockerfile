# Container with application
FROM amazoncorretto:11.0.10
COPY /build/install/tgto /tgto
ENTRYPOINT /tgto/bin/tgto
