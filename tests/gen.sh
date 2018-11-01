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
    ant build -f "$PROJ/build.xml";
fi

# Cancel if can't build
if [ "$?" -ne "0" ]; then
    exit 1;
fi

echo "";
echo "=== Generation output below ==="

TARGET="$(mktemp)"

java -ea -cp $PROJ/bin Main -gen "$FILENAME" "$TARGET"

# Print out the target
cat "$TARGET"

# Delete the target
rm "$TARGET"

