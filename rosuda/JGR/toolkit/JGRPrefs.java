package org.rosuda.JGR.toolkit;

//
//  Preferences.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.text.*;

import org.rosuda.JGR.*;

import java.util.prefs.Preferences;

public class JGRPrefs {


    public static final String VERSION = "DP4";
    public static final String TITLE = "JGR";
    public static final String SUBTITLE = "Java Gui for R";
    public static final String DEVELTIME = "2003 - 2004";
    public static final String INSTITUTION = "RoSuDa, Univ. Augsburg";
    public static final String AUTHOR1 = "Markus Helbig";
    public static final String AUTHOR2 = "Simon Urbanek";
    public static final String WEBSITE = "http://www.rosuda.org";
    public static final String LOGO = "logo.jpg";
    public static final String SPLASH = "splash.jpg";

    public static final int DEBUG = 0;
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
    /** DefaultFontSet */
    public static MutableAttributeSet DEFAULTFONT = new SimpleAttributeSet();
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
    /** Tabsize */
    public static int TABSIZE = 4;

    public static int MAXHELPTABS = 10;

    
    public static boolean useEmacsKeyBindings = false;

    public static void apply() {
        JGRPrefs.refresh();
        FontTracker.current.applyFont();
    }

    public static void initialize() {
        //later we will read the prefs file
        readPrefs();
        DefaultFont = new Font(FontName,FontStyle,FontSize);
        StyleConstants.setFontSize(SIZE,FontSize);
        StyleConstants.setFontSize(DEFAULTFONT,FontSize);
        StyleConstants.setFontFamily(DEFAULTFONT,FontName);
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
        System.out.println(FontName);
        DefaultFont = new Font(FontName,FontStyle,FontSize);
        StyleConstants.setFontSize(SIZE,FontSize);
        StyleConstants.setFontSize(DEFAULTFONT,FontSize);
        StyleConstants.setFontFamily(DEFAULTFONT,FontName);
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

    public static void readPrefs() {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(System.getProperty("user.home")+File.separator+".JGRprefsrc"));
        } catch (FileNotFoundException e) {
        }

        try {
            Preferences.importPreferences(is);
        } catch (InvalidPreferencesFormatException e) {
        } catch (IOException e) {
        }

        Preferences prefs = Preferences.userNodeForPackage(String.class);
        FontName = prefs.get("FontName","Dialog");
        FontSize = prefs.getInt("FontSize",12);
        MAXHELPTABS = prefs.getInt("MaxHelpTabs",10);
        // it is safe to use emacs bindings on Macs since that's the default in Coca widgets. on win/unix it's not safe since ctrl may be the sc modifier
        useEmacsKeyBindings = prefs.getBoolean("UseEmacsKeyBindings", org.rosuda.util.Platform.isMac);
    }

    public static void writePrefs() {
        Preferences prefs = Preferences.userNodeForPackage(String.class);

        prefs.put("FontName", FontName);        // String
        prefs.putInt("FontSize", FontSize);               // int
        prefs.putInt("MaxHelpTabs",MAXHELPTABS);
        prefs.putBoolean("UseEmacsKeyBindings", useEmacsKeyBindings);
        String packages = "";
        if (JGRPackageManager.defaultPackages.length > 0) {
        packages = JGRPackageManager.defaultPackages[0].toString();
        for (int i = 1; i < JGRPackageManager.defaultPackages.length; i++)
        	packages += ", "+JGRPackageManager.defaultPackages[i];
        }
        prefs.put("DefaultPackages", packages);
        try {
            prefs.exportNode(new FileOutputStream(System.getProperty("user.home")+File.separator+".JGRprefsrc"));
        } catch (IOException e) {
        } catch (BackingStoreException e) {
        }
    }
}