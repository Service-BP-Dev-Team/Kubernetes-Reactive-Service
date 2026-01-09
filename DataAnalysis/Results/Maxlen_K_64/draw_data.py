import os
import json
import glob
import matplotlib.pyplot as plt
from numpy import linspace
from math import sqrt
from scipy.interpolate import interp1d

def load_json_files(base_dir="."):
    result = {
        "incremental": [],
        "non_incremental": []
    }

    # Loop through all files in the directory
    for category in ["Incremental", "NoIncremental"]:
        category_path = os.path.join(base_dir, category)
        
        if not os.path.exists(category_path):
            continue
        
        for filename in os.listdir(category_path):
            # Construct the full file path
            file_path = os.path.join(category_path, filename)

            # Skip env.json and stop.json files
            if not filename.endswith(".json") or filename in ["env.json", "stop.json"]:
                continue
            
            # Load JSON file
            with open(file_path, 'r') as file:
                try:
                    data = json.load(file)
                    # Add to appropriate category
                    if category == "Incremental":
                        result["incremental"].append(data)
                    else:
                        result["non_incremental"].append(data)
                except json.JSONDecodeError:
                    print(f"Error decoding JSON from file: {file_path}")

    return result


def re_arranged_values(data):
    result ={}
    for d in data:
        val_env=d["environment"]
        val_results= d["result"]["3000000"] #since all tests are made for a single point
        number_of_blocks=val_env["NUMBER_OF_BLOCKS"]
        max_len = val_env["MAX_LEN"]
        if not (number_of_blocks in result):
            result[number_of_blocks]={}
        result[number_of_blocks][max_len]=val_results
    
    return result 

def get_list(ins, key):
    return [entry[key] for entry in ins]

def mean_value(values):
    return sum(values)/len(values)

def std_dev(values, mean):
    return sqrt(sum([(val-mean)**2 for val in values])/len(values))
def draw_data_with_suffix(data1, data2, suffix1="inc", suffix2="non_inc"):
    plt.figure()

    # Function to create var_blocks from provided data
    def create_var_blocks(data):
        return [{'K': block_key,
                 'ins': [{'length': float(key),
                          'mean': mean_value([dic['duration'] for dic in data[block_key][key]]),
                          'std_dev': std_dev([dic['duration'] for dic in data[block_key][key]], 
                                             mean_value([dic['duration'] for dic in data[block_key][key]]))
                         } for key in list(data[block_key]) if key != 'redeployment']} 
                for block_key in data]

    # Process both datasets
    for met in create_var_blocks(data1):
        x = get_list(met['ins'], 'length')
        y = get_list(met['ins'], 'mean')
        f = interp1d(x, y, kind='linear')
        ax = linspace(min(x), max(x), 600)
        fy = f(ax)
        plt.plot(x, y, 'o', label='Data Points ' + suffix1 + ' K=' + str(met['K']))
        plt.plot(ax, fy, '-', label='K=' + suffix1 + ' ' + str(met['K']))

    for met in create_var_blocks(data2):
        x = get_list(met['ins'], 'length')
        y = get_list(met['ins'], 'mean')
        f = interp1d(x, y, kind='linear')
        ax = linspace(min(x), max(x), 600)
        fy = f(ax)
        plt.plot(x, y, 'o', label='Data Points ' + suffix2 + ' K=' + str(met['K']))
        plt.plot(ax, fy, '-', label='K=' + suffix2 + ' ' + str(met['K']))

    plt.legend(loc="upper left")

    # Save and show the combined plot
    plt.savefig('Worker_means_combined.png')
    plt.show()
    plt.close()


if __name__ == "__main__":
    base_directory = "."  # Change this to your base directory
    categorized_data = load_json_files(base_directory)
    data1 = re_arranged_values(categorized_data["incremental"])
    data2 = re_arranged_values(categorized_data["non_incremental"])
    with open("output_file_data1.json", "w") as json_file:
        json.dump(data1, json_file, indent=4)
    with open("output_file_data2.json", "w") as json_file:
        json.dump(data2, json_file, indent=4)
    # Draw both datasets on the same plot
    draw_data_with_suffix(data1, data2)