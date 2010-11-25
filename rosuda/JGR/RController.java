package org.rosuda.JGR;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.rosuda.JGR.editor.Editor;
import org.rosuda.JGR.robjects.RModel;
import org.rosuda.JGR.robjects.RObject;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REXPVector;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.ibase.SVar;
import org.rosuda.ibase.SVarDouble;
import org.rosuda.ibase.SVarFact;
import org.rosuda.ibase.SVarInt;
import org.rosuda.ibase.SVarObj;
import org.rosuda.ibase.SVarSet;
import org.rosuda.util.Global;

/**
 * RController - implementations of an interface between JGR and Rengine,
 * providing all needed functions for working with R.
 * 
 * @author Markus Helbig RoSuDa 2003 - 2005
 */

public class RController {

	public static final String TEMP_MATRIX_DIM_NAMES_JGR = "tempMatrixDimNamesJGR";
	public static final String TEMP_MATRIX_CONTENT_JGR = "tempMatrixContentJGR";
	public static final String TEMP_VARIABLE_NAME = "jgrtemp";
	/**
	 * dummy object.
	 */
	public static Object dummy = new Object();

	/**
	 * Get R_HOME.
	 * 
	 * @return R_HOME path
	 */
	public static String getRHome() {
		REXP x;
		try {
			x = JGR.eval("R.home()");
			return x.asString();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Get R_LIBS.
	 * 
	 * @return R_LIBS paths
	 */
	public static String[] getRLibs() {
		try {
			REXP x = JGR.eval(".libPaths()");
			return x.asStrings();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get R prompt
	 * 
	 * @return prompt
	 */
	public static String getRPrompt() {
		try {
			REXP x = JGR.eval("try(as.character(options('prompt')),silent=TRUE)");
			return x.asString();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "> ";
	}

	/**
	 * Get R continue
	 * 
	 * @return continue
	 */
	public static String getRContinue() {
		try {
			REXP x = JGR.eval("try(as.character(options('continue')),silent=TRUE)");
			return x.asString();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "> ";
	}

	/**
	 * Get R_DEFAULT_PACKAGES.
	 * 
	 * @return default packages
	 */
	public static String getCurrentPackages() {

		REXP x;
		try {
			x = JGR.eval(".packages(TRUE)");
			if (x != null && !x.isNull() && x.asStrings() != null) {
				String p = "";
				for (int i = 0; i < x.asStrings().length - 1; i++)
					p += x.asStrings()[i] + ",";
				return p += x.asStrings()[x.asStrings().length - 1];
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// .allpkg
		return null;
	}

	/**
	 * Get default packages as defined by R
	 * 
	 * @return R default packages
	 */
	public static String[] getDefaultPackages() {
		try {
			REXP x = JGR.eval("getOption(\"defaultPackages\")");
			return x.asStrings();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String[] {};
	}

	/**
	 * Get default packages as defined by JGR (i.e. R's default packages plus
	 * auto-loaded packages from JGR preferences)
	 * 
	 * @return JGR default packages
	 */
	public static String[] getJgrDefaultPackages() {
		String jdp = JGRPrefs.defaultPackages;
		if (jdp == null)
			jdp = "JGR";
		try {
			REXP x = JGR.eval("as.character(unique(c(getOption(\"defaultPackages\"),strsplit(\"" + jdp + "\",', ?')[[1]],'JGR')))");
			return x.asStrings();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String[] {};
	}

	/** load and attach packages defined by the string "pkg, pkg, ..." */
	public static void requirePackages(String rlist) {
		try {
			JGR.getREngine().assign(".$JGR", rlist); // to
			JGR.eval("{ for (pkg in strsplit(`.$JGR`,', ?')[[1]]) require(pkg, warn.conflicts=FALSE, character.only=TRUE); rm(`.$JGR`); TRUE }");
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * loads a package. If it isn't installed, it is loaded from CRAN
	 * 
	 * @param pack
	 */
	public static void loadPackage(String pack) {
		String packages = getCurrentPackages();
		if (!packages.contains(pack)) {
			try {
				JGR.eval("cat('Package " + pack + " not found. Attempting to download...\n')");
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JGR.MAINRCONSOLE.execute("install.packages('" + pack + "');library(" + pack + ")", true);
		} else
			try {
				JGR.eval("library(" + pack + ")");
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * Show all posibilities to complete your given part of a command.
	 * 
	 * @param partOfCmd
	 *            part which you want to complete
	 * @return possible completions
	 */
	public static String[] completeCommand(String partOfCmd) {
		if (!JGR.STARTED)
			return null;
		if (partOfCmd.trim().length() == 0)
			return null;
		partOfCmd = partOfCmd.replaceAll("\\.", "\\\\\\\\."); // replace real .
		// with their
		// equivalent regex
		REXP cmds;
		try {
			cmds = JGR.idleEval("try(.completeCommand(\"" + partOfCmd + "\"),silent=TRUE)");
			String[] c = null;
			if (cmds != null && (c = cmds.asStrings()) != null)
				return c;
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Completetion of a file (doesn't supports multiple fils yet).
	 * 
	 * @param part
	 *            of file to complete
	 * @return file
	 */
	public static String[] completeFile(String part) {
		part = part.replaceFirst("~", System.getProperty("user.home"));
		int tl = part.length();
		int ls = tl - 1, fb = 0;
		if (tl == 0)
			ls = 0;
		String dir = null;
		boolean working = false;
		String fn = null;
		if (System.getProperty("os.name").startsWith("Windows"))
			part = part.replaceAll(":/", "://");
		while (ls > 0 && part.charAt(ls) != '/')
			ls--;
		if (ls == 0 && (tl == 0 || part.charAt(ls) != '/'))
			working = true;
		dir = working ? "." : ((ls == 0) ? "/" : (part.substring(0, ls)));
		fb = ls;
		if (fb < tl && part.charAt(fb) == '/')
			fb++;
		fn = (fb < tl) ? part.substring(fb) : "";
		File directory = null;
		String[] cont = null;
		if ((directory = new File(dir)).exists())
			cont = directory.list();
		if (cont == null)
			return null;
		int matches = 0;

		Vector matchedFiles = new Vector();

		String common = null;
		for (int i = 0; i < cont.length; i++) {
			String sx = cont[i];
			if (sx.startsWith(fn)) {
				if (matches == 0) {
					common = sx;
				} else
					common = commonWithPrefix(common, sx);
				matches++;

				if (JGRPrefs.showHiddenFiles)
					matchedFiles.add(sx);
				else if (!sx.trim().startsWith("."))
					matchedFiles.add(sx);
			}
		}
		if (common != null && matchedFiles.size() == 1) {
			String fnp = common.replace('\\', '/');
			File tfile = null;
			fnp = ((dir == ".") ? fnp : ((dir == "/") ? ("/" + fnp) : (dir + "/") + fnp));
			if ((tfile = new File(fnp)).exists() && tfile.isDirectory())
				fnp = fnp + "/";
			if (fnp.endsWith("//"))
				fnp = "";
			return new String[] { fnp.replaceFirst(part, "") };
		} else if (matchedFiles.size() > 1) {
			String[] m = new String[matchedFiles.size()];
			matchedFiles.copyInto(m);
			return m;
		}
		return null;
	}

	public static String addSlashes(String str){
		if(str==null) return "";

		StringBuffer s = new StringBuffer(str);
		for(int i = 0; i < s.length(); i++){
			if(s.charAt (i) == '\"')
				s.insert(i++, '\\');
			else if(s.charAt (i) == '\'')
				s.insert(i++, '\\');
		}
		
		return s.toString();
	}
	public static String makeValidVariableName(String var) {
		return var.replaceAll("[ -+*/\\()=!~`@#$%^&*<>,?;:\"\']", ".");
	}

	public static String makeRStringVector(ArrayList lis) {
		if (lis.size() == 0)
			return "c()";
		String result = "c(";
		for (int i = 0; i < lis.size(); i++) {
			result += "\"" + lis.get(i).toString() + "\"";
			if (i < lis.size() - 1)
				result += ",";
			if (i % 10 == 9)
				result += "\n";
		}
		result += ")";
		return result;
	}

	public static String makeRStringVector(JList list) {
		ArrayList varList = new ArrayList();
		for (int i = 0; i < list.getModel().getSize(); i++)
			varList.add(list.getModel().getElementAt(i));
		return makeRStringVector(varList);
	}

	public static String makeRStringVector(DefaultListModel mod) {
		ArrayList varList = new ArrayList();
		for (int i = 0; i < mod.getSize(); i++)
			varList.add(mod.getElementAt(i));
		return makeRStringVector(varList);
	}

	public static String makeRStringVector(Vector mod) {
		ArrayList varList = new ArrayList();
		for (int i = 0; i < mod.size(); i++)
			varList.add(mod.get(i));
		return makeRStringVector(varList);
	}

	public static String makeRVector(Vector vec) {
		String outcomes = "c(";
		for (int i = 0; i < vec.size(); i++) {
			outcomes += (String) vec.get(i);
			if (i < (vec.size() - 1))
				outcomes += ",";
			if (i % 10 == 9)
				outcomes += "\n";
		}
		outcomes += ")";
		return outcomes;
	}

	public static String makeRVector(DefaultListModel mod) {
		Vector tmp = new Vector();
		for (int i = 0; i < mod.getSize(); i++)
			tmp.add(mod.getElementAt(i));
		return makeRVector(tmp);
	}

	public static boolean isValidSubsetExp(String subset, String dataName) {
		if (subset == null || subset.length() < 1)
			return false;

		REXPLogical valid;
		try {
			valid = (REXPLogical) JGR.eval("(function(x,subset){" + "result<-try(e <- substitute(subset),silent=TRUE)\n"
					+ "if(class(result)==\"try-error\")\n" + "	return(FALSE)\n" + "result<-try(r <- eval(e, x, parent.frame()),silent=TRUE)\n"
					+ "if(class(result)==\"try-error\")\n" + "	return(FALSE)\n" + "is.logical(r)\n" + "})(" + dataName + "," + subset + ")");
			if (valid == null) {
				return false;
			}
			if (valid == null) {
				return false;
			}
			return valid.isTRUE()[0];
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get all keywords for syntaxhighlighting.
	 * 
	 * @return keywords
	 */
	public static String[] getKeyWords() {
		REXP x;
		String[] r = null;
		try {
			x = JGR.idleEval(".refreshKeyWords()");
			if (x != null && !x.isNull() && (r = x.asStrings()) != null)
				return r;
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Get object names used for syntaxhighlighting.
	 * 
	 * @return objects
	 */
	public static String[] getObjects() {
		REXP x;
		String[] r = null;
		try {
			x = JGR.idleEval(".refreshObjects()");
			if (x != null && !x.isNull() && (r = x.asStrings()) != null)
				return r;
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * Browse the workspace for objects and put them in JGR.MODELS, JGR.DATA,
	 * JGR.OTHER and JGR.FUNCTIONS. currently only these things which are
	 * provided by R-command: ls(pos=1).
	 */
	public static void refreshObjects() {

		String models[];
		REXP x = null;

		try {
			x = JGR.idleEval(".getModels()");
			if (x != null && x != null && !x.isNull())
				JGR.MODELS.clear();
			if (x != null && !x.isNull() && x != null && !x.isNull() && (models = x.asStrings()) != null) {
				for (int i = 0; i < models.length; i++)
					JGR.MODELS.add(createRModel(models[i], models[++i]));
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			x = JGR.idleEval(".getDataObjects()");
			String[] data;
			if (x != null && !x.isNull())
				JGR.DATA.clear();
			if (x != null && !x.isNull() && (data = x.asStrings()) != null) {
				int a = 1;
				for (int i = 0; i < data.length; i++) {
					boolean b = (data[i].equals("null") || data[i].trim().length() == 0);
					String name = b ? a + "" : data[i];
					JGR.DATA.add(createRObject(name, data[++i], null, (!b)));
					a++;
				}
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			x = JGR.idleEval(".getOtherObjects()");
			String[] other;
			if (x != null && !x.isNull())
				JGR.OTHERS.clear();
			if (x != null && !x.isNull() && (other = x.asStrings()) != null) {
				int a = 1;

				for (int i = 0; i < other.length; i++) {
					boolean b = (other[i].equals("null") || other[i].trim().length() == 0);
					String name = b ? a + "" : other[i];
					JGR.OTHERS.add(createRObject(name, other[++i], null, (!b)));
					a++;
				}
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			x = JGR.idleEval(".getFunctionsInWS()");
			String[] functions;
			if (x != null && !x.isNull())
				JGR.FUNCTIONS.clear();
			if (x != null && !x.isNull() && (functions = x.asStrings()) != null) {
				int a = 1;
				for (int i = 0; i < functions.length; i++) {
					JGR.FUNCTIONS.add(createRObject(functions[i], "function", null, true));
					a++;
				}
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get information about all packages (loaded, undloaded, defaults ...).
	 * 
	 * @return package information
	 */
	public static Object[][] refreshPackages() {
		Object[][] pkg = null;
		Hashtable loadedP = new Hashtable();
		REXP x;
		try {
			x = JGR.eval("sort(.packages(all.available=T))");
			String[] res;
			if (x != null && !x.isNull() && x.asStrings() != null) {
				REXP y = JGR.eval("(.packages())");
				if (y != null && !y.isNull() && (res = y.asStrings()) != null)
					for (int i = 0; i < res.length; i++)
						loadedP.put(res[i], dummy);
				res = x.asStrings();
				pkg = new Object[res.length][4];
				for (int i = 0; i < res.length; i++) {
					pkg[i][2] = new String(res[i]);
					try {
						pkg[i][3] = JGR.eval("packageDescription(\"" + res[i] + "\",fields=\"Title\")").asString();
					} catch (Exception e) {
						pkg[i][1] = "";
					}
					pkg[i][0] = loadedP.containsKey(res[i]) ? (new Boolean(true)) : (new Boolean(false));
					pkg[i][1] = new Boolean(false);
					for (int d = 0; d < JGRPackageManager.defaultPackages.length; d++)
						if (res[i].equals(JGRPackageManager.defaultPackages[d]) || JGRPackageManager.neededPackages.containsKey(res[i]))
							pkg[i][1] = new Boolean(true);
				}
			}
		} catch (REngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (REXPMismatchException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return pkg;
	}

	/**
	 * Get the content of an {@see RObject} (list, data.frame, table, matrix).
	 * 
	 * @param o
	 *            {@see RObject}
	 * @param c
	 *            all found objects are collected in c (currently disabled)
	 * @return vector of {@see RObject}
	 */
	public static Vector createContent(RObject o, Collection c) {
		Vector cont = new Vector();
		String p = "";
		if (o.getParent() != null && o.getParent().getType().equals("table"))
			p = "," + o.getParent().getRName();
		REXP x;
		try {
			x = JGR.eval("suppressWarnings(try(.getContent(" + (o.getRName()) + p + "),silent=TRUE))");
			String[] res;
			if (x != null && !x.isNull() && (res = x.asStrings()) != null && !res[0].startsWith("Error")) {
				int a = 1;
				for (int i = 0; i < res.length; i++) {
					boolean b = (res[i].equals("null") || res[i].trim().length() == 0);
					String name = b ? a + "" : res[i];

					RObject ro = createRObject(name, res[++i], o, (!b));

					// if (c != null) c.add(ro);
					if (ro != null)
						cont.add(ro);
					a++;
				}
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cont;
	}

	/**
	 * Ceates an {@seeRObjects} (java-side) out of R
	 * 
	 * @param sx
	 *            name
	 * @param type
	 *            type
	 * @param parent
	 *            parent {@seeRObjects}
	 * @param b
	 *            names(..) provides real names or not
	 * @return new RObject
	 */
	public static RObject createRObject(String sx, String type, RObject parent, boolean b) {
		RObject ro = new RObject(sx, type, parent, b);
		REXP y;
		if (type.equals("data.frame")) {
			try {
				y = JGR.eval("dim(" + (ro.getRName()) + ")");
				if (!y.isNull() && y.asIntegers() != null)
					ro.setInfo("dim(" + y.asIntegers()[0] + ":" + y.asIntegers()[1] + ")");
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (type.equals("matrix")) {
			try {
				y = JGR.eval("dim(" + (ro.getRName()) + ")");
				if (!y.isNull() && y.asIntegers() != null)
					ro.setInfo("dim(" + y.asIntegers()[0] + ":" + y.asIntegers()[1] + ")");
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (type.equals("factor")) {
			try {
				y = JGR.eval("length(levels(" + (ro.getRName()) + "))");
				if (!y.isNull() && y.asIntegers() != null)
					ro.setInfo("levels: " + y.asIntegers()[0]);
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (type.equals("list")) {
			try {
				y = JGR.eval("length(" + (ro.getRName()) + ")");
				if (!y.isNull() && y.asIntegers() != null)
					ro.setInfo("length: " + y.asIntegers()[0]);
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (type.equals("table")) {
			try {
				y = JGR.eval("length(dim(" + (ro.getRName()) + "))");
				if (!y.isNull() && y.asIntegers() != null)
					ro.setInfo("dim: " + y.asIntegers()[0]);
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (type.equals("function")) {
			String fHelp = getFunHelp(ro.getRName());
			if (fHelp != null) {
				ro.setInfo("arguments: " + fHelp.replaceFirst(ro.getRName(), "").replaceAll("<br>", ""));
			}
		} else if (parent != null && parent.getType().equals("table")) {
			try {
				y = JGR.eval("length(dimnames(" + parent.getRName() + ")[[\"" + ro.getName() + "\"]])");
				if (!y.isNull() && y.asIntegers() != null)
					ro.setInfo("cats: " + y.asIntegers()[0]);
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ro;
	}

	/**
	 * Create {@see RModel} (java-side) out of R
	 * 
	 * @param sx
	 *            name
	 * @param type
	 *            type (currently only lm and glm is supported
	 * @return new {@see RModel}
	 */
	public static RModel createRModel(String sx, String type) {
		RModel m = new RModel(sx, type);
		REXP y;
		double[] res;
		try {
			y = JGR.eval("try(summary(" + sx + ")[[\"r.squared\"]],silent=TRUE)");
			if (!y.isNull() && (res = y.asDoubles()) != null)
				m.setRsquared(res[0]);
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			y = JGR.eval("try(AIC(" + sx + "),silent=TRUE)");
			if (!y.isNull() && (res = y.asDoubles()) != null)
				m.setAic(res[0]);
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			y = JGR.eval("try(deviance(" + sx + "),silent=TRUE)");
			if (!y.isNull() && (res = y.asDoubles()) != null)
				m.setDeviance(res[0]);
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] res1;
		REXP x;
		try {
			x = JGR.eval("try(summary(" + sx + ")[[\"df\"]],silent=TRUE)");
			if (x != null && !x.isNull() && (res1 = x.asIntegers()) != null)
				m.setDf(res1[0]);
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] res2;
		REXP z;
		try {
			z = JGR.eval("try(family(" + sx + ")[[\"family\"]],silent=TRUE)");
			if ((z != null && !z.isNull()) && (res2 = z.asStrings()) != null)
				m.setFamily(res2[0]);
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			z = JGR.eval("suppressWarnings(try(capture.output(" + sx + "[[\"call\"]][[\"formula\"]])))");
			if ((z != null && !z.isNull()) && (res2 = z.asStrings()) != null) {
				String call = "";
				for (int i = 0; i < res2.length; i++)
					call += res2[i];
				m.setCall(call);
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // as.character((cm$call))
		try {
			z = JGR.eval("suppressWarnings(try(capture.output(" + sx + "[[\"call\"]][[\"data\"]])))");
			if ((z != null && !z.isNull()) && (res2 = z.asStrings()) != null) {
				String data = "";
				for (int i = 0; i < res2.length; i++)
					data += res2[i];
				if (!data.trim().equals("NULL"))
					m.setData(data);
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // as.character((cm$call))
		return m;
	}

	public static String getFunHelpTip(String s) {
		String tip = getFunHelp(s);
		return tip != null ? "<html><pre>" + tip + "</pre></html>" : null;
	}

	/**
	 * Get argumentes for function
	 * 
	 * @param s
	 *            function name
	 * @return arguments
	 */
	private static String getFunHelp(String s) {
		if (s == null)
			return null;
		String tip = null;
		String res[] = null;
		REXP x;
		try {
			x = JGR.idleEval("try(deparse(args(" + s + ")),silent=T)");
			if (x != null && !x.isNull() && (res = x.asStrings()) != null) {
				tip = "";
				int l = -1;
				for (int i = 0; i < (l = res.length); i++) {
					if ((l - 2) == i && !res[i].trim().equals("NULL"))
						tip += res[i].replaceFirst("function", s);
					else if (!res[i].trim().equals("NULL"))
						tip += res[i].replaceFirst("function", s) + "<br>";
				}
				tip += "";
			} else
				return null;
			if (tip.trim().length() == 0)
				return null;
		} catch (Exception e) {
			return null;
		}
		return (tip.indexOf("Error") >= 0) ? null : tip;
	}

	/**
	 * Get summary of an {@see RObject}, R-command summary(...).
	 * 
	 * @param o
	 *            {@see RObject}
	 * @return summary of object
	 */
	public static String getSummary(RObject o) {
		if (o.getType().equals("function"))
			return "<html><pre>" + getFunHelp(o.getRName()) + "</pre></html>";
		String tip = "";
		String res[] = null;
		REXP x;
		try {
			x = JGR.idleEval("suppressWarnings(try(capture.output(summary(" + (o.getRName()) + ")),silent=TRUE))");
			if (x != null && !x.isNull() && (res = x.asStrings()) != null && !res[0].startsWith("Error")) {
				// tip = "<html><pre>";
				int l = -1;
				for (int i = ((l = res.length) > 10 ? 10 : l) - 1; i >= 0; i--)
					if (i < l - 1)
						tip = res[i] + "<br>" + tip;
					else
						tip = res[i] + "       ";
				tip = "<html><pre>" + tip + (l > 10 ? "..." : "") + "</pre></html>";
			} else
				return null;
		} catch (Exception e) {
			return null;
		}
		return tip.startsWith("<html><pre>Error") ? null : tip;
	}

	public static void newFunction(RObject o) {
		REXP x;
		try {
			x = JGR.eval("suppressWarnings(try(capture.output(" + o.getRName() + "),silent=TRUE))");
			String[] res;
			if (x != null && !x.isNull() && (res = x.asStrings()) != null) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < res.length; i++)
					if (i == 0)
						sb.append(o.getRName() + " <- " + res[i] + "\n");
					else
						sb.append(res[i] + "\n");
				if (sb.length() > 0)
					new Editor().setText(sb);
			}
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create a new {@see SVarSet} of the specified {@see RObject}.
	 * 
	 * @param o
	 *            {@see RObject} which should be parsed into the dataset
	 * @return new dataset
	 */
	public static SVarSet newSet(RObject o) {
		SVarSet cvs = new SVarSet();
		cvs.setName(o.getRName());
		if (o.getType().equals("function")) {
			// thats not really the best way to do this but the easiest
			REXP x;
			try {
				x = JGR.eval("suppressWarnings(try(capture.output(" + o.getRName() + "),silent=TRUE))");
				String[] res;
				if (x != null && !x.isNull() && (res = x.asStrings()) != null) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < res.length; i++)
						if (i == 0)
							sb.append(o.getRName() + " <- " + res[i] + "\n");
						else
							sb.append(res[i] + "\n");
					if (sb.length() > 0)
						new Editor().setText(sb);
				}
			} catch (REngineException e) {
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			REXP x;
			try {
				x = JGR.eval("suppressWarnings(try(attributes(" + o.getRName() + ")[[\"row.names\"]],silent=TRUE))");
				String[] res;
				if (x != null && !x.isNull() && (res = x.asStrings()) != null && res.length > 0 && !res[0].startsWith("Error")) {
					SVar v = newVar(cvs, "row.names", x.asStrings());
					cvs.add(v);
				}
				Iterator i = createContent(o, null).iterator();
				if (!i.hasNext())
					cvs.add(createSVar(cvs, o));
				while (i.hasNext()) {
					RObject o2 = (RObject) i.next();
					cvs.add(createSVar(cvs, o2));
				}
			} catch (REngineException e) {
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				e.printStackTrace();
			}
		}

		return cvs;
	}

	/**
	 * Create SVar out of an {@see RObject}.
	 * 
	 * @param cvs
	 *            destination SVarSet
	 * @param o
	 *            {@see RObject}
	 * @return new SVar
	 */
	private static SVar createSVar(SVarSet cvs, RObject o) {

		REXP x;
		SVar v = null;
		try {
			x = JGR.eval("suppressWarnings(try(" + o.getRName() + ",silent=TRUE))");

			if (x == null || x.isNull())
				return null;
			if (o.getType().equals("factor")) {

				REXP y = JGR.eval("suppressWarnings(try(levels(" + o.getRName() + "),silent=TRUE))");

				REXP z = JGR.eval("suppressWarnings(try(as.integer(" + o.getRName() + "),silent=TRUE))");

				if ((z != null && !z.isNull()) && x != null && !x.isNull() && y.asStrings() != null && z.asIntegers() != null) {
					int id[] = new int[z.asIntegers().length];
					for (int i = 0; i < id.length; i++)
						id[i] = z.asIntegers()[i];
					v = newVar(cvs, o.getName(), id, y.asStrings());
				}

			} else if (o.getType().equals("character")) {
				if (x != null && !x.isNull() && x.isString())
					v = newVar(cvs, o.getName(), x.asStrings());
			} else if (x != null && !x.isNull() && x.isInteger())
				v = newVar(cvs, o.getName(), x.asIntegers());
			else if (x != null && !x.isNull() && x.isNumeric())
				v = newVar(cvs, o.getName(), x.asDoubles());
		} catch (REngineException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return v;
	}

	/**
	 * Construct a new numerical variable from supplied array of doubles.
	 * 
	 * @param name
	 *            variable name
	 * @param d
	 *            array of doubles
	 * @return SVar
	 */
	public static SVar newVar(SVarSet cvs, String name, double[] d) {
		if (d == null)
			return null;
		if (Global.DEBUG > 0)
			System.out.println("newVar: double[" + d.length + "]");
		if (cvs.count() > 0 && cvs.at(0).size() != d.length) {
			double[] n = new double[cvs.at(0).size()];
			for (int i = 0; i < d.length && i < n.length; i++)
				n[i] = d[i];
			d = n;
		}
		SVar v = new SVarDouble(name, d);
		return v;
	}

	/**
	 * Construct a new numerical variable from supplied array of integers.
	 * 
	 * @param name
	 *            variable name
	 * @param d
	 *            array of integers
	 * @return SVar
	 */
	public static SVar newVar(SVarSet cvs, String name, int[] d) {
		if (d == null)
			return null;
		if (Global.DEBUG > 0)
			System.out.println("newVar: int[" + d.length + "]");
		if (cvs.count() > 0 && cvs.at(0).size() != d.length) {
			int[] n = new int[cvs.at(0).size()];
			for (int i = 0; i < d.length && i < n.length; i++)
				n[i] = d[i];
			d = n;
		}
		SVar v = new SVarInt(name, d);
		return v;
	};

	/**
	 * Construct a new categorical variable from supplied array of strings.
	 * 
	 * @param name
	 *            variable name
	 * @param d
	 *            array of strings
	 * @return SVar
	 */
	public static SVar newVar(SVarSet cvs, String name, String[] d) {
		if (d == null)
			return null;
		if (Global.DEBUG > 0)
			System.out.println("newVar: String[]");
		if (cvs.count() > 0 && cvs.at(0).size() != d.length) {
			String[] n = new String[cvs.at(0).size()];
			for (int i = 0; i < d.length && i < n.length; i++)
				n[i] = d[i];
			d = n;
		}
		SVar v = new SVarObj(name);
		int i = 0;
		while (i < d.length)
			v.add(d[i++]);
		return v;
	}

	/*
	 * Construct a new factor variable from supplied array of integers (cases)
	 * and strings (levels). @param name variable name @param ix array of level
	 * IDs. IDs out of range (<1 or >length(d)) are treated as missing values
	 * @param d levels (d[0]=ID 1, d[1]=ID 2, ...) @return SVar
	 */
	public static SVar newVar(SVarSet cvs, String name, int[] ix, String[] d) {

		if (ix == null)
			return null;
		if (d == null)
			return newVar(cvs, name, ix);
		if (Global.DEBUG > 0)
			System.out.println("newVar: int[" + ix.length + "] + levels[" + d.length + "]");
		if (cvs.count() > 0 && cvs.at(0).size() != ix.length) {
			int[] n = new int[cvs.at(0).size()];
			for (int i = 0; i < d.length && i < n.length; i++)
				n[i] = ix[i];
			ix = n;
		}
		int j = 0;
		while (j < ix.length)
			ix[j++]--;
		; // reduce index by 1 since R is 1-based
		SVar v = new SVarFact(name, ix, d);
		return v;
	}

	/**
	 * Export an SVarSet to R.
	 * 
	 * @param vs
	 *            dataset
	 * @param type
	 *            R-class of {@see RObject} behind vs
	 * @return true if successful, false if not
	 */
	public static boolean export(SVarSet vs, String type) {
		boolean success = false;
		if (type == null || type.equals("data.frame"))
			success = exportDataFrame(vs);
		else if (type != null && type.equals("matrix"))
			success = exportMatrix(vs);
		else if (type != null && type.equals("list"))
			success = exportList(vs);
		else if (type != null && type.equals("numeric"))
			success = exportNumeric(vs);
		else if (type != null && type.equals("integer"))
			success = exportInteger(vs);
		else if (type != null && type.equals("character"))
			success = exportCharacter(vs);
		else if (type != null && type.equals("factor"))
			success = exportFactor(vs);
		return success;
	}

	/**
	 * Export r-numeric.
	 * 
	 * @param vs
	 *            dataset
	 * @return true if successful, false if not
	 */
	private static boolean exportNumeric(SVarSet vs) {
		try {
			if (vs.count() > 1)
				return false;
			if (vs.at(0) instanceof SVarInt) {
				return exportInteger(vs);
			} else {
				JGR.getREngine().assign(TEMP_VARIABLE_NAME, ((SVarDouble) vs.at(0)).cont);
				setVariableName(vs.getName());
				return true;
			}
		} catch (Exception e) {
			new ErrorMsg(e);
			return false;
		}
	}

	/**
	 * Export r-integer.
	 * 
	 * @param vs
	 *            dataset
	 * @return true if successful, false if not
	 */
	private static boolean exportInteger(SVarSet vs) {
		try {
			if (vs.count() > 1)
				return false;
			JGR.getREngine().assign(TEMP_VARIABLE_NAME, ((SVarInt) vs.at(0)).cont);
			setVariableName(vs.getName());
			return true;
		} catch (Exception e) {
			new ErrorMsg(e);
			return false;
		}
	}

	/**
	 * Export r-factor.
	 * 
	 * @param vs
	 *            dataset
	 * @return true if successful, false if not
	 */
	private static boolean exportFactor(SVarSet vs) {
		try {
			if (vs.count() > 1)
				return false;
			int[] ids = new int[((SVarFact) vs.at(0)).cont.length];
			for (int z = 0; z < ids.length; z++)
				ids[z] = ((SVarFact) vs.at(0)).cont[z] + 1;

			REXPFactor factor = new REXPFactor(ids, ((SVarFact) vs.at(0)).cats);
			JGR.getREngine().assign(TEMP_VARIABLE_NAME, factor);
			setVariableName(vs.getName());
			return true;
		} catch (Exception e) {
			new ErrorMsg(e);
			return false;
		}
	}

	/**
	 * Export r-character.
	 * 
	 * @param vs
	 *            dataset
	 * @return true if successful, false if not
	 */
	private static boolean exportCharacter(SVarSet vs) {
		try {
			if (vs.count() > 1)
				return false;
			JGR.getREngine().assign(TEMP_VARIABLE_NAME, ((SVarObj) vs.at(0)).getContent());
			setVariableName(vs.getName());
			return true;
		} catch (Exception e) {
			new ErrorMsg(e);
			return false;
		}
	}
	
	private static boolean setVariableName(String name) throws REngineException, REXPMismatchException {
		JGR.eval(name + " <- "+TEMP_VARIABLE_NAME+"; rm("+TEMP_VARIABLE_NAME+")");
		return true;
	}

	/**
	 * Export r-data.frame.
	 * 
	 * @param vs
	 *            dataset
	 * @return true if successful, false if not
	 */
	private static boolean exportDataFrame(SVarSet vs) {
		try {
			boolean rnames = false;
			String[] rownames = new String[vs.length()];

			SVar rn = null;
			int rnn = 0;

			for (int i = 0; i < vs.count(); i++) {
				if (vs.at(i).getName().equals("row.names")) {
					int length = rownames.length;
					for (int a = 0; a < rownames.length; a++) {
						Object o = ((SVarObj) vs.at(i)).at(a);
						if (o != null)
							rownames[a] = o.toString();
						else {
							rownames[a] = (length - 1) + "";
							length++;
						}
					}
					rnames = true;
					rnn = i;
					rn = vs.at(i);
					vs.remove(i);
					break;
				}
			}

			RList content = new RList();

			for (int i = 0; i < vs.count(); i++) {
				if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarDouble")) {
					REXPDouble rd = new REXPDouble(((SVarDouble) vs.at(i)).cont);
					content.put(vs.at(i).getName(), rd);
				} else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarInt")) {
					REXPInteger ri = new REXPInteger(((SVarInt) vs.at(i)).cont);
					content.put(vs.at(i).getName(), ri);
				} else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarFact")) {
					int[] ids = new int[((SVarFact) vs.at(i)).cont.length];
					String[] cats = ((SVarFact) vs.at(i)).cats;
					boolean NAS = false;
					for (int z = 0; z < ids.length; z++) {
						ids[z] = ((SVarFact) vs.at(i)).cont[z] + 1;
						// Detect missingCats
						if (ids[z] == 0) {
							NAS = true;
							ids[z] = cats.length + 1;
						}
					}
					// Add missing cats
					if (NAS) {
						String[] newcats = new String[cats.length + 1];
						System.arraycopy(cats, 0, newcats, 0, cats.length);
						newcats[newcats.length - 1] = "NA";
						cats = newcats;
						NAS = false;
					}

					REXPFactor rf = new REXPFactor(ids, cats);

					content.put(vs.at(i).getName(), rf);
				} else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarObj")) {
					REXPString rs = new REXPString(((SVarObj) vs.at(i)).getContent());
					content.put(vs.at(i).getName(), rs);
				}
			}

			if (!rnames)
				for (int i = 1; i <= rownames.length; i++)
					rownames[i - 1] = i + "";
			if (rnames)
				vs.insert(rnn, rn);

			REXPString rexpRowNames = new REXPString(rownames);

			JGR.getREngine().assign(TEMP_VARIABLE_NAME, createDataFrame(content, rexpRowNames));
			setVariableName(vs.getName());
			return true;
		} catch (Exception e) {
			new ErrorMsg(e);
			return false;
		}
	}

	public static REXP createDataFrame(RList l, REXP rownames) throws REXPMismatchException {
		if (l == null || l.size() < 1)
			throw new REXPMismatchException(new REXPList(l), "data frame (must have dim>0)");
		if (!(l.at(0) instanceof REXPVector))
			throw new REXPMismatchException(new REXPList(l), "data frame (contents must be vectors)");
		return new REXPGenericVector(l, new REXPList(new RList(new REXP[] { new REXPString("data.frame"), new REXPString(l.keys()), rownames },
				new String[] { "class", "names", "row.names" })));
	}

	/**
	 * Export r-matrix.
	 * 
	 * @param vs
	 *            dataset
	 * @return true if successful, false if not
	 */
	private static boolean exportMatrix(SVarSet vs) {
		try {
			String[] names = new String[vs.count()];
			Object mcont;
			int vlength = vs.at(0).size();
			boolean isInt = false;
			boolean isDouble = false;
			if (vs.at(0).getClass().getName().equals("org.rosuda.ibase.SVarDouble")) {
				mcont = new double[vs.count() * vlength];
				isDouble = true;
			} else if (vs.at(0).getClass().getName().equals("org.rosuda.ibase.SVarInt")) {
				mcont = new int[vs.count() * vlength];
				isInt = true;
			} else if (vs.at(0).getClass().getName().equals("org.rosuda.ibase.SVarObj")) {
				mcont = new String[vs.count() * vlength];
			} else
				return false;

			for (int i = 0; i < vs.count(); i++) {
				names[i] = vs.at(i).getName();
				if (isInt)
					System.arraycopy(((SVarInt) vs.at(i)).cont, 0, mcont, i * vlength, vlength);
				else if (isDouble)
					System.arraycopy(((SVarDouble) vs.at(i)).cont, 0, mcont, i * vlength, vlength);
				else 
					System.arraycopy(((SVarObj) vs.at(i)).getContent(), 0, mcont, i * vlength, vlength);
			}

			REXP content;

			if (isInt)
				content = new REXPInteger((int[]) mcont);
			else if (isDouble)
				content = new REXPDouble((double[]) mcont);
			else
				content = new REXPString((String[]) mcont);
			
			
			JGR.getREngine().assign(TEMP_MATRIX_CONTENT_JGR, content);
			

			String[] rownames = new String[vs.length()];
			for (int i = 1; i <= rownames.length; i++)
				rownames[i - 1] = i + "";

			REXPList dimnames = new REXPList(new RList(new REXP[] { new REXPString(rownames), new REXPString(names) }));

			JGR.getREngine().assign(TEMP_MATRIX_DIM_NAMES_JGR,dimnames);

			JGR.eval(vs.getName() + " <- matrix(tempMatrixContentJGR,dimnames=tempMatrixDimNamesJGR,nrow="+rownames.length+",ncol="+names.length+")");
			JGR.eval("rm("+TEMP_MATRIX_CONTENT_JGR+");rm("+TEMP_MATRIX_DIM_NAMES_JGR+")");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorMsg(e);
			return false;
		}
	}
	
	/**
	 * Export r-list.
	 * 
	 * @param vs
	 *            dataset
	 * @return true if successful, false if not
	 */
	private static boolean exportList(SVarSet vs) {
		try {
			RList content = new RList();
			for (int i = 0; i < vs.count(); i++) {
				if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarDouble")) {
					REXPDouble rd = new REXPDouble(((SVarDouble) vs.at(i)).cont);
					content.put(vs.at(i).getName(), rd);
				} else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarInt")) {
					REXPInteger ri = new REXPInteger(((SVarInt) vs.at(i)).cont);
					content.put(vs.at(i).getName(), ri);
				} else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarFact")) {
					int[] ids = new int[((SVarFact) vs.at(i)).cont.length];
					for (int z = 0; z < ids.length; z++)
						ids[z] = ((SVarFact) vs.at(i)).cont[z] + 1;
					REXPFactor rf = new REXPFactor(ids, ((SVarFact) vs.at(i)).cats);
					content.put(vs.at(i).getName(), rf);
				} else if (vs.at(i).getClass().getName().equals("org.rosuda.ibase.SVarObj")) {
					REXPString rs = new REXPString(((SVarObj) vs.at(i)).getContent());
					content.put(vs.at(i).getName(), rs);
				}
			}

			REXPList rl = new REXPList(content);
			JGR.getREngine().assign(TEMP_VARIABLE_NAME, rl);
			setVariableName(vs.getName());
			return true;
		} catch (Exception e) {
			new ErrorMsg(e);
			return false;
		}
	}

	/**
	 * Compare to string and return the common prefix.
	 * 
	 * @param str1
	 *            String 1
	 * @param str2
	 *            String 2
	 * @return common prefix
	 */
	public static String commonWithPrefix(String str1, String str2) {
		int min = Math.min(str1.length(), str2.length());
		String result = "";
		String s = "";
		for (int i = 0; i < min; i++)
			if ((s = str1.substring(i, i + 1)).equals(str2.substring(i, i + 1)))
				result += s;
			else
				break;
		return result;
	}
}
