import java.lang.*;

/** very simple profile class to measure time differences and do some basic profiling
    @version $Id$ */
public class Stopwatch {
    long ts_start;
    long ts_stop=0;
    long ts_elapsed=-1;
    boolean quiet=false;

    /** creates a new Stopwatch with current time as start time (i.e. {@link #start} doesn't have to be called) */
    public Stopwatch() { ts_start=System.currentTimeMillis(); };

    /** same as {@link #Stopwatch()} except that the user can enable/disable verbosity of the profiling
        @param beQuiet if set to true profiling function won't print anyhting to console */    
    public Stopwatch(boolean beQuiet) { this(); quiet=beQuiet; };
    /** marks the stop time and returns the elapsed time since start
        @return elapsed time between start and now in ms */
    public long stop() { ts_stop=System.currentTimeMillis(); return ts_elapsed=ts_stop-ts_start; };

    /** sets the start time to current time
        @return the start time in ms (see {@link System.currentTimeMillis}) */
    public long start() { return ts_start=System.currentTimeMillis(); };

    /** returns elapsed time between last start and stop (even if new start was issued already) or
        -1 if not a single complete start/stop sequece exists.
        @return elapsed time between last start and stop */
    public long last() { return ts_elapsed; };

    /** returns time elapsed since start without stopping the clock
        @return elapsed time since start */
    public long elapsed() { return System.currentTimeMillis()-ts_start; };

    /** same as issuing stop followed by start except that the time used for both is identical
        @return elapsed time between last start and now */
    public long restart() { stop(); ts_start=ts_stop; return ts_elapsed; };

    /** print profiling information, i.e. do a restart and print the last elapsed time if not in quiet mode */
    public void profile() { restart(); if (Common.PROFILE>0) System.out.println("time elapsed "+ts_elapsed+" ms"); };

    /** print profiling information, i.e. do a restart and print the last elapsed time if not in quiet mode */
    public void profile(String s) { restart(); if (Common.PROFILE>0) System.out.println(s+" "+ts_elapsed+" ms"); };
};