//
//  SCatSequence.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jun 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase;

import org.rosuda.util.*;

public class SCatSequence extends Notifier {
    /** array mapping positions to categories - seqToCat[position]=category */
    int[] seqToCat=null;
    /** array mapping categories to positions - catToSeq[category]=position */
    int[] catToSeq=null;
    /** the associated variable */
    SVar v;
    /** number of categories */
    int cats;
    /** owner of this sequence. mainly used to distinguish private and global sequences. it is not used by the class itself */
    Object owner;
    /** notify flag. if set to <code>true</code> then notifications are sent to the corresponding {@link SVar} object (main variable sequences) otherwise the sequence acts as a notifier itself (private sequences) */
    boolean notifyVar=false;

    public SCatSequence(SVar var, Object theOwner, boolean notifyVariable) {
        v=var; owner=theOwner;
        cats=(v!=null)?v.getNumCats():0;
        notifyVar=notifyVariable;
    }

    public SCatSequence(SVar var) {
        this(var,null,false);
    }

    public void setNotifyVarOnChange(boolean noc) {
        notifyVar=noc;
    }

    public Object getOwner() {
        return owner;
    }
    
    void updateCats() {
        if (v==null) return;
        int cc=v.getNumCats();
        if (cc==cats) return;
        if (cc==0 || seqToCat==null || catToSeq==null) {
            seqToCat=catToSeq=null;
            cats=cc;
            return;
        }
        /* what do we do if a sequence already existed and the number of categories changed?
            currenty we simply discard any order - but that could be changed ... */
        seqToCat=catToSeq=null;
        cats=cc;
    }

    public int size() {
        return cats;
    }

    /** returns the category ID at position pos */
    public int catAtPos(int id) {
        if (id<0 || id>=cats) return Global.runtimeWarning("SCatSequence on "+((v==null)?"<null>":v.getName())+": catAtPos("+id+") out of range ("+cats+" cats)");
        return (seqToCat==null)?id:seqToCat[id];
    }

    /** returns the position of the category id */
    public int posOfCat(int id) {
        if (id<0 || id>=cats) return Global.runtimeWarning("SCatSequence on "+((v==null)?"<null>":v.getName())+": posOfCat("+id+") out of range ("+cats+" cats)");
        return (catToSeq==null)?id:catToSeq[id];
    }

    /** this function is called when the first re-ordering command was issued to build the necessary arrays */
    void createFields() {
        seqToCat=catToSeq=null;
        if (cats<1) return;
        seqToCat=new int[cats];
        catToSeq=new int[cats];
        int i=0;
        while(i<cats) {
            seqToCat[i]=i;
            catToSeq[i]=i;
            i++;
        }
    }

    public void reset() {
        if (seqToCat!=null || catToSeq!=null) {
            seqToCat=catToSeq=null; // easy - we drop everything ;)
            if (notifyVar)
                v.NotifyAll(new NotifyMsg(this,Common.NM_VarSeqChange));
            else
                NotifyAll(new NotifyMsg(this,Common.NM_CatSeqChange));
            }
        return;
    }
    
    public boolean swapCatsAtPositions(int p1, int p2) {
        if (p1<0 || p2<0 || p1>=cats || p2>=cats) return
            Global.runtimeWarning("SCatSequence on "+((v==null)?"<null>":v.getName())+": swapCatsAtPositions("+p1+","+p2+") out of range ("+cats+" cats)")!=-1;
        if (seqToCat==null) createFields();
        int c1=seqToCat[p1];
        int c2=seqToCat[p2];
        seqToCat[p1]=c2; seqToCat[p2]=c1;
        catToSeq[c1]=p2; catToSeq[c2]=p1;
        if (notifyVar)
            v.NotifyAll(new NotifyMsg(this,Common.NM_VarSeqChange));
        else
            NotifyAll(new NotifyMsg(this,Common.NM_CatSeqChange));
        return true;
    }

    public boolean swapCats(int c1, int c2) {
        if (c1<0 || c2<0 || c1>=cats || c2>=cats) return
            Global.runtimeWarning("SCatSequence on "+((v==null)?"<null>":v.getName())+": swapCats("+c1+","+c2+") out of range ("+cats+" cats)")!=-1;
        if (seqToCat==null) createFields();
        int p1=catToSeq[c1];
        int p2=catToSeq[c2];
        seqToCat[p1]=c2; seqToCat[p2]=c1;
        catToSeq[c1]=p2; catToSeq[c2]=p1;
        if (notifyVar)
            v.NotifyAll(new NotifyMsg(this,Common.NM_VarSeqChange));
        else
            NotifyAll(new NotifyMsg(this,Common.NM_CatSeqChange));
        return true;
    }

    public boolean moveCatAtPosTo(int p1, int p2) {
        if (p1==p2) return true;
        if (p1<0 || p2<0 || p1>=cats || p2>=cats) return
            Global.runtimeWarning("SCatSequence on "+((v==null)?"<null>":v.getName())+": moveCatAtPosTo("+p1+","+p2+") out of range ("+cats+" cats)")!=-1;
        if (seqToCat==null) createFields();
        int c1=seqToCat[p1];
        if (p1<p2) { // move to back
            int r=p1;
            while (r<p2) {
                int c=seqToCat[r+1];
                seqToCat[r]=c;
                catToSeq[c]=r;
                r++;
            }
            seqToCat[r]=c1;
            catToSeq[c1]=r;
        } else { // move to front
            int r=p1;
            while (r>p2) {
                int c=seqToCat[r-1];
                seqToCat[r]=c;
                catToSeq[c]=r;
                r--;
            }
            seqToCat[r]=c1;
            catToSeq[c1]=r;            
        }
        if (notifyVar)
            v.NotifyAll(new NotifyMsg(this,Common.NM_VarSeqChange));
        else
            NotifyAll(new NotifyMsg(this,Common.NM_CatSeqChange));
        return true;
    }

    public String toString() {
        return "SCatSequence(var=\""+((v==null)?"<null>":v.name)+ "\",cats="+cats+((seqToCat==null)?",straight":",mapped")+")";
    }
}
