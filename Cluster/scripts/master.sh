#!/bin/bash
#
# Setup for Control Plane (Master) servers

set -euxo pipefail

NODENAME=$(hostname -s)

# Explicitly tell kubeadm to use CRI-O runtime
sudo kubeadm config images pull --cri-socket unix:///var/run/crio/crio.sock

echo "Preflight Check Passed: Downloaded All Required Images"

sudo kubeadm init \
  --cri-socket unix:///var/run/crio/crio.sock \
  --apiserver-advertise-address=$CONTROL_IP \
  --apiserver-cert-extra-sans=$CONTROL_IP \
  --pod-network-cidr=$POD_CIDR \
  --service-cidr=$SERVICE_CIDR \
  --node-name "$NODENAME" \
  --ignore-preflight-errors Swap

mkdir -p "$HOME"/.kube
sudo cp -i /etc/kubernetes/admin.conf "$HOME"/.kube/config
sudo chown "$(id -u)":"$(id -g)" "$HOME"/.kube/config

# Save Configs to shared /Vagrant location

# For Vagrant re-runs, check if there is existing configs in the location and delete it for saving new configuration.

config_path="/vagrant/configs"

if [ -d $config_path ]; then
  rm -f $config_path/*
else
  mkdir -p $config_path
fi

cp -i /etc/kubernetes/admin.conf $config_path/config
touch $config_path/join.sh
chmod +x $config_path/join.sh

kubeadm token create --print-join-command | \
sed 's|kubeadm join|kubeadm join --cri-socket unix:///var/run/crio/crio.sock|' \
> $config_path/join.sh

# Install Calico Network Plugin

curl https://raw.githubusercontent.com/projectcalico/calico/v${CALICO_VERSION}/manifests/calico.yaml -O

kubectl apply -f calico.yaml

sudo -i -u vagrant bash << EOF
whoami
mkdir -p /home/vagrant/.kube
sudo cp -i $config_path/config /home/vagrant/.kube/
sudo chown 1000:1000 /home/vagrant/.kube/config
EOF


# ------------------------------------------------------------
# Generate self-signed certificate WITH SubjectAltName (SAN)
# ------------------------------------------------------------
# Docker requires SAN (Subject Alternative Name), not just CN

CERT_DIR="/certificates"
mkdir -p $CERT_DIR
cd $CERT_DIR

# Always regenerate certificate to avoid stale/invalid cert issues
echo "Generating self-signed certificate with SAN..."

rm -f domain.crt domain.key san.cnf || true

cat > san.cnf <<EOF
[req]
default_bits = 4096
prompt = no
default_md = sha256
distinguished_name = dn
req_extensions = req_ext

[dn]
CN = $CONTROL_IP

[req_ext]
subjectAltName = @alt_names

[alt_names]
IP.1 = $CONTROL_IP
EOF

sudo openssl req -x509 -nodes -days 365 \
  -newkey rsa:4096 \
  -keyout domain.key \
  -out domain.crt \
  -config san.cnf \
  -extensions req_ext

# ------------------------------------------------------------
# Ensure registry uses the latest certificate
# ------------------------------------------------------------

# Remove old container if it exists (avoids stale TLS cert issues)
if [ "$(docker ps -aq -f name=registry)" != "" ]; then
  echo "Removing old registry container..."
  sudo docker rm -f registry
fi

echo "Starting local Docker registry..."
sudo docker run -d -p 5000:5000 --restart=always --name registry \
  -v /certificates:/certificates \
  -e REGISTRY_HTTP_TLS_CERTIFICATE=/certificates/domain.crt \
  -e REGISTRY_HTTP_TLS_KEY=/certificates/domain.key \
  registry:2

# ------------------------------------------------------------
# Trust certificate (SYSTEM + DOCKER) on master node
# ------------------------------------------------------------

# 1. System-wide trust (needed for Kubernetes / CRI-O / curl)
sudo cp domain.crt /usr/local/share/ca-certificates/registry.crt
sudo update-ca-certificates

# 2. Docker-specific trust (required for docker push/pull)
REGISTRY_DIR="/etc/docker/certs.d/${CONTROL_IP}:5000"
sudo mkdir -p $REGISTRY_DIR
sudo cp domain.crt $REGISTRY_DIR/ca.crt

# Restart Docker to apply changes
sudo systemctl restart docker

# ------------------------------------------------------------
# 8. (Optional) Push a test image to the registry
# ------------------------------------------------------------
# This verifies that the registry is working correctly

echo "Pushing test image to local registry..."
sleep 10  # give registry time to start

sudo docker pull hello-world
sudo docker tag hello-world ${CONTROL_IP}:5000/hello-world
sudo docker push ${CONTROL_IP}:5000/hello-world

# ------------------------------------------------------------
# Install Python environment for test execution
# ------------------------------------------------------------

echo "Installing Python dependencies..."

sudo apt-get update
sudo apt-get install -y python3 python3-pip

# Install required Python packages
pip3 install --no-cache-dir psutil