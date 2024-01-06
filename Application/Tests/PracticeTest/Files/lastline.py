filename = input("Enter a filename: ")
final_line = ''
for current_line in open(filename): 
    final_line = current_line
print(final_line, end='')
#other solution
"""
filepath=input("enter a file path")
last=""
with open(filepath) as f :
    
    while True:
        
        line=f.readline()
        if line :
            last=line
        else:
            break

print(last)
"""

#reading a byte file with chunk:
f = open(filename, 'rb')
while True:
    one_chunk = f.read(1000)

    if not one_chunk:
        #GH 'break' should be indented
        break
    print(f"This chunk contains {len(one_chunk)} bytes")