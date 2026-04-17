from runtest import runtest
import datetime
import os
import json
import psutil
import time
import argparse

# ---------------- DEFAULT ENV (from your JSON) ----------------
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
    "INPUT_SIZE_GROWTH": "ARITHMETIC",

    # --------- Some other parameters (kept as comments) ---------
    # "STEP_SET": [93750],
    # "INCREMENTAL_EXECUTION": True,
}

# ---------------- LOAD EXTERNAL JSON ----------------
def load_env_from_file(file_path):
    with open(file_path, "r") as f:
        data = json.load(f)
        return data.get("environment", data)

parser = argparse.ArgumentParser()
parser.add_argument("--env", help="Path to JSON env file", required=False)
args = parser.parse_args()

if args.env:
    print(f"Loading environment from {args.env}")
    file_env = load_env_from_file(args.env)
    env_variables.update(file_env)

# ---------------- INIT ----------------
initSize = env_variables.get("WARMING_INPUT_SIZE")
inputSize = env_variables.get("INPUT_SIZE_START")

globalKeys = ["VARYING","STEP_LIST","STEP_INCREMENT","STEP_GROWTH","START_AT","STOP_AT","DO_ONLY_INCREMENTAL_EXECUTION"]

running_env = {
    key: value for key, value in env_variables.items()
    if key not in globalKeys
}

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
            result = True

    if plugged:
        print("The computer is plugged in.")
        if percent > 0:
            result = True

    return result

# ---------------- JSON READER ----------------
def read_json_from_file(execution_type, varying, varying_value, directory='Results'):
    file_prefix = f"{execution_type}_{varying}={varying_value}_"
    json_data_tuples = []

    for filename in os.listdir(directory):
        if filename.startswith(file_prefix) and filename.endswith(".json"):
            with open(os.path.join(directory, filename), 'r') as f:
                json_data_tuples.append(filename)
                json_data_tuples.append(json.load(f))

    if len(json_data_tuples) < 2:
        print("Not enough matching JSON files found.")
        return None

    return (json_data_tuples[0], json_data_tuples[1])

# ---------------- MERGE ----------------
def merge_json_data(json_data1, json_data2):
    merged_environment = json_data1['environment']
    merged_result = json_data1['result'].copy()

    for key, value in json_data2['result'].items():
        if key != "redeployment":
            merged_result[key] = value

    return {
        "environment": merged_environment,
        "result": merged_result
    }

# ---------------- CORE EXECUTION ----------------
def perform_test(env, execution_type, global_env):

    env["INCREMENTAL_EXECUTION"] = (execution_type == "INCREMENTAL")
    folder_execution_type = "Incremental" if execution_type == "INCREMENTAL" else "NoIncremental"

    geometric = global_env.get("STEP_GROWTH") == "GEOMETRIC"
    arithmetic = global_env.get("STEP_GROWTH") == "ARITHMETIC"
    step_set = global_env.get("STEP_SET", [])

    if global_env.get("VARYING") == "NUMBER_OF_BLOCKS":
        varying, destinationDirectory = "NUMBER_OF_BLOCKS", "NumberOfBlocks"
    elif global_env.get("VARYING") == "NUMBER_OF_WORKER_PODS":
        varying, destinationDirectory = "NUMBER_OF_WORKER_PODS", "NumberOfWorkers"
    elif global_env.get("VARYING") == "MAX_LEN":
        varying, destinationDirectory = "MAX_LEN", "Maxlen"
    else:
        varying, destinationDirectory = "WORKER_REQUEST_FAILURE_PROBABILITY", "Probability"

    i = global_env.get("START_AT") if (arithmetic or geometric) else 0
    now = datetime.datetime.now()

    currentProgressFilePath = os.path.join("Results", destinationDirectory, folder_execution_type, "stop_merge.json")
    executionGlobalParameterFilePath = os.path.join("Results", destinationDirectory, folder_execution_type, "env_merge.json")

    if not os.path.isfile(currentProgressFilePath):
        print("starting execution from the beginning")
        with open(executionGlobalParameterFilePath, "w") as current:
            current.write(json.dumps({"environment": global_env}))

    step_stop = global_env.get("STOP_AT") if (arithmetic or geometric) else (len(step_set) - 1)

    while (i <= step_stop):

        if not isBaterryOkay():
            return

        varying_value = i if (arithmetic or geometric) else step_set[i]

        target_folder = os.path.join("Results", destinationDirectory, folder_execution_type)
        json_content = read_json_from_file(execution_type, varying, varying_value, target_folder)

        if not json_content:
            print("Skipping due to missing file")
            return

        file_name, initial_file_content = json_content
        file_path = os.path.join(target_folder, file_name)

        running_env[varying] = varying_value
        result = runtest(running_env)

        output = {
            "environment": running_env,
            "result": result
        }

        output_to_store = merge_json_data(initial_file_content, output)

        with open(file_path, "w") as file:
            file.write(json.dumps(output_to_store))

        if geometric:
            i *= global_env.get("STEP_INCREMENT")
        elif arithmetic:
            i += global_env.get("STEP_INCREMENT")
        else:
            i += 1

        with open(currentProgressFilePath, "w") as current:
            current.write(json.dumps({"environment": running_env}))

        if i <= step_stop:
            time.sleep(5 * 60)

# ---------------- MAIN ----------------
if env_variables.get("VARYING"):

    if not env_variables.get("DO_ONLY_INCREMENTAL_EXECUTION"):
        perform_test(running_env, "NON_INCREMENTAL", env_variables)

    perform_test(running_env, "INCREMENTAL", env_variables)

else:
    print(runtest(running_env))
