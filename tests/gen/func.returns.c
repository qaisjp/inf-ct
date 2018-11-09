/*gen:expect/*
Main!
Something!
52709
Back at main again!
/*gen:expect*/

#include "minic-stdlib.h"

int print_something() {
	print_s((char*) "Something!\n");
	return 52709;
}

void main() {
	print_s((char*) "Main!\n");
	print_i(print_something());
	print_s((char*) "\nBack at main again!\n");
}
