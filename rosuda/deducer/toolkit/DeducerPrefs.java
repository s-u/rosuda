package org.rosuda.deducer.toolkit;

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

import org.rosuda.ibase.Common;
import org.rosuda.JGR.JGR;

public class DeducerPrefs {

	public static boolean SHOWDATA ;
	public static boolean SHOWANALYSIS;
	public static boolean USEQUAQUACHOOSER;
	public static boolean VIEWERATSTARTUP;
	
	public static void initialize() {
		SHOWDATA = true;
		SHOWANALYSIS = true;
		USEQUAQUACHOOSER = Common.isMac() ? true : false;
		VIEWERATSTARTUP = true;		
		readPrefs();
	}
	
	public static void readPrefs() {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(System
					.getProperty("user.home")
					+ File.separator + ".DeducerPrefs"));
		} catch (FileNotFoundException e) {
			JGR.R.eval("cat('\\nNote: Deducer prefernce file not found.\\nDon't worry about this if it is your first time loading Deducer.\\n')");
		}

		try {
			if (is != null) {
				Preferences prefs = Preferences
						.userNodeForPackage(org.rosuda.deducer.Deducer.class);
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

		if (is == null){
			JGR.R.eval("cat('unable to read prefs\\n')");
			return;
		}
		Preferences prefs = Preferences.userNodeForPackage(org.rosuda.deducer.Deducer.class);
		
		SHOWDATA = prefs.getBoolean("SHOWDATA",true);
		SHOWANALYSIS = prefs.getBoolean("SHOWANALYSIS",true);
		USEQUAQUACHOOSER = prefs.getBoolean("USEQUAQUACHOOSER",Common.isMac() ? true : false);
		VIEWERATSTARTUP = prefs.getBoolean("VIEWERATSTARTUP",true);
	}
	
	public static void writePrefs() {
		Preferences prefs = Preferences
				.userNodeForPackage(org.rosuda.deducer.Deducer.class);

		try {
			prefs.clear();
		} catch (Exception x) {
			JGR.R.eval("cat('unable to clear prefs\\n')");
		}
		prefs.putBoolean("SHOWDATA", SHOWDATA);
		prefs.putBoolean("SHOWANALYSIS", SHOWANALYSIS);
		prefs.putBoolean("USEQUAQUACHOOSER", USEQUAQUACHOOSER);
		prefs.putBoolean("VIEWERATSTARTUP", VIEWERATSTARTUP);
		try {
			prefs.exportNode(new FileOutputStream(System
					.getProperty("user.home")
					+ File.separator + ".DeducerPrefs"));
		} catch (IOException e) {
			JGR.R.eval("cat('unable to write prefs\\n')");
		} catch (BackingStoreException e) {
			JGR.R.eval("cat('unable to write prefs\\n')");
		}

	}
	
}
