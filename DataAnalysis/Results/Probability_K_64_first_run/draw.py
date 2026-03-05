import json 
import glob
import matplotlib.pyplot as plt
import numpy as np
import pprint
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

def estimation(fail_ratios,durations,pba):
    x=np.array(fail_ratios)
    y=np.array(durations)
    coefs = np.polyfit(x, y, deg=1)
    return coefs[1]+ coefs[0]*pba

def fail_ratio(dic):
    return dic['statistics']['failure']['total']/(dic['statistics']['failure']['total']+len([x for x in dic['statistics']['success']['durations'] if x !=0]))

f_names = [name for name in glob.glob("./Incremental/*.json") if name not in ("./Incremental/env.json", "./Incremental/stop.json")]
tests = sorted([get_data(name) for name in f_names], key=lambda d: d['environment']['WORKER_REQUEST_FAILURE_PROBABILITY'])
data ={'inc_w' : [sorted([[dic['statistics']['failure']['total'],len([x for x in dic['statistics']['success']['durations'] if x !=0]), dic['duration']] for dic in d['result']['3000000']], key=lambda x:  x[0]/(x[0]+x[1])) for d in tests ],
    'inc_t': sorted([[dic['statistics']['failure']['total'],len([x for x in dic['statistics']['success']['durations'] if x !=0]), dic['duration']] for d in tests for dic in d['result']['3000000']], key=lambda x: x[0]/(x[0]+x[1]))}
f_names = [name for name in glob.glob("./NoIncremental/*.json") if name not in ("./NoIncremental/env.json", "./NoIncremental/stop.json")]
tests = sorted([get_data(name) for name in f_names], key=lambda d: d['environment']['WORKER_REQUEST_FAILURE_PROBABILITY'])
data['ninc_w']=[sorted([[dic['statistics']['failure']['total'],len([x for x in dic['statistics']['success']['durations'] if x !=0]), dic['duration']] for dic in d['result']['3000000']], key=lambda x:  x[0]/(x[0]+x[1])) for d in tests ]
data['ninc_t']= sorted([[dic['statistics']['failure']['total'],len([x for x in dic['statistics']['success']['durations'] if x !=0]), dic['duration']] for d in tests for dic in d['result']['3000000']], key=lambda x: x[0]/(x[0]+x[1]))

def print_means(inc,ninc, name):
    x = [mean_value([v[0]/(v[0]+v[1]) for v in t]) for t in inc]
    y = [mean_value([v[2] for v in t]) for t in inc]
    f = interp1d(x, y, kind='linear')
    ax = np.linspace(min(x), max(x) , num=700)
    fy = f(ax)
    plt.plot(ax, fy, '-', label = 'incremental')
    x = [mean_value([v[0]/(v[0]+v[1]) for v in t]) for t in ninc]
    y = [mean_value([v[2] for v in t]) for t in ninc]
    f = interp1d(x, y, kind='linear')
    ax = np.linspace(min(x), max(x) , num=700)
    fy = f(ax)
    plt.plot(ax, fy, '-', label = 'non-inccremental')
    plt.legend(loc="upper left")
    plt.savefig('./'+name+'.png')
    plt.show()
    plt.close()
def print_real(inc,ninc, name):
    xi = [sum([v[0] for v in t])/sum([v[0]+v[1] for v in t]) for t in inc]
    yi = [mean_value([v[2] for v in t]) for t in inc]
    fi = interp1d(xi, yi, kind='linear')
    axi = np.linspace(min(xi), max(xi) , num=700)
    fyi = fi(axi)
    plt.plot(axi, fyi, '-', label = 'incremental')
    xn = [sum([v[0] for v in t])/sum([v[0]+v[1] for v in t]) for t in ninc]
    yn = [mean_value([v[2] for v in t]) for t in ninc]
    fn = interp1d(xn, yn, kind='linear')
    axn = np.linspace(min(xn), max(xn) , num=700)
    fyn = fn(axn)
    plt.plot(axn, fyn, '-', label = 'non-incremental')
    plt.legend(loc="upper left")
    plt.savefig('./'+name+'.png')
    plt.show()
    plt.close()
    gy=fn(axn)-fi(axi)
    plt.plot(axi, gy, '-')
    plt.savefig('./delta_'+name+'.png')
    plt.show()
    plt.close()
#print_means(data['inc_w'], data['ninc_w'], 'mean_mean')
#print_real(data['inc_w'], data['ninc_w'], 'real_mean')
def chunks(xs, n):
    n = max(1, n)
    return [xs[i:i+n] for i in range(0, len(xs), n)]

#print_means(chunks(data['inc_t'],10), chunks(data['ninc_t'],10), 'mean_10')
for n in range(0,49,7):
    print_real(chunks(data['inc_t'],n), chunks(data['ninc_t'],n), 'real_'+str(n))
