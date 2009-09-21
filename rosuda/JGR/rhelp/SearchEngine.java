package org.rosuda.JGR.rhelp;

/*
 * ============================================================================
 * Project: Simple JAVA Search Engine for Keyword Search JAVA Source file for
 * the class SearchEngine COPYRIGHT (C), 1998-2000, Thomas Baier, R Core
 * Development Team This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ============================================================================
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.JGRHelp;
import org.rosuda.JGR.util.ErrorMsg;

/**
 * SearchEngine.java from R-project modified version for being able to use it
 * with JGR
 */

public class SearchEngine {

	/* Instance Variables */

	private IndexTable iIndexTable;

	private String iSearchTerm;

	private URL IndexFile;

	private JGRHelp help = null;

	/* Static Data */

	private static final String cIndexFile = "index.txt";

	private boolean started = false;

	public SearchEngine() {
		try {
			File tempfile = new File(JGRHelp.RHELPLOCATION + "/doc/html/search/");
			if (tempfile.exists()) {
				IndexFile = tempfile.toURI().toURL();
				readIndexFile(cIndexFile);
				started = true;
			} else
				JOptionPane.showMessageDialog(null, "Help will not be available", "Path not found", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setRHelp(JGRHelp help) {
		this.help = help;
	}

	/* perform the search and return result as string */

	public URL search(String key, boolean exactMatch, boolean searchDesc, boolean searchKeywords, boolean searchAliases) {
		if (!started)
			return null;
		iSearchTerm = key;

		if (help != null) {
			help.setWorking(true);
			help.link.setText("Search for \"" + iSearchTerm);
		}

		Vector foundItems = null;

		if (iSearchTerm != null)
			foundItems = iIndexTable.search(iSearchTerm, exactMatch ? true : searchDesc, exactMatch ? false : searchKeywords, exactMatch ? true
					: searchAliases);
		else
			foundItems = null;

		String result = "";
		File out = new File(System.getProperty("java.io.tmpdir") + File.separator + iSearchTerm + ".htm");
		URL helpRes = null;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(out));

			// if nothing found, return a special string
			if (foundItems == null)
				result += "No matches for <b>\"" + iSearchTerm + "\"</b> have been found!<hr>";
			else {

				Enumeration cursor = foundItems.elements();

				result += "The search string was <b>\"" + iSearchTerm + "</b>\"" + "<hr>" + "<dl>";

				while (cursor.hasMoreElements()) {
					IndexEntry entry = (IndexEntry) cursor.nextElement();

					if (exactMatch && entry.getEntry().equals(iSearchTerm)) {
						if (help != null)
							help.link.setText(" ");
						File f = new File(JGR.RLIBS[0] + File.separator + entry.getURL());
						if (!f.exists())
							for (int i = 1; i < JGR.RLIBS.length; i++) {
								f = new File(JGR.RLIBS[i] + File.separator + entry.getURL());
								if (f.exists())
									break;
							}
						else
							f = new File(JGRHelp.RHELPLOCATION + File.separator + "/library" + File.separator + entry.getURL());
						return f.toURI().toURL();
					}
					File f = new File(JGR.RLIBS[0] + File.separator + entry.getURL());
					if (!f.exists())
						for (int i = 1; i < JGR.RLIBS.length; i++) {
							f = new File(JGR.RLIBS[i] + File.separator + entry.getURL());
							if (f.exists())
								break;
						}
					else
						f = new File(JGRHelp.RHELPLOCATION + File.separator + "library" + File.separator + entry.getURL());
					result += "<dt><a href=\"" + f.toURI().toURL() + "\">" + entry.getEntry() + "</a></dt>\n";
					result += "<dd>" + entry.getDescription() + "</dd>\n";
				}

				result += "</dl>";
			}
			writer.write(result);
			writer.flush();
			writer.close();
			helpRes = out.toURI().toURL();
		} catch (Exception e) {
			new ErrorMsg(e);
		}
		if (help != null) {
			help.link.setText(" ");
			help.setWorking(false);
		}
		return helpRes;
	}

	private void readIndexFile(String idxFile) {
		// create the index table
		iIndexTable = new IndexTable();

		URL baseURL = IndexFile;

		// get the index file and parse its contents
		try {
			URL idxFileURL = new URL(baseURL, idxFile);

			// get an IndexStream object for ease of parsing
			IndexStream idxStream = new IndexStream(idxFileURL);

			// now start parsing...

			/*
			 * An entry consists of a title, keywords, aliases, an URL and a
			 * description, everything else is ignored. Every entry starts with
			 * the keyword "Entry" (case is ignored) must-have entries are
			 * "Entry" and "Keywords" 98-06-01: bugfix: don't null the variables
			 */
			String entry = "";
			String keywords = "";
			String aliases = "";
			String url = "";
			String description = "";
			String prefix = "";
			String suffix = "";

			Value value = idxStream.popEntry();

			while (value != null) {
				// parse the value now
				if (value.getKey().equalsIgnoreCase("entry")) {
					// if a new entry is about to start, add the current one
					addEntry(entry, keywords, aliases, description, url, prefix, suffix);

					entry = value.getValue();
					aliases = entry;
					keywords = "";
					url = "";
					description = "";
				} else if (value.getKey().equalsIgnoreCase("keywords"))
					keywords += value.getValue();
				else if (value.getKey().equalsIgnoreCase("aliases"))
					aliases += value.getValue();
				else if (value.getKey().equalsIgnoreCase("url"))
					url = prefix + value.getValue() + suffix;
				else if (value.getKey().equalsIgnoreCase("description"))
					description = value.getValue();
				else if (value.getKey().equalsIgnoreCase("prefix")) {
					prefix = value.getValue();
					Tracer.write("using new URL prefix \"" + prefix + "\"\n");
				} else if (value.getKey().equalsIgnoreCase("suffix")) {
					suffix = value.getValue();
					Tracer.write("using new URL suffix \"" + suffix + "\"\n");
				}
				value = idxStream.popEntry();
			}

			// the final entry just read
			addEntry(entry, keywords, aliases, description, url, prefix, suffix);
		} catch (MalformedURLException exc) {
			exc.printStackTrace();
			// an error occured while reading...
		}

		return;
	}

	private void addEntry(String entry, String keywords, String aliases, String description, String url, String prefix, String suffix) {
		// the entry must be set
		if (entry.length() == 0)
			return;

		// if the URL is empty, construct one following the rule:
		// URL = prefix + entry + suffix
		if (url.length() == 0)
			url = prefix + entry + suffix;
		IndexEntry idxEntry = new IndexEntry(entry, keywords, aliases, description, url);
		iIndexTable.addElement(idxEntry);

		return;
	}
}
// Local Variables:
// mode: Java
// mode: font-lock
// End:
