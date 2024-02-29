from runtest import runtest
import datetime
import os
env_variables = {
    'NUMBER_OF_BLOCKS': 8,
 #   'NUMBER_OF_BLOCKS': '1',
    'KUBE_CONTROLLER_NAME': 'java-rest-service:8000',
    'KUBE_NAME': 'java-rest-service:8000',
    'KUBE_WORKER_NAME': 'java-worker-service:8000',
    'NUMBER_OF_CONTROLLER_PODS': 1,
    'NUMBER_OF_WORKER_PODS': 4,
    'WORKER_POD_CAPACITY':1000,
    'SPEC_TO_LOAD' : "",
    'MAX_LEN': 100000,
    'SYNC_IN_NOTIFICATION_TIME' : 1,
    'READY_TASK_WAIT_TIME' : 1,
    'INCREMENTAL_EXECUTION':False,
    'VARYING' : "NUMBER_OF_BLOCKS", # possible case are NUMBER_OF_BLOCKS, MAX_LEN and INPUT_SIZE
    "STEP_INCREMENT" : 10,
    "START_AT": 1,
    "STOP_AT" : 1000
  #  'INCREMENTAL_EXECUTION':True,
    

}
max_len =0
if env_variables.get("MAX_LEN",False) and not env_variables.get("VARYING",False):
    max_len=env_variables.get("MAX_LEN")
else:
    max_len=env_variables.get("START_AT")
number_of_workers=env_variables.get("NUMBER_OF_WORKER_PODS")

initSize=min(500000,max_len * number_of_workers*2)

inputSize= 1000000
globalKeys=["VARYING","STEP_INCREMENT","START_AT","STOP_AT"]
running_env = {key:value for key,value in env_variables.items() 
               if not key in globalKeys }


# Get the current date and time
now = datetime.datetime.now()

# Format the date and time components
year = now.year
month = now.month
day = now.day
minutes = now.minute


def perform_test(env,execution_type,global_env,inputSize,initSize):

    env["INCREMENTAL_EXECUTION"]= True if execution_type=="INCREMENTAL" else False

    file_name = os.path.join("Results", f"{execution_type}_{year}_{month}_{day}_{minutes}.txt")
    # Create a new file with the generated name
    head=f"Test results of non incremental execution {year}_{month}_{day}_{minutes}\
                   \n inputSize={inputSize}, initSize={initSize}, number of worker pods = {env.get('NUMBER_OF_WORKER_PODS')}\
                    worker pod capacity = {env.get('WORKER_POD_CAPACITY')} \n"
        
    with open(str(file_name), 'w') as file:
        # Perform test and write in the file
        file.write(head)
        i=global_env.get("START_AT")
        while(i<global_env.get("STOP_AT")):
            running_env["NUMBER_OF_BLOCKS"]=i
            output= runtest(inputSize, initSize,running_env)
            to_print =f"K={i}, R={output} \n"
            print(to_print)
            file.write(to_print)
            i+=global_env.get("STEP_INCREMENT")


if env_variables.get("VARYING",False)=="NUMBER_OF_BLOCKS":
    # Perform non incremental test
    perform_test(running_env,"NON_INCREMENTAL",env_variables,inputSize,initSize)
    # Perform incremental test
    perform_test(running_env,"INCREMENTAL",env_variables,inputSize,initSize)
        
else:
    print(runtest(inputSize, initSize,running_env))
