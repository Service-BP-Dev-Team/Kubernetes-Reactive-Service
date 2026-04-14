#!/bin/bash
#
# Setup for Node servers

set -euxo pipefail

config_path="/vagrant/configs"

# ------------------------------------------------------------
# Trust registry certificate (SYSTEM + DOCKER) on worker nodes
# ------------------------------------------------------------

CERT_PATH="/vagrant/Application/domain.crt"

if [ -f "$CERT_PATH" ]; then
  echo "Configuring trust for private registry..."

  # 1. System-wide trust (needed for CRI-O / Kubernetes)
  sudo cp $CERT_PATH /usr/local/share/ca-certificates/registry.crt
  sudo update-ca-certificates

  # 2. Docker-specific trust (for docker CLI usage if needed)
  REGISTRY_DIR="/etc/docker/certs.d/${CONTROL_IP}:5000"
  sudo mkdir -p $REGISTRY_DIR
  sudo cp $CERT_PATH $REGISTRY_DIR/ca.crt

  # Restart Docker to apply changes
  sudo systemctl restart docker
fi

# Join cluster (CRI socket already embedded inside join.sh)
/bin/bash $config_path/join.sh -v

sudo -i -u vagrant bash << EOF
whoami
mkdir -p /home/vagrant/.kube
sudo cp -i $config_path/config /home/vagrant/.kube/
sudo chown 1000:1000 /home/vagrant/.kube/config
NODENAME=$(hostname -s)
kubectl label node $(hostname -s) node-role.kubernetes.io/worker=worker
EOF
