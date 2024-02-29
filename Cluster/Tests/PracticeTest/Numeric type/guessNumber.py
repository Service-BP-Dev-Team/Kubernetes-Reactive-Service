import random


number = random.randint(0,100)
name = input("Enter your name")
print(f"Hello {name}")
print("You will have to guess my secret number")
userNumber=-1;
threshold= 10;
while(userNumber!=number):
    userNumber= int(input("enter a number"))
    if userNumber == number:
        print(f"Congratulations {name}, you have guess my number")
    elif userNumber - threshold >= number :
        print("Too high")
    elif userNumber + threshold <= number :
        print("Too low")
    else :
        print("Just right")

