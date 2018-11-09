/*gen:expect/*
qqq
/*gen:expect*/

#include "minic-stdlib.h"

char* a;
char* b;
void main() {
	char u;

	a = (char*) mcmalloc(sizeof(char));
	b = (char*) mcmalloc(sizeof(char));

	u = 'q';
	print_c(u);
	*a = u;
	print_c(*a);
	b = a;
	print_c(*b);
}
