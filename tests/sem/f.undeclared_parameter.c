void test_params(int a, char b, void* c, int* d, char* d) {}

void main() {
    test_params(1, (char) 2, (char*) "This");
    test_params(1, (char) 2, (int*) ((char*) "This"), "That");
    test_params(1, (char) 2, (int*) (char*) "This", (char*) "That", "Ours");
    test_params();
    test_params("asdf");
}

