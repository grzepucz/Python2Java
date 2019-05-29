class MyClass:
  "This is my second class"
  a = 10
  def functionnnn(self):
    print('Hello')

# Output: 10
print(MyClass.a)

# Output: <function MyClass.func at 0x0000000003079BF8>
print(MyClass.func)

# Output: 'This is my second class'
print(MyClass.__doc__)

a = 10

if a==10:
  print("matchObj.group()")
else:
  print("No match!!")
