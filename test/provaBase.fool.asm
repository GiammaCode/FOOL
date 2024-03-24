push 0
push 5
push 3
sub
push 7
push 5
mult
push 1
lfp
push -3
add
lw
lfp
push -2
add
lw
beq label2
push 0
b label3
label2:
push 1
label3:
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