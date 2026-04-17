## Kubernetes-compatible java rest framework for assessment and deployment of distributed service-based application

This is a framework for the deployment of distributed rest services using GAG (Guarded Attribute Grammars), Java and Docker. The application is developped in Java 11 with 
[`jdk.httpserver`](https://docs.oracle.com/javase/10/docs/api/com/sun/net/httpserver/package-summary.html) module 
and a few additional Java libraries (like [vavr](http://www.vavr.io/), [lombok](https://projectlombok.org/))..
The application consist of a java server that allows to execute the rest services of a GAG specification on a distributed conatiner-based architecture. By doing so, such services can be deploy in a container orchestrator such as kubernetes.

## Example of distibuted services
The framework provide a yaml syntax to define both service interfaces and service specifications. The interface of a service defines the list of expected inputs, and the list of expected outputs after providing the inputs. The term "expected inputs" means that the services are incremental and lazy. More precisely, a service can compute some output even though some inputs might be still missing. In fact, a output is computed as soon as all the inputs it depends on are provided. This behavior is particularly suitable for distributed architecture where inputs may come from several sources (other pending services). Hence it allows to avoid latencies that the waiting of all the inputs may cause. 
Let's consider for instance the ex

## Package list
The project contains 10 packages, each package has a readme describing in more details its purpose and implentation. The package list is the following :
- *com.comsulner.app*. This package contains the project launcher class named com.comsulner.app.Application. It serves to launch the application 
- *com.consulner.app.api*. This package contains constant and utility classes to handle http communication.
-  *com.consulner.app.api.mergesort*. This package contains controllers that execute the merge sort in a distributed manner using json as the communication format.
-  *com.consulner.app.errors*. This packahe contains utility classes to handle http communication errors.
-  *com.local*. This package contains local java methods that are used in the rest service specifications.
-  *com.reactive.service.api*. This package contains controllers to handle the execution of the distributed rest services.
-  *com.reactive.service.model.configuration*. This package contains the classes defining the runtime objects used when executing a service specification.
-  *com.reactive.service.model.specification*. This package contains classes that allow to represent a service specification in memory.
-  *com.reactive.service.parser*. This package allows to parse yaml specification of services. It allows to parse a specification written in a yaml file and transform it to an in memory java representation using the classes of the package *com.reactive.service.model.specification*.
-  *com.reactive.service.util*. This package contains utility function and algorithm use in the execution of a service.      

## Launch the framework.
In order to launch an application build from this framework, you have to complile the docker image and run it using *docker run* or a *kubernetes deployment*. Here are the command to build the image and push into a docker repository

```
docker build . -t java-rest-service
sudo docker tag java-rest-service <docker-repository>/java-rest-service:v1
sudo docker push <docker-repository>/java-rest-service:v1
```
After pushing the image you can run it with docker or kubernetes. Here is an example of deployment file for kubernetes :
```yaml
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-rest
  namespace: default
spec:
  selector:
    matchLabels:
      app: java-rest
  replicas: 4 
  template:
    metadata:
      labels:
        app: java-rest
    spec:
      containers:
      - image: <docker-repository>/java-rest-service:v1
        imagePullPolicy: Always
        name: java-rest
        ports:
        - containerPort: 8000
---
apiVersion: v1
kind: Service
metadata:
  name: java-rest-service
spec:
  selector:
    app: java-rest
  type: LoadBalancer
  ports:
    - port: 8000
      targetPort: 8000
      nodePort: 31000
```
The deployment above can be applied in kubernetes using the following command :
```
kubectl apply -f <path-of-deployment> 
```
