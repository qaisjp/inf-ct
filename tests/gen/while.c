/*gen:expect/*
10
9
8
7
6
5
4
3
2
1
0
-1

/*gen:expect*/
#include "minic-stdlib.h"

void main() {
    int counter;
    counter = 100;

    {
        int counter;
        counter = 10;
        while (counter != -2) {
            int i;
            i = 0;
            print_i(counter + i);
            print_c('\n');
            counter = counter - 1 + i;
        }
    }
}