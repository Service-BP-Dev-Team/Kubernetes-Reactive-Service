def firstlast(mylist):
    return mylist[:1]+mylist[-1:]

print(f"The first-last of abc is {firstlast('abc')}")
print(f"The first-last of [1,2,3,4] is {firstlast([1,2,3,4])}")