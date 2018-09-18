#include "minic-stdlib.h"

void main() {
  int n;
  int first;
  int second;
  int next;
  int c;
  char t;

  // check to make sure keywords
  // don't affect identifiers
  // (these are unused)
  char ifner;
  char elser;
  char returner;

  // read n from the standard input
  n = read_i();

  // double 0 to check multiple digit int literal
  first = 00;
  second = 1;
    
  print_s((char*)"First ");
  print_i(n);
  print_s((char*)" terms of Fibonacci series are : ");
 
  c = 0;
  while (c < n) {
    if ( c <= 1 )
      next = c;
    else
      {
	next = first + second;
	first = second;
	second = next;
      }
    print_i(next);
    print_s((char*)" ");
    c = c+1;
  }
}
