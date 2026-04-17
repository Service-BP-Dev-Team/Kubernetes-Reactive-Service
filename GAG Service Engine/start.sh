#!/bin/bash


sudo docker build . -t java-rest 


sudo docker tag java-rest 10.0.0.10:5000/java-rest:v2
sudo docker push 10.0.0.10:5000/java-rest:v2
