# Kubernetes-Reactive-Service

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
     install docker using "sh '/vagrant/Custom Scripts/docher_install.sh' "
- Step 4 you can make a first deployment by executing "kubectl apply -f /vagrant/nginx/deployment.yml" in the master machine. 
