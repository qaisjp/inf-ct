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

int main() {
	return (call_first() && 0) && call_second();
}
