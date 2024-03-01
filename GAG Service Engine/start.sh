#!/bin/bash

# Source folder name
source_folder=".."

folder_name="/Specification"

# Destination folder name
destination_folder="."

# Delete the folder if it exists in the source folder
if [ -d "$destination_folder/$folder_name" ]; then
    rm -rf "$destination_folder/$folder_name"
fi

# Copy the folder
cp -r "$source_folder/$folder_name" "$destination_folder/$folder_name"

#perform some operation

sudo docker build . -t java-rest 

#delete the folder after doing those operation
rm -rf "$destination_folder/$folder_name"

sudo docker tag java-rest 10.0.0.10:5000/java-rest:v2
sudo docker push 10.0.0.10:5000/java-rest:v2
