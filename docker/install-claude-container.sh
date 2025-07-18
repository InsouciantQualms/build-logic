#!/bin/bash

# install-claude-container.sh - Install claude command to user's PATH

set -e

# Configuration variables
LAUNCHER_SCRIPT_NAME="claude-container.sh"
INSTALL_COMMAND_NAME="claude-container"
INSTALL_DIR="$HOME/.local/bin"

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
CLAUDE_SCRIPT="${SCRIPT_DIR}/${LAUNCHER_SCRIPT_NAME}"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}🤖 Claude Container CLI Installer${NC}"

# Verify the launcher script exists
if [ ! -f "${CLAUDE_SCRIPT}" ]; then
    echo -e "${YELLOW}Error: ${LAUNCHER_SCRIPT_NAME} not found at ${CLAUDE_SCRIPT}${NC}"
    exit 1
fi

# Check if install directory exists, create if not
if [ ! -d "${INSTALL_DIR}" ]; then
    echo -e "${YELLOW}Creating ${INSTALL_DIR} directory...${NC}"
    mkdir -p "${INSTALL_DIR}"
fi

# Create a simple wrapper that calls the actual script
cat > "${INSTALL_DIR}/${INSTALL_COMMAND_NAME}" << EOF
#!/bin/bash
exec "${CLAUDE_SCRIPT}" "\$@"
EOF

chmod +x "${INSTALL_DIR}/${INSTALL_COMMAND_NAME}"

echo -e "${GREEN}✅ Claude Container CLI installed successfully!${NC}"
echo

# Check if install directory is in PATH
if [[ ":$PATH:" != *":${INSTALL_DIR}:"* ]]; then
    echo -e "${YELLOW}⚠️  ${INSTALL_DIR} is not in your PATH${NC}"
    echo "Add the following line to your shell configuration file (~/.bashrc, ~/.zshrc, etc.):"
    echo
    echo -e "${BLUE}export PATH=\"${INSTALL_DIR}:\$PATH\"${NC}"
    echo
    echo "Then reload your shell configuration or open a new terminal."
else
    echo -e "${GREEN}You can now use '${INSTALL_COMMAND_NAME}' from any directory!${NC}"
fi

echo
echo "Usage: ${INSTALL_COMMAND_NAME} [options]"
echo "This will launch Claude Code with the current directory as the workspace."