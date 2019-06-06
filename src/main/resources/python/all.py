class MyClass:
    """This is the module docstring."""
    a = 10
    b = "baba"
    b = "sratata"

    def functionnnn(self):
        print('Hello')

# Output: 10
print(MyClass.a)

# Output: 'This is my second class'
print(MyClass.__doc__)

x = 10

# if x==10:
#     print("matchObj.group()")
# else:
#     print("No match!!")
#
#

def rec_fib(n):
    '''inefficient recursive function as defined, returns Fibonacci number'''
    if n > 1:
        return rec_fib(n-1) + rec_fib(n-2)
    return n

for i in range(40,50):
    print(i, rec_fib(i))


for num in [1,2,3]:
    print ('Current number :', num)


for fruit in 'fruits':
    print ('Current fruit :', fruit, ' asas', fruit)

for fruit in range(len(fruits)):
    print ('Current fruit :', fruit)

var = 20 - 10


def scope_test():
    def do_global(a, x, y):
        global spam
        b = 0
        b = a + a
        spam = "global spam"
        return b + a

    spam = "test spam"
    do_global(10, "asa", 10.994)
    print("After global assignment:", spam)

scope_test()
print("In global scope:", spam)
