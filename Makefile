JRclient.jar: rosuda/JRclient/*.java
	rm -rf org
	javac -d . rosuda/JRclient/*.java
	jar fvc JRclient.jar org
	rm -rf org

clean:
	rm -rf JRclient.jar org *~

