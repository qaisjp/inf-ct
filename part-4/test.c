int some_number = 1234;

char another() {
    return 'a';
}

int switchy() {
  char grade = '0';
  int n = 0;
  int b = 5;

  switch(grade) {
      case 'A' :
        n = 1;
        break;
      case 'B' :
        n = 2;
        break;
      case 'C' :
          n = 3;
          int c = n + b;
         grade = 'F';
         break;
      case 'D' :
          n = 4;
         break;
      case 'F' :
          n = 5;
         break;
      default :
          n = 6;
         b = 8;
   }

  return n;
}

int foo() {
  volatile int volly = 1337;
  int a = 7;
  int b = a * 2;
  int c = b - a; // dead
  int d = c / a; // dead (depends on c)
  return b;
}

int* something() {
  char a = 'a';
  return &some_number;
}

int main() {
  int a = foo();
  int b = a + 2; // dead
  return 1;
}

int sum(int a, int b) {
  int i;
  int res = 1;

  for (i = a; i < b; i++) {
    res *= i;
  }

  return res;
}
