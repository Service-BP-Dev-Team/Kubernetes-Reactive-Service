filename="./etc-passwd.txt"
d= {}
with open(filename) as f:
    for line in open(filename):
        if not line.startswith("#") and line.strip():
            line=line.strip().split(":")
            d[line[0]]=line[2]

for key,value in sorted(d.items()):
    print(f"{key}:{value}")
     