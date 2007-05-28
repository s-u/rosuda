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
#IBASE_SRC_JGR:=rosuda/ibase/toolkit/FrameDevice.java rosuda/ibase/toolkit/TJFrame.java $(wildcard rosuda/ibase/toolkit/Win*.java) $(wildcard rosuda/ibase/toolkit/WTentry*.java) rosuda/ibase/SCatSequence.java rosuda/ibase/SMarker.java $(wildcard rosuda/ibase/SVar*.java) rosuda/ibase/Commander.java rosuda/ibase/Common.java rosuda/ibase/Dependent.java $(wildcard rosuda/ibase/Map*.java) $(wildcard rosuda/ibase/Notif*.java) rosuda/ibase/Loader.java rosuda/plugins/Plugin.java rosuda/plugins/PluginManager.java $(wildcard rosuda/util/Global*.java) rosuda/util/ImageSelection.java rosuda/util/JSpacingPanel.java rosuda/util/ProgressDlg.java $(wildcard rosuda/util/Platform*.java) rosuda/util/RecentList.java rosuda/util/Stopwatch.java rosuda/util/Tools.java
KLIMT_SRC:=$(wildcard rosuda/klimt/*.java) $(wildcard rosuda/klimt/plots/*.java)
PLUGINS_SRC:=$(wildcard rosuda/plugins/*.java)
JRCLIENT_SRC:=$(wildcard rosuda/JRclient/*.java)
IPLOTS_SRC:=$(wildcard rosuda/iplots/*.java)
IWIDGETS_SRC:=$(wildcard rosuda/iWidgets/*.java)
JAVAGD_SRC:=$(wildcard rosuda/javaGD/*.java)
JGR_SRC:=rosuda/JGR/editor/FindReplaceDialog.java $(wildcard rosuda/JGR/*.java) $(wildcard rosuda/JGR/toolkit/*.java) $(wildcard rosuda/JGR/util/*.java) $(wildcard rosuda/JGR/rhelp/*.java) $(wildcard rosuda/JGR/robjects/*.java) 
JRI_SRC:=$(wildcard rosuda/JRI/*.java)
RENGINE_SRC:=$(wildcard rosuda/REngine/*.java)
CLASSPATH_XTREME:=rosuda/projects/klimt/jogl.jar
ICUSTOM_SRC:=$(wildcard rosuda/icustom/*.java)
MRJSTUBS_SRC:=$(wildcard rosuda/util/MRJstubs/*.java)

#ifneq ($(JOGL),yes)
#IBASE_SRC:=$(filter-out %Jogl% %JOGL%,$(IBASE_SRC))
IBASE_SRC:=$(filter-out %JOGLGraphicsDevice.java,$(IBASE_SRC))
#endif

#--------- targets ---------

TARGETS=REngine.jar JRclient.jar ibase.jar klimt.jar iplots.jar iwidgets.jar JGR.jar JRI.jar Mondrian.jar javaGD.jar icustom.jar

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

JGR.jar: javaGD.jar ibase.jar JRI.jar MRJstubs.jar $(JGR_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath javaGD.jar:ibase.jar:JRI.jar:MRJstubs.jar $(JGR_SRC)
	cp rosuda/projects/jgr/splash.jpg jgrsplash.jpg
	cp -r rosuda/projects/jgr/icons .
	jar fcm $@ rosuda/projects/jgr/JGR.mft jgrsplash.jpg icons org rosuda/JGR/LICENSE rosuda/JGR/GPL.txt
	rm -rf org jgrsplash.jpg icons

jgr-docs: $(JGR_SRC) 
	rm -rf JavaDoc
	mkdir JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link $(JAPIURL) $^


ibase.jar: MRJstubs.jar $(IBASE_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath $^
	jar fc $@ org
	rm -rf org	

JRclient.jar: $(JRCLIENT_SRC)
	$(can-with-jar)

klimt.jar: MRJstubs.jar $(IBASE_SRC) $(KLIMT_SRC) $(PLUGINS_SRC) $(JRCLIENT_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath $^
	cp rosuda/projects/klimt/splash.jpg .
	jar fcm $@ rosuda/projects/klimt/Klimt.mft splash.jpg org
	rm -rf org splash.jpg

klimt-docs: $(IBASE_SRC) $(KLIMT_SRC) $(PLUGINS_SRC) $(JRCLIENT_SRC)
	rm -rf JavaDoc
	mkdir JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link $(JAPIURL) $^

REngine.jar: $(RENGINE_SRC)
	$(can-with-jar)

iplots.jar: MRJstubs.jar $(IBASE_SRC) $(IPLOTS_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath $^
	jar fc $@ org
	rm -rf org	

javaGD.jar: $(JAVAGD_SRC)
	$(can-with-jar)

#icustom.jar: iplots.jar REngine.jar $(ICUSTOM_SRC)
icustom.jar: iplots.jar JRI.jar $(ICUSTOM_SRC)
	rm -rf org
#	$(JAVAC) -d . -classpath iplots.jar:REngine.jar $(ICUSTOM_SRC)
	$(JAVAC) -d . -classpath iplots.jar:JRI.jar $(ICUSTOM_SRC)
	jar fc $@ org
	rm -rf org

iwidgets.jar: javaGD.jar JGR.jar ibase.jar JRI.jar $(IWIDGETS_SRC)
	rm -rf org
	$(JAVAC) -d . -classpath javaGD.jar:JGR.jar:ibase.jar:JRI.jar $(IWIDGETS_SRC)
#	$(JAVAC) -d . -classpath iplots.jar:JGR.jar $(IWIDGETS_SRC)
	jar fc $@ org
	rm -rf org

JRI.jar: $(JRI_SRC)
	$(can-with-jar)

MRJstubs.jar: $(MRJSTUBS_SRC)
# MRJ stubs go into com.apple.mrj. so we can't use can-with-jar
	rm -rf com
	$(JAVAC) -d . $^
	jar fc $@ com
	rm -rf com

docs: doc

doc: $(IBASE_SRC) $(KLIMT_SRC) $(PLUGINS_SRC) $(JRCLIENT_SRC) $(JGR_SRC) $(IPLOTS_SRC) $(IWIDGETS_SRC) $(JRI_SRC) $(JAVAGD_SRC) $(RENGINE_SRC) $(ICUSTOM_SRC)
	rm -rf JavaDoc
	mkdir JavaDoc
	javadoc -d JavaDoc -author -version -breakiterator -link $(JAPIURL) $^

clean:
	rm -rf `find . -name ".DS_*"`
	rm -rf $(TARGETS) com net org JavaDoc *~ rtest.class TextConsole.class *.java rosuda/JRI*.tar.gz rosuda/JGRlinux*.tar.gz rosuda/JGRsrc*.tar.gz rosuda/projects/klimt/build rosuda/projects/jgr/build rosuda/projects/iplots/build
	$(MAKE) -C rosuda/Mondrian clean

.PHONY: clean all doc docs

