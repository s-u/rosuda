public class rtest {
    public static void main(String[] args) {
        System.out.println("Creating Rengine");
	Rengine re=new Rengine();
        System.out.println("Rengine created, waiting for R");
        if (!re.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }
        System.out.println("R is ready, press <Enter> to continue (time to attach the debugger is necessary)");
        try { System.in.read(); } catch(Exception e) {};

        {
            System.out.println("Parsing");
            long e=re.rniParse("data(iris)", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building RXP");
            RXP x=new RXP(re, r);
            System.out.println("RXP result = "+x);
        }
        {
            System.out.println("Parsing");
            long e=re.rniParse("iris", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building RXP");
            RXP x=new RXP(re, r);
            System.out.println("RXP result = "+x);
        }
        {
            System.out.println("Parsing");
            long e=re.rniParse("names(iris)", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building RXP");
            RXP x=new RXP(re, r);
            System.out.println("RXP result = "+x);
            String s[]=x.asStringArray();
            if (s!=null) {
                int i=0; while (i<s.length) { System.out.println("["+i+"] \""+s[i]+"\""); i++; }
            }
        }
        {
            System.out.println("Parsing");
            long e=re.rniParse("rnorm(10)", 1);
            System.out.println("Result = "+e+", running eval");
            long r=re.rniEval(e, 0);
            System.out.println("Result = "+r+", building RXP");
            RXP x=new RXP(re, r);
            System.out.println("RXP result = "+x);
            double d[]=x.asDoubleArray();
            if (d!=null) {
                int i=0; while (i<d.length) { System.out.print(((i==0)?"":", ")+d[i]); i++; }
                System.out.println("");
            }
            System.out.println("");
        }
        {
            RXP x=re.eval("1:10");
            System.out.println("RXP result = "+x);
            int d[]=x.asIntArray();
            if (d!=null) {
                int i=0; while (i<d.length) { System.out.print(((i==0)?"":", ")+d[i]); i++; }
                System.out.println("");
            }
        }
	re.eval("X11()");
	re.eval("plot(rnorm(1000))");

        System.out.println("R is ready, press <Enter> to continue (time to attach the debugger is necessary)");
        try { System.in.read(); } catch(Exception e2) {};

        re.end();
        System.out.println("end");
    }
}
