import statistics
from statistics import mean, median

class MyClass:
    """This is the module docstring."""
    a = 10
    b = "baba"
    b = "sratata"

    def functionnnn(self):
        return "szesc"

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

count = 0
while count < 9:
    print ('The count is:', count)
    count = count + 1

if var3 or var1 :
    print("Value of expression is 100")
elif (var2 or var4):
    print("Value of expression is 200")
else:
    print("Value of expression is 300")


var = 20 - 10

def do_global(a, x, y):
    b = 0
    b = a + a
    spam = "global spam"
    return b + a

def scope_test():
    spam = "test spam"
    do_global(10, "asa", 10.994)
    print("After global assignmnt:", spam)

scope_test()
print("In global scope:", spam)
