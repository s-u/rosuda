//
//  KlimtSplash.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;
import java.io.*;
import java.awt.FileDialog;

public class KlimtSplash extends SplashScreen {
    public KlimtSplash() {
        super("Klimt v"+Common.Version+" (release "+Common.Release+")");
    }

    /* we need to re-define this since we're KlimtSplash */
    public static void runMainAsAbout() {
        if (main==null) main=new KlimtSplash();
        main.runAsAbout();
    }
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd); // let SplashScreen handle the defaults
        String openFn=null;
        
        if (cmd=="prefs") {
            org.rosuda.util.Platform.getPlatform().handlePrefs();
        }

        if (cmd.startsWith("recent:")) {
            openFn=cmd.substring(7);
            cmd="openData";
        }
        
        if (cmd=="openData") {
            SVarSet tvs=new SVarSet();
            DataRoot dr=Klimt.addData(tvs);
            
            if (openFn==null) {
                FileDialog fd=new FileDialog(this,"Select data file");
		fd.setModal(true);
		fd.show();
		openFn=fd.getDirectory()+fd.getFile();
		if (fd.getFile()==null) return null;
            }
            
            File f=new File(openFn);
            
            try {
                String prefix=f.getParent();
                BufferedReader r=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                String s=r.readLine();
                if (s.startsWith("### KLIMT.TaskFile 1")) {
                    // ok, the file seems to work, so let's put it in the recent list
                    if (SplashScreen.recentOpen!=null) SplashScreen.recentOpen.addEntry(f.getAbsolutePath());
                    if (Global.DEBUG>0) System.out.println("KlimtSplash.run:openData: encountered KLIMT task file ("+s+"), processing");
                    boolean displayNew=true;
                    boolean treeLoadBatch=false;
                    while (r.ready()) {
                        s=r.readLine();
                        if (s.equals(">end.load.trees")) treeLoadBatch=false;
                        if (s.startsWith(">load.data ") || s.startsWith(">load.tree ") || treeLoadBatch) {
                            boolean treeOnly=treeLoadBatch || s.startsWith(">load.tree ");
                            String fn=(treeLoadBatch)?s:s.substring(11);
                            if (fn.length()>0) {
                                if (fn.charAt(0)!=File.separatorChar)
                                    fn=prefix+File.separator+fn;
                                if (Global.DEBUG>0) System.out.println("KlimtSplash.run:openData: load.xxx of \""+fn+"\"");
                                if (!treeOnly && tvs.count()>0) { // create new data if there's already a datafile
                                    tvs=new SVarSet();
                                    dr=Klimt.addData(tvs);
                                }
                                File dfn=new File(fn);
                                SNode t=Klimt.loadTreeFile(dfn, dr, displayNew);
                                if (!treeOnly && tvs.count()>0) {
                                    if (tvs.getMarker()==null && (tvs.at(0)!=null)&&(tvs.at(0).size()>0))
                                        tvs.setMarker(new SMarker(tvs.at(0).size()));
                                    VarFrame vf=Klimt.newVarDisplay(dr,Common.screenRes.width-150,0,140,(Common.screenRes.height>600)?600:Common.screenRes.height-30);
                                    setVisible(false);
                                }
                            }
                        }
                        if (s.startsWith(">display.new ")) {
                            String yn=s.substring(13);
                            if (yn.startsWith("y")) displayNew=true;
                            if (yn.startsWith("n")) displayNew=false;
                        }
                        if (s.equals(">end")) break;
                        if (s.equals(">begin.load.trees")) treeLoadBatch=true;
                    }
                    String wars=Common.getWarnings();
                    if (wars!=null) {
                        HelpFrame hf=new HelpFrame();
                        hf.t.setText("Following warnings were produced during task processing:\n\n"+wars);
                        hf.setTitle("Load warnings");
                        //hf.setModal(true);
                        hf.show();
                    }
                    return null;
                }
            } catch (Exception tfe) {
                System.out.println("Task file detection failed: "+tfe.getMessage());
                tfe.printStackTrace();
            }
            
            SNode t=Klimt.openTreeFile(this,openFn,dr);
            if (t==null && tvs.count()<1) {
            } else {
                if (SplashScreen.recentOpen!=null) SplashScreen.recentOpen.addEntry(f.getAbsolutePath());
                if (t!=null) {
                    TFrame fr=new TFrame("Tree "+tvs.getName(),TFrame.clsTree);
                    Klimt.newTreeDisplay(t,fr,0,0,Common.screenRes.width-160,(Common.screenRes.height>600)?600:Common.screenRes.height-20);
                }
                VarFrame vf=Klimt.newVarDisplay(dr,Common.screenRes.width-150,0,140,(Common.screenRes.height>600)?600:Common.screenRes.height-30);
                setVisible(false);
            }
        }

        return null;
    }
    
}
