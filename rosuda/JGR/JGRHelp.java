package org.rosuda.JGR;

/**
 *  JGRHelp
 * 
 * 	little htmlbrowser combined with r-help search engine
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.rosuda.JGR.toolkit.*;
import org.rosuda.JGR.rhelp.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

public class JGRHelp extends iFrame implements ActionListener, KeyListener,
    MouseListener {

    public static JGRHelp current = null;
    private static WTentry MyEntry = null;

    private String keyWord = null;

    private JPanel topPanel = new JPanel();
    private JTabbedPane tabArea = new JTabbedPane();
    private HelpArea helpArea;

    private JTextField inputKeyWord = new JTextField();
    private JButton search = new JButton("Search");
    public JLabel link = new JLabel(" ");

    private JPanel options = new JPanel();
    private JCheckBox exactMatch = new JCheckBox("Exact Match",true);
    private JCheckBox searchDesc = new JCheckBox("Help Page Titles", true);
    private JCheckBox searchKeyWords = new JCheckBox("Keywords", false);
    private JCheckBox searchAliases = new JCheckBox("Object Names", false);

    public IconButton back;
    public IconButton home;
    public IconButton forward;

    private SearchEngine searchRHelp;

    private boolean INITIALIZED = false;
    
    private TextFinder textFinder = new TextFinder();


    private static String index;
    public static String RHELPLOCATION;

    public JGRHelp() {
        this(null);
    }

    public JGRHelp(String location) {
        super("Help", iFrame.clsHelp);
        MyEntry = this.getMYEntry();
        while(!JGR.STARTED);

        String[] Menu = {"+", "File", "@PPrint", "print", "~File.Basic.End",
            "+", "Edit", "@CCopy", "copy",/*"@FFind","search","@GFind Next","searchnext",*/
            "~Preferences",
            "~Window", "0"};
        iMenu.getMenu(this, this, Menu);

        if (System.getProperty("os.name").startsWith("Windows")) {
            RHELPLOCATION = RController.getRHome();
            index = "file:/"+RHELPLOCATION.replace('\\','/')+"/doc/html/packages.html";
        }
        else {
            RHELPLOCATION = JGR.R.eval("paste(tempdir(), \"/.R\", sep = \"\")").asString();
            index = "file://"+RHELPLOCATION.replace('\\','/')+"/doc/html/packages.html";
        }

        searchRHelp = new SearchEngine();
        searchRHelp.setRHelp(this);

        search.setActionCommand("searchHelp");
        search.addActionListener(this);

        this.getRootPane().setDefaultButton(search);

        FontTracker.current.add(inputKeyWord);
        inputKeyWord.setMinimumSize(new Dimension(330, 25));
        inputKeyWord.setPreferredSize(new Dimension(330, 25));
        inputKeyWord.setEditable(true);

        options.add(exactMatch);
        options.add(searchDesc);
        options.add(searchKeyWords);
        options.add(searchAliases);

        topPanel.setLayout(new GridBagLayout());
        topPanel.add(inputKeyWord, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(5, 0, 1, 5), 0, 0));
        topPanel.add(search, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(5, 1, 1, 5), 0, 0));
        topPanel.add(options, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(1, 0, 1, 5), 0, 0));
        topPanel.add(back = new IconButton("/icons/back.png", "Back", this,
                                           "back"),
                     new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 15, 2, 5), 0, 0));
        topPanel.add(home = new IconButton("/icons/home.png", "Home", this,
                                           "home"),
                     new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 2, 5), 0, 0));
        topPanel.add(forward = new IconButton("/icons/forward.png", "Forward", this,
                                              "forward"),
                     new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                                            GridBagConstraints.WEST,
                                            GridBagConstraints.NONE,
                                            new Insets(5, 5, 2, 5), 0, 0));

        back.setEnabled(false);
        forward.setEnabled(false);

        link.setPreferredSize(new Dimension(200, 20));


        helpArea = new HelpArea(tabArea,this,null);
        helpArea.addKeyListener(this);
        tabArea.addTab(keyWord==null?"packages":keyWord,new CloseIcon(getClass().getResource("/icons/close.png")),helpArea);
        tabArea.addMouseListener(this);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(topPanel,BorderLayout.NORTH);
        this.getContentPane().add(tabArea,BorderLayout.CENTER);
        this.getContentPane().add(link,BorderLayout.SOUTH);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit();
            }
        });
        this.setMinimumSize(new Dimension(570,600));
        this.setSize(new Dimension(600,
                                   Common.screenRes.height < 800 ?
                                   Common.screenRes.height - 50 : 700));
        this.setLocation(Common.screenRes.width - this.getSize().width - 50, 10);
        super.show();
        inputKeyWord.requestFocus();
        current = this;
    }

    public void print() {
        DocumentRenderer docrender = new DocumentRenderer();
        docrender.print(((HelpArea) tabArea.getSelectedComponent()).helpPane);
    }

    public void refresh() {
        helpArea = new HelpArea(tabArea,this,null);
    }


    private void back() {
        ((HelpArea) tabArea.getSelectedComponent()).back();
    }

    private void home() {
        ((HelpArea) tabArea.getSelectedComponent()).goTo(index,true);
    }

    private void forward() {
        ((HelpArea) tabArea.getSelectedComponent()).forward();
    }

    private void exit() {
        current = null;
        finalize();
        dispose();
    }

    public void goTo(String keyword, String file) {
            if (tabArea.getTabCount()==JGRPrefs.maxHelpTabs) tabArea.remove(JGRPrefs.maxHelpTabs-1);
            tabArea.add(new HelpArea(tabArea,this, keyword), 0);
            tabArea.setSelectedIndex(0);
            tabArea.setIconAt(0,new CloseIcon(getClass().getResource("/icons/close.png")));
            tabArea.setTitleAt(0,keyword);
    }

    private void search() {
        String keyword = inputKeyWord.getText().trim();
        search(keyword);
    }

    public void search(String keyword, boolean exact) {
        exactMatch.setSelected(exact);
        if (!exact) {
            searchDesc.setSelected(true);
            searchKeyWords.setSelected(true);
            searchAliases.setSelected(true);
        }
        search(keyword);
    }

    public void search(String keyword) {
        if (keyword != null && !keyword.equals("")) {
            if (tabArea.getTabCount()==JGRPrefs.maxHelpTabs) tabArea.remove(JGRPrefs.maxHelpTabs-1);
            tabArea.add(new HelpArea(tabArea, this, keyword), 0);
            tabArea.setSelectedIndex(0);
            tabArea.setIconAt(0,new CloseIcon(getClass().getResource("/icons/close.png")));
            tabArea.setTitleAt(0,keyword);
        }
    }

    public void popUpMenu(MouseEvent e) {
        JPopupMenu close = new JPopupMenu();
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setActionCommand("tab_close");
        closeItem.addActionListener(this);
        close.add(closeItem);
        close.show((JComponent)e.getSource(),e.getX(),e.getY());
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "back") {
            back();
        } else if (cmd == "copy") {
            ((HelpArea) tabArea.getSelectedComponent()).helpPane.copy();
        } else if (cmd == "exit") {
            exit();
        } else if (cmd == "forward") {
            forward();
        } else if (cmd == "home") {
            try {
                home();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (cmd == "print") print();
        else if (cmd == "selAll") {
            ((HelpArea) tabArea.getSelectedComponent()).helpPane.selectAll();

        }
        else if (cmd=="searchHelp") search();
        else if (cmd == "search") {
            textFinder.setSearchArea(((HelpArea) tabArea.getSelectedComponent()).helpPane);
            textFinder.showFind(false);
        }
        else if (cmd == "searchnext") {
            textFinder.setSearchArea(((HelpArea) tabArea.getSelectedComponent()).helpPane);
            textFinder.showFind(true);
        }
        else if (cmd=="tab_close") tabArea.remove(tabArea.getSelectedIndex());
    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
        if ((ke.isMetaDown() || ke.isControlDown()) && ke.getKeyCode() == KeyEvent.VK_ENTER) {
            String cmd = ( (JTextComponent) ke.getComponent()).getSelectedText().trim();
            JGR.MAINRCONSOLE.execute(cmd);
        }
    }

    public void mouseClicked(MouseEvent e) {
        int tabNumber= tabArea.getUI().tabForCoordinate(tabArea, e.getX(), e.getY());
        if (tabNumber < 0) return;
        Rectangle rect=((CloseIcon)tabArea.getIconAt(tabNumber)).getBounds();
        if (rect.contains(e.getX(), e.getY())) tabArea.remove(tabNumber);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    class HelpArea extends JScrollPane {

        public JEditorPane helpPane = new JEditorPane();


        private JGRHelp rhelp;
        private String keyword;

        private Vector history = new Vector();
        private int currentURLIndex = -1;
        
        private JTabbedPane tabArea = null;
        
        
        public HelpArea(JTabbedPane parent, JGRHelp rhelp, String keyword) {
            this.rhelp = rhelp;
            this.keyword = keyword;
            this.tabArea = parent;
            FontTracker.current.add(helpPane);
            this.getViewport().add(helpPane);
            this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            helpPane.setEditable(false);
            helpPane.setContentType("text/html");
            helpPane.addKeyListener(rhelp);
            helpPane.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                        if (link != null) link.setText(e.getURL().toString());
                    }
                    else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                        if (link != null) link.setText(" ");
                    }
                    else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        goTo(e.getURL(),true);
                    }
                }
            });
            this.addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    setButtons();
                }
            });
            if (keyword == null) goTo(rhelp.index);
            else search();
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
                try { history.remove(currentURLIndex);
                    currentURLIndex--;
                    rhelp.back.setEnabled(currentURLIndex > 0);
                    rhelp.forward.setEnabled(currentURLIndex + 1 < history.size());
                    url = (URL) history.get(currentURLIndex);
                    //System.out.println(url.toString());
                }
                catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                                                  "URL Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
            finally { 
			if (href ) {
					try {
						String title = url.toString().substring(url.toString().lastIndexOf("/")+1);
						title = title.substring(0,title.lastIndexOf('.'));
						int index = tabArea.getSelectedIndex();
						if (index >= 0 && !title.matches("^[0-9][0-9].*") || title.startsWith("file")) tabArea.setTitleAt(index,title);
						else {
							int i = url.toString().indexOf("html");
							title = url.toString().substring(0,i-1);
							title = title.substring(title.lastIndexOf("/")+1);
							if (index >= 0 && !title.matches("^[0-9][0-9].*") || title.startsWith("file")) tabArea.setTitleAt(index,title);
						}
					} catch (Exception ex2) {}
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
            goTo(url,false);
        }
        
        public void goTo(URL url,boolean href) {
            if (url != null) {
                currentURLIndex++;
                history.setSize(currentURLIndex);
                history.add(url);
				updatePage(href);
			}
        }
		
		public void goTo(String url) {
			goTo(url,false);
		}

        public void goTo(String url_l,boolean href) {
            URL url = null;
            try {
                url = new URL(url_l);
                goTo(url,href);
            } catch (MalformedURLException e) {
                new org.rosuda.JGR.util.ErrorMsg(e);
                JOptionPane.showMessageDialog(null, e.getMessage(),
                                              "URL Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        public void search() {
            if (keyword != null && !keyword.equals(""))
                if (rhelp.searchRHelp != null) goTo(rhelp.searchRHelp.search(keyword,rhelp.exactMatch.isSelected(),rhelp.searchDesc.isSelected(),rhelp.searchKeyWords.isSelected(),rhelp.searchAliases.isSelected()));
        }
    }

    class CloseIcon extends ImageIcon {

        private int x,y,width,height;
        private Icon icon;

        public CloseIcon(URL url) {
            super(url);

        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            this.x = x; this.y = y; width = getIconWidth(); height = getIconHeight();
            super.paintIcon(c,g,x,y);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }

    /*public class JComboBoxExt extends JComboBox implements JComboBox.KeySelectionManager {

        public class CBDocument
            extends PlainDocument {

            public void insertString(int offset, String str, AttributeSet a) throws
                BadLocationException {
                if (str == null)
                    return;
                super.insertString(offset, str, a);
                if (!isPopupVisible() && str.length() != 0) {
                    fireActionEvent();
                }
            }
        }

        public JComboBoxExt(ComboBoxModel aModel) {
            super(aModel);
            lap = new java.util.Date().getTime();
            setKeySelectionManager(this);
            final JTextField tf;
            if (getEditor() != null) {
                tf = (JTextField) getEditor().getEditorComponent();
                tf.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                    }

                    public void keyReleased(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER)
                            search();
                    }
                });
                if (tf != null) {
                    tf.setDocument(new CBDocument());
                    addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            JTextField tf = (JTextField) getEditor().
                                getEditorComponent();
                            String text = tf.getText();
                            ComboBoxModel aModel = getModel();
                            String current;
                            for (int i = 0; i < aModel.getSize(); i++) {
                                current = aModel.getElementAt(i).toString();
                                if (current.startsWith(text)) {
                                    tf.setText(current);
                                    tf.setSelectionStart(text.length());
                                    tf.setSelectionEnd(current.length());
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        }

        public int selectionForKey(char aKey, ComboBoxModel aModel) {
            long now = new java.util.Date().getTime();
            if (searchFor != null && aKey == KeyEvent.VK_BACK_SPACE &&
                searchFor.length() > 0) {
                searchFor = searchFor.substring(0, searchFor.length() - 1);
            }
            else {
                if (lap + 1000 < now) {
                    searchFor = "" + aKey;
                }
                else {
                    searchFor = searchFor + aKey;
                }
            }
            lap = now;
            String current;
            for (int i = 0; i < aModel.getSize(); i++) {
                current = aModel.getElementAt(i).toString();
                if (current.startsWith(searchFor))
                    return i;
            }
            return -1;
        }

        public void fireActionEvent() {
            super.fireActionEvent();
        }

        private String searchFor;
        private long lap;
    }*/
}