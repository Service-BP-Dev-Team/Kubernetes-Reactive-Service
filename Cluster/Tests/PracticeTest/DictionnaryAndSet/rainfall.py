d={}
enterInput= True
while enterInput :
    val=input("enter a  town : ")
    if val :
        rainfall=input("enter a ranfall in milimeter : ")
        if rainfall.isdigit():
            rain= int(rainfall)
            d[val]=d.get(val,0)+rain
        else:
            print("You didn't enter a valid number; try again.")
            continue
    else:
        enterInput=False

for key,value in d.items():
    print(key)
    print(value)