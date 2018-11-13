#!/bin/bash

# Colors
GREY='\033[0;37m' # Well, light grey.
RED='\033[1;31m' # It's actually light red
NC='\033[0m' # No Color
CYAN='\033[0;36m'
YELLOW='\033[1;33m'

# First check if environment variable PROJ is set
if [ -z "$PROJ" ]; then
    echo "PROJ has not been set yet.";
    exit 1;
fi

# Change to proj
cd "$PROJ"

SHOULD_COMPILE=false;
SHOULD_EDIT=false;
FILENAME="$1";

if [ "$1" = "-e" ]; then
    SHOULD_EDIT=true;
    FILENAME="$2";
elif [ "$2" = "-e" ]; then
    SHOULD_EDIT=true;
fi

if $SHOULD_EDIT; then
    "$EDITOR" "$FILENAME";
    exit 0;
fi

if [ "$1" = "-c" ]; then
    SHOULD_COMPILE=true;
    FILENAME="$2";
elif [ "$2" = "-c" ]; then
    SHOULD_COMPILE=true;
fi

SHOULD_GCC=false;
if [ "$1" = "-gcc" ]; then
    SHOULD_GCC=true;
    FILENAME="$2";
elif [ "$2" = "-gcc" ]; then
    SHOULD_GCC=true;
fi

# Check if args provided
if [ "$FILENAME" = "" ]; then
    echo "$0 filename.c";
    echo "";
    echo "Provide flag -c anywhere to compile as well";
    echo "Note: \$PROJ environment variable must be defined";
    echo "";
    exit 0;
fi

# Check if file exists
if [ ! -f "$FILENAME" ]; then
    echo "Filename \"$FILENAME\" does not exist";
    exit 1;
fi

# If gcc, we do something completely different.
if $SHOULD_GCC; then
    echo -e "${GREY}=== GCC output below${NC}";
    EXEC_FILE=$(mktemp);
    gcc "$FILENAME" -o "$EXEC_FILE";
    echo "";
    echo -e "${CYAN}=== Executable running${NC}";
    "$EXEC_FILE";
    exit $?;
fi

# Attempt build
if $SHOULD_COMPILE; then
    echo -e "${GREY}=== Build output below ===${NC}"
    ant build -f "$PROJ/build.xml";
fi

# Cancel if can't build
if [ "$?" -ne "0" ]; then
    exit 1;
fi

echo "";
echo -e "${CYAN}=== Codegen debug output below ===${NC}"

TARGET="$(dirname $FILENAME)/tmp.$(basename $FILENAME).asm"

java -ea -cp $PROJ/bin Main -gen "$FILENAME" "$TARGET"

CODEGEN_CODE="$?"

echo "";
echo -e "${RED}=== Codegen MIPS output below ===${NC}"

# Print out the target
echo "cat \"$TARGET\""
cat -n "$TARGET"
echo "cat \"$TARGET\""

# Print out the mips simulation
echo "";
echo -e "${YELLOW}=== MARS output below ===${NC}"

# Don't run MARS simulator if we couldn't generate MIPS
if [ "$CODEGEN_CODE" -ne "0" ]; then
    echo "Code generation status code non-zero: $CODEGEN_CODE"
    exit 1;
fi

java -jar "$PROJ/desc/part3/Mars4_5.jar" nc sm "$TARGET"
SIMULATOR_CODE="$?"
if [ "$SIMULATOR_CODE" -ne "0" ]; then
    echo "ALL OK: Simulator status code non-zero: $SIMULATOR_CODE"
fi
