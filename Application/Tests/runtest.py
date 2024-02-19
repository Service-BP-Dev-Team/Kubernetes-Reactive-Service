import subprocess
import json
import time
from changeenv import buildEnvironment

rootPathIn ="/vagrant/Application/Tests/TestRunIn"
rootPathSpecGAGIncremental="/vagrant/Application/Tests/TestRunIn/incremental"
rootPathSpecGAGNOIncremental="/vagrant/Application/Tests/TestRunIn/no-incremental"
rootPathOut ="/vagrant/Application/Tests/TestRunOut"
deploymentPath1="/".join([rootPathOut,"deployment.yml"])
deploymentPath2="/".join([rootPathOut,"deployment-worker.yml"])
# List of environment variables and their corresponding values
env_variables = {
    'NUMBER_OF_BLOCKS': 8,
 #   'NUMBER_OF_BLOCKS': '1',
    'KUBE_CONTROLLER_NAME': 'java-rest-service:8000',
    'KUBE_NAME': 'java-rest-service:8000',
    'KUBE_WORKER_NAME': 'java-worker-service:8000',
    'NUMBER_OF_CONTROLLER_PODS': 1,
    'NUMBER_OF_WORKER_PODS': 8,
    'WORKER_POD_CAPACITY':100,
    'SPEC_TO_LOAD' : "",
    'MAX_LEN': 100000,
    'SYNC_IN_NOTIFICATION_TIME' : 1,
    'READY_TASK_WAIT_TIME' : 1,
    'INCREMENTAL_EXECUTION':True,
  #  'INCREMENTAL_EXECUTION':True,
    

}
inputSize=1000000
initSize=200000
def runtest(input,init,env):

    number_of_iteration = 10
    # Directory containing the text files to modify
    source_directory = rootPathIn

    # Directory to store the modified files
    destination_directory = rootPathOut

    buildEnvironment(env_variables,source_directory,destination_directory)

    # Command to execute
    commands = ["kubectl delete deployment --all",
                f"kubectl apply -f {deploymentPath1}",
                f"kubectl apply -f {deploymentPath2}"]
    get_pod_command= "kubectl get pods --field-selector=status.phase=Running"

    # Run the commands and capture the output
    for command in commands:
        try:
            output = subprocess.check_output(command, shell=True, universal_newlines=True)
            
        except subprocess.CalledProcessError as e:
            print(f"Error executing command: {e}")

    #wait for the deployment to finish
    time.sleep(60)
    # Run the command to get one pod id 
    try:
        output = subprocess.check_output(get_pod_command, shell=True, universal_newlines=True)
        # Process the output lines
        podId=output.split("\n")[1].split(" ")[0]
        # Access and manipulate the dictionary as needed
        print(podId)
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")

    podCommand="\"curl -X POST java-rest-service:8000/api/service/merge-sort-enhanced/assesment -d \'{\\\"size\\\":";
    podInitCommand=podCommand+f"{initSize}"+"}\'\"";
    podCommand=podCommand+f"{inputSize}"+"}\'\""
    #print(podCommand)
    commantToInit=f"kubectl exec -it {podId} -- /bin/bash -c {podInitCommand}"
    commantToRun=f"kubectl exec -it {podId} -- /bin/bash -c {podCommand}"
    #print(commantToRun)

    # Run the command that assess the execution time 
    try:
        # execute init 4 times
        for i in range(2):
            output = subprocess.check_output(commantToInit, shell=True, universal_newlines=True)
        # the engine is warn : we now execute the desired command
        sumResult=0
        for i in range(number_of_iteration):
            output = subprocess.check_output(commantToRun, shell=True, universal_newlines=True)
            # Process the output lines
            #print(output)
            output_dict = json.loads(output)
            # Access and manipulate the dictionary as needed
            sumResult+=output_dict["duration"]
        return sumResult/number_of_iteration
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")

if __name__ == "__main__":
    print(runtest(inputSize,initSize,env_variables))