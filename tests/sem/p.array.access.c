int a[2];

int that() {
    return 1;
}

void main() {
    a[that()];
}