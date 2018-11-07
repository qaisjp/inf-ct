/*gen:expect/*
0
1
2
Hi
Hi!
/*gen:expect*/

// todo: add struct test

void main() {
	int a;      // 4 bytes  4 (a word)
	char b;     // 1 byte   4 (padded to 4)
	char c[4];  // 4 bytes  4 (padded to 4)
	char* d;    // 4 bytes  4 (a pointer)
	            //
	            // Total:   16 bytes

	// Print default value of a
	print_i(a);
	print_c('\n');

	// Print 1
	a = 1;
	print_i(a);
	print_c('\n');

	// Print a 2
	b = '2';
	print_c(b);
	print_c('\n');

	// Hi
	c[0] = 'H';
	c[1] = 'i';
	c[2] = '\0';
	print_s((char*) c);

	print_c('\n');

	c[2] = '!';
	c[3] = '\0';

	d = (char*) c;
	print_s(d);
}
