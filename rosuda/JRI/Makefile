# JRI - Java/R Interface      experimental!
#--------------------------------------------------------------------------
#
# *** PLEASE MODIFY SETTINGS BELOW TO MATCH YOUR CONFIGURATION ***
# (later we'll use autoconf, but probably not before the first release ;))

#--- the following settings are OK for Macs
JAVAINC=-I/System/Library/Frameworks/JavaVM.framework/Headers
JNISO=.jnilib
JNILD=-dynamiclib -framework JavaVM
CPICF=-fno-common

#--- the following might work on Linux (if you fix the -L...)
#JAVAHOME=/usr/lib/java
#JAVAINC=-I$(JAVAHOME)/include -I$(JAVAHOME)/include/linux
#JNISO=.so
#JNILD=-L/$(JAVAHOME)/jre/client/i386
#CPICF=-fPIC

#--- comment out the following for non-debug version
CFLAGS+=-g

#--- uncomment the one that fits your R installation
RHOME=/Library/Frameworks/R.framework/Resources
#RHOME=/usr/local/lib/R

#--- if javac is not in the PATH you may want to change the following one
JAVAC=javac $(JFLAGS)


#--------------------------------------------------------------------------
# you shouldn't need to touch anything below this line

RINC=-I$(RHOME)/include
RLD=-L$(RHOME)/bin -lR

TARGETS=libjri$(JNISO) rtest.class

all: $(TARGETS)

src/Rengine.h: Rengine.class
	javah -d src Rengine

src/Rengine.o: src/Rengine.c src/Rengine.h
	$(CC) -c -o $@ src/Rengine.c $(CFLAGS) $(CPICF) $(JAVAINC) $(RINC)

src/jri.o: src/jri.c
	$(CC) -c -o $@ src/jri.c $(CFLAGS) $(CPICF) $(JAVAINC) $(RINC)

src/jri$(JNISO): src/Rengine.o src/jri.o
	$(CC) -o $@ $^ $(JNILD) $(RLD)

libjri$(JNISO): src/jri$(JNISO)
	ln -sf $^ $@

Rengine.class RXP.class: Rengine.java RXP.java
	$(JAVAC) $^

rtest.class: rtest.java Rengine.class RXP.class
	$(JAVAC) rtest.java

clean:
	rm -rf $(TARGETS) src/*.o src/*~ src/Rengine.h src/*$(JNISO) *.class *~

.PHONY: clean all

