#!/bin/bash

set -eu o pipefail

credentials_file="sonatype.credentials"
container_image="sonatype/nexus3"
container_name="nexus"
container_port="19999"

container_id=$(docker ps -q --filter "name=$container_name")

if [ -z "$container_id" ]; then
  echo "Launching sonatype/nexus3 on port $container_port"
  docker run -d --rm -p "$container_port":8081 --name "$container_name" $container_image
  container_id=$(docker ps -q --filter "name=$container_name")
  echo "Waiting nexus to launch on $container_port..."

  while ! curl -s "http://localhost:$container_port" > /dev/null; do
    sleep 1 # wait for 1 second before check again
  done

  echo "Nexus launched"
else
  echo "Existing sonatype/nexus3 running with id $container_id"
fi

echo "Retrieving generated password"
password=$(docker container exec -i "$container_id" cat /nexus-data/admin.password)

echo "Generating credentials file ($credentials_file)"
cat > "$credentials_file" << EOF
realm=Sonatype Nexus Repository Manager
host=localhost
user=admin
password=$password
EOF
