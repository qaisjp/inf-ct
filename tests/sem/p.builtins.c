void t_ps() {
    return print_s((char*) "me");
}

void t_pi() {
    return print_i(1);
}

void t_pc() {
    return print_c('c');
}

char t_rc() {
    return read_c();
}

int t_ri() {
    return read_i();
}

void* t_mcm() {
    return mcmalloc(16);
}
