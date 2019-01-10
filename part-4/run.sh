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
export LL_FILE="$PART4/test-pre.ll"
export NEW_LL_FILE="$PART4/test-post.ll"

# Associative array of pass directories
typeset -A passDir
passDir[counter]="llvm-pass-instruction-counter"
passDir[simple]="llvm-pass-simple-dce"
passDir[my]="llvm-pass-my-dce"
export PASSNAME="${passDir[$1]}"

# Associative array of pass names
typeset -A passName
passName[counter]="skeletonpass"
passName[simple]="skeletonpass"
passName[my]="live"
export PASSARG="-${passName[$1]}"

# Make sure we use the correct cmake
export CMAKE="cmake";
if type cmake3 > /dev/null; then
    CMAKE="cmake3";
fi

if [ "$1" = "ll" ]; then
    "$LLVM_DIR/bin/clang" -S -emit-llvm -Xclang -disable-O0-optnone "$TEST_FILE" -o "$LL_FILE"
elif [ "$1" = "diff" ]; then
    diff "$LL_FILE" "$NEW_LL_FILE"
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

        # Cancel if can't build
        if [ "$?" -ne "0" ]; then
            exit 1;
        fi

        # This does not run mem2reg, so our DCE will not work with this command.
        # "$LLVM_DIR/bin/clang" -Xclang -load -Xclang "$PASS/build/skeleton/libSkeletonPass.so" "$TEST_FILE"

        # Pipe to /dev/null needed because opt returns bytecode. Messages are printed to stderr.
        "$LLVM_DIR/bin/opt" -S -load "$PASS/build/skeleton/libSkeletonPass.so" -mem2reg "$PASSARG" "$LL_FILE" -o "$NEW_LL_FILE"
    fi
else
    echo "Arguments:"
    echo "- ll: creates the ll file"
    echo "- <PASSNAME>: builds and runs the pass (pass cmake as an extra arg to rebuild make project)"
fi
