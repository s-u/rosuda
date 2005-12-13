/*
 * DataFileOpenDialog.java
 *
 * Created on 18. August 2005, 16:10
 *
 */

package org.rosuda.JClaR;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.rosuda.JRclient.RSrvException;

/**
 * Implementation of a file-dialog which allows loading datasets into R by choosing several options.
 * Originally called JGRDataFileOpenDialog.
 * @author Markus Helbig
 *
 * 	RoSuDa 2003 - 2005
 * @author Tobias Wichtrey
 */
public final class DataFileOpenDialog extends JFileChooser implements ActionListener, ItemListener, PropertyChangeListener {
    
    
    //private JTextField dataName = new JTextField();
    private JTextField otherSeps = new JTextField();
    private JCheckBox header = new JCheckBox("Header",true);
    //private JCheckBox attach = new JCheckBox("Attach",false);
    
    private JComboBoxExt sepsBox = new JComboBoxExt(new String[] {"\\t","\\w",",",";","|","Others..."});
    private String[] seps = new String[] {"\\t","",",",";","|"};
    
    private JComboBoxExt quoteBox = new JComboBoxExt(new String[] {"None","\\\"","\\'","Others..."});
    private String[] quotes = new String[] {"","\\\"","\\'"};
    
    private String rname="d";
    private int status=STATUS_PENDING;
    
    private static final int STATUS_PENDING = 0;
    static final int STATUS_SUCCESS = 1;
    static final int STATUS_ERROR = 2;
    static final int STATUS_CANCELED = 3;
    
    /**
     * Create a new DataFileOpenDialog
     * @param f parent frame
     * @param directory current directory
     */
    DataFileOpenDialog(final Frame f,final String directory, final String rname) {
        
        this.rname=rname;
        
        /*dataName.setMinimumSize(new Dimension(180,22));
        dataName.setPreferredSize(new Dimension(180,22));
        dataName.setMaximumSize(new Dimension(180,22));*/
        
        quoteBox.setMinimumSize(new Dimension(90,22));
        quoteBox.setPreferredSize(new Dimension(90,22));
        quoteBox.setMaximumSize(new Dimension(90,22));
        
        sepsBox.setMinimumSize(new Dimension(90,22));
        sepsBox.setPreferredSize(new Dimension(90,22));
        sepsBox.setMaximumSize(new Dimension(90,22));
        
        quoteBox.addItemListener(this);
        sepsBox.addItemListener(this);
        
        this.addActionListener(this);
        this.addPropertyChangeListener(this);
        if (directory != null && new File(directory).exists())  {
            this.setCurrentDirectory(new File(directory));
        }
        
        
        
        
        final JPanel options = new JPanel();
        /*BoxLayout box = new BoxLayout(options,BoxLayout.Y_AXIS);
        JPanel command = new JPanel(new FlowLayout(FlowLayout.LEFT));
        command.add(new JLabel(" read.table(...) -> "));
        command.add(dataName);*/
        
        final JPanel command2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        command2.add(header);
        command2.add(new JLabel(", sep="));
        command2.add(sepsBox);
        command2.add(new JLabel(", quote="));
        command2.add(quoteBox);
        
/*        JPanel att = new JPanel(new FlowLayout(FlowLayout.LEFT));
        att.add(attach);*/
        
        
        //options.add(command);
        options.add(command2);
        //options.add(att);
        
        /*if (System.getProperty("os.name").startsWith("Window")) {
            final JPanel fileview = (JPanel)((JComponent)((JComponent)this.getComponent(2)).getComponent(2)).getComponent(2);
            //fileview.add(command);
            fileview.add(command2);
            //fileview.add(att);
            final JPanel pp = (JPanel) ((JComponent)((JComponent)this.getComponent(2)).getComponent(2)).getComponent(0);
            pp.add(new JPanel());
            this.setPreferredSize(new Dimension(660,450));
        } else*/ {
            final JPanel filename = (JPanel) this.getComponent(this.getComponentCount()-1);
            final JPanel buttons = (JPanel) filename.getComponent(filename.getComponentCount()-1);
            this.setControlButtonsAreShown(false);
            //filename.add(command);
            filename.add(command2);
            //filename.add(att);
            filename.add(buttons);
            this.setPreferredSize(new Dimension(550,450));
        }
        this.showOpenDialog(f);
    }
    
    /**
     * Open selected datafile, with specified options, R-command: read.table(...)
     */
    private void loadFile() {
        final File selFile = this.getSelectedFile();
        if (selFile != null && selFile.canRead()) {
            //JGR.directory = this.getCurrentDirectory().getAbsolutePath()+File.separator;
            final String file = selFile.toString();
            
            final String useSep;
            if (sepsBox.getSelectedIndex() >= seps.length)  {
                useSep = sepsBox.getSelectedItem().toString();
            }
            
            else  {
                useSep = seps[sepsBox.getSelectedIndex()];
            }
            
            final String useQuote;
            if (quoteBox.getSelectedIndex() >= quotes.length)  {
                useQuote = quoteBox.getSelectedItem().toString();
            }
            
            else  {
                useQuote = quotes[quoteBox.getSelectedIndex()];
            }
            
            
            final String cmd = rname + " <- read.table(\""+file.replace('\\','/')+"\",header="+(header.isSelected()?"T":"F")+",sep=\""+useSep+"\", quote=\""+useQuote+"\")"; //+(attach.isSelected()?";attach("+rname+")":"")+"";
            status = STATUS_SUCCESS;
            try{
                RserveConnection.getRconnection().voidEval(cmd);
            } catch (RSrvException rse){
                // no error message!
                status=STATUS_ERROR;
            }
        } else {
            status = STATUS_ERROR;
        }
        Main.setLast_directory(selFile.getParent());
    }
    
    /**
     * actionPerformed: handle action event: menus.
     */
    public void actionPerformed(final ActionEvent e) {
        final String cmd = e.getActionCommand();
        if ("ApproveSelection".equals(cmd))  {
            loadFile();
        }
        
        else if ("CancelSelection".equals(cmd))  {
            status = STATUS_CANCELED;
        }
        
    }
    
    /**
     * itemStateChanged: handle itemStateChanged event, et separator and quote box enabled if "Others..." is choosen.
     */
    public void itemStateChanged(final ItemEvent e) {
        final Object source = e.getItemSelectable();
        boolean edit = false;
        if (source == quoteBox) {
            edit = quoteBox.getSelectedIndex() == quoteBox.getItemCount()-1?true:false;
            quoteBox.setEditable(edit);
        } else if (source == sepsBox) {
            edit = sepsBox.getSelectedIndex() == sepsBox.getItemCount()-1?true:false;
            sepsBox.setEditable(edit);
        }
    }
    
    private void checkFile(final File file) {
        if(file!=null  && file.canRead()){
            try {
                final BufferedReader reader = new BufferedReader(new FileReader(file));
                String line1 = null;
                String line2 = null;
                if (reader.ready())  {
                    line1 = reader.readLine();
                }
                
                if (reader.ready())  {
                    line2 = reader.readLine();
                }
                
                reader.close();
                if (line2 != null) {
                    int i = line2.indexOf("\"");
                    if (i > -1 && line2.indexOf("\"",i+1) > -1)  {
                        quoteBox.setSelectedItem("\\\"");
                    }
                    
                    else {
                        i = line2.indexOf("\'");
                        if (i > -1 && line2.indexOf("\'",i+1) > -1)  {
                            quoteBox.setSelectedItem("\\\'");
                        }
                        
                        else  {
                            quoteBox.setSelectedItem("None");
                        }
                        
                    }
                    sepsBox.setSelectedItem("\\w"); //fallback
                    i = line2.indexOf("\t");
                    if (i > -1 && line2.indexOf("\t",i+1) > -1)  {
                        sepsBox.setSelectedItem("\\t");
                    }
                    
                    i = line2.indexOf(";");
                    if (i > -1 && line2.indexOf(";",i+1) > -1)  {
                        sepsBox.setSelectedItem(";");
                    }
                    
                    i = line2.indexOf(",");
                    if (i > -1 && line2.indexOf(",",i+1) > -1)  {
                        sepsBox.setSelectedItem(",");
                    }
                    
                    i = line2.indexOf("|");
                    if (i > -1 && line2.indexOf("|",i+1) > -1)  {
                        sepsBox.setSelectedItem("|");
                    }
                    
                }
                if (line1 != null && line2 != null) {
                    String sep = seps[sepsBox.getSelectedIndex()];
                    sep = "\\t".equals(sep)?"\t":sep;
                    int z1 = 0, z2 = 0;
                    if (sep.length() == 0) {
                        z1 = new StringTokenizer(line1).countTokens();
                        z2 = new StringTokenizer(line2).countTokens();
                    } else {
                        int i = -1;
                        while ((i = line1.trim().indexOf(sep,i+1)) > -1)  {
                            z1++;
                        }
                        
                        i = -1;
                        while ((i = line2.trim().indexOf(sep,i+1)) > -1)  {
                            z2++;
                        }
                        
                    }
                    if (z1+1==z2 || (z1==z2 && line1.matches("^[a-zA-Z\"].*")))  {
                        header.setSelected(true);
                    }
                    
                    else  {
                        header.setSelected(false);
                    }
                    
                }
                
            } catch (Exception e) { e.printStackTrace();}
        }
    }
    
    /**
     * propertyChange: handle propertyChange, used for setting the name where the file should be assigned to.
     */
    public void propertyChange(final PropertyChangeEvent e) {
        final File file = this.getSelectedFile();
        if(file!=null && !file.isDirectory()) {
/*            String name = file.getName().replaceAll("\\..*", "");
            name = name.replaceAll("^[0-9]+|[^a-zA-Z|^0-9|^_]",".");*/
            checkFile(file);
        }
    }
    
    int getStatus() {
        return this.status;
    }
}
