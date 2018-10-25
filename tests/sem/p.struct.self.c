struct person {
    char* name;
    int age;

    struct person* children;
};

struct house {
    struct person occupants[10];
    struct house garage;
};
