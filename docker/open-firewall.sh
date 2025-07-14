#!/usr/bin/env bash

set -euo pipefail  # Exit on error, undefined vars, and pipeline failures
set -x
IFS=$'\n\t'       # Stricter word splitting

# If not running as root, error out
if [[ $EUID -ne 0 ]]; then
  echo "Error: must be run as root" >&2
  exit 1
fi

if ! command -v dig &> /dev/null; then
  echo "ERROR: dig not found in PATH" >&2
  exit 11
fi

# Create ipset with CIDR support
modprobe ip_set_hash_net || true
modprobe xt_set          || true
ipset create allowed-domains hash:net -exist
iptables -I INPUT   1 -i lo -j ACCEPT
iptables -I OUTPUT  1 -o lo -j ACCEPT
iptables -I INPUT   2 -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT
iptables -I OUTPUT  2 -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT
NSIP=$(awk '/^nameserver/ {print $2; exit}' /etc/resolv.conf)
iptables -I OUTPUT  3 -p udp --dport 53 -d "${NSIP}" -j ACCEPT
iptables -I OUTPUT  4 -p tcp --dport 53 -d "${NSIP}" -j ACCEPT

# Resolve and add other allowed domains
for domain in "registry.npmjs.org" \
              "api.anthropic.com" \
              "sentry.io" \
              "statsig.anthropic.com" \
              "statsig.com" \
              "repo.maven.apache.org" \
              "repo1.maven.org" \
              "plugins.gradle.org" \
              "jcenter.bintray.com" \
              "maven.google.com" \
              "dl.google.com"; do
    echo "Resolving $domain..."
    ips=$(dig +short A "$domain")
    if [ -z "$ips" ]; then
        echo "ERROR: Failed to resolve $domain"
        exit 2
    fi
    echo "Resolved $ips."
    while read -r ip; do
        if [[ ! "$ip" =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
            echo "ERROR: Invalid IP from DNS for $domain: $ip"
            exit 3
        fi
        echo "Adding $ip for $domain"
        ipset add allowed-domains "$ip"
    done < <(echo "$ips")
done

iptables -A OUTPUT  -m set --match-set allowed-domains dst -j ACCEPT
iptables -A INPUT   -m set --match-set allowed-domains src -j ACCEPT

# iptables -R INPUT 1 -m set --match-set allowed-domains src -j ACCEPT
iptables -P INPUT DROP
iptables -P FORWARD DROP
iptables -P OUTPUT DROP

echo "Firewall configuration complete"
echo "Verifying firewall rules..."
if curl --connect-timeout 5 https://example.com >/dev/null 2>&1; then
    echo "ERROR: Firewall verification failed - was able to reach https://example.com"
    exit 4
fi
if ! curl -sS https://api.anthropic.com/ -o /dev/null; then
  echo "ERROR: Unable to reach api.anthropic.com" >&2
  exit 5
fi
echo "Firewall verification passed - unable to reach https://example.com as expected"
