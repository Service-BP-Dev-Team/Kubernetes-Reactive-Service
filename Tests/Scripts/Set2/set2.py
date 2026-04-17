import json
import glob
import os
import argparse
import matplotlib.pyplot as plt
from numpy import linspace
from math import sqrt
from scipy.interpolate import interp1d

# ---------------- ARGUMENT PARSER ----------------
parser = argparse.ArgumentParser(description="Analyze test results (NUMBER_OF_BLOCKS)")
parser.add_argument(
    "--dir",
    type=str,
    default=".",
    help="Directory containing JSON result files (default: current directory)"
)
args = parser.parse_args()

DATA_DIR = args.dir

# ---------------- HELPERS ----------------
def get_data(file_name):
    with open(file_name, "r", encoding="utf-8") as fh:
        return json.load(fh)

def mean_value(values):
    return sum(values) / len(values)

def std_dev(values, mean):
    return sqrt(sum((val - mean) ** 2 for val in values) / len(values))

# Files to ignore
IGNORED_FILES = {"env.json", "stop.json", "env_merge.json", "stop_merge.json"}

# ---------------- LOAD FILES ----------------
pattern = os.path.join(DATA_DIR, "*.json")

f_names = [
    name for name in glob.glob(pattern)
    if os.path.basename(name) not in IGNORED_FILES
]

if not f_names:
    print(f"No JSON files found in {DATA_DIR}")
    exit(1)

# ---------------- SORT TESTS ----------------
tests = sorted(
    [get_data(name) for name in f_names],
    key=lambda d: d["environment"]["NUMBER_OF_BLOCKS"]
)

# ---------------- PROCESS DATA ----------------
var_blocks = [
    {
        "K": d["environment"]["NUMBER_OF_BLOCKS"],
        "ins": [
            {
                "length": float(key),
                "mean": mean_value([dic["duration"] for dic in d["result"][key]]),
                "std_dev": std_dev(
                    [dic["duration"] for dic in d["result"][key]],
                    mean_value([dic["duration"] for dic in d["result"][key]])
                ),
            }
            for key in list(d["result"])
            if key != "redeployment"
        ],
    }
    for d in tests
]

def get_list(ins, key):
    return [entry[key] for entry in ins]

# ---------------- PLOTTING ----------------
for met in var_blocks:
    x = [0] + get_list(met["ins"], "length")
    y = [0] + get_list(met["ins"], "mean")

    f = interp1d(x, y, kind="linear")

    ax = linspace(0, max(x), 600)
    fy = f(ax)

    plt.plot(x, y, "o")
    plt.plot(ax, fy, "-", label="K=" + str(met["K"]))
    plt.legend(loc="upper left")

# Save in the same directory
output_path = os.path.join(DATA_DIR, "set2.png")

plt.savefig(output_path)
plt.show()

print("Last curve length:", len(y))
plt.close()
