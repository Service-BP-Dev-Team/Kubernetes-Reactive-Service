filename = input ("enter the file location ")
cptline=0
cptword=0
cptcharacter=0
cptUniqueWord=0
wordset=set()
with open(filename) as f:
    for line in f:
        cptline+=1
        words=line.split()
        cptword+=len(words)
        cptcharacter+=len(line)
        wordset.update(words)

print(f"the number of characters is : {cptcharacter}")
print(f"the number of  words is : {cptword}")
print(f"the number of line is : {cptline}")
print(f"the number of unique word is : {len(wordset)}")
