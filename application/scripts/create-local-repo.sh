#!/bin/bash
# Script to create local repositories.
sudo mkdir -p /export/bfabric/data/b-fabric-internal-repo
sudo mkdir -p /export/bfabric/data/b-fabric-external-repo
sudo mkdir -p /export/bfabric/data/tmp

sudo chown -R $(id -u):$(id -g) /export/bfabric/data

exit 0

