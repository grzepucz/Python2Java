class MyClass:
	"""This is my class"""
	a = 10
	b = '4'
	c = "Panda "
	c = c + b
	d = true
	def func(self):
		if (d):
			print('Hello')

# Output: 10
print(MyClass.a)

# Output: <function MyClass.func at 0x0000000003079BF8>
print(MyClass.func)

# Output: 'This is my second class'
print(MyClass.__doc__)
