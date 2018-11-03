#!/bin/bash

# First check if environment variable PROJ is set
if [ -z "$PROJ" ]; then
    echo "PROJ has not been set yet.";
    exit 1;
fi

SHOULD_COMPILE=false;
FILENAME="$1";
if [ "$1" = "-c" ]; then
    SHOULD_COMPILE=true;
    FILENAME="$2";
elif [ "$2" = "-c" ]; then
    SHOULD_COMPILE=true;
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
    echo "Filname \"$FILENAME\" does not exist";
    exit 1;
fi

# Attempt build
if $SHOULD_COMPILE; then
    echo "=== Build output below ==="
    ant build -f "$PROJ/build.xml";
fi

# Cancel if can't build
if [ "$?" -ne "0" ]; then
    exit 1;
fi

echo "";
echo "=== Generation debug output below ==="

TARGET="$(mktemp)"

java -ea -cp $PROJ/bin Main -gen "$FILENAME" "$TARGET"

echo "";
echo "=== Generation MIPS output below ==="

# Print out the target
cat "$TARGET"

# Print out the mips simulation
echo "";
echo "=== MIPS simulated output below ==="
java -jar "$PROJ/desc/part3/Mars4_5.jar" nc sm "$TARGET"

# Delete the target
rm "$TARGET"

