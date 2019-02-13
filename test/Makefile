#
# A simple makefile for compiling three java classes
#

# define a makefile variable for the java compiler
#
JCC = javac

JUNIT = ".:/Library/Junit/junit-4.10.jar:" 

JFLAGS = -classpath $(JUNIT)

default: test

test:
		$(JCC) $(JFLAGS) *.java

clean: 
		$(RM) *.class
