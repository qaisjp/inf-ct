int main() {
    foo();
    return 1;
}

char another() {
    return 'a';
}

int foo() {
  int a = 7;
  int b = a * 2;
  int c = b - a;   // dead
  int d = c / a;   // dead
  return b;
}

