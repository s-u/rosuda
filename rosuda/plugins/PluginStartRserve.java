package org.rosuda.plugins;

//  PluginStartRserve.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Apr 11 2003.
//  Copyright (c) 2003 Simon Urbanek. All rights reserved.
//

import org.rosuda.ibase.*;
import org.rosuda.JRclient.*;
import org.rosuda.util.*;
import org.rosuda.util.*;

/** This plugin encapsulates simple communication with Rserve and starts Rserve is the communication fails. this plugin requires JRclient. */
public class PluginStartRserve extends Plugin {
    String uRs=null;

    public boolean initPlugin() {
        if (uRs==null) uRs="R CMD Rserve";
        return true;
    }

    public void setParameter(String par, Object val) {
        if (par=="startCmd") uRs=(String)val;
    }

    public Object getParameter(String par) {
        if (par=="startCmd") return uRs;
        return null;
    }
        
    public boolean execPlugin() {
        Rconnection rc=new Rconnection();
        if (!rc.isOk()) {
            if (Global.DEBUG>0)
                System.out.println("Rserve is not running, trying to start it ("+uRs+")");
            try {
                Runtime.getRuntime().exec(uRs);
                return true;
            } catch(Exception rte) {
                if (Global.DEBUG>0)
                    System.out.println("Can't start Rserve: "+rte.getMessage());
                return false;
            }
        }
        return true;
    }
}
