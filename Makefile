IGLOBAL_SRC:=$(wildcard rosuda/util/*.java)
# PoGraSS must be generated manually, because SVG is optional
POGRASS_SRC:=rosuda/pograss/PoGraSS.java rosuda/pograss/PoGraSSPS.java rosuda/pograss/PoGraSSPDF.java rosuda/pograss/PoGraSSmeta.java rosuda/pograss/PoGraSSgraphics.java
IBASE_SRC:= $(IGLOBAL_SRC) $(wildcard rosuda/ibase/*.java) $(wildcard rosuda/ibase/plots/*.java) $(wildcard rosuda/ibase/toolkit/*.java) $(POGRASS_SRC) rosuda/plugins/Plugin.java rosuda/plugins/PluginManager.java
KLIMT_SRC:=$(wildcard rosuda/klimt/*.java) $(wildcard rosuda/klimt/plots/*.java)
PLUGINS_SRC:=$(wildcard rosuda/plugins/*.java)
JRCLIENT_SRC:=$(wildcard rosuda/JRclient/*.java)
IPLOTS_SRC:=$(wildcard rosuda/iplots/*.java)
IWIDGETS_SRC:=$(wildcard rosuda/iWidgets/*.java)
RGUI_SRC:=$(wildcard rosuda/RGui/*.java) $(wildcard rosuda/RGui/toolkit/*.java)

TARGETS=JRclient.jar ibase.jar klimt.jar iplots.jar iwidgets.jar RGui.jar

JAVAC=javac $(JFLAGS)

all: $(TARGETS)

define can-with-jar
	rm -rf org
	$(JAVAC) -d . $^
	jar fc $@ org
	rm -rf org	
endef

RGui.jar: $(IBASE_SRC) $(RGUI_SRC)
	rm -rf org
	$(JAVAC) -d . $^
	cp rosuda/RGui/splash.jpg org/rosuda/RGui
	cp -r rosuda/RGui/icons org/rosuda/RGui
	echo "Main-class: org.rosuda.RGui.RGui" > RGui.mft
	jar fcm $@ RGui.mft org
	rm -rf org RGui.mft

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

