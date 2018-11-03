// Test arrays
int intArray[5];
char charArray[5];
char* stringArray[2];

// Test primitives
int integer;
char character;
// void voids can't be tested;

// Test pointers
int* intPointer;
char* charPointer;

// Test 4-byte boundaries
char charArray_be_4_01[1]; // should be 4
char charArray_be_4_02[2]; // should be 4
char charArray_be_4_03[3]; // should be 4
char charArray_be_4_04[4]; // should be 4
char charArray_be_8_01[5]; // should be 8
char charArray_be_8_02[7]; // should be 8
char charArray_be_8_03[8]; // should be 8
char charArray_be_12_01[9]; // should be 12
