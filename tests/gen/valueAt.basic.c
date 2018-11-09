#include "minic-stdlib.h"

/*gen:expect/*
q
define a, and print
1q
b = a (just pointers)
1q

/*gen:expect*/

struct container {
	int innerK;
	char* innerCP;
};

struct container* a;
struct container* b;

void main() {
	char c;
	char* cp;
	c = 'q';

	"ZYX987POIL";

	a = (struct container*) mcmalloc(sizeof(struct container));
	cp = (char*) mcmalloc(sizeof(char));

	*cp = c;

	print_c(*cp); // should be q
	print_c('\n');

	// print_s((char*) "Check contents on stack:\n");
	// print_c(c);
	// print_c(*cp);
	// print_c('\n');

	(*a).innerK = 1;
	(*a).innerCP = cp;

	print_s((char*)"define a, and print\n");
	print_i((*a).innerK);
	print_c(*((*a).innerCP));
	print_c('\n');

	print_s((char*)"b = a (just pointers)\n");
	b = a;

	print_i((*b).innerK);
	print_c(*((*b).innerCP));
	print_c('\n');

}
