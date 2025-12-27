from runtest import runtest
import datetime
import os
import json
import psutil
import time
env_variables = {
    'NUMBER_OF_BLOCKS': 1,
  #  'NUMBER_OF_BLOCKS': 32,
    'KUBE_CONTROLLER_NAME': 'java-rest-service:8000',
    'KUBE_NAME': 'java-rest-service:8000',
    'KUBE_WORKER_NAME': 'java-worker-service:8000',
    'NUMBER_OF_CONTROLLER_PODS': 1,
    'NUMBER_OF_WORKER_PODS': 1,
    'WORKER_POD_CAPACITY':300,
    'SPEC_TO_LOAD' : "",
    'MAX_LEN': 20000,
    'SYNC_IN_NOTIFICATION_TIME' : 1,
    'READY_TASK_WAIT_TIME' : 1,
    'MAX_CONCURRENT_SERVICE_REQUEST':100,
    'USE_VIRTUAL_THREAD' : True,
    'WORKER_REQUEST_FAIL_DETECT_DURATION':50,
    'VARYING' : 'NUMBER_OF_WORKER_PODS', # possible case are NUMBER_OF_BLOCKS,  WORKER_REQUEST_FAILURE_PROBABILITY, NUMBER_OF_WORKER_PODS, or MAX_LEN
    'WORKER_REQUEST_FAILURE_PROBABILITY':0.0,
    'STEP_INCREMENT' : 1,
    #'STEP_SET': [11719,23438,46875,93750,187500],
    #'STEP_SET': [300000,600000,900000],
    #'STEP_SET': [0.0,0.3],
    'START_AT': 5,
    'STOP_AT' : 5,
    'STEP_GROWTH': "ARITHMETIC", # the value are GEOMETRIC, ARITHMETIC and STEP_SET
    'DO_ONLY_INCREMENTAL_EXECUTION': True,
    'WARMING_INPUT_SIZE': 400000,
    'NUMBER_OF_WARMING':10,
    'NUMBER_OF_ITERATION':60,
    'INPUT_SIZE_START':300000,
    'INPUT_SIZE_GROWTH': "INPUT_SET", # the value are GEOMETRIC and ARITHMETIC and INPUT_SET
    'INPUT_SET': [2400000]
    #'INPUT_SIZE_INCREMENT':300000,
    #'INPUT_SIZE_STOP':3000000, 
    #'INCREMENTAL_EXECUTION':True,
    

}

initSize=env_variables.get("WARMING_INPUT_SIZE")

inputSize= env_variables.get("INPUT_SIZE_START")
globalKeys=["VARYING","STEP_LIST","STEP_INCREMENT","STEP_GROWTH","START_AT","STOP_AT","DO_ONLY_INCREMENTAL_EXECUTION"]
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
        if percent > 0:
           result = True
    
    return result



def read_json_from_file(execution_type, varying, varying_value, directory='Results'):
    # Construct the file prefix based on the provided parameters
    file_prefix = f"{execution_type}_{varying}={varying_value}_"
    print(file_prefix)
    # List to hold the filenames and their corresponding JSON data
    json_data_tuples = []

    # Iterate through the files in the specified directory
    for filename in os.listdir(directory):
        print(filename)
        # Check if the filename starts with the desired prefix and ends with .json
        if filename.startswith(file_prefix) and filename.endswith(".json"):
            with open(os.path.join(directory, filename), 'r') as f:
                # Append filename followed by the JSON data
                json_data_tuples.append(filename)  # Append filename
                json_data_tuples.append(json.load(f))  # Append JSON data

    # Check if there are at least two items in json_data_tuples
    if len(json_data_tuples) < 2:
        print("Not enough matching JSON files found.")
        return None

    # Return a tuple of the first two items (filename and corresponding JSON)
    return (json_data_tuples[0], json_data_tuples[1])


def get_top_duration_entries(array1_entries, array2_entries, n=50):
    """
    Combines entries from two lists, sorts them by duration, 
    and returns the top n entries while maintaining their original order.
    
    Parameters:
    array1_entries (list): List of entries from the first input.
    array2_entries (list): List of entries from the second input.
    n (int): The number of top entries to return.

    Returns:
    list: The sorted list of top n entries.
    """
    # Combine the entries from both lists
    combined_entries = array1_entries + array2_entries

    print("Combined Entries:")
    for idx, entry in enumerate(combined_entries):
        print(f"Entry {idx}: {entry}")

    # Check that all entries are dictionaries and have the 'duration' key
    for entry in combined_entries:
        if not isinstance(entry, dict) or 'duration' not in entry:
            raise TypeError("Each entry must be a dictionary with a 'duration' key.")

    # Sort the combined entries by duration and get the top n
    sorted_entries = sorted(combined_entries, key=lambda x: x["duration"])[:n]

    print("\nSorted Entries:")
    for idx, entry in enumerate(sorted_entries):
        print(f"Entry {idx}: {entry}")

    # Re-arrange combined_entries to maintain original order
    re_arranged_entries = []
    for el in array1_entries:
        if el in sorted_entries:
            re_arranged_entries.append(el)
    
    for el in array2_entries:
        if el in sorted_entries:
            re_arranged_entries.append(el)

    print("\nRe-arranged Entries:")
    for idx, entry in enumerate(re_arranged_entries):
        print(f"Entry {idx}: {entry}")

    return re_arranged_entries

def merge_json_data(json_data1, json_data2):
    # Merging the 'environment' sections (assuming they should be the same)
    merged_environment = json_data1['environment']
    
    # Merging the 'result' sections, keeping values from json_data2
    merged_result = json_data1['result'].copy()  # Start with the first result
    
    # Directly copy from json_data2
    for key, value in json_data2['result'].items():
        if str(key) in json_data1['result'] and key!="redeployment":
            print(key)
            print(value)
            print(json_data1['result'][str(key)])
            top_entries = get_top_duration_entries(json_data1['result'][str(key)],value)
            merged_result[key]=top_entries
        elif key!="redeployment":
            merged_result[key] = value  # Overwrite or add new keys
    
    # Create a new merged JSON object
    merged_json_data = {
        "environment": merged_environment,
        "result": merged_result
    }
    
    return merged_json_data


def perform_test(env,execution_type,global_env):

    env["INCREMENTAL_EXECUTION"]= True if execution_type=="INCREMENTAL" else False
    folder_execution_type="Incremental" if execution_type=="INCREMENTAL" else "NoIncremental"
    
    # Create a new file with the generated name
    geometric = global_env.get("STEP_GROWTH", False)=="GEOMETRIC"
    arithmetic= global_env.get("STEP_GROWTH", False)=="ARITHMETIC"
    is_step_set = global_env.get("STEP_GROWTH", False)=="STEP_SET"
    #when it is a step set
    step_set=global_env.get("STEP_SET",[])
    print(f"the geomertric step growth is set to {geometric}")
    print(f"the step set is set to {is_step_set}")
    varying=""
    destinationDirectory=""
    #when varying number_of_blocks
    if global_env.get("VARYING",False)=="NUMBER_OF_BLOCKS":
        varying = "NUMBER_OF_BLOCKS"
        destinationDirectory="NumberOfBlocks"
    elif global_env.get("VARYING",False)=="NUMBER_OF_WORKER_PODS":
        varying = "NUMBER_OF_WORKER_PODS"
        destinationDirectory="NumberOfWorkers"
    elif global_env.get("VARYING",False)=="MAX_LEN":
        varying = "MAX_LEN"
        destinationDirectory="Maxlen"    
    else:
        varying = "WORKER_REQUEST_FAILURE_PROBABILITY"
        destinationDirectory="Probability"
    i=global_env.get("START_AT") if (arithmetic or geometric) else 0
    # Get the current date and time
    now = datetime.datetime.now()
    #get the last place we were before we left
    currentProgressFilePath=os.path.join("Results",destinationDirectory,folder_execution_type,"stop_merge.json")
    executionGlobalParameterFilePath=os.path.join("Results",destinationDirectory,folder_execution_type,"env_merge.json")
    if os.path.isfile(currentProgressFilePath):
        with open(currentProgressFilePath, "r") as file:
            file_contents = file.read()
            currentProgress=json.loads(file_contents)
            if currentProgress.get("environment",False):
                resuming_value=0
                if geometric:
                    i = currentProgress["environment"][varying]*global_env.get("STEP_INCREMENT")
                    resuming_value=i
                elif arithmetic :
                    i=currentProgress["environment"][varying]+global_env.get("STEP_INCREMENT")
                    resuming_value=i
                else :
                    
                    index=step_set.index(currentProgress["environment"][varying])
                    i=index+1
                    if i<len(step_set):
                        resuming_value=step_set[i]
                    else :
                        resuming_value=-1
                print(f"Resuming execution ! from {varying} = {resuming_value}")
                
                
    else:
        print("starting execution from the beginning")
        #store where we haved started
        now_env_to_store = datetime.datetime.now()
        env_execution_to_store={"environment":global_env,
                         "year":now_env_to_store.year,
                         "month":now_env_to_store.month,
                         "day":now_env_to_store.day,
                         "hour":now_env_to_store.hour,
                         "minutes":now_env_to_store.minute}
        with open(str(executionGlobalParameterFilePath),"w") as current:
            current.write(json.dumps(env_execution_to_store))
    #do the execution
    step_stop = global_env.get("STOP_AT") if (arithmetic or geometric) else (len(step_set)-1)
    while (i <= step_stop):
        if not isBaterryOkay() :
            #we do not continue execution if we do not have enough batery
            return
        
      
     
        # Format the date and time components
        year = now.year
        month = now.month
        day = now.day
        hour = now.hour
        minutes = now.minute
        varying_value=i if (geometric or arithmetic) else step_set[i]

        #this script does not create a new file but merge with existing result
        #run the test and get the result in a file
        target_folder=os.path.join("Results",destinationDirectory,folder_execution_type)
        json_content=read_json_from_file(execution_type,varying,varying_value,target_folder)
        file_name, initial_file_content = json_content
        file_path = os.path.join("Results",destinationDirectory,folder_execution_type,file_name)
        running_env[varying]=varying_value
        result = runtest(running_env) #running the test
        output = {}
        output["environment"]=running_env
        output["result"]=result
        output_to_store=merge_json_data(initial_file_content,output)
        #to_print =f"K={i}, R={output} \n"
        #display the result
        #print(to_print)
        #store the result in a file
        with open(str(file_path),"w") as file:
            file.write(json.dumps(output_to_store))
        #increment for next execution
        if geometric:
            i=i*global_env.get("STEP_INCREMENT")
        elif arithmetic:
            i+=global_env.get("STEP_INCREMENT")
        else :
            #when the step are a list we increment the index
            i=i+1
        now = datetime.datetime.now()
        #store where we are in a file
        #storing where we are is necessary in order to not perform
        # test again from zero after an issue
        currentProgress={"environment":running_env,
                         "year":year,
                         "month":month,
                         "day":day,
                         "hour":hour,
                         "minutes":minutes}
        with open(str(currentProgressFilePath),"w") as current:
            current.write(json.dumps(currentProgress))
        
        #wait 5 minutes before the next execution
        if(i<=step_stop):
            time.sleep(5*60)
        #time.sleep(1)
        # why I make it sleep because I don't want to break my computer
        # my computer is expensive

if env_variables.get("VARYING",False):

    # Perform non incremental test
    if not env_variables.get("DO_ONLY_INCREMENTAL_EXECUTION",False):
        perform_test(running_env,"NON_INCREMENTAL",env_variables)
    # Perform incremental test
    perform_test(running_env,"INCREMENTAL",env_variables)
        
else:
    print(runtest(running_env))

