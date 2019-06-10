#!/usr/bin/python3

try:
    fh = open("testfile", "w")
    fh.write("This is my test file for exception handling!!")
except IOError:
    print ("Error: can\'t find file or read data")
except ValueError as Argument:
    print ("The argument does not contain numbers\n", Argument)
finally:
    print("This would always be executed.")
