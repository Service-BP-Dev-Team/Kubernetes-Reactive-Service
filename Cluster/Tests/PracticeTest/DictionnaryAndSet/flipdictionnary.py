d = {'a':1, 'b':2, 'c':3}
d1={}
for key,value in d.items():
    d1[value]=key
print(d1)

print({value:key for key,value in d.items()})