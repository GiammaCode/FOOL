push 0
lhp
push function0
lhp
sw
lhp
push 1
add
shp
push 5
lhp
sw
lhp
push 1
add
shp
push 9998
lw
lhp
sw
lhp
lhp
push 1
add
shp
lfp
lfp
push -3
add
lw
stm
ltm
ltm
lw
push 0
add
lw
js
print
halt

function0:
cfp
lra
lfp
lw
push -1
add
lw
stm
sra
pop
sfp
ltm
lra
js