package org.rosuda.JGR.toolkit;

//
//  Preferences.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.font.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import org.rosuda.JGR.*;


public class Preferences {


    public static String VERSION = "DP1";
    public static String TITLE = "JGR";
    public static String SUBTITLE = "Java Gui for R";
    public static String DEVELTIME = "2003 - 2004";
    public static String INSTITUTION = "RoSuDa, Univ. Augsburg";
    public static String AUTHORS = "Markus Helbig, Simon Urbanek";
    public static String LOGO = "logo.jpg";
    public static String SPLASH = "splash.jpg";

    public static int DEBUG = 0;
    public static boolean isMac = false;

    /** DefaultFontName */
    public static String FontName = "Dialog";
    /** DefaultFontStyle */
    public static int FontStyle = Font.PLAIN;
    /** DefaultFontSize */
    public static int  FontSize = 12;
    /** DefaultFont */
    public static Font DefaultFont;
    /** DefaultHighLightColor */
    public static Color HighLightColor = Color.green;
    /** DefaultCommandColor */
    public static Color CMDColor = Color.red;
    /** DefaultResultColor */
    public static Color RESULTColor = Color.blue;
    /** DefaultErrorColor */
    public static Color ERRORColor = Color.red;
    /** DefaultBracketHighLightColor */
    public static Color BRACKETHighLight = new Color(200, 255, 255);
    /** DefaultSizeSet */
    public static MutableAttributeSet SIZE = new SimpleAttributeSet();
    /** DefaultCMDSet */
    public static MutableAttributeSet CMD = new SimpleAttributeSet();
    /** DefaultResultSet */
    public static MutableAttributeSet RESULT = new SimpleAttributeSet();
    /** DefaultSet */
    public static MutableAttributeSet NORMAL = new SimpleAttributeSet();
    /** DefaultNumberSet */
    public static MutableAttributeSet NUMBER = new SimpleAttributeSet();
    /** DefaultNumberColor*/
    public static Color NUMBERColor = Color.red;
    /** DefaultKEYWORDSet */
    public static MutableAttributeSet KEYWORD = new SimpleAttributeSet();
    /** DefaultKeyWordColor */
    public static Color KEYWORDColor = new Color(0,0,140);
    /** DefaultKEYWORDOBJECTSet */
    public static MutableAttributeSet OBJECT = new SimpleAttributeSet();
    /** DefaultKeyWordObjectColor */
    public static Color OBJECTColor = new Color(50,0,140);
    /** DefaultCommentSet */
    public static MutableAttributeSet COMMENT = new SimpleAttributeSet();
    /** DefaultCommentColor */
    public static Color COMMENTColor = new Color(0,120,0);
    /** DefaultQuoteSet */
    public static MutableAttributeSet QUOTE = new SimpleAttributeSet();
    /** DefaultQuoteColor */
    public static Color QUOTEColor = Color.blue;

    public static int MAXHELPTABS = 10;

    public static Hashtable KEYWORDS = null;
    public static Hashtable KEYWORDSOBJECTS = null;


    public static void apply() {
        Preferences.refresh();
        FontTracker.current.applyFont();
    }

    public static void initialize() {
        //later we will read the prefs file
        readPrefs();
        refreshKeyWords();
        DefaultFont = new Font(FontName,FontStyle,FontSize);
        StyleConstants.setFontSize(SIZE,FontSize);
        StyleConstants.setForeground(CMD,CMDColor);
        StyleConstants.setForeground(RESULT,RESULTColor);
        StyleConstants.setForeground(NORMAL, Color.black);
        StyleConstants.setFontSize(NORMAL,FontSize);
        StyleConstants.setForeground(NUMBER, NUMBERColor);
        StyleConstants.setForeground(COMMENT, COMMENTColor);
        StyleConstants.setForeground(KEYWORD, KEYWORDColor);
        StyleConstants.setBold(KEYWORD, true);
        StyleConstants.setForeground(OBJECT, OBJECTColor);
        StyleConstants.setItalic(OBJECT, true);
        StyleConstants.setForeground(QUOTE, QUOTEColor);
    }

    public static void refresh() {
        refreshKeyWords();
        DefaultFont = new Font(FontName,FontStyle,FontSize);
        StyleConstants.setFontSize(SIZE,FontSize);
        StyleConstants.setForeground(CMD,CMDColor);
        StyleConstants.setForeground(RESULT,RESULTColor);
        StyleConstants.setForeground(NORMAL, Color.black);
        StyleConstants.setFontSize(NORMAL,FontSize);
        StyleConstants.setForeground(NUMBER, NUMBERColor);
        StyleConstants.setForeground(COMMENT, COMMENTColor);
        StyleConstants.setForeground(KEYWORD, KEYWORDColor);
        StyleConstants.setBold(KEYWORD, true);
        StyleConstants.setForeground(OBJECT, OBJECTColor);
        StyleConstants.setItalic(OBJECT, true);
        StyleConstants.setForeground(QUOTE, QUOTEColor);
    }

    public static void refreshKeyWords() {
        if (KEYWORDS==null) KEYWORDS = new Hashtable();
        if (KEYWORDSOBJECTS==null) KEYWORDSOBJECTS = new Hashtable();
        if (JGR.R != null) {
            KEYWORDS.clear();
            RTalk.getKeyWords();
        }
    }

    public static void readPrefs() {
        File prefs;
        try {
            if ((prefs=new File(System.getProperty("user.home")+File.separator+".JGRprefsrc")).exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(prefs));
                String line;
                while((line=reader.readLine())!=null) {
                    if (line.startsWith("FontName")) FontName = line.substring(line.indexOf("=")+1);
                    else if (line.startsWith("FontSize")) FontSize = new Integer(line.substring(line.indexOf("=")+1)).intValue();
                    else if (line.startsWith("MAXHELPTABS")) MAXHELPTABS = new Integer(line.substring(line.indexOf("=")+1)).intValue();
                }
                reader.close();
            }
        }
        catch (Exception e) {
            new iError(e);
        }
    }

    public static void writePrefs() {
        File prefs;
        try {
             prefs = new File(System.getProperty("user.home")+File.separator+".JGRprefsrc");
             BufferedWriter writer = new BufferedWriter(new FileWriter(prefs));
             writer.write("FontName="+FontName+"\n");
             writer.write("FontSize="+FontSize+"\n");
             writer.write("MAXHELPTABS="+MAXHELPTABS+"\n");
             writer.flush();
             writer.close();
        }
        catch (Exception e) {
            new iError(e);
        }
    }

}