# JRI - Java/R Interface      experimental!
#--------------------------------------------------------------------------
#
# *** PLEASE MODIFY SETTINGS BELOW TO MATCH YOUR CONFIGURATION ***
# (later we'll use autoconf, but probably not before the first release ;))

#--- the following settings are OK for Macs
#JAVAINC=-I/System/Library/Frameworks/JavaVM.framework/Headers
#JNISO=.jnilib
#JNILD=-dynamiclib -framework JavaVM
#CPICF=-fno-common
#JAVAB=java

#--- the following might work on Linux
#JAVAHOME=/usr/lib/java
#JAVAINC=-I$(JAVAHOME)/include -I$(JAVAHOME)/include/linux
#JNISO=.so
#JNILD=-shared -L$(JAVAHOME)/jre/lib/i386/client -ljvm
#CPICF=-fPIC
#JAVAB=$(JAVAHOME)/bin/java

include Makefile.win

#--- comment out the following for non-debug version
CFLAGS+=-g

#--- uncomment the one that fits your R installation
RHOME=/Library/Frameworks/R.framework/Resources
#RHOME=/usr/lib/R
RHOME=N:/rw1090

#--- normally you don't need to change this - modify JAVAB instead
JAVAC=$(JAVAB)c $(JFLAGS)
JAVAH=$(JAVAB)h

#--------------------------------------------------------------------------
# you shouldn't need to touch anything below this line

RINC=-I$(RHOME)/src/include
RLD=-L$(RHOME)/bin -lR

TARGETS=libjri$(JNISO) rtest.class run

all: $(TARGETS)

src/org_rosuda_JRI_Rengine.h: org/rosuda/JRI/Rengine.class
	$(JAVAH) -d src org.rosuda.JRI.Rengine

src/Rengine.o: src/Rengine.c src/org_rosuda_JRI_Rengine.h
	$(CC) -c -o $@ src/Rengine.c $(CFLAGS) $(CPICF) $(JAVAINC) $(RINC)

src/jri.o: src/jri.c
	$(CC) -c -o $@ src/jri.c $(CFLAGS) $(CPICF) $(JAVAINC) $(RINC)

src/jri$(JNISO): src/Rengine.o src/jri.o
	$(CC) -o $@ $^ $(JNILD) $(RLD)

libjri$(JNISO): src/jri$(JNISO)
	ln -sf $^ $@

org/rosuda/JRI/Rengine.class org/rosuda/JRI/REXP.class org/rosuda/JRI/Mutex.class: Rengine.java REXP.java Mutex.java RMainLoopCallbacks.java
	$(JAVAC) -d . $^

rtest.class: rtest.java org/rosuda/JRI/Rengine.class org/rosuda/JRI/REXP.class
	$(JAVAC) rtest.java

run:
	echo "#!/bin/sh" > run
	echo "export R_HOME=$(RHOME)" >> run
	echo "export DYLD_LIBRARY_PATH=$(RHOME)/bin" >> run
	echo "export LD_LIBRARY_PATH=.:$(RHOME)/bin:$(JAVAHOME)/jre/lib/i386:$(JAVAHOME)/jre/lib/i386/client" >> run
	echo "$(JAVAB) rtest \$$*" >> run
	chmod a+x run

clean:
	rm -rf $(TARGETS) org src/*.o src/*~ src/org_rosuda_JRI_Rengine.h src/*$(JNISO) *.class *~

.PHONY: clean all

