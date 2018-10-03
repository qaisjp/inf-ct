

struct a {
 int b;
 int c;
 char d[2];
 char e;
 void f;
};

struct cont {
    struct a a[5];
};

void this() {

}

int that(char t) {
    return (int) t + 1;
}

char* m() {

}

struct a b() {

}

struct a* b() {

}

int main() {
    int a;
    int b[2];
    int temp;
    struct a str;
    struct cont container;

    b[0] = a;
    a = a + 1;
    temp = b[0];
    b[0] = a;
    b[1] = temp;

    str.d[0] = (char)b[0];

    container.a[0] = str;

    if (container.a[0].d[0] == 1) {
        temp = temp + container.a[0].d[0];
    }
    
    return container.a[0].d[0];
}