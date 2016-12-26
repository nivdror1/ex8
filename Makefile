###############################################################################
#
# Makefile for Java project
#
# Students:
# Bart Simpson, ID 011111111, bart@cs.huji.ac.il
# Lisa Simpson, ID 022222222, lisa@cs.huji.ac.il
#
###############################################################################

JAVAC=javac
JAVACFLAGS=

SRCS=*.java
EXEC=VMtranslator

TAR=tar
TARFLAGS=cvf
TARNAME=project7.tar
TARSRCS=$(SRCS) $(EXEC) README Makefile

all: compile

compile:
	$(JAVAC) $(JAVACFLAGS) $(SRCS)
	chmod +x $(EXEC)

tar:
	$(TAR) $(TARFLAGS) $(TARNAME) $(TARSRCS)

clean:
	rm -f *.class *~

