# Stage 1: Build the Angular application
FROM node:20-alpine AS builder

# Set working directory
WORKDIR /app

# Copy package files (path is relative to build context root)
COPY events-ui/package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy project files (path is relative to build context root)
COPY events-ui/ .

# Build the application for production
RUN npm run build

# Stage 2: Serve the application with nginx
FROM nginx:alpine

# Copy custom nginx configuration (path is relative to build context root)
COPY events-ui/nginx.conf /etc/nginx/conf.d/default.conf

# Copy built application from builder stage
# Angular 20 with @angular/build outputs to dist/event-booking-ui/browser
COPY --from=builder /app/dist/event-booking-ui/browser /usr/share/nginx/html


# Expose port 80
EXPOSE 80

# Add healthcheck for Kubernetes
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
