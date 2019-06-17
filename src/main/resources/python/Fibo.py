#!/usr/bin/python3

def rec_fib(n):
    '''inefficient recursive function as defined, returns Fibonacci number'''
    if n > 1:
        return rec_fib(n-1) + rec_fib(n-2)
    return n

for i in [1,2,3,4]:
    print(i, rec_fib(i))

