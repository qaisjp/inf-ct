struct sa {
	int b;
};

struct sa name() {
	struct sa that;
	return that;
}

int main() {
	return name().b;
}