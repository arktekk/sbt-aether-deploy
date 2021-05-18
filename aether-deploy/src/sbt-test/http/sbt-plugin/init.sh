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

  while ! curl -s "http://localhost:$container_port" >/dev/null; do
    sleep 1 # wait for 1 second before check again
  done

  echo "Nexus launched"
else
  echo "Existing sonatype/nexus3 running with id $container_id"
fi

echo "Retrieving generated password"
password=$(docker container exec -i "$container_id" cat /nexus-data/admin.password)

echo "Setting 'maven-releases' layoutPolicy to 'PERMISSIVE'"
maven_releases=$(
  curl -s -X GET \
    -u "admin:$password" \
    -H "accept: application/json" \
    "http://localhost:$container_port/service/rest/v1/repositories/maven/hosted/maven-releases"
)
updated_maven_releases=$(echo "$maven_releases" | jq -c '.maven.layoutPolicy = "PERMISSIVE"')
curl -s -X PUT \
  -u "admin:$password" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d "$updated_maven_releases" \
  "http://localhost:$container_port/service/rest/v1/repositories/maven/hosted/maven-releases"

echo "Setting 'maven-snapshots' layoutPolicy to 'PERMISSIVE'"
maven_snapshots=$(
  curl -s -X GET \
    -u "admin:$password" \
    -H "accept: application/json" \
    "http://localhost:$container_port/service/rest/v1/repositories/maven/hosted/maven-snapshots"
)
updated_maven_snapshots=$(echo "$maven_snapshots" | jq -c '.maven.layoutPolicy = "PERMISSIVE"')
curl -s -X PUT \
  -u "admin:$password" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d "$updated_maven_snapshots" \
  "http://localhost:$container_port/service/rest/v1/repositories/maven/hosted/maven-snapshots"

echo "Generating credentials file ($credentials_file)"
cat >"$credentials_file" <<EOF
realm=Sonatype Nexus Repository Manager
host=localhost
user=admin
password=$password
EOF
