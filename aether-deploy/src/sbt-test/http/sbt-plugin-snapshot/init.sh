#!/bin/bash

set -eu o pipefail

credentials_file="sonatype.credentials"
container_image="dzikoysk/reposilite:3.5.23"
container_name="reposilite"
container_port="19999"

container_id=$(docker ps -q --filter "name=$container_name")

if [ -z "$container_id" ]; then
  echo "Launching dzikoysk/reposilite on port $container_port"
  docker run -d -e "REPOSILITE_OPTS=--token admin:secret" --rm -p "$container_port:8080" --name "$container_name" $container_image
  container_id=$(docker ps -q --filter "name=$container_name")
  echo "Waiting reposilite to launch on $container_port..."

  while ! curl -s "http://localhost:$container_port" > /dev/null; do
    sleep 1 # wait for 1 second before check again
  done

  echo "Reposilite launched"
else
  echo "Existing Reposilite running with id $container_id"
fi

echo "Generating credentials file ($credentials_file)"
cat > "$credentials_file" << EOF
realm=Reposilite
host=localhost
user=admin
password=secret
EOF
