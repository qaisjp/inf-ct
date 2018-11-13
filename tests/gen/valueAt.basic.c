#include "minic-stdlib.h"
/*gen:debug*/
/*gen:expect/*
Initial a, b addresses (should be 0, 0)
0, 0

address `a` after malloc:
268697600

address `cp` after malloc:
268697608

Perform *cp = 'q'
    (should not change address of cp)

value `*cp`:
q

address `cp`:
268697608

New a address after a->innerK = 1:
268697600

New innerK value after a->innerK = 1:
1

Old a.innerCP address:
0

New a.innerCP address after a->innerCP = cp:
268697608

New innerCP value (*a->innerCP)
q

b = a (just pointers)
    (b and a should be equal now)

New b address after b = a:
268697600

Values of b->innerK, b->innerCP should be same as a:
1, q

/*gen:expect*/

struct container {
    int innerK;
    char* innerCP;
};

struct container* a;
struct container* b;

void main() {
    char* cp;

    "ZYX987POIL";

    print_s((char*) "Initial a, b addresses (should be 0, 0)\n");
    print_address((void*)a);
    print_c(','); print_c(' ');
    print_address((void*)b);

    a = (struct container*) mcmalloc(sizeof(struct container));
    print_s((char*) "\n\naddress `a` after malloc:\n");
    print_address((void*)a);

    cp = (char*) mcmalloc(sizeof(char));
    print_s((char*) "\n\naddress `cp` after malloc:\n");
    print_address((void*)cp);

    print_s((char*) "\n\nPerform *cp = 'q'\n    (should not change address of cp)");
    *cp = 'q';

    print_s((char*) "\n\nvalue `*cp`:\n");
    print_c(*cp); // should be q

    print_s((char*) "\n\naddress `cp`:\n");
    print_address((void*)cp);

    // print_s((char*) "Check contents on stack:\n");
    // print_c(c);
    // print_c(*cp);
    // print_c('\n');

    (*a).innerK = 1;

    print_s((char*) "\n\nNew a address after a->innerK = 1:\n");
    print_address((void*)a);

    print_s((char*) "\n\nNew innerK value after a->innerK = 1:\n");
    print_i((*a).innerK);

    print_s((char*) "\n\nOld a.innerCP address:\n");
    print_address((void*)((*a).innerCP));

    (*a).innerCP = cp;

    print_s((char*) "\n\nNew a.innerCP address after a->innerCP = cp:\n");
    print_address((void*)((*a).innerCP));

    print_s((char*)"\n\nNew innerCP value (*a->innerCP)\n");
    print_c(*((*a).innerCP));

    print_s((char*)"\n\nb = a (just pointers)\n    (b and a should be equal now)");
    b = a;

    print_s((char*) "\n\nNew b address after b = a:\n");
    print_address((void*)a);

    print_s((char*) "\n\nValues of b->innerK, b->innerCP should be same as a:\n");
    print_i((*b).innerK);
    print_c(','); print_c(' ');
    print_c(*((*b).innerCP));
    print_c('\n');
}
