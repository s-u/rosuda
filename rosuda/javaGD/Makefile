SRC:=$(wildcard *.java)
OBJ:=$(SRC:%.java=org/rosuda/javaGD/%.class)
JAR=javaGD.jar

TARGETS=$(JAR)

JAVAC=javac $(JFLAGS)

all: $(JAR)

#org/rosuda/javaGD/JavaGD.class: JavaGD.java org/rosuda/javaGD/GDCanvas.class
#	$(JAVAC) -d . $<

#org/rosuda/javaGD/%.class: %.java
#	$(JAVAC) -d . $^

$(JAR):
	$(JAVAC) -d . *.java
	jar fc $@ org

clean:
	rm -rf org *~ $(TARGETS)

.PHONY: all clean
