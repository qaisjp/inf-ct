/*gen:expect/*
z
123
z128
a
24
z128
q104-

Everything:

-q123q104
a24q104
/*gen:expect*/

#include "minic-stdlib.h"

struct blah {
    char* blah_c;
    int blah_i;
};

char* globl_c;
int* globl_i;
struct blah* globl_b;

void main() {
	char* stack_c;
	int* stack_i;
	struct blah* stack_b;

	// Allocate space for all these pointers
	stack_c = (char*) mcmalloc(sizeof(char));
	stack_i = (int*) mcmalloc(sizeof(int));
	stack_b = (struct blah*) mcmalloc(sizeof(struct blah));
	
	globl_c = (char*) mcmalloc(sizeof(char));
	globl_i = (int*) mcmalloc(sizeof(int));
	globl_b = (struct blah*) mcmalloc(sizeof(struct blah));

	// s_c = 'z';
	*stack_c = 'z';
	print_c(*stack_c);

	print_c('\n'); // newline

	// s_i = 123;
	*stack_i = 123;
	print_i(*stack_i);

	print_c('\n'); // newline

	// s_b.blah_c struct assignments
	(*stack_b).blah_c = stack_c;
	print_c(*(*stack_b).blah_c); // z
	(*stack_b).blah_i = *stack_i + 5;
	print_i((*stack_b).blah_i); // 128

	print_c('\n'); // newline

	// g_c = 'a';
	*globl_c = 'a';
	print_c(*globl_c);

	print_c('\n'); // newline

	// g_i = 24;
	*globl_i = 24;
	print_i(*globl_i);

	print_c('\n'); // newline

	*globl_b = *stack_b;
	print_c(*(*stack_b).blah_c); // z
	print_i((*stack_b).blah_i); // 128

	print_c('\n'); // newline

	*(*stack_b).blah_c = 'q';
	print_c(*(*stack_b).blah_c); // q

	(*stack_b).blah_i = (*stack_b).blah_i - *globl_i; // 128 - 24 = 104
	print_i((*stack_b).blah_i); // 128

	print_c('-');

	///////
	print_s((char*) "\n\nEverything:\n\n");

	print_c('-');

	print_c(*stack_c);
	print_i(*stack_i);
	print_c(*(*stack_b).blah_c); // q
	print_i((*stack_b).blah_i); // 104

	print_c('\n');

	print_c(*globl_c);
	print_i(*globl_i);
	print_c(*(*stack_b).blah_c); // z
	print_i((*stack_b).blah_i); // 128
}
