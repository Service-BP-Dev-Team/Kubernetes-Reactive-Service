word = input("enter a word : ")
result=[]
for el in word:
    if el in "aeuio":
        result.append("ub"+el)
    else:
        result.append(el)

print(f"the result is {''.join(result)}")