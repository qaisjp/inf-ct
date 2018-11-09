/*gen:expect/*
169q169q
/*gen:expect*/

#include "minic-stdlib.h"

struct blah {
	int a;
	char b;
};

struct blah* d;

void main() {
	struct blah* c;

	"QAIS PATANKAR";

	d = (struct blah*) mcmalloc(sizeof(struct blah));
	c = (struct blah*) mcmalloc(sizeof(struct blah));

	(*c).a = 169;
	(*c).b = 'q';

	print_i((*c).a);
	print_c((*c).b);

	// print_s((char*)"\n- c: ");
	// print_address((void*)c); print_s((char*)"\n- d: ");
	// print_address((void*)d); print_c('\n');
	d = c;
	// print_s((char*)"\n- c: ");
	// print_address((void*)c); print_s((char*)"\n- d: ");
	// print_address((void*)d); print_c('\n');


	print_i((*d).a);
	print_c((*d).b);

}


