def scope_test():
    def do_global(a):
        global spam
        spam = "global spam"

    spam = "test spam"
    do_global(a)
    print("After global assignment:", spam)

scope_test()
print("In global scope:", spam)
