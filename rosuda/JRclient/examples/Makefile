all: jt.class

jt.class: jt.java ../JRclient.jar
	javac -classpath ../JRclient.jar jt.java

t.class: t.java ../JRclient.jar
	javac -classpath ../JRclient.jar t.java

run: jt.class ../JRclient.jar
	java -cp ../JRclient.jar:. jt

clean:
	rm -rf *.class *~
