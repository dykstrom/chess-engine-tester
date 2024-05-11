# syntax=docker/dockerfile:1.4

FROM maven:3.9.6-eclipse-temurin-17-alpine

RUN apk update && \
    apk add gnuchess && \
    apk cache clean && \
    mkdir -p /cet/engines

WORKDIR /cet/engines

ARG RONJA_VERSION=0.9.0
ARG RONJA_PACKAGE=ronja-${RONJA_VERSION}-bin.zip

RUN wget https://github.com/dykstrom/ronja/releases/download/ronja-${RONJA_VERSION}/${RONJA_PACKAGE} && \
    unzip ${RONJA_PACKAGE} && \
    rm -f ${RONJA_PACKAGE}

WORKDIR /cet

COPY . /cet/

RUN --mount=type=cache,id=mvn-cache,target=/root/.m2 mvn clean verify -P slow-tests

CMD bash
