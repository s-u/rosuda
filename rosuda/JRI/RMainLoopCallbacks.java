package org.rosuda.JRI;

public interface RMainLoopCallbacks {
    public void   rWriteConsole (Rengine re, String text);
    public void   rBusy         (Rengine re, int which);
    public String rReadConsole  (Rengine re, String prompt, int addToHistory);
    public void   rShowMessage  (Rengine re, String message);
}
