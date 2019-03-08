docker build -t wheel-agent .
docker tag wheel-agent:latest registry.cloud.nikio.io/wheel/wheel-agent:latest
docker push registry.cloud.nikio.io/wheel/wheel-agent:latest