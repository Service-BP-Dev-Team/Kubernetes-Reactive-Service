import json
import os
#dirname = input("Enter a directory name: ")
dirname ="jsonscoredir"

dictdir = {filename : json.load(open(os.path.join(dirname,filename))) 
         for filename in os.listdir(dirname)
         if os.path.isfile(os.path.join(dirname,filename))}

result= {}
for filename,content in dictdir.items():
    print(filename)
    for el in ["science", "math", "literature"] :
        scores = [item.get(el) for item in content]
        #print(scores)
        print("\t", f"{el}: min {min(scores)}, max {max(scores)}, average {sum(scores)/len(scores)} ")