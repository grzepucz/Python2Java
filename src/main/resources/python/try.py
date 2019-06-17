#!/usr/bin/python3

try:
    data = "src/path/to/file"
    fh = open(data)
except IOError:
    print ("Error: can\'t find file or read data")
except ValueError as Argument:
    print ("The argument does not contain numbers\n", Argument)
finally:
    print("This would always be executed.")
