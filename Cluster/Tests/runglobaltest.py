from runtest import runtest
import datetime
import os
import json
import psutil
import time
import argparse

# ---------------- DEFAULT ENV ----------------
env_variables = {
    "NUMBER_OF_BLOCKS": 1,
    "KUBE_CONTROLLER_NAME": "java-rest-service:8000",
    "KUBE_NAME": "java-rest-service:8000",
    "KUBE_WORKER_NAME": "java-worker-service:8000",
    "NUMBER_OF_CONTROLLER_PODS": 1,
    "NUMBER_OF_WORKER_PODS": 1,
    "WORKER_POD_CAPACITY": 300,
    "SPEC_TO_LOAD": "",
    "MAX_LEN": 20000,
    "SYNC_IN_NOTIFICATION_TIME": 1,
    "READY_TASK_WAIT_TIME": 1,
    "MAX_CONCURRENT_SERVICE_REQUEST": 100,
    "USE_VIRTUAL_THREAD": True,
    "WORKER_REQUEST_FAIL_DETECT_DURATION": 50,
    "VARYING": "NUMBER_OF_WORKER_PODS",
    "WORKER_REQUEST_FAILURE_PROBABILITY": 0.0,
    "STEP_INCREMENT": 1,
    "START_AT": 1,
    "STOP_AT": 6,
    "STEP_GROWTH": "ARITHMETIC",
    "DO_ONLY_INCREMENTAL_EXECUTION": True,
    "WARMING_INPUT_SIZE": 500000,
    "NUMBER_OF_WARMING": 10,
    "NUMBER_OF_ITERATION": 50,
    "INPUT_SIZE_START": 300000,
    "INPUT_SIZE_INCREMENT": 300000,
    "INPUT_SIZE_STOP": 3000000,
    "INPUT_SIZE_GROWTH": "ARITHMETIC"
}

# ---------------- ARGUMENT PARSING ----------------
parser = argparse.ArgumentParser()
parser.add_argument("--env-file", type=str, help="Path to JSON environment file")
args = parser.parse_args()

# ---------------- LOAD EXTERNAL ENV ----------------
if args.env_file:
    if not os.path.isfile(args.env_file):
        raise FileNotFoundError(f"Env file not found: {args.env_file}")
    
    with open(args.env_file, "r") as f:
        data = json.load(f)
        
        if "environment" not in data:
            raise ValueError("JSON file must contain an 'environment' field")
        
        print(f"[INFO] Loading environment from {args.env_file}")
        env_variables = data["environment"]  # FULL override

# ---------------- DERIVED VARIABLES ----------------
globalKeys=["VARYING","STEP_LIST","STEP_INCREMENT","STEP_GROWTH","START_AT","STOP_AT","DO_ONLY_INCREMENTAL_EXECUTION"]

running_env = {
    key: value for key, value in env_variables.items()
    if key not in globalKeys
}

initSize = env_variables.get("WARMING_INPUT_SIZE")
inputSize = env_variables.get("INPUT_SIZE_START")


# ---------------- BATTERY CHECK ----------------
def isBaterryOkay():
    battery = psutil.sensors_battery()
    result = False
    if battery is None:
        return False
    else:
        plugged = battery.power_plugged
        percent = battery.percent
        if percent > 75:
            result=True

    if plugged:
        print("The computer is plugged in.")
        if percent > 30:
           result = True
    
    return result


# ---------------- TEST EXECUTION ----------------
def perform_test(env,execution_type,global_env):

    env["INCREMENTAL_EXECUTION"]= True if execution_type=="INCREMENTAL" else False
    folder_execution_type="Incremental" if execution_type=="INCREMENTAL" else "NoIncremental"
    
    geometric = global_env.get("STEP_GROWTH", False)=="GEOMETRIC"
    arithmetic= global_env.get("STEP_GROWTH", False)=="ARITHMETIC"
    is_step_set = global_env.get("STEP_GROWTH", False)=="STEP_SET"

    step_set=global_env.get("STEP_SET",[])
    print(f"the geomertric step growth is set to {geometric}")
    print(f"the step set is set to {is_step_set}")

    varying=""
    destinationDirectory=""

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

    now = datetime.datetime.now()

    currentProgressFilePath=os.path.join("Results",destinationDirectory,folder_execution_type,"stop.json")
    executionGlobalParameterFilePath=os.path.join("Results",destinationDirectory,folder_execution_type,"env.json")

    if os.path.isfile(currentProgressFilePath):
        with open(currentProgressFilePath, "r") as file:
            currentProgress=json.load(file)

            if currentProgress.get("environment",False):
                if geometric:
                    i = currentProgress["environment"][varying]*global_env.get("STEP_INCREMENT")
                elif arithmetic:
                    i=currentProgress["environment"][varying]+global_env.get("STEP_INCREMENT")
                else:
                    index=step_set.index(currentProgress["environment"][varying])
                    i=index+1

                print(f"Resuming execution from {varying} = {i}")
    else:
        print("starting execution from the beginning")

        now_env_to_store = datetime.datetime.now()
        env_execution_to_store={
            "environment":global_env,
            "year":now_env_to_store.year,
            "month":now_env_to_store.month,
            "day":now_env_to_store.day,
            "hour":now_env_to_store.hour,
            "minutes":now_env_to_store.minute
        }

        os.makedirs(os.path.dirname(executionGlobalParameterFilePath), exist_ok=True)

        with open(executionGlobalParameterFilePath,"w") as current:
            json.dump(env_execution_to_store, current)

    step_stop = global_env.get("STOP_AT") if (arithmetic or geometric) else (len(step_set)-1)

    while (i <= step_stop):
        if not isBaterryOkay():
            return
        
        now = datetime.datetime.now()

        varying_value=i if (geometric or arithmetic) else step_set[i]

        file_name =f"{execution_type}_{varying}={varying_value}_{now.year}_{now.month}_{now.day}_{now.hour}_{now.minute}.json"
        file_path = os.path.join("Results",destinationDirectory,folder_execution_type,file_name)

        running_env[varying]=varying_value

        result = runtest(running_env)

        output = {
            "environment":running_env,
            "result":result
        }

        os.makedirs(os.path.dirname(file_path), exist_ok=True)

        with open(file_path,"w") as file:
            json.dump(output, file)

        if geometric:
            i=i*global_env.get("STEP_INCREMENT")
        elif arithmetic:
            i+=global_env.get("STEP_INCREMENT")
        else:
            i=i+1

        currentProgress={
            "environment":running_env,
            "year":now.year,
            "month":now.month,
            "day":now.day,
            "hour":now.hour,
            "minutes":now.minute
        }

        with open(currentProgressFilePath,"w") as current:
            json.dump(currentProgress, current)

        time.sleep(5*60)


# ---------------- MAIN ----------------
if env_variables.get("VARYING",False):

    if not env_variables.get("DO_ONLY_INCREMENTAL_EXECUTION",False):
        perform_test(running_env,"NON_INCREMENTAL",env_variables)

    perform_test(running_env,"INCREMENTAL",env_variables)

else:
    print(runtest(running_env))
