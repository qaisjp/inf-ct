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
    echo -e "${GREY}=== Build output below ===${NC}"
    ant build -f "$PROJ/build.xml";
fi

# Cancel if can't build
if [ "$?" -ne "0" ]; then
    exit 1;
fi

echo "";
echo -e "${CYAN}=== Generation debug output below ===${NC}"

TARGET="$(mktemp)"

java -ea -cp $PROJ/bin Main -gen "$FILENAME" "$TARGET"

echo "";
echo -e "${RED}=== Generation MIPS output below ===${NC}"

# Print out the target
cat "$TARGET"

# Print out the mips simulation
echo "";
echo -e "${YELLOW}=== MIPS simulated output below ===${NC}"
java -jar "$PROJ/desc/part3/Mars4_5.jar" nc sm "$TARGET"

# Delete the target
rm "$TARGET"

