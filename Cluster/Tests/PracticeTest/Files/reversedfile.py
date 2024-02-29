#filename=input("Enter the file name : ")
filename = "reversed.txt"
with open(filename) as f, open("filereversed.txt",'w') as newfile:
    for line in f:
        sline = line.rstrip()
        nline = sline[::-1]
        newfile.write(nline+"\n")