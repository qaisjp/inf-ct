#!/bin/bash

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

if [ "$1" = "ll" ]; then
    "$LLVM_DIR/bin/clang" -c -S -emit-llvm -Xclang -disable-O0-optnone "$TEST_FILE" -o "$LLVM_DIR/test.ll"
elif [ "$1" = "counter" ]; then
    export PASS="$PASSES/llvm-pass-instruction-counter"
    cd "$PASS/build"
    make -j8
    "$LLVM_DIR/bin/clang" -Xclang -load -Xclang "$PASS/build/skeleton/libSkeletonPass.so" "$TEST_FILE"
else
    echo "Arguments:"
    echo "- ll: creates the ll file"
    echo "- counter: builds and runs llvm-pass-instruction-counter"
fi