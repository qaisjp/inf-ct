/*gen:expect/*
Hello, world!A1337
/*gen:expect*/
char* str;
char male;

void main() {
    str = (char*) "Hello, world!";
    male = 'A';
    print_s(str);
    print_c(male);
    print_i(1337);
}
