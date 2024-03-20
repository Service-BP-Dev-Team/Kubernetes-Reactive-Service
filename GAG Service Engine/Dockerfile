FROM ubuntu:22.04
#FROM debian:stable-slim
#FROM ubuntu:stable-slim
#RUN apk add --no-cache curl tar bash procps
RUN apt-get update
RUN apt-get install curl bash procps -y

#install java

#RUN  apt install software-properties-common -y
#RUN  apt install  build-essential git -y
#RUN  apt install openjdk-21-jre -y
RUN  apt install openjdk-21-jdk -y
# Downloading and installing Maven

#install maven
RUN apt install maven -y

WORKDIR /app


COPY . .

#RUN mvn dependency:resolve
#COPY ./Specification/MergeSort/com/local/ ./src/main/java/com/local/
RUN mvn clean package
EXPOSE 8000
COPY ./Specification/MergeSort/ ./target/spec-merge-sort-enhanced/
RUN cd target
#CMD ["/bin/bash"]
#CMD ["java","-jar", "pure-java-rest-api-1.0-SNAPSHOT-jar-with-dependencies.jar"]
CMD ["/bin/bash","-c", "cd target;java -Xmx3g -jar pure-java-rest-api-1.0-SNAPSHOT-jar-with-dependencies.jar > /tmp/output.txt 2> /tmp/error.txt "]
#CMD ["/bin/bash","-c", "java -version; mvn --version"]

