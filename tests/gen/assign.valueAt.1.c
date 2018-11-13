/*gen:debug*/
/*gen:expect/*
address `cp` after malloc:
268697600

Perform *cp = 'q'
    (should not change address of cp)

address `cp` after assign:
268697600

value `*cp`:
q
/*gen:expect*/
#include "minic-stdlib.h"

void main() {
    char* cp;

    cp = (char*) mcmalloc(sizeof(char));
    print_s((char*) "address `cp` after malloc:\n");
    print_address((void*)cp);

    print_s((char*) "\n\nPerform *cp = 'q'\n\t(should not change address of cp)");
    *cp = 'q';

    print_s((char*) "\n\naddress `cp` after assign:\n");
    print_address((void*)cp);

    print_s((char*) "\n\nvalue `*cp`:\n");
    print_c(*cp); // should be q
}
