import java.io.File;

public class JRIBootstrap {
    String rHome;
    String rJavaHome;

    public JRIBootstrap() {
	rHome = findR();
	if (rHome == null)
	    fail("Unable to find R!");
	rJavaHome = findRJava();
	if (rJavaHome == null) {
	    // installRJava();
	    rJavaHome = findRJava();
	    fail("Unable to find and install rJava!");
	}
    }

    public static final int HKLM = 0; // HKEY_LOCAL_MACHINE
    public static final int HKCU = 1; // HKEY_CURRENT_USER

    public static native String getenv(String var);
    public static native void setenv(String var, String val);
    
    public static native String regvalue(int root, String key, String value);
    public static native String[] regsubkeys(int root, String key);

    public static native String expand(String val);

    String findR() {
	return null;
    }

    String findRJava() {
	return null;
    }

    String extractLibrary(String basename) {
	String libName = "lib"+basename;
	String ext = ".so";
	String os = System.getProperty("os.name");
	if (os.startsWith("Win")) {
	    os = "Win";
	    ext= ".dll";
	    libName=basename;
	}
	if (os.startsWith("Mac")) {
	    os = "Mac";
	    ext= ".jnilib";
	}
	
	try {
	    File tf = File.createTempFile(basename,ext);
	    // extract libName+ext from the JAR file
	} catch (Exception foo) {
	}
	return null;
    }
    
    void fail(String msg) {
	System.err.println("ERROR: "+msg);
	System.exit(1);
    }
}
