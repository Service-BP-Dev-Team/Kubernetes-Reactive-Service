# Prequisites

- Have installed virtual box and vagrant on your local computer.


# Setup the kubernetes cluster using vbox, vagrant and docker

- Step 1 clone the repository using ssh or https
- Step 2 launch the cluster using the command "vagrant up".
- If you have an error concerning the network creation,
  use the following fix (1) create a folder vbox in your /etc/
  (2) inside the folder create a file networks.conf with the following content
  ```
  * 10.0.0.0/8 192.168.0.0/16
  * 2001::/64
  ```
  (3) destroy all machines with "vagrant destroy" and relaunch with "vagrant up"

- Step 3 connect to each machine with "vagrant ssh machine_name" (eg "vagrant ssh master" for the master) and
     install docker using "sh '/vagrant/Custom Scripts/docker_install.sh' "
- Step 4 you can make a first deployment by executing "kubectl apply -f /vagrant/nginx/deployment.yml" in the master machine. 

For more details about the config of vagrant with kubernetes go through this link : [Vagrant+Kubectl+vbox](https://blog.devops.dev/how-to-setup-kubernetes-cluster-with-vagrant-e2c808795840?gi=58dccfb37276)

# Setup a local docker repository

In this section we explain how to setup a local docker repository for fast test of reactive service with kubernetes. First choose the node where you want to add your repository. You can for instance choose the master node. Add the IP address of the selected node in subjectAltName in the openssl.cnf before generating certficates. In this kubernetes cluster set up with vagrant the IP address of the master node is 10.0.0.10.

```
sudo vi /etc/ssl/openssl.cnf

```
Add the following with your VM specific IP address under the section [ v3_ca ] 

```
[ v3_ca ]
subjectAltName=IP:IP_ADDRESS_OF_YOUR_VM

```

Create a local folder which will hold the certificates and that can be referenced by the Docker Registry server

```
mkdir -p /certificates

cd certificates

openssl req \
  -newkey rsa:4096 -nodes -sha256 -keyout domain.key \
  -x509 -days 365 -out domain.crt
  
#Enter all required fields (it could be anything) but please enter your Server IP address when it prompts for -> Common Name (e.g. server FQDN or YOUR name)

Common Name (e.g. server FQDN or YOUR name) []: IP_ADDRESS_OF_YOUR_VM

# Check if the certificates are created in the current directory (certificates)

ls

```

Launch Docker registry using version 2 and referencing the certificates folder for TLS

```

sudo docker run -d -p 5000:5000 --restart=always --name registry \
  -v /certificates:/certificates \
  -e REGISTRY_HTTP_TLS_CERTIFICATE=/certificates/domain.crt \
  -e REGISTRY_HTTP_TLS_KEY=/certificates/domain.key \
  registry:2
  
docker ps
docker logs CONTAINER-ID

#Check & proceed further if there are no errors in the registry container log

```
In order to make all the nodes of your cluster to trust your self signed repository, you have to add your certificates in the list of trusted certificates in each node.
In the node where you have created the certificate (and the docker registry) execute this code log
```
#place the certificate in shared vagrant folder for use in other nodes
mkdir /vagrant/Application
sudo cp domain.crt /vagrant/Application/
# manually force the trust of the certificates
sudo cp /vagrant/Application/domain.crt /usr/local/share/ca-certificates/
sudo update-ca-certificates

```
In the other nodes of your cluster execute the following command 
```
# manually force the trust of the certificates
sudo cp /vagrant/Application/domain.crt /usr/local/share/ca-certificates/
sudo update-ca-certificates

```
To verify our Docker registry, let us pull a small hello-world docker image from Docker-Hub registry, tag it appropriately and try to push it to our local Registry. Fisrt restarts all your nodes, or merely restart all the docker service with :

```
sudo systemctl restart docker
```
 Then try this :
```

docker pull hello-world
docker tag hello-world IP_ADDRESS_OF_YOUR_VM:5000/hello-world

docker push IP_ADDRESS_OF_YOUR_VM:5000/hello-world
```
Note : If you are interested in setting up a local self-signed registry without the use of vagrant, visit this link:
[self signed local docker registry](https://github.com/rchidana/Docker-Private-Registry/)

```
