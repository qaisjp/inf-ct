/*gen:expect/*
== Stack
stack_id:
- exists: s
- email: stack should be: 5, t - okay?

stack_qais:
- id:
    - exists: s
    - email: stack should be: 5, t - okay?
- age: 5
- email: t


== Globals
global_id:
- exists: g
- email: global should be: 8, h - okay?

global_qais:
- id:
    - exists: g
    - email: global should be: 8, h - okay?
- age: 8
- email: h
11
/*gen:expect*/
/*gen:debug*/

struct online {
    char exists;
    char* email;
};

struct person {
    struct online id;
    int age;
    char gender;
};

struct online global_id;
struct person global_qais;

struct online make_id(char exists, char* email) {
    struct online out;
    out.exists = exists;
    out.email = email;
    return out;
}

struct person make_person(struct online id, int age, char gender) {
    struct person p;
    p.id = id;
    p.age = age;
    p.gender = gender;
    return p;
}

void main() {
    struct online stack_id;		// 8 bytes   8
    struct person stack_qais; 	// 16 bytes  16
                                //
                                // Real total: 24 bytes

    int originalSP;
    int originalFP;
    originalSP = get_register(29);
    originalFP = get_register(30);

    print_s((char*) "== Stack\n");

    stack_id = make_id('s', (char*) "stack should be: 5, t - okay?");
    stack_qais = make_person(stack_id, 5, 't');

    print_s((char*) "stack_id:\n");

    print_s((char*) "- exists: ");
    print_c(stack_id.exists);
    print_c('\n');

    print_s((char*) "- email: ");
    print_s(stack_id.email);
    print_c('\n');

    print_s((char*) "\nstack_qais:\n");

    print_s((char*) "- id:\n");
    print_s((char*) "    - exists: ");
    print_c(stack_qais.id.exists);
    print_c('\n');
    print_s((char*) "    - email: ");
    print_s(stack_qais.id.email);
    print_c('\n');

    print_s((char*) "- age: ");
    print_i(stack_qais.age);
    print_c('\n');

    print_s((char*) "- email: ");
    print_c(stack_qais.gender);
    print_c('\n');



    print_s((char*) "\n\n== Globals\n");

    // Globals
    global_id = make_id('g', (char*) "global should be: 8, h - okay?");
    global_qais = make_person(global_id, 8, 'h');

    print_s((char*) "global_id:\n");

    print_s((char*) "- exists: ");
    print_c(global_id.exists);
    print_c('\n');

    print_s((char*) "- email: ");
    print_s(global_id.email);
    print_c('\n');

    print_s((char*) "\nglobal_qais:\n");

    print_s((char*) "- id:\n");
    print_s((char*) "    - exists: ");
    print_c(global_qais.id.exists);
    print_c('\n');
    print_s((char*) "    - email: ");
    print_s(global_qais.id.email);
    print_c('\n');

    print_s((char*) "- age: ");
    print_i(global_qais.age);
    print_c('\n');

    print_s((char*) "- email: ");
    print_c(global_qais.gender);
    print_c('\n');

    print_i(originalSP == get_register(29));
    print_i(originalFP == get_register(30));
}
