# 🐳 Docker Setup Guide

This guide explains how to run the Hospital Management System using Docker.

## 📋 Prerequisites

- Docker installed on your system
- Docker Compose installed
- At least 2GB of available RAM
- Ports 8081, 8082, and 3307 available

## 🚀 Quick Start

### 1. Build the Docker Image

```bash
# Make the build script executable
chmod +x build-docker.sh

# Build the development image
./build-docker.sh dev
```

### 2. Start the Application

```bash
# Start all services (database, application, admin interface)
docker-compose -f docker-compose.dev.yml up -d
```

### 3. Verify Everything is Running

```bash
# Check container status
docker-compose -f docker-compose.dev.yml ps
```

You should see all containers with status "Up" and health "healthy".

## 🌐 Access Points

- **Application**: http://localhost:8081
- **Database Admin**: http://localhost:8082
- **Database**: localhost:3307

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root to customize settings:

```bash
# Copy the example file
cp env.example .env

# Edit the file with your preferred values
nano .env

**⚠️ Required Variables:**
You MUST set these variables in your `.env` file:
- `MYSQL_ROOT_PASSWORD=your_secure_password_here`
- `JWT_SECRET=your_jwt_secret_at_least_32_characters`
```

### Default Configuration

**⚠️ IMPORTANT: You MUST create a `.env` file with your passwords before starting the application!**

The application requires these environment variables to be set:

- `MYSQL_ROOT_PASSWORD` - **REQUIRED** (no default)
- `JWT_SECRET` - **REQUIRED** (no default)
- Other variables have sensible defaults

## 🔧 Management Commands

### View Logs

```bash
# Application logs
docker-compose -f docker-compose.dev.yml logs hospital-app

# Database logs
docker-compose -f docker-compose.dev.yml logs mysql

# All logs
docker-compose -f docker-compose.dev.yml logs -f
```

### Stop Services

```bash
# Stop all services
docker-compose -f docker-compose.dev.yml down

# Stop and remove volumes (deletes database data)
docker-compose -f docker-compose.dev.yml down -v
```

### Restart Services

```bash
# Restart all services
docker-compose -f docker-compose.dev.yml restart

# Restart specific service
docker-compose -f docker-compose.dev.yml restart hospital-app
```

### Update Application

```bash
# Rebuild and restart
./build-docker.sh dev
docker-compose -f docker-compose.dev.yml up -d --build
```

## 🗄️ Database Management

### Access Database Admin Interface

1. Open http://localhost:8082 in your browser
2. Use these credentials:
   - **System**: MySQL
   - **Server**: mysql
   - **Username**: root
   - **Password**: `[your_password]` (from your .env file)
   - **Database**: hospital_dev

### Database Connection Details

- **Host**: localhost
- **Port**: 3307
- **Database**: hospital_dev
- **Username**: root
- **Password**: `[your_password]` (from your .env file)

## 🐛 Troubleshooting

### Port Already in Use

```bash
# Check what's using the ports
sudo netstat -tulpn | grep :8081
sudo netstat -tulpn | grep :3307

# Stop conflicting services or change ports in docker-compose.dev.yml
```

### Container Won't Start

```bash
# Check container logs
docker-compose -f docker-compose.dev.yml logs

# Check container status
docker-compose -f docker-compose.dev.yml ps

# Restart containers
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml up -d
```

### Database Connection Issues

```bash
# Wait for database to be ready
docker-compose -f docker-compose.dev.yml logs mysql

# Check database health
docker-compose -f docker-compose.dev.yml exec mysql mysqladmin ping -h localhost -u root -p
```

### Out of Memory

```bash
# Check Docker resource usage
docker stats

# Increase Docker memory limit in Docker Desktop settings
# Or reduce JVM memory in docker-compose.dev.yml
```

## 🧹 Cleanup

### Remove Everything

```bash
# Stop and remove containers, networks, and volumes
docker-compose -f docker-compose.dev.yml down -v

# Remove Docker images
docker rmi hospital-management:dev

# Remove unused Docker resources
docker system prune -a
```

### Keep Data

```bash
# Stop containers but keep data
docker-compose -f docker-compose.dev.yml down

# Start again (data preserved)
docker-compose -f docker-compose.dev.yml up -d
```

## 📝 Notes

- The application takes about 30-60 seconds to start up
- Database initialization happens automatically on first run
- All data is stored in Docker volumes and persists between restarts
- The application runs on port 8081 to avoid conflicts with other services
- Database runs on port 3307 to avoid conflicts with local MySQL installations

## ✅ Success Indicators

Your setup is working correctly when:

- All containers show "Up" status
- Application responds at http://localhost:8081
- Database admin interface loads at http://localhost:8082
- No error messages in container logs
