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

/** RecentList manages a list of unique {@link String} entries. The list has a limited size and the "oldest" entries are removed if an overflow occurs. New entries are added to the top of the list and duplicates are always removed (effectively by moving the existing entry to the top). The typical use of the list is to hold the names of most recently accessed files. */
public class RecentList {
    String appName, recentKey;
    int maxEntries;
    int active;
    int serial;

    /** if set to <code>true</code> then modification operations such as {@link #addEntry} or {@link #reset} call implicitly {@saveToGlobalConfig}. */
    public boolean autoSave=true;
    
    String[] list;

    /** creates a new recent list and loads its content from the global config file (unless key==<code>null</code>).
        @param application name (to be used for global config file), if <code>null</code> then the application name is set to "default".
        @param key key for the global config file (the config key is constructed as "appName."+app+"."+key). If <code>null</code> then config file is not used.
        @param maxEntries the maximal number of entries. Any new entries added after the maximal number was reached will replace the oldest entries.
        */        
    public RecentList(String appName, String key, int maxEntries) {
        if (maxEntries<1) maxEntries=8;
        this.maxEntries=maxEntries; recentKey=key;
        this.appName=(appName!=null)?appName:"default";
        list=new String[maxEntries];
        active=0;
        loadFromGlobalConfig();
    }

    /** resets/clears the list */
    public void reset() {
        active=0;
        int i=0; while (i<maxEntries) { list[i++]=null; }
        serial++;
        if (autoSave) saveToGlobalConfig();
    }

    /** adds a new entry to the list. If the string already exists then it's moved to the top of the list.
        @param e new list entry */
    public void addEntry(String e) {
        if (e==null || e.length()<1) return;
        int i=0;
        while (i<active) {
            if (list[i].compareTo(e)==0) {
                if (i>0) {
                    String h=list[i];
                    int k=i;
                    while (k>0) {
                        list[k]=list[k-1];
                        k--;
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
            int j=maxEntries-1;
            while (j>0) {
                list[j]=list[j-1];
                j--;
            }
            list[0]=e;
        } else {
            if (active>0) {
                int j=active;
                while (j>0) {
                    list[j]=list[j-1];
                    j--;
                }
            }
            list[0]=e;
            active++;
        }
        serial++;
        if (autoSave) saveToGlobalConfig();
    }

    /** retrieves the nr. of entries in the list
        @return number of entries in the list */
    public int count() { return active; }

    /** the serial ID is incremented whenever a change was made to the list.
        this allows other classes to check whether something changed since
        they last polled the list */
    public int getSerial() { return serial; }

    /** loads the list from the global config file, unless key=<code>null</code> (see {@link #RecentList(String, String, int)}). */
    void loadFromGlobalConfig() {
        if (recentKey==null)
            return;
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

    /** saves the list to the global config file, unless key=<code>null</code> (see {@link #RecentList(String, String, int)}). */
    void saveToGlobalConfig() {
        if (recentKey==null)
            return;
        GlobalConfig gc=GlobalConfig.getGlobalConfig();
        String t=null;
        int i=0;
        while (i<active) {
            t=(t==null)?list[i]:(t+"\t"+list[i]);
            i++;
        }
        gc.setParS("app."+appName+"."+recentKey,(t==null)?"":t);
    }

    /** returns the entire array of entries. Note that the array has always the length of maxEntries. You must check {@link #count()} to obtain the number of valid entries.
        @return array of strings of the length maxEntries */
    public String[] getAllEntries() {
        return list;
    }

    /** get the short form of the entries as an array. Unlike in {@link #getAllEntries()} the array length is always {@link #count()}. Assuming that the stored values are paths to files, the sort form is generated by returing the file name only, except for duplicate file names which are returned in a form that is distinguishable.
        @return array of short-forms of the entries */
    public String[] getShortEntries() {
        String[] se=new String[active];
        int i=0;
        while (i<active) {
            String s=list[i];
            int l=s.lastIndexOf(File.separatorChar);
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
            i++;
        }
        return se;
    }
}
