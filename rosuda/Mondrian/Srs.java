import org.rosuda.JRclient.*;
import java.io.*;
import java.util.*;

class StreamHog extends Thread
{
  InputStream is;
  StreamHog(InputStream is) { this.is=is; start(); }
  public void run()
  {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line=null;
      while ( (line = br.readLine()) != null) {
        System.out.println("Rserve>" + line);
      }
    } catch (IOException e) { e.printStackTrace(); }
  }
}

public class Srs {
  public static boolean launchRserve(String cmd) {
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      System.out.println("waiting for Rserve to start ... ("+p+")");
      // we need to fetch the output - some platforms will die if you don't ...
      StreamHog errorHog = new StreamHog(p.getErrorStream());
      StreamHog outputHog = new StreamHog(p.getInputStream());
      p.waitFor();
      System.out.println("call terminated, let us try to connect ...");
    } catch (Exception x) {
      System.out.println("failed to start Rserve process with "+x.getMessage());
      return false;
    }
    try {
      Rconnection c = new Rconnection();
      System.out.println("Rserve is running.");
      c.close();
      return true;
    } catch (Exception e2) {
      System.out.println("Try failed with: "+e2.getMessage());
    }
    return false;
  }

  public static boolean checkLocalRserve() {
    try {
      Rconnection c = new Rconnection();
      System.out.println("Rserve is running.");
      c.close();
      return true;
    } catch (Exception e) {
      System.out.println("First connect try failed with: "+e.getMessage());
    }
    String opt=" CMD Rserve --no-save";
    return (launchRserve("R"+opt) ||
            ((new File("/usr/local/lib/R/bin/R")).exists() && launchRserve("/usr/local/lib/R/bin/R"+opt)) ||
            ((new File("/usr/lib/R/bin/R")).exists() && launchRserve("/usr/lib/R/bin/R"+opt)) ||
            ((new File("/usr/local/bin/R")).exists() && launchRserve("/usr/local/bin/R"+opt)) ||
            ((new File("/sw/bin/R")).exists() && launchRserve("/sw/bin/R"+opt)) ||
            ((new File("/Library/Frameworks/R.framework/Resources/bin/R")).exists() && launchRserve("/Library/Frameworks/R.framework/Resources/bin/R"+opt)));
  }

  /*	public static void main(String[] args) {
		System.out.println("result="+checkLocalRserve());
		try {
      Rconnection c=new Rconnection();
      c.shutdown();
    } catch (Exception x) {};
  }
*/
}
