from  functools import cmp_to_key

people = [{'first':'Reuven', 'last':'Lerner', 'email':'reuven@lerner.co.il'},
{'first':'Donald', 'last':'Trump', 'email':'president@whitehouse.gov'},
{'first':'Vladimir', 'last':'Putin', 'email':'president@kremvax.ru'}
]
def compare(el1,el2):
    if(el1["last"] < el2["last"]):
        return -1
    elif el1["last"] == el2["last"]:
        if(el1["first"] < el2["first"]):
            return -1
        elif (el1["first"] == el2["last"]):
            return 0
    return 1

result=sorted(people,key=cmp_to_key(compare))
#a quicker solution
"""
import operator
for person in sorted(people, key=operator.itemgetter('last', 'first')):
print(f"{person['last']}, {person['first']}: {person['email']}")
❶
"""

print("Last Name, First Name : email")
for el in result:
    print(f"{el['last']}, {el['first']} : {el['email']}")

