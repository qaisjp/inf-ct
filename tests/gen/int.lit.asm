.text
	j main
.data
	h: .asciiz "hello"
.text

main:
	li	$v0, 4
	la	$a0, h
	syscall

	la	$a0, w
	syscall

.data
	w: .asciiz "world"
