push 3
push 3
sub
push -1
bleq label2
push 1
b label3
label2:
push 0
label3:
push 1
beq label0
push 0
b label1
label0:
push 1
label1:
print
halt