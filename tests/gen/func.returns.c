/*gen:expect/*
Main!
Something!
52709
Back at main again!

ALL OK: Simulator status code non-zero: 255
/*gen:expect*/

#include "minic-stdlib.h"

char newline() {
    return '\n';
}

int print_something() {
    print_s((char*) "Something!");
    print_c(newline());
    return 52709;
}

void back() {
    print_s((char*)"Back at main again!\n");
    return;
}

int main() {
    print_s((char*) "Main!\n");
    print_i(print_something());
    print_c(newline());
    back();

    return 255;
}
