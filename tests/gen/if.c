/*gen:debug*/
/*gen:expect/*
1337
20
1337109
1
/*gen:expect*/
#include "minic-stdlib.h"

void eww() {
    int a; // 4 +
    int b; // 4 +
    int c; // 4 +
    int d; // 4 +
    int e; // 4  = 20
    print_c('\n');
    print_i(get_register(30) - get_register(29));
    print_c('\n');
    a = 0;
    b = 1;
    c = 2;
    d = 3;
    e = 4;
}

void main() {
    int f;
    int original;
    f = 9;
    original = get_register(29);
    if (0) {
        int c;
        c = 1080;
        print_i(1080);
    }

    if (1) {
        int d; // 12
        d = 1337;
        print_i(d);
        eww();
        print_i(d);
    } else {
        print_i(f);
    }

    if (f) {
        print_i(f + 100);
    }
    print_c('\n');
    print_i(original == get_register(29));
}