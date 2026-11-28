#!/bin/sh
# Starts the loan service in the container.
#
# Neon (and most hosts) give the database as one connection string:
#   postgresql://user:password@host/dbname?sslmode=require
# Spring Boot wants a JDBC URL with the user and password separate, so if
# DATABASE_URL is set we split it into DB_URL, DB_USERNAME and DB_PASSWORD.
set -e

if [ -n "$DATABASE_URL" ]; then
  rest="${DATABASE_URL#*://}"          # user:password@host/dbname?params
  credentials="${rest%%@*}"            # user:password
  location="${rest#*@}"                # host/dbname?params
  export DB_USERNAME="${credentials%%:*}"
  export DB_PASSWORD="${credentials#*:}"
  # The Java driver doesn't know Neon's channel_binding option, so leave it out
  location=$(printf '%s' "$location" | sed -e 's/[?&]channel_binding=[a-z]*//' -e 's/^\([^?]*\)&/\1?/')
  export DB_URL="jdbc:postgresql://${location}"
fi

exec java -jar /app/loan-service.jar
