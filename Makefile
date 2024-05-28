JAVAC = javac
JFLAGS =

SRCS = $(wildcard *.java)
OBJS = $(SRCS:%.java=%.class)

all: $(OBJS)

%.class: %.java
	$(JAVAC) $(JFLAGS) $<

clean:
	del *.class

run: all
	java Main

.PHONY: all clean run
