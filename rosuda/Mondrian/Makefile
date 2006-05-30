MONDRIAN_SRC:=$(shell ./parsepb Mondrian.pbproj)

JAVAC=javac $(JFLAGS)

TARGETS=Mondrian.jar

all: $(TARGETS)

Mondrian.jar: $(MONDRIAN_SRC)
	rm -f *.class
	$(JAVAC) $^
	jar fcm $@ Mondrian.mft *.class *.gif com

docs: $(MONDRIAN_SRC)
	mkdir -p JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link http://java.sun.com/j2se/1.4.2/docs/api $^

clean:
	rm -rf JavaDoc
	rm -f $(TARGETS) *.class *~

.PHONY: clean all
