FROM node:22-alpine AS build-ui
WORKDIR /ui

COPY ui ./
RUN --mount=type=cache,target=/root/.npm npm ci

RUN npm run build
RUN npm run check

FROM eclipse-temurin:21-alpine AS build-server
WORKDIR /app

COPY . ./
RUN --mount=type=cache,target=/root/.gradle ./gradlew testClasses jar --info

# The final image
FROM eclipse-temurin:21-jre-alpine AS final
RUN adduser -S user
RUN rm -fr /usr/sbin /bin/ch*

WORKDIR /app
COPY --from=build-ui /ui/build ui/public
COPY --from=build-server /app/build/libs ./
COPY .env ./

ARG VERSION=dev
ENV VERSION=$VERSION
RUN echo "Setting built version to $VERSION" && sed -Ei "s/\\\$VERSION/$VERSION/" ui/public/index.html
RUN gzip -k9 ui/public/assets/*

USER user

ENV TZ=Europe/Tallinn
ENV JAVA_TOOL_OPTIONS="-Xss256k -Xmx1G -XX:+ExitOnOutOfMemoryError"
CMD SMTP_HOST=${SMTP_HOST:-$(ip route | awk 'NR==1 {print $3}')} java -jar *.jar

ENV PORT=8080
EXPOSE $PORT
