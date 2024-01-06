
import csv
filename="readwrite.csv"
users_dict={}
with open(filename) as f:
    users_dict={line.split(":")[0]:line.split(":")[2] for line in f
                if not line.startswith('#')}
print(users_dict)

with open('./stuff.csv', 'w') as f:
    o = csv.writer(f,delimiter="\t") 
    for key,value in users_dict.items():
        o.writerow([key,value])