def allas(mylist):
    if(len(mylist)<6):
        for i in range(0,len(mylist)):
            mylist[i]='a'
        mylist.extend(['a' for i in range(0,6-len(mylist))])
    else:
        for i in range(0,6):
            mylist[i]='a'
        for i in range(6,len(mylist)):
            #print(i)
            mylist.pop()

mylist=[1,2,4,5,8,5,4,5,9]
mylist=[1,2,3,4,5,6]
alias=mylist
allas(mylist)
print(f"the new mylist is {mylist}")
print(f"the old mylist is {alias}")

"""
A more simple solution is 
mylist = list(range(10))
mylist[:] = ['a'] * 6
print(mylist)
"""

def allas1(mylist):
    mylist[:] = ['a'] * 6

mylist=[1,2,4,5,8,5,4,5,9]
mylist=[1,2,3,4,5,6]
alias=mylist
allas1(mylist)
print(f"the new mylist is {mylist}")
print(f"the old mylist is {alias}")