//
//  RecentList.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Sep 12 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.util;

import java.util.StringTokenizer;
import java.io.File;

public class RecentList {
    String appName, recentKey;
    int maxEntries;
    int active;
    int serial;
    boolean autoSave=true;
    
    String[] list;
    
    public RecentList(String appName, String key, int maxEntries) {
        if (maxEntries<1) maxEntries=8;
        this.maxEntries=maxEntries; recentKey=key;
        this.appName=appName;
        list=new String[maxEntries];
        active=0;
        loadFromGlobalConfig();
    }

    public void addEntry(String e) {
        int i=0;
        while (i<active) {
            if (list[i].compareTo(e)==0) {
                if (i>0) {
                    String h=list[i];
                    int k=i;
                    while (k>0) {
                        list[k]=list[k-1];
                        k++;
                    }
                    list[0]=h;
                    serial++;
                    if (autoSave) saveToGlobalConfig();
                }
                return;
            }
            i++;
        }
        if (active>=maxEntries) {
            int j=1;
            while (j<active) {
                list[j-1]=list[j];
                j++;
            }
            list[maxEntries-1]=e;
        } else {
            list[active]=e;
            active++;
        }
        serial++;
        if (autoSave) saveToGlobalConfig();
    }

    public int count() { return active; }

    /** the serial ID is incremented whenever a change was made to the list.
        this allows other classes to check whether something changed since
        they last polled the list */
    public int getSerial() { return serial; }
    
    void loadFromGlobalConfig() {
        boolean save=autoSave;
        autoSave=false;
        GlobalConfig gc=GlobalConfig.getGlobalConfig();
        String s=gc.getParS("app."+appName+"."+recentKey);
        if (s!=null) {
            StringTokenizer st=new StringTokenizer(s, "\t");
            active=0;
            while (st.hasMoreTokens())
                addEntry(st.nextToken());
        }
        autoSave=save;
        serial++;
    }

    void saveToGlobalConfig() {
        GlobalConfig gc=GlobalConfig.getGlobalConfig();
        String t=null;
        int i=0;
        while (i<active) {
            t=(t==null)?list[i]:(t+"\t"+list[i]);
            i++;
        }
        gc.setParS("app."+appName+"."+recentKey,t);
    }

    public String[] getAllEntries() {
        return list;
    }
    
    public String[] getShortEntries() {
        String[] se=new String[active];
        int i=0;
        while (i<active) {
            String s=list[i];
            int l=s.lastIndexOf(File.pathSeparatorChar);
            if (l>0) s=s.substring(l+1);
            se[i]=s;
            i++;
        }
        i=0;
        while (i<active) { // make sure we have no short-form duplicates
            String s=se[i];
            int j=0;
            boolean hasDupes=false;
            while (j<active) {
                if (i!=j && se[j].compareTo(s)==0) {
                    hasDupes=true;
                    se[j]=list[j];
                }
                j++;
            }
            if (hasDupes)
                se[i]=list[i];
        }
        return se;
    }
}
