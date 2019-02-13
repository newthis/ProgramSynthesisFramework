#!/bin/bash

JUNIT=".:/Library/Junit/junit-4.10.jar:" 
JFLAGS="-classpath $JUNIT"

$(make)
java $JFLAGS TestRunner
$(make clean)
