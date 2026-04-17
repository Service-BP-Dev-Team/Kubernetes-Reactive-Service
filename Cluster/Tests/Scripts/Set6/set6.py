import json
import glob
import os
import argparse
import matplotlib.pyplot as plt
import numpy as np
from math import sqrt
from scipy.interpolate import interp1d

# ---------------- ARGUMENT PARSER ----------------
parser = argparse.ArgumentParser(
    description="Analyze and plot Incremental vs Non-Incremental datasets"
)
parser.add_argument(
    "--dir",
    type=str,
    default=".",
    help="Base directory containing Incremental/ and NoIncremental/ folders"
)
args = parser.parse_args()

BASE_DIR = args.dir
INC_DIR = os.path.join(BASE_DIR, "Incremental")
NINC_DIR = os.path.join(BASE_DIR, "NoIncremental")

# Files to ignore
IGNORED_FILES = {"env.json", "stop.json"}


# ---------------- HELPERS ----------------
def get_data(file_name):
    """Load a JSON file"""
    with open(file_name) as fh:
        return json.load(fh)


def mean_value(values):
    return sum(values) / len(values)


def std_dev(values, mean):
    return sqrt(sum([(val - mean) ** 2 for val in values]) / len(values))


def estimation(fail_ratios, durations, pba):
    """Linear estimation using polynomial fit"""
    x = np.array(fail_ratios)
    y = np.array(durations)
    coefs = np.polyfit(x, y, deg=1)
    return coefs[1] + coefs[0] * pba


def fail_ratio(dic):
    """Compute failure ratio for one record"""
    return dic['statistics']['failure']['total'] / (
        dic['statistics']['failure']['total'] +
        len([x for x in dic['statistics']['success']['durations'] if x != 0])
    )


# ---------------- LOAD DATA ----------------
def load_category(folder):
    """Load and sort JSON files from a given folder"""
    if not os.path.exists(folder):
        print(f"Warning: {folder} does not exist")
        return []

    f_names = [
        name for name in glob.glob(os.path.join(folder, "*.json"))
        if os.path.basename(name) not in IGNORED_FILES
    ]

    tests = sorted(
        [get_data(name) for name in f_names],
        key=lambda d: d['environment']['WORKER_REQUEST_FAILURE_PROBABILITY']
    )

    return tests


# ---------------- BUILD DATA STRUCTURE ----------------
def build_data(inc_tests, ninc_tests):
    """Transform raw JSON into plotting-friendly structure"""
    data = {}

    # Incremental
    data['inc_w'] = [
        sorted(
            [[dic['statistics']['failure']['total'],
              len([x for x in dic['statistics']['success']['durations'] if x != 0]),
              dic['duration']]
             for dic in d['result']['3000000']],
            key=lambda x: x[0] / (x[0] + x[1])
        )
        for d in inc_tests
    ]

    data['inc_t'] = sorted(
        [[dic['statistics']['failure']['total'],
          len([x for x in dic['statistics']['success']['durations'] if x != 0]),
          dic['duration']]
         for d in inc_tests for dic in d['result']['3000000']],
        key=lambda x: x[0] / (x[0] + x[1])
    )

    # Non-incremental
    data['ninc_w'] = [
        sorted(
            [[dic['statistics']['failure']['total'],
              len([x for x in dic['statistics']['success']['durations'] if x != 0]),
              dic['duration']]
             for dic in d['result']['3000000']],
            key=lambda x: x[0] / (x[0] + x[1])
        )
        for d in ninc_tests
    ]

    data['ninc_t'] = sorted(
        [[dic['statistics']['failure']['total'],
          len([x for x in dic['statistics']['success']['durations'] if x != 0]),
          dic['duration']]
         for d in ninc_tests for dic in d['result']['3000000']],
        key=lambda x: x[0] / (x[0] + x[1])
    )

    return data


# ---------------- PLOTTING ----------------
def print_real(inc, ninc, name):
    """Plot real aggregated values and their delta"""
    xi = [sum([v[0] for v in t]) / sum([v[0] + v[1] for v in t]) for t in inc]
    yi = [mean_value([v[2] for v in t]) for t in inc]

    fi = interp1d(xi, yi, kind='linear')
    axi = np.linspace(min(xi), max(xi), num=700)
    fyi = fi(axi)

    plt.plot(axi, fyi, '-', label='incremental')

    xn = [sum([v[0] for v in t]) / sum([v[0] + v[1] for v in t]) for t in ninc]
    yn = [mean_value([v[2] for v in t]) for t in ninc]

    fn = interp1d(xn, yn, kind='linear')
    axn = np.linspace(min(xn), max(xn), num=700)
    fyn = fn(axn)

    plt.plot(axn, fyn, '-', label='non-incremental')

    plt.legend(loc="upper left")

    # Save inside selected directory
    output_path = os.path.join(BASE_DIR, f"{name}.png")
    plt.savefig(output_path)
    plt.show()
    plt.close()

    # Plot delta
    gy = fn(axn) - fi(axi)
    plt.plot(axi, gy, '-')

    delta_path = os.path.join(BASE_DIR, f"delta_{name}.png")
    plt.savefig(delta_path)
    plt.show()
    plt.close()


# ---------------- UTIL ----------------
def chunks(xs, n):
    """Split list into chunks of size n"""
    n = max(1, n)
    return [xs[i:i + n] for i in range(0, len(xs), n)]


# ---------------- MAIN ----------------
if __name__ == "__main__":

    inc_tests = load_category(INC_DIR)
    ninc_tests = load_category(NINC_DIR)

    if not inc_tests and not ninc_tests:
        print("No valid JSON data found.")
        exit(1)

    data = build_data(inc_tests, ninc_tests)

    # Generate plots for different chunk sizes
    for n in range(0, 49, 7):
        print_real(
            chunks(data['inc_t'], n),
            chunks(data['ninc_t'], n),
            f"real_{n}"
        )