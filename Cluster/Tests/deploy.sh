#!/bin/bash


# Command 1: Build Docker image
sudo docker build . -t java-rest

# Command 2: Tag Docker image
sudo docker tag java-rest 10.0.0.10:5000/java-rest:v2

# Command 3: Push Docker image
sudo docker push 10.0.0.10:5000/java-rest:v2