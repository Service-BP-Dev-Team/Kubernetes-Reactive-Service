from collections import Counter
words = ['this', 'is', 'a', 'test', 'program']
mx=words[0]
mxcpt=max(Counter(mx).values())
for el in words[1:]:
    cpt= max(Counter(el).values())
    if(cpt>mxcpt):
        mx=el
        mxcpt = cpt

print(f"the maximal word with repeating value is {mx}")
