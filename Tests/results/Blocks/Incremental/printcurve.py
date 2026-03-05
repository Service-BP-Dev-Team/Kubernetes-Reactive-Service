import json
import glob
import matplotlib.pyplot as plt
from numpy import linspace
from math import sqrt
from scipy.interpolate import interp1d

def get_data(file_name):
    fh = open(file_name)
    data = json.load(fh)
    fh.close()
    return data

def mean_value(values):
    return sum(values)/len(values)

def std_dev(values, mean):
    return sqrt(sum([(val-mean)**2 for val in values])/len(values))

f_names = glob.glob("*.json")
tests = sorted([get_data(name) for name in f_names], key=lambda d: d['environment']['NUMBER_OF_BLOCKS'])
var_blocks = [{'K' : d['environment']['NUMBER_OF_BLOCKS'],
            'ins': [{'length': float(key),
                     'mean': mean_value([dic['duration'] for dic in d['result'][key]]),
                     'std_dev': std_dev([dic['duration'] for dic in d['result'][key]],mean_value([dic['duration'] for dic in d['result'][key]]))
                    } for key in list(d['result']) if key != 'redeployment']} for d in tests]

def get_list(ins, key):
    return [entry[key] for entry in ins]

for met in var_blocks:
    x = [0]+get_list(met['ins'],'length')
    y = [0]+get_list(met['ins'],'mean')
    f = interp1d(x, y, kind='linear')

    ax = linspace(0, max(x) , 600)
    fy = f(ax)
    plt.plot(x, y, 'o')
    plt.plot(ax, fy, '-', label = 'K='+str(met['K']))
    plt.legend(loc="upper left")

plt.savefig('Blocks_means.png')
#plt.show()
#print(len(y))
plt.close()