#!/usr/bin/env bash

set -euo pipefail

(
sleep 5
echo "Initializing firewall ..."
# sudo /usr/local/bin/init-firewall.sh

echo "Opening firewall ..."
sudo /usr/local/bin/open-firewall.sh

echo "Success!  Running as $(whoami) and waiting..."
)
# exec gosu node:node "$@"
exec "$@"
