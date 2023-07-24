FROM gitpod/workspace-full@sha256:4cd82d34678edda8e68cfce0891d4580629f98806f3df94dd8d83572e5d88457

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
	sed -i -e 's/sdkman_auto_answer=false/sdkman_auto_answer=true/g' /home/gitpod/.sdkman/etc/config && \
	sdk install java 17.0.3-tem && \
	wget -q https://packages.microsoft.com/keys/microsoft.asc -O- | sudo apt-key add - && \
	sudo add-apt-repository 'deb [arch=amd64] https://packages.microsoft.com/repos/vscode stable main' && \
	sudo apt update -q && sudo apt install -yq code xvfb"
