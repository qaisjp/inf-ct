/*gen:expect/*
30-10101064
/*gen:expect*/

/*gen:put/*
SOMETHING GOES IN
/*gen:put*/

int a;
int b;
int c;

void main() {
	a = 1;
	b = 2;

	// add
	c = a + b;
	print_i(c); // 3

	print_i(0); // 0

	// sub
	c = a - b;
	print_i(c); // -1

	print_i(0); // 0

	// eq
	c = c == -1;
	print_i(c); // 1

	print_i(0); // 0

	// ne
	c = c != 1337;
	print_i(c); // 1

	print_i(0); // 0

	// mul
	c = 3;
	c = c * b; // 3 * 2
	print_i(c); // 6

	// mod
	c = 9; // c is now 9
	b = 5; // b is now 5
	a = c % b; // c mod b = 9 mod 5 = 4
	print_i(a); // 4
}
