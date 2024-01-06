
userInput=""
averageList=[]
while userInput != "q":
    userInput  = input("enter 10km run time :")
    if(userInput !="q"):
        averageList.append(float(userInput))

"""Compute the average """
av=0;
for el in averageList:
 av+=el
av =av/len(averageList)

print(f"your average 10km run is: {av}")

