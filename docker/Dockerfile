FROM claude-code

USER root

# Create dirs for IntelliJ remote IDE to persist settings across builds (mounted from host)

RUN \
  mkdir -p /home/node/.cache/JetBrains/RemoteDev/dist/idea-IU-251.26094.121 && \
  mkdir -p /.jbdevcontainer/JetBrains/IntelliJIdea2025.1/plugins && \
  mkdir -p /.jbdevcontainer/data/JetBrains/IntelliJIdea2025.1 && \
  mkdir -p /home/node/.config/JetBrains/RemoteDev-IU && \
  chown -R node:node /home/node && \
  chown -R node:node /.jbdevcontainer

# Install java

RUN apt-get update \
  && apt-get install -y --no-install-recommends \
       wget gnupg lsb-release gosu \
  && wget -qO- https://packages.adoptium.net/artifactory/api/gpg/key/public \
       | gpg --dearmor > /usr/share/keyrings/adoptium.gpg \
  && echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] \
       https://packages.adoptium.net/artifactory/deb \
       $(lsb_release -cs) main" \
       > /etc/apt/sources.list.d/adoptium.list \
  && apt-get update \
  && apt-get install -y --no-install-recommends temurin-21-jdk \
  && rm -rf /var/lib/apt/lists/*

RUN java -version

# Install gradle

ARG GRADLE_VERSION=8.14.2
ENV GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION}
ENV PATH=${GRADLE_HOME}/bin:${PATH}

RUN wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -P /tmp \
 && unzip -d /opt/gradle /tmp/gradle-${GRADLE_VERSION}-bin.zip \
 && rm /tmp/gradle-${GRADLE_VERSION}-bin.zip

RUN gradle --version

# Copy and set up open-firewall script
COPY open-firewall.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/open-firewall.sh && \
  echo "node ALL=(root) NOPASSWD: /usr/local/bin/open-firewall.sh" > /etc/sudoers.d/node-open-firewall && \
  chmod 0440 /etc/sudoers.d/node-open-firewall

# Copy the entrypoint.sh
COPY entrypoint.sh /usr/local/bin/
RUN chmod 755 /usr/local/bin/entrypoint.sh && \
  chown node /usr/local/bin/entrypoint.sh

USER node
WORKDIR /workspace
