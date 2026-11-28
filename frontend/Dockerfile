# The Angular admin, built and served by nginx (used by docker-compose.yml).

# ---- 1. Build ----
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Production build: talks to the backend at http://localhost:8080 (src/environments/environment.ts)
RUN npx ng build

# ---- 2. Serve ----
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
