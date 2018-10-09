#include "../minic-stdlib.h"

char contents[5];

char* str(int a) {
    contents[0] = 'h';
    contents[1] = 'e';
    contents[2] = 'l';
    contents[3] = 'l';
    contents[4] = 'o';
    
    return &contents[0];
}

int main() {
    char* (*t)(int) = &str;
    char* (*x[1])(int) = {t};

    print_c(x[0](2));

    print_c(str(1)[4]);

    if (str(1)[4] == 'o') {
        return 1;
    }

    return 0;
}
