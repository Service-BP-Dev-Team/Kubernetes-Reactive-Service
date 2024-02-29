word= input('enter a word in english')
result=""
if word[0] in "auioe":
    result=word+"way"
else:
    result=word[1:]+word[0]+"ay"

print(f" the result is {result}")
