/*gen:expect/*
30-10101
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
}
