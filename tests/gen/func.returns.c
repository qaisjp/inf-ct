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

char newline() {
    return '\n';
}

void main() {
    print_s((char*) "Main!\n");
    print_i(print_something());
    print_c(newline());
    print_s((char*) "Back at main again!\n");
}
