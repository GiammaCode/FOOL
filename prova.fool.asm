push 0
push 5
push 3
sub
push 1
push 1
sub
push 1
beq label0
push 0
b label1
label0:
push 1
label1:
print
halt