def sortstr(strg):
    result=[]
    tab = list(strg)
    for i in range(len(tab)):
        minIndex=i
        for j in range(i+1,len(tab)):
            if(tab[minIndex]>tab[j]):
                minIndex=j
        """Permutation
        temp=tab[i]
        tab[i]=tab[minIndex]
        tab[minIndex]=temp """
        tab[i],tab[minIndex] = tab[minIndex], tab[i]
    return "".join(tab)

""" another implementation
def strsort(s):
return ''.join(sorted(s))
"""
print(f"the sort of {sortstr('cabuty')}")