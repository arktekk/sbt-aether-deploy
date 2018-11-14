#!/bin/bash

echo "Waiting nexus to launch on 19999..."

while ! curl -s http://localhost:19999 > /dev/null; do
  sleep 1 # wait for 1 second before check again
done

echo "Nexus launched"
