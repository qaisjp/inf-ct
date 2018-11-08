/*gen:expect/*
success
/*gen:expect*/

struct blah {
    int x;
};

struct blah s;
int s_x;

void main() {
	print_s((char*) "success");
}
