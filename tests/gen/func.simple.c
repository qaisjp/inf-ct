/*gen:expect/*
Main!
Something!
Back at main again!
/*gen:expect*/

#include "minic-stdlib.h"

void print_something() {
	print_s((char*) "Something!");
}

void main() {
	print_s((char*) "Main!");
	print_something();
	print_s((char*) "Back at main again!");
}
