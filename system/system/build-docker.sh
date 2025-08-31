#!/bin/bash

# Docker build script for Hospital Management System
# Usage: ./build-docker.sh [dev|prod|latest] [version]

set -e

# Default values
ENVIRONMENT=${1:-latest}
VERSION=${2:-$(date +%Y%m%d-%H%M%S)}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🏥 Hospital Management System - Docker Build${NC}"
echo -e "${BLUE}============================================${NC}"
echo -e "Environment: ${YELLOW}$ENVIRONMENT${NC}"
echo -e "Version: ${YELLOW}$VERSION${NC}"
echo ""

# Function to build image
build_image() {
    local env=$1
    local version=$2
    local tag="hospital-management:$env-$version"
    local latest_tag="hospital-management:$env"
    
    echo -e "${GREEN}🔨 Building Docker image...${NC}"
    echo -e "Tag: ${YELLOW}$tag${NC}"
    echo -e "Latest: ${YELLOW}$latest_tag${NC}"
    echo ""
    
    # Build the image
    docker build \
        --build-arg BUILD_ENV=$env \
        --build-arg BUILD_VERSION=$version \
        --tag $tag \
        --tag $latest_tag \
        --file Dockerfile \
        .
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Image built successfully!${NC}"
        echo -e "${GREEN}📦 Image tags:${NC}"
        echo -e "  - ${YELLOW}$tag${NC}"
        echo -e "  - ${YELLOW}$latest_tag${NC}"
    else
        echo -e "${RED}❌ Build failed!${NC}"
        exit 1
    fi
}

# Function to push image (if registry is configured)
push_image() {
    local env=$1
    local version=$2
    local tag="hospital-management:$env-$version"
    
    if [ ! -z "$DOCKER_REGISTRY" ]; then
        echo -e "${BLUE}📤 Pushing image to registry...${NC}"
        docker tag $tag $DOCKER_REGISTRY/$tag
        docker push $DOCKER_REGISTRY/$tag
        echo -e "${GREEN}✅ Image pushed successfully!${NC}"
    else
        echo -e "${YELLOW}⚠️  No registry configured. Skipping push.${NC}"
        echo -e "${YELLOW}   Set DOCKER_REGISTRY environment variable to push.${NC}"
    fi
}

# Function to show image info
show_image_info() {
    local env=$1
    local version=$2
    local tag="hospital-management:$env-$version"
    
    echo -e "${BLUE}📊 Image Information:${NC}"
    docker images | grep hospital-management
    echo ""
    
    echo -e "${BLUE}🔍 Image details:${NC}"
    docker inspect $tag --format='{{.Id}}' | head -1
    echo ""
}

# Main build logic
case $ENVIRONMENT in
    "dev")
        echo -e "${GREEN}🚀 Building DEVELOPMENT image...${NC}"
        build_image "dev" $VERSION
        show_image_info "dev" $VERSION
        ;;
    "prod")
        echo -e "${GREEN}🚀 Building PRODUCTION image...${NC}"
        build_image "prod" $VERSION
        show_image_info "prod" $VERSION
        push_image "prod" $VERSION
        ;;
    "latest")
        echo -e "${GREEN}🚀 Building LATEST image...${NC}"
        build_image "latest" $VERSION
        show_image_info "latest" $VERSION
        ;;
    *)
        echo -e "${RED}❌ Invalid environment: $ENVIRONMENT${NC}"
        echo -e "${YELLOW}Usage: $0 [dev|prod|latest] [version]${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}🎉 Build completed successfully!${NC}"
echo ""
echo -e "${BLUE}📋 Next steps:${NC}"
echo -e "  - Run development: ${YELLOW}docker-compose -f docker-compose.dev.yml up${NC}"
echo -e "  - Run production:  ${YELLOW}docker-compose -f docker-compose.prod.yml up${NC}"
echo -e "  - Run with messaging: ${YELLOW}docker-compose --profile messaging up${NC}" 