package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.JGRPackageManager;
import org.rosuda.JGR.RController;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

/**
 * JGRPrefs - preferences like fonts colors ....
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2005
 */

public class JGRPrefs {

	/** Preference version */
	public static final int prefsVersion = 0x0102; 

	/** Debuglevel */
	public static final int DEBUG = 0;

	/** Is JGR running on a Mac? */
	public static boolean isMac = false;
	
	public static boolean isWindows = false;

	/** DefaultFontName */
	public static String FontName = "Monospaced";

	/** DefaultFontStyle */
	public static int FontStyle = Font.PLAIN;

	/** DefaultFontSize */
	public static int FontSize = 12;

	/** MinimalFontSize */
	public static final int MINFONTSIZE = 18;

	/** DefaultFont */
	public static Font DefaultFont;

	/** DefaultHighLightColor */
	public static Color HighLightColor = Color.green;

	/** DefaultCommandColor */
	public static Color CMDColor = Color.red;

	/** DefaultResultColor */
	public static Color RESULTColor = Color.black;

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

	/** DefaultNumberColor */
	public static Color NUMBERColor = Color.red;

	/** DefaultKEYWORDSet */
	public static MutableAttributeSet KEYWORD = new SimpleAttributeSet();

	/** DefaultKeyWordColor */
	public static Color KEYWORDColor = new Color(0, 0, 140);
	
	/** Default Keyword bolding */
	public static boolean KEYWORD_BOLD = true;

	/** DefaultKEYWORDOBJECTSet */
	public static MutableAttributeSet OBJECT = new SimpleAttributeSet();

	/** DefaultKeyWordObjectColor */
	public static Color OBJECTColor = new Color(50, 0, 140);
	
	/** Default Object italics */
	public static boolean OBJECT_IT = true;
	
	/** Automatic tabs **/
	public static boolean AUTOTAB =true;

	/** DefaultCommentSet */
	public static MutableAttributeSet COMMENT = new SimpleAttributeSet();

	/** DefaultCommentColor */
	public static Color COMMENTColor = new Color(0, 120, 0);

	/** Default Comment italics */
	public static boolean COMMENT_IT = true;
	
	/** DefaultQuoteSet */
	public static MutableAttributeSet QUOTE = new SimpleAttributeSet();

	/** DefaultQuoteColor */
	public static Color QUOTEColor = Color.blue;
	
	/** Default Line Highlight */
	public static boolean LINE_HIGHLIGHT = true;

	/** Default line HIGHLIGHTColor */
	public static Color HIGHLIGHTColor = new Color(0xe0e0e0);
	
	/** Default Line numbering */
	public static boolean LINE_NUMBERS = true;
	
	/** MaximalHelpTabs */
	public static int maxHelpTabs = 10;

	/** UseHelpAgent */
	public static boolean useHelpAgent = true;

	/** UseHelpAgent in editor */
	public static boolean useHelpAgentEditor = true;

	/** UseHelpAgent in console */
	public static boolean useHelpAgentConsole = true;

	/** UseEmacsKeyBindings */
	public static boolean useEmacsKeyBindings = false;

	/** ShowHiddenFiles */
	public static boolean showHiddenFiles = false;
	

	/** Packages which were installed when JGR was running the last time */
	public static String previousPackages = null;

	/** Initial working directory */
	public static String workingDirectory = System.getProperty("user.home");

	/** Default packages (more precisely default package + those to load on startup) */
	public static String defaultPackages = null;
	
	/** ask for saving workspace */
	public static boolean askForSavingWorkspace = true;
	

	/** Tab width */
	public static int tabWidth = 4;
	
	
	/**Console dims*/
	public static int consoleWidth = 550;
	public static int consoleHeight = 700;
	

	/**
	 * Apply current settings to JGR.
	 */
	public static void apply() {
		JGRPrefs.refresh();
		FontTracker.current.applyFont();
	}

	/**
	 * Initialize settings from .JGRPrefsrc.
	 */
	public static void initialize() {
		FontSize = JGRPrefs.isWindows ? 10 : 12;
		readPrefs();
		DefaultFont = new Font(FontName, FontStyle, FontSize);
		StyleConstants.setFontSize(SIZE, FontSize);
		StyleConstants.setFontSize(DEFAULTFONT, FontSize);
		StyleConstants.setFontFamily(DEFAULTFONT, FontName);
		StyleConstants.setForeground(CMD, CMDColor);
		StyleConstants.setForeground(RESULT, RESULTColor);
		StyleConstants.setForeground(NORMAL, Color.black);
		StyleConstants.setFontSize(NORMAL, FontSize);
		StyleConstants.setForeground(NUMBER, NUMBERColor);
		StyleConstants.setForeground(COMMENT, COMMENTColor);
		StyleConstants.setItalic(COMMENT, COMMENT_IT);
		StyleConstants.setForeground(KEYWORD, KEYWORDColor);
		StyleConstants.setBold(KEYWORD, KEYWORD_BOLD);
		StyleConstants.setForeground(OBJECT, OBJECTColor);
		StyleConstants.setItalic(OBJECT, OBJECT_IT);
		StyleConstants.setForeground(QUOTE, QUOTEColor);
	}

	/**
	 * Refresh current settings.
	 */
	public static void refresh() {
		DefaultFont = new Font(FontName, FontStyle, FontSize);
		StyleConstants.setFontSize(SIZE, FontSize);
		StyleConstants.setFontSize(DEFAULTFONT, FontSize);
		StyleConstants.setFontFamily(DEFAULTFONT, FontName);
		StyleConstants.setForeground(CMD, CMDColor);
		StyleConstants.setForeground(RESULT, RESULTColor);
		StyleConstants.setForeground(NORMAL, Color.black);
		StyleConstants.setFontSize(NORMAL, FontSize);
		StyleConstants.setForeground(NUMBER, NUMBERColor);
		StyleConstants.setForeground(COMMENT, COMMENTColor);
		StyleConstants.setItalic(COMMENT, COMMENT_IT);
		StyleConstants.setForeground(KEYWORD, KEYWORDColor);
		StyleConstants.setBold(KEYWORD, KEYWORD_BOLD);
		StyleConstants.setForeground(OBJECT, OBJECTColor);
		StyleConstants.setItalic(OBJECT, OBJECT_IT);
		StyleConstants.setForeground(QUOTE, QUOTEColor);
		if (JGR.getREngine() != null && JGR.STARTED) {
			JGR.threadedEval("options(width=" + JGR.MAINRCONSOLE.getFontWidth() + ")");
		}
	}

	/**
	 * Read prefs form .JGRPrefsrc in user's home directory.
	 */
	public static void readPrefs() {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(System
					.getProperty("user.home")
					+ File.separator + ".JGRprefsrc"));
		} catch (FileNotFoundException e) {
		}

		try {
			if (is != null) {
				Preferences prefs = Preferences
						.userNodeForPackage(org.rosuda.JGR.JGR.class);
				try {
					prefs.clear();
				} catch (Exception x) {
				}
				prefs = null;
				Preferences.importPreferences(is);
			}
		} catch (InvalidPreferencesFormatException e) {
		} catch (IOException e) {
		}

		if (is == null)
			return;
	
		Preferences prefs = Preferences
				.userNodeForPackage(org.rosuda.JGR.JGR.class);
		FontName = prefs.get("FontName", FontName);
		defaultPackages = prefs.get("DefaultPackages", null);
		FontSize = prefs.getInt("FontSize", FontSize);
		int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
	    double fs = ((double)prefs.getInt("FontSize", FontSize)) * (screenRes / 72.0);
	    FontSize = (int) Math.round(fs);
		maxHelpTabs = prefs.getInt("MaxHelpTabs", maxHelpTabs);
		useHelpAgent = prefs.getBoolean("UseHelpAgent", true);
		useHelpAgentConsole = prefs.getBoolean("UseHelpAgentConsole",
				useHelpAgentConsole);
		useHelpAgentEditor = prefs.getBoolean("UseHelpAgentEditor",
				useHelpAgentEditor);
		// it is safe to use emacs bindings on Macs since that's the default in
		// Coca widgets. on win/unix it's not safe since ctrl may be the sc
		// modifier
		useEmacsKeyBindings = prefs.getBoolean("UseEmacsKeyBindings",
				org.rosuda.util.Platform.isMac);
		previousPackages = prefs.get("PreviousPackages", null);
		showHiddenFiles = prefs.getBoolean("ShowHiddenFiles", false);
		workingDirectory = prefs.get("WorkingDirectory", System
				.getProperty("user.home"));
		tabWidth = prefs.getInt("tabWidth", 4);
		askForSavingWorkspace = prefs.getBoolean("AskForSavingWorkspace", true);
		
		
		is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(System
					.getProperty("user.home")
					+ File.separator + ".JGREditorprefsrc"));
		} catch (FileNotFoundException e) {
		}

		try {
			if (is != null) {
				Preferences editPrefs = Preferences
						.userNodeForPackage(org.rosuda.JGR.editor.Editor.class);
				try {
					editPrefs.clear();
				} catch (Exception x) {
				}
				editPrefs = null;
				Preferences.importPreferences(is);
			}
		} catch (InvalidPreferencesFormatException e) {
			new ErrorMsg(e);
		} catch (IOException e) {
			new ErrorMsg(e);
		}

		if (is == null)
			return;
		Preferences editPrefs = Preferences
		.userNodeForPackage(org.rosuda.JGR.editor.Editor.class);
		CMDColor = Color.decode(editPrefs.get("CMDColor", "#ff0000"));
		RESULTColor = Color.decode(editPrefs.get("RESULTColor", "#0000ff"));
		ERRORColor = Color.decode(editPrefs.get("ERRORColor", "#ff0000"));
		BRACKETHighLight = Color.decode(editPrefs.get("BRACKETHighLight", "#ffffff"));
		NUMBERColor = Color.decode(editPrefs.get("NUMBERColor", "#ff0000"));
		KEYWORDColor = Color.decode(editPrefs.get("KEYWORDColor", "#00008c"));
		KEYWORD_BOLD = editPrefs.getBoolean("KEYWORD_BOLD", true);
		OBJECTColor = Color.decode(editPrefs.get("OBJECTColor", "#32008c"));
		OBJECT_IT = editPrefs.getBoolean("OBJECT_IT", true);
		AUTOTAB = editPrefs.getBoolean("AUTOTAB", true);
		COMMENTColor = Color.decode(editPrefs.get("COMMENTColor", "#000000"));//"#007800"));
		COMMENT_IT = editPrefs.getBoolean("COMMENT_IT", true);
		QUOTEColor = Color.decode(editPrefs.get("QUOTEColor", "#0000ff"));
		LINE_HIGHLIGHT = editPrefs.getBoolean("LINE_HIGHLIGHT", true);
		HIGHLIGHTColor = Color.decode(editPrefs.get("HIGHLIGHTColor", "#e0e0e0"));
		LINE_NUMBERS = editPrefs.getBoolean("LINE_NUMBERS", true);
		
		consoleWidth = editPrefs.getInt("consoleWidth", 550);
		consoleHeight = editPrefs.getInt("consoleHeight", 700);
	}

	/**
	 * Save preferences to .JGRPrefsrc.
	 * 
	 */
	public static void writePrefs() {
		Preferences prefs = Preferences
				.userNodeForPackage(org.rosuda.JGR.JGR.class);

		try {
			prefs.clear();
		} catch (Exception x) {
		}
	
		prefs.putBoolean("AskForSavingWorkspace",askForSavingWorkspace);
		prefs.putInt("PrefsVersion", prefsVersion);
		prefs.put("FontName", FontName); // String
		int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
	    double fs = ((double)FontSize) * ( 72.0 / screenRes);
		prefs.putInt("FontSize",(int) Math.round(fs)); // int
		prefs.putInt("MaxHelpTabs", maxHelpTabs);
		prefs.putBoolean("UseHelpAgent", useHelpAgent);
		prefs.putBoolean("UseHelpAgentConsole", useHelpAgentConsole);
		prefs.putBoolean("UseHelpAgentEditor", useHelpAgentEditor);
		prefs.putBoolean("UseEmacsKeyBindings", useEmacsKeyBindings);
		prefs.putBoolean("ShowHiddenFiles", showHiddenFiles);
		prefs.put("PreviousPackages", RController.getCurrentPackages()
				+ (JGRPackageManager.remindPackages == null ? ""
						: ("," + JGRPackageManager.remindPackages)));
		prefs.put("WorkingDirectory", workingDirectory);
		prefs.putInt("tabWidth", tabWidth);
		
		if (JGRPackageManager.defaultPackages != null
				&& JGRPackageManager.defaultPackages.length > 0) {
			String packages = JGRPackageManager.defaultPackages[JGRPackageManager.defaultPackages.length - 1]
					.toString();
			for (int i = JGRPackageManager.defaultPackages.length - 2; i >= 0; i--)
			{
				String pkg = (String) JGRPackageManager.defaultPackages[i];
				if (!"JGR".equals(pkg) && !"rJava".equals(pkg) && ! "JavaGD".equals(pkg) && !"iplots".equals(pkg))
					packages += ", " + pkg;
			}
			// JGR will always be appended at the end on start-up, se we don't need it here
			//packages += ",rJava,JavaGD,iplots,JGR";
			
			prefs.put("DefaultPackages", packages);
		}
		try {
			prefs.exportNode(new FileOutputStream(System
					.getProperty("user.home")
					+ File.separator + ".JGRprefsrc"));
		} catch (IOException e) {
		} catch (BackingStoreException e) {
		}
		
		Preferences editPrefs = Preferences
		.userNodeForPackage(org.rosuda.JGR.editor.Editor.class);
		try {
			editPrefs.clear();
		} catch (Exception x) {
		}
		

		editPrefs.putInt("PrefsVersion", prefsVersion);
		editPrefs.put("CMDColor","#"+ Integer.toHexString(CMDColor.getRGB()).substring(2));
		editPrefs.put("RESULTColor","#"+ Integer.toHexString(RESULTColor.getRGB()).substring(2));
		editPrefs.put("ERRORColor","#"+ Integer.toHexString(ERRORColor.getRGB()).substring(2));
		editPrefs.put("BRACKETHighLight","#"+ Integer.toHexString(BRACKETHighLight.getRGB()).substring(2));
		editPrefs.put("NUMBERColor","#"+ Integer.toHexString(NUMBERColor.getRGB()).substring(2));
		editPrefs.put("KEYWORDColor","#"+ Integer.toHexString(KEYWORDColor.getRGB()).substring(2));
		editPrefs.putBoolean("KEYWORD_BOLD",KEYWORD_BOLD );
		editPrefs.put("OBJECTColor","#"+ Integer.toHexString(OBJECTColor.getRGB()).substring(2));
		editPrefs.putBoolean("OBJECT_IT", OBJECT_IT);
		editPrefs.putBoolean("AUTOTAB", AUTOTAB);
		editPrefs.put("COMMENTColor","#"+ Integer.toHexString(COMMENTColor.getRGB()).substring(2));
		editPrefs.putBoolean("COMMENT_IT", COMMENT_IT);
		editPrefs.put("QUOTEColor","#"+ Integer.toHexString(QUOTEColor.getRGB()).substring(2));
		editPrefs.putBoolean("LINE_HIGHLIGHT", LINE_HIGHLIGHT);
		editPrefs.put("HIGHLIGHTColor","#"+ Integer.toHexString(HIGHLIGHTColor.getRGB()).substring(2));
		editPrefs.putBoolean("LINE_NUMBERS", LINE_NUMBERS);
		
		editPrefs.putInt("consoleWidth", JGR.MAINRCONSOLE.getWidth());
		editPrefs.putInt("consoleHeight", JGR.MAINRCONSOLE.getHeight());
		try {
			editPrefs.exportNode(new FileOutputStream(System
					.getProperty("user.home")
					+ File.separator + ".JGREditorprefsrc"));
		} catch (IOException e) {
		} catch (BackingStoreException e) {
		}
		
	}

	/**
	 * Save missing packages if the user likes to be reminded.
	 */
	public static void writeCurrentPackagesWhenExit() {
		readPrefs();
		writePrefs();
	}
}
