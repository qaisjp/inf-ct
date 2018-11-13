/*gen:debug*/
/*gen:expect/*
Initial a, b addresses (should be 0, 0)
0, 0

address `a` after malloc:
268697600

address `b` after malloc:
268697604

Perform a = b

address `a` after assign:
268697604

address `b` after assign:
268697604

Initial c, d addresses (should be 0, 0)
0, 0

address `c` after malloc:
268697608

address `d` after malloc:
268697612

Perform c = d

address `c` after assign:
268697612

address `d` after assign:
268697612
/*gen:expect*/

#include "minic-stdlib.h"

char* a;
char* b;
void main() {
    // char u;
    char* c;
    char* d;

    print_s((char*) "Initial a, b addresses (should be 0, 0)\n");
    print_address((void*)a);
    print_c(','); print_c(' ');
    print_address((void*)b);

    a = (char*) mcmalloc(sizeof(char));
    print_s((char*) "\n\naddress `a` after malloc:\n");
    print_address((void*)a);

    b = (char*) mcmalloc(sizeof(char));
    print_s((char*) "\n\naddress `b` after malloc:\n");
    print_address((void*)b);

    print_s((char*) "\n\nPerform a = b");
    a = b;

    print_s((char*) "\n\naddress `a` after assign:\n");
    print_address((void*)a);

    print_s((char*) "\n\naddress `b` after assign:\n");
    print_address((void*)b);

    // u = 'q';
    // print_c(u);
    // *a = u;
    // print_c(*a);
    // b = a;
    // print_c(*b);

    print_s((char*) "\n\nInitial c, d addresses (should be 0, 0)\n");
    print_address((void*)c);
    print_c(','); print_c(' ');
    print_address((void*)d);

    c = (char*) mcmalloc(sizeof(char));
    print_s((char*) "\n\naddress `c` after malloc:\n");
    print_address((void*)c);

    d = (char*) mcmalloc(sizeof(char));
    print_s((char*) "\n\naddress `d` after malloc:\n");
    print_address((void*)d);

    print_s((char*) "\n\nPerform c = d");
    c = d;

    print_s((char*) "\n\naddress `c` after assign:\n");
    print_address((void*)c);

    print_s((char*) "\n\naddress `d` after assign:\n");
    print_address((void*)d);
}
