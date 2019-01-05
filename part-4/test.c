int some_number = 1234;

char another() {
    return 'a';
}

int foo() {
  int a = 7;
  int b = a * 2;
  int c = b - a; // dead
  int d = c / a; // dead (depends on c)
  return b;
}

int* something() {
  return &some_number;
}

int main() {
  int a = foo();
  int b = a + 2; // dead
  return 1;
}
