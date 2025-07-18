#!/bin/bash

# claude-container.sh - Launch Claude Code in a Docker container with current directory as workspace

set -e

# Configuration variables
COMPOSE_FILE="docker-compose-cli.yml"
SERVICE_NAME="claude-code-cli"
IMAGE_NAME="claude-code-cli"
BASE_IMAGE_NAME="claude-code"
BASE_IMAGE_REPO="https://github.com/anthropics/claude-code.git"
BASE_IMAGE_BRANCH="main"
SCRIPT_NAME="$(basename "$0")"

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Set the workspace to the current directory
export CLAUDE_WORKSPACE="$(pwd)"
export CLAUDE_DIRNAME="$(basename "$(pwd)")"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🤖 Claude Code Docker Launcher${NC}"
echo -e "${GREEN}Workspace: ${CLAUDE_WORKSPACE}${NC}"
echo -e "${GREEN}Mounted as: /workspace/${CLAUDE_DIRNAME}${NC}"

# Check if docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}Error: Docker is not installed or not in PATH${NC}"
    exit 1
fi

# Check if docker compose is available
if docker compose version &> /dev/null; then
    DOCKER_COMPOSE="docker compose"
elif command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE="docker-compose"
else
    echo -e "${YELLOW}Error: Docker Compose is not installed${NC}"
    exit 1
fi

# Change to the docker directory
cd "${SCRIPT_DIR}"

# Check if the base claude-code image exists
if ! docker images | grep -q "^${BASE_IMAGE_NAME} "; then
    echo -e "${YELLOW}Base image '${BASE_IMAGE_NAME}' not found.${NC}"
    
    # Note: Using docker buildx which often works without explicit Docker Hub login
    
    echo -e "${BLUE}Building base Claude Code image from GitHub...${NC}"
    echo -e "${BLUE}This may take several minutes on first run...${NC}"
    
    # Use buildx which handles authentication better
    if ! docker buildx build -t "${BASE_IMAGE_NAME}" "${BASE_IMAGE_REPO}#${BASE_IMAGE_BRANCH}:.devcontainer" 2>&1 | tee /tmp/claude-docker-build.log; then
        echo -e "${YELLOW}Failed to build base image.${NC}"
        
        # Check for common errors
        if grep -q "401 Unauthorized" /tmp/claude-docker-build.log; then
            echo -e "${YELLOW}Docker Hub authentication issue detected.  Please use docker login first.${NC}"
        fi
        
        rm -f /tmp/claude-docker-build.log
        exit 1
    fi
    rm -f /tmp/claude-docker-build.log
    echo -e "${GREEN}Base image built successfully!${NC}"
fi

# Check if the image needs to be built
if ! docker images | grep -q "^${IMAGE_NAME} "; then
    echo -e "${BLUE}Building Claude Code Docker image with Java support...${NC}"
    $DOCKER_COMPOSE -f "${COMPOSE_FILE}" build
fi

# Run the container
echo -e "${BLUE}Starting Claude Code...${NC}"

# Check if container is already running
if docker ps --format '{{.Names}}' | grep -q "^${SERVICE_NAME}$"; then
    echo -e "${GREEN}Container '${SERVICE_NAME}' is already running.${NC}"
    echo -e "${BLUE}Attaching to existing container...${NC}"
    docker exec -it "${SERVICE_NAME}" /bin/bash
    # Don't stop existing container on exit
else
    # Start the container in detached mode
    echo -e "${BLUE}Starting new container...${NC}"
    $DOCKER_COMPOSE -f "${COMPOSE_FILE}" up -d
    
    # Give it a moment to start
    sleep 2
    
    # Exec into the container
    echo -e "${GREEN}Container started. Attaching to shell...${NC}"
    docker exec -it "${SERVICE_NAME}" /bin/bash
    
    # Stop the container after exiting the shell
    echo
    echo -e "${BLUE}Stopping container...${NC}"
    $DOCKER_COMPOSE -f "${COMPOSE_FILE}" down
    echo -e "${GREEN}Container stopped.${NC}"
fi