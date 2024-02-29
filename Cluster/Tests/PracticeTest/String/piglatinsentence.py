
sentence = input("Enter a sentence you want to translate to piglatin: ")
words = sentence.split(" ")
def piglatin(strg):
    result=""
    if strg[0] in "auioe":
        result=strg+"way"
    else:
        result=strg[1:]+strg[0]+"ay"
    return result

result = ""
for el in words:
    result+=piglatin(el)+" "

print(f"The result is {result}")
