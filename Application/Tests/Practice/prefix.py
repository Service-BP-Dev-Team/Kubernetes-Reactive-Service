import re
def compute(input):
    if type(input)==str:
        inp=input.strip()
        inp = input.split(" ")
        oper=inp[0]
        el1 = inp[1]
        el2 = inp[2]
        if(oper=="+"):
            return int(el1)+int(el2)
        elif oper=="-":
            return int(el1)-int(el2)
        elif oper=="*":
            return int(el1)*int(el2)
        elif oper=="/":
            return int(el1)/int(el2)
        elif oper=="**":
            return int(el1)**int(el2)
    return 0

val=input("enter the prefixed test")

print(compute(val))