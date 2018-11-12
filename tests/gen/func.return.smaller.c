/*gen:expect/*
1234
/*gen:expect*/
int a() {
	return 1234;
}

void main() {
	int b;
	b = a();

	print_i(b);
}
