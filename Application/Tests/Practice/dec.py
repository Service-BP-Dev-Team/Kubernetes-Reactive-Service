def repeat(n):
    def decorator_function(func):
        def wrapper(*args, **kwargs):
            for _ in range(n):
                result = func(*args, **kwargs)
            return result
        return wrapper
    return decorator_function

@repeat(3)
def greet(name):
    print(f"Hello, {name}!")

greet("John")

#python -m venv env_name