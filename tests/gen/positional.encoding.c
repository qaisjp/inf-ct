/*gen:expect/*

First called! 0
First called! 1
Hello, world!0
Hello, world!
First called! 1
First called! 1
/*gen:expect*/

#include "minic-stdlib.h"

int call_first() {
    print_s((char*) "\nFirst called! ");
    return 1;
}

int call_second() {
    print_s((char*) "\nHello, world!");
    return 0;
}

void main() {
    int n;

    call_first();

    n = 0 && call_second();
    print_i(n);

    call_first();

    n = 1 || call_second();
    print_i(n);

    n = call_second() && call_first();
    print_i(n);

    n = call_second() || call_first();
    print_i(n);

    n = call_first() || call_second();
    print_i(n);

    // return 0;
}
