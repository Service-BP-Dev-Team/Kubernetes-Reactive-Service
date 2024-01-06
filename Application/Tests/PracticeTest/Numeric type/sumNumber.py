def sumNumber(*args):
    sum=0
    for el in args:
        sum+=el
    return sum

print(sumNumber(1,7,9,10))