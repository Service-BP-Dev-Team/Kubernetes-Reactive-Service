import os
import json
import glob
import argparse
import matplotlib.pyplot as plt
from numpy import linspace
from math import sqrt
from scipy.interpolate import interp1d

# ---------------- ARGUMENT PARSER ----------------
parser = argparse.ArgumentParser(description="Compare Incremental vs Non-Incremental results")
parser.add_argument(
    "--dir",
    type=str,
    default=".",
    help="Base directory containing Incremental/ and NoIncremental/ folders"
)
args = parser.parse_args()

BASE_DIR = args.dir

# ---------------- HELPERS ----------------
def mean_value(values):
    return sum(values) / len(values)

def std_dev(values, mean):
    return sqrt(sum((val - mean) ** 2 for val in values) / len(values))

def get_list(ins, key):
    return [entry[key] for entry in ins]

# Files to ignore
IGNORED_FILES = {"env.json", "stop.json", "env_merge.json", "stop_merge.json"}

# ---------------- LOAD JSON ----------------
def load_json_files(base_dir):
    result = {
        "incremental": [],
        "non_incremental": []
    }

    for category in ["Incremental", "NoIncremental"]:
        category_path = os.path.join(base_dir, category)

        if not os.path.exists(category_path):
            print(f"Warning: {category_path} does not exist")
            continue

        for filename in os.listdir(category_path):

            if (
                not filename.endswith(".json")
                or filename in IGNORED_FILES
            ):
                continue

            file_path = os.path.join(category_path, filename)

            try:
                with open(file_path, "r") as file:
                    data = json.load(file)

                    if category == "Incremental":
                        result["incremental"].append(data)
                    else:
                        result["non_incremental"].append(data)

            except json.JSONDecodeError:
                print(f"Error decoding JSON: {file_path}")

    return result

# ---------------- REFORMAT DATA ----------------
def re_arranged_values(data):
    result = {}

    for d in data:
        val_env = d["environment"]

        # fixed value
        if "3000000" not in d["result"]:
            continue

        val_results = d["result"]["3000000"]

        number_of_blocks = val_env["NUMBER_OF_BLOCKS"]
        max_len = val_env["MAX_LEN"]

        if number_of_blocks not in result:
            result[number_of_blocks] = {}

        result[number_of_blocks][max_len] = val_results

    return result

# ---------------- DRAW ----------------
def draw_data_with_suffix(data1, data2, base_dir, suffix1="", suffix2="-non_inc"):
    plt.figure()

    def create_var_blocks(data):
        return [
            {
                "K": block_key,
                "ins": [
                    {
                        "length": float(key),
                        "mean": mean_value([dic["duration"] for dic in data[block_key][key]]),
                        "std_dev": std_dev(
                            [dic["duration"] for dic in data[block_key][key]],
                            mean_value([dic["duration"] for dic in data[block_key][key]])
                        ),
                    }
                    for key in data[block_key]
                    if key != "redeployment"
                ],
            }
            for block_key in data
        ]

    # Incremental
    for met in create_var_blocks(data1):
        x = get_list(met["ins"], "length")
        y = get_list(met["ins"], "mean")

        if len(x) < 2:
            continue

        f = interp1d(x, y, kind="linear")
        ax = linspace(min(x), max(x), 600)
        fy = f(ax)

        plt.plot(x, y, "o")
        plt.plot(ax, fy, "-", label="K=" + str(met["K"]) + suffix1)

    # Non Incremental
    for met in create_var_blocks(data2):
        x = get_list(met["ins"], "length")
        y = get_list(met["ins"], "mean")

        if len(x) < 2:
            continue

        f = interp1d(x, y, kind="linear")
        ax = linspace(min(x), max(x), 600)
        fy = f(ax)

        plt.plot(x, y, "o")
        plt.plot(ax, fy, "-", label="K=" + str(met["K"]) + suffix2)

    plt.legend(loc="upper left")

    # Save inside provided directory
    output_path = os.path.join(base_dir, "set4.png")
    plt.savefig(output_path)

    plt.show()
    plt.close()

    print(f"Plot saved to: {output_path}")

# ---------------- MAIN ----------------
if __name__ == "__main__":
    categorized_data = load_json_files(BASE_DIR)

    if not categorized_data["incremental"] and not categorized_data["non_incremental"]:
        print("No valid JSON data found.")
        exit(1)

    data1 = re_arranged_values(categorized_data["incremental"])
    data2 = re_arranged_values(categorized_data["non_incremental"])

    draw_data_with_suffix(data1, data2, BASE_DIR)
