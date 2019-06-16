#!/usr/bin/python3

class MyClass:
  """This is the module docstring."""
  a = 10
  b = "baba"

  def functionnnn(self):
    for num in [1,2,3]:
      print ('Current number :', num)
    print('Hello')

# Output: 'This is my second class'
print(MyClass.__doc__)

class SecondClass:
  """This is the 2nd module docstring."""
  x = 1
  napis = "naafawfm"
  x = x + 1

  def printuj(self):
    for fruit in 'fruits':
      print ('Current fruit :', fruit, ' asas', fruit)
