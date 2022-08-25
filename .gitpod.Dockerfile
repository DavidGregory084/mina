FROM gitpod/workspace-full

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
	sed -i -e 's/sdkman_auto_answer=false/sdkman_auto_answer=true/g' /home/gitpod/.sdkman/etc/config && \
	sdk install java 18.0.2-tem && \
	wget -q https://packages.microsoft.com/keys/microsoft.asc -O- | sudo apt-key add - && \
	sudo add-apt-repository 'deb [arch=amd64] https://packages.microsoft.com/repos/vscode stable main' && \
	sudo apt update -q && sudo apt install -yq code xvfb"
