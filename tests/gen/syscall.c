/*gen:put/*
2
a
/*gen:put*/

/*gen:expect/*
Hello, world!
2
a
/*gen:expect*/

int a;
char c;
void main() {
    // Print string
    print_s((char*) "Hello, world!\n");

    // Read integer
    a = read_i();

    // Print integer (from expression)
    print_i(a);

    // Print char (from literal)
    print_c('\n');

    // Read character
    c = read_c();

    // Print character (from expression)
    print_c(c);
}

