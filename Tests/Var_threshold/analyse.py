import json 
import glob
import matplotlib.pyplot as plt
import numpy as np
from sklearn.linear_model import LinearRegression
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

def estimation(fail_ratios,durations):
    x=np.array(fail_ratios).reshape((-1, 1))
    y=np.array(durations)
    model = LinearRegression().fit(x, y)
    return model.intercept_ + model.coef_[0]/2

def fail_ratio(dic):
    return dic['statistics']['failure']['total']/(dic['statistics']['failure']['total']+dic['statistics']['success']['total'])

f_names = glob.glob("Var_threshold/K16/Incremental/*.json")
tests = sorted([get_data(name) for name in f_names], key=lambda d: d['environment']['MAX_LEN'])
var_threshold = {'K16-Inc': [{'threshold' : d['environment']['MAX_LEN'],
            'ins': [{'length': float(key),
                     'estimation': estimation([fail_ratio(dic) for dic in d['result'][key]], [dic['duration'] for dic in d['result'][key]]) 
                    } for key in list(d['result']) if key != 'redeployment']} for d in tests]}

f_names = glob.glob("Var_threshold/K32/Incremental/*.json")
tests = sorted([get_data(name) for name in f_names], key=lambda d: d['environment']['MAX_LEN'])
var_threshold['K32-Inc'] = [{'threshold' : d['environment']['MAX_LEN'],
            'ins': [{'length': float(key),
                     'estimation': estimation([fail_ratio(dic) for dic in d['result'][key]], [dic['duration'] for dic in d['result'][key]]) 
                    } for key in list(d['result']) if key != 'redeployment']} for d in tests]

f_names = glob.glob("Var_threshold/K16/Non-incremental/*.json")
tests = sorted([get_data(name) for name in f_names], key=lambda d: d['environment']['MAX_LEN'])
var_threshold['K16-Non_inc'] = [{'threshold' : d['environment']['MAX_LEN'],
            'ins': [{'length': float(key),
                     'estimation': estimation([fail_ratio(dic) for dic in d['result'][key]], [dic['duration'] for dic in d['result'][key]]) 
                    } for key in list(d['result']) if key != 'redeployment']} for d in tests]

f_names = glob.glob("Var_threshold/K32/Non-incremental/*.json")
tests = sorted([get_data(name) for name in f_names], key=lambda d: d['environment']['MAX_LEN'])
var_threshold['K32-Non_inc'] = [{'threshold' : d['environment']['MAX_LEN'],
            'ins': [{'length': float(key),
                     'estimation': estimation([fail_ratio(dic) for dic in d['result'][key]], [dic['duration'] for dic in d['result'][key]]) 
                    } for key in list(d['result']) if key != 'redeployment']} for d in tests]

for key, met in var_threshold.items():
    x = [inst['threshold'] for inst in met]
    y = [inst['ins'][0]['estimation'] for inst in met]
    f = interp1d(x, y, kind='linear')

    ax = np.linspace(6000, max(x) , 600)
    fy = f(ax)
    plt.plot(x, y, 'o')
    plt.plot(ax, fy, '-', label = key)
    plt.legend(loc="upper right")

plt.savefig('Var_threshold/curve.png')
plt.show()
plt.close()

x = [inst['threshold'] for inst in var_threshold['K16-Inc']]
y = [ninc['ins'][0]['estimation']-inc['ins'][0]['estimation'] for inc, ninc in zip(var_threshold['K16-Inc'],var_threshold['K16-Non_inc'])]
f = interp1d(x, y, kind='linear')
ax = np.linspace(6000, max(x) , 600)
fy = f(ax)
plt.title('Non-incremental overhead')
plt.plot(x, y, 'o')
plt.plot(ax, fy, '-', label = 'K=16')
y = [ninc['ins'][0]['estimation']-inc['ins'][0]['estimation'] for inc, ninc in zip(var_threshold['K32-Inc'],var_threshold['K32-Non_inc'])]
f = interp1d(x, y, kind='linear')
ax = np.linspace(6000, max(x) , 600)
fy = f(ax)
plt.plot(x, y, 'o')
plt.plot(ax, fy, '-', label = 'K=32')
plt.legend(loc="upper right")
plt.savefig('Var_threshold/delta.png')
plt.show()
plt.close()