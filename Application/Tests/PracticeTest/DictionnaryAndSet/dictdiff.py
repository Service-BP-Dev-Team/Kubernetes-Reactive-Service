
import operator
def dictdiff(first, second):
    output = {}
    all_keys = set(first.keys()) | set(second.keys())

    for key in all_keys:
        if first.get(key) != second.get(key):
            output[key] = [first.get(key), second.get(key)] 
    return output

d1 = {'a':1, 'b':2, 'c':3}
d2 = {'a':1, 'b':2, 'c':4}
print(dictdiff(d1, d1)) 
print(dictdiff(d1, d2)) 
d3 = {'a':1, 'b':2, 'd':3}
d4 = {'a':1, 'b':2, 'c':4}
print(dictdiff(d3, d4)) 
d5 = {'a':1, 'b':2, 'd':4}
print(dictdiff(d1, d5)) 