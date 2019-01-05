#!/usr/bin/env zsh

# Require PROJ
if [ -z "${PROJ}" ]; then
    echo "PROJ has not been set yet.";
    exit 1;
fi

# Set environment variables
export LLVM_DIR="$PROJ/build"
export PART4="$PROJ/ct-18-19/part-4"
export PASSES="$PART4/passes"
export TEST_FILE="$PART4/test.c"

# Associative array of passes
typeset -A passes
passes[counter]="llvm-pass-instruction-counter"
passes[simple]="llvm-pass-simple-dce"
passes[my]="llvm-pass-my-dce"
export PASSNAME="${passes[$1]}"

# Make sure we use the correct cmake
export CMAKE="cmake";
if type cmake3 > /dev/null; then
    CMAKE="cmake3";
fi

if [ "$1" = "ll" ]; then
    "$LLVM_DIR/bin/clang" -c -S -emit-llvm -Xclang -disable-O0-optnone "$TEST_FILE" -o "$LLVM_DIR/test.ll"
elif [ ! -z "${PASSNAME}" ]; then
    export PASS="$PASSES/$PASSNAME"
    cd "$PASS"
    if [ "$2" = "cmake" ]; then
        rm -rf build
        mkdir build
        cd build
        $CMAKE ..
    else
        cd build
        make -j8
        "$LLVM_DIR/bin/clang" -Xclang -load -Xclang "$PASS/build/skeleton/libSkeletonPass.so" "$TEST_FILE"
    fi
else
    echo "Arguments:"
    echo "- ll: creates the ll file"
    echo "- <PASSNAME>: builds and runs the pass (pass cmake as an extra arg to rebuild make project)"
fi
