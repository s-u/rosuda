import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class JRIBootstrap {
	//--- global constants ---
	public static final int HKLM = 0; // HKEY_LOCAL_MACHINE
	public static final int HKCU = 1; // HKEY_CURRENT_USER

	//--- native methods ---
	public static native String getenv(String var);
	public static native void setenv(String var, String val);

	public static native String regvalue(int root, String key, String value);
	public static native String[] regsubkeys(int root, String key);

	public static native String expand(String val);

	public static native boolean hasreg();

	public static native String arch();

	//--- helper methods ---	
	static void fail(String msg) {
		System.err.println("ERROR: "+msg);
		System.exit(1);
	}

    // set ONLY after findR was run
    public static boolean isWin32 = false;

    static String findR() {
	String ip = null;
	if (hasreg()) {
	    isWin32 = true;
	    int rroot = HKLM;
	    System.out.println("has registry, trying to find R");
	    ip = regvalue(HKLM, "SOFTWARE\\R-core\\R","InstallPath");
	    if (ip == null)
		ip = regvalue(rroot=HKCU, "SOFTWARE\\R-core\\R","InstallPath");
	    if (ip == null) {
		System.out.println(" - InstallPath not present (possibly uninstalled R)");
		String[] vers = regsubkeys(rroot=HKLM, "SOFTWARE\\R-core\\R");
		if (vers == null)
		    vers = regsubkeys(rroot=HKCU, "SOFTWARE\\R-core\\R");
		if (vers!=null) {
		    String lvn = ""; // FIXME: we compare versions lexicographically which may fail if we really reach R 2.10
		    int i = 0; while (i<vers.length) {
			if (vers[i] != null && lvn.compareTo(vers[i]) < 0)
			    lvn = vers[i];
			i++;
		    }
		    if (!lvn.equals(""))
			ip = regvalue(rroot, "SOFTWARE\\R-core\\R\\"+lvn, "InstallPath");
		}
	    }
	    if (ip == null) {
		ip = getenv("R_HOME");
		if (ip==null || ip.length()<1) ip = getenv("RHOME");
		if (ip==null || ip.length()<1) ip=null;
	    }
	    return ip;
	}
	ip = getenv("R_HOME");
	if (ip == null || ip.length()<1)
	    ip = getenv("RHOME");
	if (ip == null || ip.length()<1) {
	    // find ...
	}
	return ip;
    }

    public static String u2w(String fn) {
	return (java.io.File.separatorChar != '/')?fn.replace('/',java.io.File.separatorChar):fn;
    }

    public static Object bootRJavaLoader = null;

    public static Object getBootRJavaLoader() {
	System.out.println("JRIBootstrap.bootRJavaLoader="+bootRJavaLoader);
	return bootRJavaLoader;
    }

    static void addClassPath(String s) {
	if (bootRJavaLoader==null) return;
	try {
	    Method m = bootRJavaLoader.getClass().getMethod("addClassPath", new Class[] { String.class });
	    m.invoke(bootRJavaLoader, new Object[] { s });
	} catch (Exception e) {
	    System.err.println("FAILED: JRIBootstrap.addClassPath");
	}
    }

    static Object createRJavaLoader(String rhome, String[] cp, boolean addJRI) {
	String rJavaRoot = u2w(rhome+"/library/rJava");
	File f = new File(u2w(rJavaRoot+"/java/boot"));
	if (!f.exists()) {
	    // try harder ...
	    return null;
	}
	String rJavaHome = u2w(rJavaRoot);
	File lf = new File(u2w(rJavaRoot+"/libs/"+arch()));
	if (!lf.exists()) lf = new File(u2w(rJavaRoot+"/libs"));
	String rJavaLibs = lf.toString();
	JRIClassLoader mcl = JRIClassLoader.getMainLoader();
	mcl.addClassPath(f.toString()); // add rJava boot to primary CP
	try {
	    // force the use of the MCL even if the system loader could find it
	    Class rjlclass = mcl.findAndLinkClass("RJavaClassLoader");
	    Constructor c = rjlclass.getConstructor(new Class[] { String.class, String.class });
	    Object rjcl = c.newInstance(new Object[] { rJavaHome, rJavaLibs });
	    System.out.println("RJavaClassLoader: "+rjcl);
	    if (addJRI) {
		if (cp==null || cp.length==0)
		    cp = new String[] { u2w(rJavaRoot+"/jri/JRI.jar") };
		else {
		    String[] ncp = new String[cp.length+1];
		    System.arraycopy(cp, 0, ncp, 1, cp.length);
		    ncp[0] = u2w(rJavaRoot+"/jri/JRI.jar");
		    cp = ncp;
		}
	    }
	    if (cp==null || cp.length==0)
		cp = new String[] { u2w(rJavaRoot+"/java/boot") };
	    else {
		String[] ncp = new String[cp.length+1];
		System.arraycopy(cp, 0, ncp, 1, cp.length);
		ncp[0] = u2w(rJavaRoot+"/java/boot");
		cp = ncp;
	    }
	    if (cp != null) {
		System.out.println(" - adding class paths");
		Method m = rjlclass.getMethod("addClassPath", new Class[] { String[].class });
		m.invoke(rjcl, new Object[] { cp });
	    }
	    return rjcl;
	} catch (Exception rtx) {
	    System.err.println("ERROR: Unable to create new RJavaClassLoader in JRIBootstrap! ("+rtx+")");
	    rtx.printStackTrace();
	    System.exit(2);
	}
	return null;
    }
	
	//--- main bootstrap method ---
	public static void bootstrap(String[] args) {
		System.out.println("JRIBootstrap("+args+")");
		try {
			System.loadLibrary("boot");
		} catch (Exception e) {
			fail("Unable to load boot library!");
		}
		
		// just testing from now on
		String rhome = findR();
		if (rhome == null) fail("Unable to find R!");
		if (isWin32) {
		    String path = getenv("PATH");
		    if (path==null || path.length()<1) path=rhome+"\\bin";
		    else path=rhome+"\\bin;"+path;
		    setenv("PATH",path);
		}
		
		System.out.println("PATH="+getenv("PATH"));
		Object o = bootRJavaLoader = createRJavaLoader(rhome, new String[] { "main" }, true);

		addClassPath(u2w(rhome+"/library/JGR/cont/JGR.jar"));
		addClassPath(u2w(rhome+"/library/iplots/cont/iplots.jar"));
		String mainClass = "org.rosuda.JGR.JGR";

		try {
		    Method m = o.getClass().getMethod("bootClass", new Class[] { String.class, String.class, String[].class });
		    m.invoke(o, new Object[] { mainClass, "main", args });
		} catch(Exception ie) {		    
		    System.out.println("cannot boot the final class: "+ie);
		    ie.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.err.println("*** WARNING: JRIBootstrap.main should NOT be called directly, it is intended for debugging use ONLY. Use Boot wrapper instead.");
		// just for testing
		bootstrap(args);
	}
}
