IGLOBAL_SRC:=$(wildcard rosuda/util/*.java)
IBASE_SRC:=$(shell cat ibase.sources)
KLIMT_SRC:=$(wildcard rosuda/klimt/*.java) $(wildcard rosuda/klimt/plots/*.java)
PLUGINS_SRC:=$(wildcard rosuda/plugins/*.java)
JRCLIENT_SRC:=$(wildcard rosuda/JRclient/*.java)
IPLOTS_SRC:=$(wildcard rosuda/iplots/*.java)
IWIDGETS_SRC:=$(wildcard rosuda/iWidgets/*.java)

TARGETS=JRclient.jar ibase.jar klimt.jar iplots.jar iwidgets.jar

JAVAC=javac $(JFLAGS)

all: $(TARGETS)

define can-with-jar
	rm -rf org
	$(JAVAC) -d . $^
	jar fc $@ org
	rm -rf org	
endef

ibase.jar: $(IBASE_SRC)
	$(can-with-jar)

JRclient.jar: $(JRCLIENT_SRC)
	$(can-with-jar)

klimt.jar: $(IBASE_SRC) $(KLIMT_SRC) $(PLUGINS_SRC) $(JRCLIENT_SRC)
	rm -rf org
	$(JAVAC) -d . $^
	cp rosuda/projects/klimt/splash.jpg .
	jar fcm $@ rosuda/projects/klimt/Klimt.mft splash.jpg org
	rm -rf org splash.jpg

iplots.jar: $(IBASE_SRC) $(IPLOTS_SRC)
	$(can-with-jar)

iwidgets.jar: iplots.jar $(IWIDGETS_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath iplots.jar $(IWIDGETS_SRC)
	jar fc $@ org
	rm -rf org

clean:
	rm -rf $(TARGETS) org *~

.PHONY: clean all

