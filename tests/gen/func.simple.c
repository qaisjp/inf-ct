/*gen:expect/*
Main!
Something!
Back at main again!
/*gen:expect*/

#include "minic-stdlib.h"

void print_something() {
    print_s((char*) "Something!\n");
}

void main() {
    print_s((char*) "Main!\n");
    print_something();
    print_s((char*) "Back at main again!\n");
}
