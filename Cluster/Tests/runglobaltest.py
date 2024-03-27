from runtest import runtest
import datetime
import os
import json
import psutil
import time
env_variables = {
    'NUMBER_OF_BLOCKS': 8,
 #   'NUMBER_OF_BLOCKS': '1',
    'KUBE_CONTROLLER_NAME': 'java-rest-service:8000',
    'KUBE_NAME': 'java-rest-service:8000',
    'KUBE_WORKER_NAME': 'java-worker-service:8000',
    'NUMBER_OF_CONTROLLER_PODS': 1,
    'NUMBER_OF_WORKER_PODS': 2,
    'WORKER_POD_CAPACITY':1500,
    'SPEC_TO_LOAD' : "",
    'MAX_LEN': 7813,
    'SYNC_IN_NOTIFICATION_TIME' : 1,
    'READY_TASK_WAIT_TIME' : 1,
    'INCREMENTAL_EXECUTION':False,
    'MAX_CONCURRENT_SERVICE_REQUEST':1,
    'USE_VIRTUAL_THREAD' : True,
    'WORKER_REQUEST_FAIL_DETECT_DURATION':20,
    'VARYING' : "NUMBER_OF_BLOCKS", # possible case are NUMBER_OF_BLOCKS, or WORKER_REQUEST_FAILURE_PROBABILITY
    "STEP_INCREMENT" : 1,
    "WORKER_REQUEST_FAILURE_PROBABILITY":0.0,
    "START_AT": 1,
    "STOP_AT" : 2,
    "INPUT_SIZE": 1000000
  #  'INCREMENTAL_EXECUTION':True,
    

}
max_len =0
if env_variables.get("MAX_LEN",False) and not env_variables.get("VARYING",False):
    max_len=env_variables.get("MAX_LEN")
else:
    max_len=env_variables.get("START_AT")
number_of_workers=env_variables.get("NUMBER_OF_WORKER_PODS")

initSize=max(500000,max_len * number_of_workers)

inputSize= env_variables.get("INPUT_SIZE")
globalKeys=["VARYING","STEP_INCREMENT","START_AT","STOP_AT"]
running_env = {key:value for key,value in env_variables.items() 
               if not key in globalKeys }




def isBaterryOkay():
    battery = psutil.sensors_battery()
    result = False
    if battery is None:
        return False
    else:
        plugged = battery.power_plugged
        percent = battery.percent
        #print(f"Battery level: {percent}%")
        if percent > 75:
            result=True


    if plugged:
        print("The computer is plugged in.")
        if percent > 65:
           result = True
    
    return result


def perform_test(env,execution_type,global_env,inputSize,initSize,number_of_warming,number_of_iteration):

    env["INCREMENTAL_EXECUTION"]= True if execution_type=="INCREMENTAL" else False
    folder_execution_type="Incremental" if execution_type=="INCREMENTAL" else "NoIncremental"
    
    # Create a new file with the generated name
    
    varying=""
    destinationDirectory=""
    #when varying number_of_blocks
    if global_env.get("VARYING",False)=="NUMBER_OF_BLOCKS":
        varying = "NUMBER_OF_BLOCKS"
        destinationDirectory="NumberOfBlocks"
    else:
        varying = "WORKER_REQUEST_FAILURE_PROBABILITY"
        destinationDirectory="Probability"
    i=global_env.get("START_AT")
    # Get the current date and time
    now = datetime.datetime.now()
    #get the last place we were before we left
    currentProgressFilePath=os.path.join("Results",destinationDirectory,folder_execution_type,"stop.txt")
        
    if os.path.isfile(currentProgressFilePath):
        with open(currentProgressFilePath, "r") as file:
            file_contents = file.read()
            currentProgress=json.loads(file_contents)
            if currentProgress.get("environment",False):
                i=currentProgress["environment"][varying]+global_env.get("STEP_INCREMENT")
                print(f"Resuming execution ! from {varying} = {i}")
                
                
    else:
        print("starting execution from the beginning")

    #do the execution
    while (i <= global_env.get("STOP_AT")):
        if not isBaterryOkay() :
            #we do not continue execution if we do not have enough batery
            return
        
      
     
        # Format the date and time components
        year = now.year
        month = now.month
        day = now.day
        minutes = now.minute
        #run the test and get the result in a file
        file_name =f"{execution_type}_{year}_{month}_{day}_{minutes}.txt"
        file_path = os.path.join("Results",destinationDirectory,folder_execution_type,file_name)
        running_env[varying]=i
        result = runtest(inputSize, initSize,running_env,number_of_warming,number_of_iteration)
        output = {}
        output["environment"]=running_env
        output["result"]=result
        to_print =f"K={i}, R={output} \n"
        #display the result
        print(to_print)
        #store the result in a file
        with open(str(file_path),"w") as file:
            file.write(json.dumps(output))
        #increment for next execution
        i+=global_env.get("STEP_INCREMENT")
        now = datetime.datetime.now()
        #store where we are in a file
        #storing where we are is necessary in order to not perform
        # test again from zero after an issue
        currentProgress={"environment":running_env,
                         "year":year,
                         "month":month,
                         "day":day,
                         "minutes":minutes}
        with open(str(currentProgressFilePath),"w") as current:
            current.write(json.dumps(currentProgress))
        
        #wait 5 minutes before the next execution
        #time.sleep(5*60)
        time.sleep(1)
        # why I make it sleep because I don't want to break my computer
        # my computer is expensive

if env_variables.get("VARYING",False):

    number_of_iteration = 20

    number_of_warming = 10
    # Perform non incremental test
    perform_test(running_env,"NON_INCREMENTAL",env_variables,inputSize,initSize,number_of_warming,number_of_iteration)
    # Perform incremental test
    perform_test(running_env,"INCREMENTAL",env_variables,inputSize,initSize,number_of_warming,number_of_iteration)
        
else:
    print(runtest(inputSize, initSize,running_env,10,20))


