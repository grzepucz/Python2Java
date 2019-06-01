def scope_test():
    def do_global(a, x, y):
        global spam
        b = a + a
        spam = "global spam"
        return b + a

    spam = "test spam"
    do_global(10, "asa", 10.994)
    print("After global assignment:", spam)

scope_test()
print("In global scope:", spam)
