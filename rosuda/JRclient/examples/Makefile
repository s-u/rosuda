all: jt.class t.class

jt.class: jt.java ../JRclient.jar
	javac -classpath ../JRclient.jar jt.java

t.class: t.java ../JRclient.jar
	javac -classpath ../JRclient.jar t.java

run: jt.class ../JRclient.jar
	java -cp ../JRclient.jar:. jt

test: t.class ../JRclient.jar
	java -cp ../JRclient.jar:. t

clean:
	rm -rf *.class *~
