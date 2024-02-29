def myxml(*args, **kwargs):
    val=None
    if args :
        start = f"<{args[0]} " 
        middle=">"
        if kwargs :
            for key,value in kwargs.items():
                start+=f"{key}={value} "
        for el in args[1:]:
            middle+=f"{el} "
        end=f"</{args[0]}>"
        val=start+middle+end
    return val

print(myxml())
print(myxml("foo","bar"))
print(myxml("foo","bar",a="1",b="2"))
#other inplementation
def myxml2(tagname, content='', **kwargs):
    attrs = ''.join([f' {key}="{value}"'
    for key, value in kwargs.items()])

    return f"<{tagname}{attrs}>{content}</{tagname}>"