from os import path, scandir
dirname =input('enter the dir name ')
#dirname="lonwordperdir"
result={}
if(path.isdir(dirname)):
    for entry in scandir(dirname):
        if(entry.is_file()):
            with open(entry.path) as f:
                for line in f:
                    words=line.split()
                    if words:
                        words.append(result.get(entry.path,words[0]))
                        maxi=max(words,key=len)
                        result[entry.path]=maxi

print(result)

#another solution  below
"""
import os
dirname = input("Enter a directory name: ")
def find_longest_word(filename):
longest_word = ''
for one_line in open(filename):
//GH The following line should be further indented
for one_word in one_line.split():
if len(one_word) > len(longest_word):
//GH The following line should be further indented
longest_word = one_word
return longest_word
print({filename : find_longest_word(os.path.join(dirname, filename))
for filename in os.listdir(dirname) 
if os.path.isfile(os.path.join(dirname, filename))}) 
"""
        


