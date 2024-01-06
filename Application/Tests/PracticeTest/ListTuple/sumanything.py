def mysum(*args):
    if not args:
        return args
    else:
        sum=args[0]
        for i in range(1,len(args)):
            sum+=args[i] 
        return sum

print(f"The sum of mysum('abc', 'def') is {mysum('abc', 'def')} ")
print(f"The sum of mysum([1,2,3], [4,5,6]) is {mysum([1,2,3], [4,5,6])} ")
print(f"The sum of mysum(1,2,3) is {mysum(1,2,3)} ")