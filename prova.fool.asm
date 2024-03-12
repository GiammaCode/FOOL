push 0
push 5
push 3
add
push function0
lfp
push 8
lfp
push -2
add
lw
lfp
stm
ltm
ltm
push -3
add
lw
js
push 1
beq label5
push 10
b label6
label5:
push 0
label6:
print
halt

function0:
cfp
lra
push 1
lfp
push -2
add
lw
lfp
push 1
add
lw
lfp
push 2
add
lw
beq label2
bleq label3
b label2
label2:
push 1
b label4
label3:
push 0
label4:
beq label0
push 0
b label1
label0:
push 1
label1:
stm
pop
sra
pop
pop
pop
sfp
ltm
lra
js