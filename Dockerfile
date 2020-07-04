FROM openjdk:15-alpine
WORKDIR /mangahome
COPY /build/libs/mangadex_at_home.jar .
RUN apk update && apk add --no-cache libsodium
VOLUME "/mangahome/cache"
EXPOSE 443 8080
CMD java -Dfile-level=off -Dstdout-level=trace -jar mangadex_at_home.jar