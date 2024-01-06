import math
numberSt = input("enter an hexadecimal value to convert to decimal")
val=0
for power,digit in enumerate(reversed(numberSt)):
    val = val+int(digit,16)*math.pow(16,power)
    

print(f"the hexadecimal value is {val}")