/*gen:expect/*
First called!
/*gen:expect*/

int call_first() {
	print_s((char*) "First called!");
	return 1;
}

int call_second() {
	print_s((char*) "Hello, world!");
	return 1;
}

void main() {
    int n;

    call_first();

    n = 0 && call_second();

	// return 0;
}
