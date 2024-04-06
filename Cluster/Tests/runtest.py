import subprocess
import json
import time
from changeenv import buildEnvironment

rootPathIn ="/vagrant/Tests/TestRunIn"
rootPathOut ="/vagrant/Tests/TestRunOut"
deploymentPath1="/".join([rootPathOut,"deployment.yml"])
deploymentPath2="/".join([rootPathOut,"deployment-worker.yml"])
# List of environment variables and their corresponding values
env_variables = {
    'NUMBER_OF_BLOCKS': 128,
 #   'NUMBER_OF_BLOCKS': '1',
    'KUBE_CONTROLLER_NAME': 'java-rest-service:8000',
    'KUBE_NAME': 'java-rest-service:8000',
    'KUBE_WORKER_NAME': 'java-worker-service:8000',
    'NUMBER_OF_CONTROLLER_PODS': 1,
    'NUMBER_OF_WORKER_PODS': 2,
    'WORKER_POD_CAPACITY':1000,
    'SPEC_TO_LOAD' : "",
    'MAX_LEN': 7813,
    'SYNC_IN_NOTIFICATION_TIME' : 1,
    'READY_TASK_WAIT_TIME' : 1,
  #  'MAXIMUM_THREAD_POOL' : 2,
    'USE_VIRTUAL_THREAD' : True,
    'INCREMENTAL_EXECUTION':True,
    'WORKER_REQUEST_FAILURE_PROBABILITY':0.0,
    'MAX_CONCURRENT_SERVICE_REQUEST':1,
  #  'INCREMENTAL_EXECUTION':True,
    'WORKER_REQUEST_FAIL_DETECT_DURATION':10,
    'WARMING_INPUT_SIZE': 1000000,
    'NUMBER_OF_WARMING':10,
    'NUMNER_OF_ITERATION':20,
    'INPUT_SIZE_START':500000,
    'INPUT_SIZE_INCREMENT':50000,
    'INPUT_SIZE_STOP':500000,
    'INPUT_SIZE_GROWTH': "GEOMETRIC" # the value are GEOMETRIC and ARITHMETIC 
    

}
inputSize=1000000
#inputSize=5001
initSize=min(500000,env_variables.get("MAX_LEN")*env_variables.get("NUMBER_OF_WORKER_PODS"))
initSize = max(inputSize//4,initSize)

def deploy(env):
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
    time.sleep(120)
    # Run the command to get one pod id 
    try:
        output = subprocess.check_output(get_pod_command, shell=True, universal_newlines=True)
        # Process the output lines
        podId=output.split("\n")[1].split(" ")[0]
        return podId
        # Access and manipulate the dictionary as needed
        if __name__ == "__main__":
            print(podId)
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")

def warming(podId,init,number_of_warming):
        
    podCommandBase="\"curl -X POST java-rest-service:8000/api/service/merge-sort-enhanced/assesment -d \'{\\\"size\\\":";
    podInitCommand=podCommandBase+f"{init}"+"}\'\""
    
    #if __name__ == "__main__":
        #print(podCommand)
    commantToInit=f"kubectl exec -it {podId} -- /bin/bash -c {podInitCommand}"
            # execute init 4 times
    try:
        for i in range(number_of_warming):
            output = subprocess.check_output(commantToInit, shell=True, universal_newlines=True)
            if __name__ == "__main__":
                print(f"warming {(i+1)}/{number_of_warming}")
            # the engine is warn : we now execute the desired command
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")

def runtest(env):
    input_start=env.get("INPUT_SIZE_START")
    input_stop=env.get("INPUT_SIZE_STOP")
    input_increment=env.get("INPUT_SIZE_INCREMENT")
    init=env.get("WARMING_INPUT_SIZE")
    number_of_warming = env.get("NUMBER_OF_WARMING",10)
    number_of_iteration = env.get("NUMBER_OF_ITERATION",20)
    # Directory containing the text files to modify
    source_directory = rootPathIn

    # Directory to store the modified files
    destination_directory = rootPathOut

    buildEnvironment(env,source_directory,destination_directory)
    
    #deploy
    podId=deploy(env)
    #warm
    warming(podId,init,number_of_warming)
    podCommandBase="\"curl -X POST java-rest-service:8000/api/service/merge-sort-enhanced/assesment -d \'{\\\"size\\\":"; 
    #print(commantToRun)

    # Run the command that assess the execution time 
    try:
        sumResult=0
        result={'redeployment':0}
        input=input_start
        podCommand=podCommandBase+f"{input}"+"}\'\""
        commantToRun=f"kubectl exec -it {podId} -- /bin/bash -c {podCommand}"
        geometric = env.get("INPUT_SIZE_GROWTH", False)=="GEOMETRIC"
        while input<=input_stop:
            element_of_result=[]
            print(f"performing execution for input {input}")
            i=0    
            while i < number_of_iteration:
                output = subprocess.check_output(commantToRun, shell=True, universal_newlines=True)
                # Process the output lines
                #print(output)
                output_dict = json.loads(output)
                # Access and manipulate the dictionary as needed
                duration=output_dict.get("duration",0)
                if not duration ==0 :
                    duration = output_dict["duration"]
                    statistics = output_dict["additionnalExecutionInformation"]
                    time.sleep(10)
                    # I make a sleep because I don't want to break my computer
                    if __name__ == "__main__":
                        print(f"{(i+1)} / {number_of_iteration} : ")
                        print(f"duration -> {duration}")
                        print(f"statistics -> {statistics}")
                    sumResult+=duration
                    element_of_result.append({"duration":duration,"statistics":statistics})
                    i=i+1
                else:
                    result['redeployment']=result["redeployment"]+1
                    #redeploy
                    podId=deploy(env)
                    #warm
                    warming(podId,init,number_of_warming)
                    commantToRun=f"kubectl exec -it {podId} -- /bin/bash -c {podCommand}"
                    # in case of error we do not want to lose all logs statistics
                    # we redeploy and resume from where we were

            #add the result of the input to the global result
            result[input]=element_of_result
            #update input
            if geometric:
                input=input*input_increment
            else:
                input+=input_increment
            #update command to run
            podCommand=podCommandBase+f"{input}"+"}\'\""
            commantToRun=f"kubectl exec -it {podId} -- /bin/bash -c {podCommand}"
        return result
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")

if __name__ == "__main__":

    result = runtest(env_variables)
    if len(result)==1:
        element_result=result.get([el for el in result ][0])
        print(sum( el["duration"] for el in element_result )/len(element_result))