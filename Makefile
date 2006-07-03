# Makefile for most Java-based RoSuDa projects
# $Id$
#
# Note that some projects may be better compiled using xcodebuild

JAPIURL:=http://java.sun.com/j2se/1.4.2/docs/api
ifeq ($(JDKVER),)
JDKVER:=1.4
endif
JFLAGS+=-encoding UTF-8 -target $(JDKVER) -source $(JDKVER)

#----------- source definitions --------------

IGLOBAL_SRC:=$(wildcard rosuda/util/*.java)
# PoGraSS must be generated manually, because SVG is optional
POGRASS_SRC:=rosuda/pograss/PoGraSS.java rosuda/pograss/PoGraSSPS.java rosuda/pograss/PoGraSSPDF.java rosuda/pograss/PoGraSSmeta.java rosuda/pograss/PoGraSSgraphics.java
# variables with XTREME suffix use JOGL for OpenGL
POGRASS_SRC_XTREME:=$(POGRASS_SRC) rosuda/pograss/PoGraSSjogl.java
IBASE_SRC_RAW:= $(IGLOBAL_SRC) $(wildcard rosuda/ibase/*.java) $(wildcard rosuda/ibase/plots/*.java) $(wildcard rosuda/ibase/toolkit/*.java) $(POGRASS_SRC) rosuda/plugins/Plugin.java rosuda/plugins/PluginManager.java
IBASE_SRC_XTREME:=rosuda/ibase/toolkit/PGSJoglCanvas.java
IBASE_SRC:=$(filter-out $(IBASE_SRC_XTREME),$(IBASE_SRC_RAW))
IBASE_SRC_JGR:=rosuda/ibase/toolkit/FrameDevice.java rosuda/ibase/toolkit/TJFrame.java $(wildcard rosuda/ibase/toolkit/Win*.java) $(wildcard rosuda/ibase/toolkit/WTentry*.java) rosuda/ibase/SCatSequence.java rosuda/ibase/SMarker.java $(wildcard rosuda/ibase/SVar*.java) rosuda/ibase/Commander.java rosuda/ibase/Common.java rosuda/ibase/Dependent.java $(wildcard rosuda/ibase/Map*.java) $(wildcard rosuda/ibase/Notif*.java) rosuda/ibase/Loader.java rosuda/plugins/Plugin.java rosuda/plugins/PluginManager.java $(wildcard rosuda/util/Global*.java) rosuda/util/ImageSelection.java rosuda/util/JSpacingPanel.java rosuda/util/ProgressDlg.java $(wildcard rosuda/util/Platform*.java) rosuda/util/RecentList.java rosuda/util/Stopwatch.java rosuda/util/Tools.java
KLIMT_SRC:=$(wildcard rosuda/klimt/*.java) $(wildcard rosuda/klimt/plots/*.java)
PLUGINS_SRC:=$(wildcard rosuda/plugins/*.java)
JRCLIENT_SRC:=$(wildcard rosuda/JRclient/*.java)
IPLOTS_SRC:=$(wildcard rosuda/iplots/*.java)
IWIDGETS_SRC:=$(wildcard rosuda/iWidgets/*.java)
JAVAGD_SRC:=$(wildcard rosuda/javaGD/*.java)
JGR_SRC:=$(wildcard rosuda/JGR/*.java) $(wildcard rosuda/JGR/toolkit/*.java) $(wildcard rosuda/JGR/util/*.java) $(wildcard rosuda/JGR/rhelp/*.java) $(wildcard rosuda/JGR/robjects/*.java) 
JRI_SRC:=$(wildcard rosuda/JRI/*.java)
RENGINE_SRC:=$(wildcard rosuda/REngine/*.java)
CLASSPATH_XTREME:=rosuda/projects/klimt/jogl.jar
ICUSTOM_SRC:=$(wildcard rosuda/icustom/*.java)

ifneq ($(shell uname),Darwin)
# remove all references to Mac platform as those classes can be compiled on a Mac only
IBASE_SRC:=$(filter-out %PlatformMac.java,$(IBASE_SRC))
IBASE_SRC_JGR:=$(filter-out %PlatformMac.java,$(IBASE_SRC_JGR))
KLIMT_SRC:=$(filter-out %PlatformMac.java,$(KLIMT_SRC))
IGLOBAL_SRC:=$(filter-out %PlatformMac.java,$(IGLOBAL_SRC))
IPLOTS_SRC:=$(filter-out %PlatformMac.java,$(IPLOTS_SRC))
JGR_SRC:=$(filter-out %PlatformMac.java,$(JGR_SRC))
endif

#ifneq ($(JOGL),yes)
#IBASE_SRC:=$(filter-out %Jogl% %JOGL%,$(IBASE_SRC))
IBASE_SRC:=$(filter-out %JOGLGraphicsDevice.java,$(IBASE_SRC))
#endif

#--------- targets ---------

TARGETS=REngine.jar JRclient.jar ibase.jar klimt.jar iplots.jar iwidgets.jar JGR.jar JGRinst.jar Mondrian.jar javaGD.jar icustom.jar

JAVAC=javac $(JFLAGS)

all: $(TARGETS)

define can-with-jar
	rm -rf org
	$(JAVAC) -d . $^
	jar fc $@ org
	rm -rf org	
endef

Mondrian.jar:
	$(MAKE) -C rosuda/Mondrian Mondrian.jar
	cp rosuda/Mondrian/Mondrian.jar .

JGR.jar: javaGD.jar $(IBASE_SRC_JGR) $(JGR_SRC) $(JRI_SRC)  $(RENGINE_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath javaGD.jar $(IBASE_SRC_JGR) $(JGR_SRC) $(JRI_SRC) $(RENGINE_SRC)
	cp rosuda/projects/jgr/splash.jpg .
	cp -r rosuda/projects/jgr/icons .
	jar fcm $@ rosuda/projects/jgr/JGR.mft splash.jpg icons org rosuda/JGR/LICENSE rosuda/JGR/GPL.txt
	rm -rf org splash.jpg icons

jgr-docs: $(JGR_SRC) 
	rm -rf JavaDoc
	mkdir JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link $(JAPIURL) $^


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

klimt-docs: $(IBASE_SRC) $(KLIMT_SRC) $(PLUGINS_SRC) $(JRCLIENT_SRC)
	rm -rf JavaDoc
	mkdir JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link $(JAPIURL) $^

REngine.jar: $(RENGINE_SRC)
	$(can-with-jar)

iplots.jar: $(IBASE_SRC) $(IPLOTS_SRC)
	$(can-with-jar)

javaGD.jar: $(JAVAGD_SRC)
	$(can-with-jar)

icustom.jar: iplots.jar REngine.jar $(ICUSTOM_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath iplots.jar:REngine.jar $(ICUSTOM_SRC)
	jar fc $@ org
	rm -rf org

iwidgets.jar: javaGD.jar JGR.jar $(IWIDGETS_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath javaGD.jar:JGR.jar $(IWIDGETS_SRC)
#	$(JAVAC) -d . -classpath iplots.jar:JGR.jar $(IWIDGETS_SRC)
	jar fc $@ org
	rm -rf org

docs: doc

doc: $(IBASE_SRC) $(KLIMT_SRC) $(PLUGINS_SRC) $(JRCLIENT_SRC) $(JGR_SRC) $(IPLOTS_SRC) $(IWIDGETS_SRC) $(JRI_SRC) $(JAVAGD_SRC) $(RENGINE_SRC) $(ICUSTOM_SRC)
	rm -rf JavaDoc
	mkdir JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link $(JAPIURL) $^

clean:
	rm -rf $(TARGETS) net org JavaDoc *~ rtest.class TextConsole.class *.java rosuda/JGRlinux*.tar.gz rosuda/JGRsrc*.tar.gz
	$(MAKE) -C rosuda/Mondrian clean

.PHONY: clean all doc docs

