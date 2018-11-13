/*gen:expect/*
123
Initial: 567
Broken: 567
Pointer: 568
Person Qais is 567 years old and is of gender M.

Person Qaisi is 568 years old and is of gender F.

/*gen:expect*/
#include "minic-stdlib.h"

struct person {
    char* name;
    int age;
    char gender;
};

struct person global_qais;

void printc(char c) {
    print_c(c);
}

int add(int i, char c) {
    return i + (int) c;
}

void broken_happy_bday(struct person someone) {
    someone.age = someone.age + 1;
}

void pointer_happy_bday(struct person* someone) {
    (*someone).age = (*someone).age + 1;
}

void print_person(struct person guy) {
    print_s((char*)"\nPerson ");
    print_s(guy.name);
    print_s((char*)" is ");
    print_i(guy.age);
    print_s((char*)" years old and is of gender ");
    print_c(guy.gender);
    print_c('.');
    print_c('\n');
}

void main() {
    struct person* blah;

    print_i(add(26, 'a'));
    printc('\n');

    global_qais.gender = 'M';
    global_qais.age = 567;
    global_qais.name = (char*) "Qais";

    print_s((char*)"Initial: ");
    print_i(global_qais.age);

    broken_happy_bday(global_qais);
    print_s((char*)"\nBroken: ");
    print_i(global_qais.age);

    blah = (struct person*) mcmalloc(sizeof(struct person));
    (*blah).age = global_qais.age;
    (*blah).name = (char*) "Qaisi";
    (*blah).gender = 'F';

    pointer_happy_bday(blah);
    print_s((char*)"\nPointer: ");
    print_i((*blah).age);

    print_person(global_qais);
    print_person(*blah);
}