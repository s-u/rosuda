all: JRclient.jar
	make -C examples

JRclient.jar: *.java
	rm -rf org
	javac -d . *.java
	jar fvc JRclient.jar org
	rm -rf org

clean:
	rm -rf JRclient.jar org *~
	make -C examples clean

