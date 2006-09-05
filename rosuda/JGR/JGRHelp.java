package org.rosuda.JGR;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.rosuda.JGR.editor.FindReplaceDialog;
import org.rosuda.JGR.rhelp.SearchEngine;
import org.rosuda.JGR.toolkit.FontTracker;
import org.rosuda.JGR.toolkit.IconButton;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.toolkit.TextFinder;
import org.rosuda.JGR.toolkit.iFrame;
import org.rosuda.JGR.toolkit.iMenu;
import org.rosuda.JGR.util.DocumentRenderer;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.WTentry;

/**
 * JGRHelp - an implemenation of a simple htmlbrowser combined with r-help
 * search engine.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2005
 */

public class JGRHelp extends iFrame implements ActionListener, KeyListener,
		MouseListener {

	/** Current JGRHelp, because we only want to open one helpbrowser. */
	public static JGRHelp current = null;

	private static WTentry MyEntry = null;

	private final String keyWord = null;

	private final JPanel topPanel = new JPanel();

	private final JTabbedPane tabArea = new JTabbedPane();

	private HelpArea helpArea;

	private final JTextField inputKeyWord = new JTextField();

	private final JButton search = new JButton("Search");

	/** Current link located with the mouse cursor */
	public JLabel link = new JLabel(" ");

	private final JPanel options = new JPanel(new FlowLayout(FlowLayout.CENTER));

	private final JCheckBox exactMatch = new JCheckBox("Exact Match", true);

	private final JCheckBox searchDesc = new JCheckBox("Help Page Titles", true);

	private final JCheckBox searchKeyWords = new JCheckBox("Keywords", false);

	private final JCheckBox searchAliases = new JCheckBox("Object Names", false);

	private IconButton back;

	private IconButton home;

	private IconButton forward;

	private SearchEngine searchRHelp;

	private final TextFinder textFinder = new TextFinder();

	private static String index;

	/** Path to html help of R */
	public static String RHELPLOCATION;

	public JGRHelp() {
		this(null);
	}

	/**
	 * Create a new JGRHelp - browser with specified help-location.
	 * 
	 * @param location
	 *            path pointing to html-help
	 */
	public JGRHelp(String location) {
		super("Help", iFrame.clsHelp);
		MyEntry = this.getMYEntry();
		while (!JGR.STARTED)
			;

		String[] Menu = { "+", "File", "@PPrint", "print", "+", "Edit",
				"@CCopy", "copy", "-", "@RRun selection", "runselection",
				"@FFind", "search", "@GFind Next", "searchnext",
				"~Preferences", "~Window", "0" };
		iMenu.getMenu(this, this, Menu);

		if (System.getProperty("os.name").startsWith("Windows")) {
			RHELPLOCATION = RController.getRHome();
			index = "file:/" + RHELPLOCATION.replace('\\', '/')
					+ "/doc/html/packages.html";
		} else {
			RHELPLOCATION = JGR.R.eval("tempdir()").asString() + "/.R";
			index = "file://" + RHELPLOCATION.replace('\\', '/')
					+ "/doc/html/packages.html";
		}

		searchRHelp = new SearchEngine();
		searchRHelp.setRHelp(this);

		search.setActionCommand("searchHelp");
		search.addActionListener(this);

		this.getRootPane().setDefaultButton(search);

		FontTracker.current.add(inputKeyWord);
		inputKeyWord.setMinimumSize(new Dimension(150, 25));
		inputKeyWord.setPreferredSize(new Dimension(300, 25));
		inputKeyWord.setMaximumSize(new Dimension(350, 25));
		inputKeyWord.setEditable(true);

		options.add(exactMatch);
		options.add(searchDesc);
		options.add(searchKeyWords);
		options.add(searchAliases);

		JPanel top1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		top1.add(inputKeyWord);
		top1.add(search);
		top1.add(new Spacer(30));
		top1
				.add(back = new IconButton("/icons/back.png", "Back", this,
						"back"));
		top1
				.add(home = new IconButton("/icons/home.png", "Home", this,
						"home"));
		top1.add(forward = new IconButton("/icons/forward.png", "Forward",
				this, "forward"));

		topPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.gridx = 0;
		gbc.gridy = 0;
		topPanel.add(top1, gbc);
		gbc.gridy = 1;
		topPanel.add(options, gbc);

		back.setEnabled(false);
		forward.setEnabled(false);

		link.setPreferredSize(new Dimension(200, 20));

		helpArea = new HelpArea(tabArea, this, null);
		helpArea.addKeyListener(this);
		tabArea.addTab(keyWord == null ? "packages" : keyWord, new CloseIcon(
				getClass().getResource("/icons/close.png")), helpArea);
		tabArea.addMouseListener(this);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(topPanel, BorderLayout.NORTH);
		this.getContentPane().add(tabArea, BorderLayout.CENTER);
		this.getContentPane().add(link, BorderLayout.SOUTH);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exit();
			}
		});
		this.setMinimumSize(new Dimension(570, 600));
		this.setSize(new Dimension(600,
				Common.screenRes.height < 800 ? Common.screenRes.height - 50
						: 700));
		this
				.setLocation(
						Common.screenRes.width - this.getSize().width - 50, 10);
		super.show();
		inputKeyWord.requestFocus();
		current = this;
	}

	/**
	 * Print current help-page.
	 */
	public void print() {
		DocumentRenderer docrender = new DocumentRenderer();
		docrender.print(((HelpArea) tabArea.getSelectedComponent()).helpPane);
	}

	private void back() {
		((HelpArea) tabArea.getSelectedComponent()).back();
	}

	private void home() {
		((HelpArea) tabArea.getSelectedComponent()).goTo(index, true);
	}

	private void forward() {
		((HelpArea) tabArea.getSelectedComponent()).forward();
	}

	private void exit() {
		current = null;
		finalize();
		dispose();
	}

	/**
	 * Search for the keyword written in the search-field.
	 */
	public void search() {
		String keyword = inputKeyWord.getText().trim();
		search(keyword);
	}

	/**
	 * Search for the keyword, you can choosed it it should match exactly or not
	 * 
	 * @param keyword
	 *            keyword
	 * @param exact
	 *            match exactly or not
	 */
	public void search(String keyword, boolean exact) {
		exactMatch.setSelected(exact);
		if (!exact) {
			searchDesc.setSelected(true);
			searchKeyWords.setSelected(true);
			searchAliases.setSelected(true);
		}
		search(keyword);
	}

	/**
	 * Search for the keyword.
	 * 
	 * @param keyword
	 *            keyword
	 */
	public void search(String keyword) {
		if (keyword != null && !keyword.equals("")) {
			try {
				if (((HelpArea) tabArea.getComponentAt(0)).helpPane.getText()
						.indexOf("No matches for") >= 0)
					tabArea.remove(0);
			} catch (Exception e) {
			}
			if (tabArea.getTabCount() == JGRPrefs.maxHelpTabs)
				tabArea.remove(JGRPrefs.maxHelpTabs - 1);
			tabArea.add(new HelpArea(tabArea, this, keyword), 0);
			tabArea.setSelectedIndex(0);
			tabArea.setIconAt(0, new CloseIcon(getClass().getResource(
					"/icons/close.png")));
			tabArea.setTitleAt(0, keyword);
		}
	}

	/**
	 * actionPerformed: handle action event: menus.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "back")
			back();
		else if (cmd == "copy")
			((HelpArea) tabArea.getSelectedComponent()).helpPane.copy();
		else if (cmd == "exit")
			exit();
		else if (cmd == "forward")
			forward();
		else if (cmd == "home")
			try {
				home();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		else if (cmd == "print")
			print();
		else if (cmd == "runselection")
			try {
				String s = ((HelpArea) tabArea.getSelectedComponent()).helpPane
						.getSelectedText().trim();
				if (s.length() > 0)
					JGR.MAINRCONSOLE.execute(s.trim(), true);
			} catch (Exception ex) {
			}
		else if (cmd == "selAll")
			((HelpArea) tabArea.getSelectedComponent()).helpPane.selectAll();
		else if (cmd == "searchHelp")
			search();
		else if (cmd == "search") {
			FindReplaceDialog.findExt(this,((HelpArea) tabArea.getSelectedComponent()).helpPane);
			//textFinder
					//.setSearchArea(((HelpArea) tabArea.getSelectedComponent()).helpPane);
			//textFinder.showFind(false);
		} else if (cmd == "searchnext") {
			FindReplaceDialog.findNextExt(this,((HelpArea) tabArea.getSelectedComponent()).helpPane);
			//textFinder
					//.setSearchArea(((HelpArea) tabArea.getSelectedComponent()).helpPane);
			//textFinder.showFind(true);
		} else if (cmd == "tab_close")
			tabArea.remove(tabArea.getSelectedIndex());
	}

	/**
	 * keyTyped: handle key event.
	 */
	public void keyTyped(KeyEvent ke) {
		if (System.getProperty("os.name").indexOf("Mac") == -1)
			this.getRootPane().setDefaultButton(search);
	}

	/**
	 * keyPressed: handle key event.
	 */
	public void keyPressed(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER && ke.isControlDown()
				&& System.getProperty("os.name").indexOf("Mac") == -1)
			try {
				if (((JTextComponent) ke.getComponent()).getSelectedText()
						.trim().length() > 0)
					this.getRootPane().setDefaultButton(null);
			} catch (Exception e) {
			}
	}

	/**
	 * keyReleased: handle key event: transfer selected commands to console.
	 */
	public void keyReleased(KeyEvent ke) {
		if ((ke.isMetaDown() || ke.isControlDown())
				&& ke.getKeyCode() == KeyEvent.VK_ENTER) {
			String cmd = ((JTextComponent) ke.getComponent()).getSelectedText()
					.trim();
			if (cmd.length() > 0)
				JGR.MAINRCONSOLE.execute(cmd, true);
		}
	}

	/**
	 * mouseClicked: handle mouse event: close tab.
	 */
	public void mouseClicked(MouseEvent e) {
		int tabNumber = tabArea.getUI().tabForCoordinate(tabArea, e.getX(),
				e.getY());
		if (tabNumber < 0)
			return;
		Rectangle rect = ((CloseIcon) tabArea.getIconAt(tabNumber)).getBounds();
		if (rect.contains(e.getX(), e.getY()))
			tabArea.remove(tabNumber);
	}

	/**
	 * mouseEntered: handle mouse event.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * mousePressed: handle mouse event.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * mouseReleased: handle mouse event.
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * mouseExited: handle mouse event.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * HelpArea, inner tab of the browser.
	 * 
	 * navigating and showing selected pages is implemented here.
	 */
	public class HelpArea extends JScrollPane {

		public JEditorPane helpPane = new JEditorPane();

		private JGRHelp rhelp;

		private String keyword;

		private final Vector history = new Vector();

		private int currentURLIndex = -1;

		private JTabbedPane tabArea = null;

		public HelpArea(JTabbedPane parent, JGRHelp rhelp, String keyword) {
			this.rhelp = rhelp;
			this.keyword = keyword;
			tabArea = parent;
			FontTracker.current.add(helpPane);
			this.getViewport().add(helpPane);
			this
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			helpPane.setEditable(false);
			helpPane.setContentType("text/html");
			helpPane.addKeyListener(rhelp);
			helpPane.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
						if (link != null)
							link.setText(e.getURL().toString());
					} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
						if (link != null)
							link.setText(" ");
					} else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
						goTo(e.getURL(), true);
				}
			});
			this.addComponentListener(new ComponentAdapter() {
				public void componentShown(ComponentEvent e) {
					setButtons();
				}
			});
			if (keyword == null)
				goTo(JGRHelp.index);
			else
				search();
		}

		private void setButtons() {
			back.setEnabled(currentURLIndex > 0);
			forward.setEnabled(currentURLIndex + 1 < history.size());
		}

		private void updatePage(boolean href) {
			rhelp.cursorWait();
			rhelp.back.setEnabled(currentURLIndex > 0);
			rhelp.forward.setEnabled(currentURLIndex + 1 < history.size());
			URL url = (URL) history.get(currentURLIndex);
			try {
				helpPane.setPage(url);
			} catch (IOException ex) {
				ex.printStackTrace();
				try {
					history.remove(currentURLIndex);
					currentURLIndex--;
					rhelp.back.setEnabled(currentURLIndex > 0);
					rhelp.forward.setEnabled(currentURLIndex + 1 < history
							.size());
					url = (URL) history.get(currentURLIndex);
					// System.out.println(url.toString());
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, ex.getMessage(),
							"URL Error", JOptionPane.ERROR_MESSAGE);
				}
			} finally {
				if (href)
					try {
						String title = url.toString().substring(
								url.toString().lastIndexOf("/") + 1);
						title = title.substring(0, title.lastIndexOf('.'));
						int index = tabArea.getSelectedIndex();
						if (index >= 0 && !title.matches("^[0-9][0-9].*")
								|| title.startsWith("file"))
							tabArea.setTitleAt(index, title);
						else {
							int i = url.toString().indexOf("html");
							title = url.toString().substring(0, i - 1);
							title = title.substring(title.lastIndexOf("/") + 1);
							if (index >= 0 && !title.matches("^[0-9][0-9].*")
									|| title.startsWith("file"))
								tabArea.setTitleAt(index, title);
						}
					} catch (Exception ex2) {
					}

				rhelp.cursorDefault();
			}
		}

		private void back() {
			currentURLIndex--;
			updatePage(true);
		}

		private void forward() {
			currentURLIndex++;
			updatePage(true);
		}

		public void goTo(URL url) {
			goTo(url, false);
		}

		public void goTo(URL url, boolean href) {
			if (url != null) {
				currentURLIndex++;
				history.setSize(currentURLIndex);
				history.add(url);
				updatePage(href);
			}
		}

		public void goTo(String url) {
			goTo(url, false);
		}

		public void goTo(String url_l, boolean href) {
			URL url = null;
			try {
				url = new URL(url_l);
				goTo(url, href);
			} catch (MalformedURLException e) {
				new org.rosuda.JGR.util.ErrorMsg(e);
				JOptionPane.showMessageDialog(null, e.getMessage(),
						"URL Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		public void search() {
			if (keyword != null && !keyword.equals(""))
				if (rhelp.searchRHelp != null)
					goTo(rhelp.searchRHelp.search(keyword, rhelp.exactMatch
							.isSelected(), rhelp.searchDesc.isSelected(),
							rhelp.searchKeyWords.isSelected(),
							rhelp.searchAliases.isSelected()));
		}
	}

	/*
	 * Icon for closing a tab in {@see JTabbedPane}
	 */
	class CloseIcon extends ImageIcon {

		private int x, y, width, height;

		public CloseIcon(URL url) {
			super(url);

		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			this.x = x;
			this.y = y;
			width = getIconWidth();
			height = getIconHeight();
			super.paintIcon(c, g, x, y);
		}

		public Rectangle getBounds() {
			return new Rectangle(x, y, width, height);
		}
	}

	class Spacer extends JPanel {
		public Spacer(int width) {
			this.setMinimumSize(new Dimension(width, 0));
			this.setMaximumSize(new Dimension(width, 0));
			this.setPreferredSize(new Dimension(width, 0));
		}
	}
}