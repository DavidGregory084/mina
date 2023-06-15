#!/usr/bin/env bash
set -euo pipefail

# Install VS Code so that we can test the VS Code plugin
curl -fsSL https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor -o packages.microsoft.gpg
install -D -o root -g root -m 644 packages.microsoft.gpg /usr/share/keyrings/packages.microsoft.gpg
sh -c 'echo "deb [arch=amd64,arm64,armhf signed-by=/usr/share/keyrings/packages.microsoft.gpg] https://packages.microsoft.com/repos/code stable main" > /etc/apt/sources.list.d/vscode.list'
apt-get -qq update && apt-get -y install code xvfb

# Run the Gradle build
./gradlew build --info
