package org.rosuda.JGR.toolkit;

/**
*  SyntaxInput
 *
 * 	inherits SyntaxArea, is responsible for codecompletion and helpagent
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2004
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.rosuda.JGR.RController;

/**
* @author Markus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SyntaxInput extends SyntaxArea implements KeyListener {
	
    private boolean disableEnter = false;
    //private Vector commands = new Vector();
    private String fun = null;
    private String funHelp = null;
    private Popup funHelpTip = null;
    private Popup cmdHelp = null;
    private JToolTip Tip = new JToolTip();
    public  CodeCompleteMultiple mComplete;
    private Point p;
	
    public SyntaxInput(boolean disableEnter){
        this.disableEnter = disableEnter;
        this.addKeyListener(this);
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (funHelpTip != null) funHelpTip.hide();
                if (mComplete != null && mComplete.isVisible()) {
                    if (cmdHelp != null) cmdHelp.hide();
                    mComplete.setVisible(false);
                }
            }
        });
        this.setDocument(new SyntaxInputDocument());
        mComplete = new CodeCompleteMultiple(this);
    }
	
    private String getLastCommand() {
        String word = null;
        String text = this.getText();
        int pos = this.getCaretPosition();
        int lastb = this.getText(0,pos+1).lastIndexOf('(');
        int lasteb = this.getText(0,pos).lastIndexOf(')');
        if (lasteb > lastb) return null;
        if (lastb < 0) lastb = text.indexOf('(',pos);
        if (lastb < 0) return null;
        if (pos < 0) return null;
        
        int offset = lastb--, end = lastb; pos=lastb;
        if (text==null) return null;
        while (offset > -1 && pos > -1) {
            char c = text.charAt(pos);
            System.out.println(c);
            if (((c>='a')&&(c<='z'))||((c>='A')&&(c<='Z'))||c=='.'||c=='_') offset--;
            else break;
            pos--;
        }
        offset = offset==-1?0:offset;
        end = ++lastb;
        return (offset!=end)?text.substring(offset,end).trim():null;
    }
	
    private String getLastPart() {
        String word = null;
        String text = this.getText();
        int pos = this.getCaretPosition();
        if (pos > 0 && text.length() > 0 && text.charAt(pos-1)=='(') pos--;
        if (pos < 0) return null;
        int offset = pos-1, end = pos; pos--;
        if (text==null) return null;
        int l = text.length();
        while (offset > -1 && pos > -1) {
            char c = text.charAt(pos);
            if (((c>='a')&&(c<='z'))||((c>='A')&&(c<='Z'))||c=='.'||c=='_'||c=='\\'||c=='/' || c=='~') offset--;
            else break;
            pos--;
        }
        offset = offset==-1?0:++offset;
        return (offset!=end)?text.substring(offset,end).trim():null;
    }
	
    public void showCmdCompletions(String[] result) {
        try {
            if (cmdHelp != null) cmdHelp.hide();
        	p = getCaret().getMagicCaretPosition();
        	SwingUtilities.convertPointToScreen(p,this);
        	mComplete.refresh(result);
        	mComplete.setVisible(true);
        	cmdHelp = PopupFactory.getSharedInstance().getPopup(this,mComplete,p.x,p.y+15);
        	cmdHelp.show();
        } catch (Exception e) {}
    }
	
    public void keyTyped(KeyEvent ke) {
    }
	
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (cmdHelp != null) cmdHelp.hide();
            mComplete.setVisible(false);
            if (funHelpTip != null) funHelpTip.hide();
        }
        if (JGRPrefs.useEmacsKeyBindings) {
            if (ke.getKeyCode() == KeyEvent.VK_E && ke.isControlDown()) {
                this.setCaretPosition(this.getText().length());
            }
            if (ke.getKeyCode() == KeyEvent.VK_A && ke.isControlDown()) {
                this.setCaretPosition(0);
                this.select(0,0); //should be safer
            }
        }
    }
	
    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_TAB) {
            String text = null;
            int pos = getCaretPosition();
            if (pos==0) return;
            try {
                int i = getLineStartOffset(getLineOfOffset(pos));
                text = getText(i,pos-i);
            } catch (Exception e) {
            }
            if (text == null) return;
            int tl = text.length(), tp=0, quotes=0, dquotes=0, lastQuote=-1;
            while (tp<tl) {
                char c=text.charAt(tp);
                if (c=='\\') tp++;
                else {
                    if (dquotes==0 && c=='\'') {
                        quotes^=1;
                        if (quotes==0) lastQuote=tp;
                    }
                    if (quotes==0 && c=='"') {
                        dquotes^=1;
                        if (dquotes==0) lastQuote=tp;
                    }
                }
                tp++;
            }
            fun = getLastPart();
            if (fun != null) {
                String[] result = new String[1];
                if ((quotes+dquotes)>0) result[0] = RController.completeFile(fun);
				else result = RController.completeCommand(fun);
                if (result != null && result.length > 1) {
                    if (funHelpTip != null) funHelpTip.hide();
                    if (p == null || !p.equals(getCaret().getMagicCaretPosition()))
                        showCmdCompletions(result);
                    if (JGRPrefs.isMac && cmdHelp != null) cmdHelp.show();
					
                }
                else {
                    if (result != null && result.length > 0 && result[0] != null && !result[0].equals(fun) ) {
                        insertAt(pos,result[0].replaceFirst(fun,""));
                        if (cmdHelp != null) cmdHelp.hide();
                        if (mComplete != null) mComplete.setVisible(false);
                    }
                    else Toolkit.getDefaultToolkit().beep();
                }
            }
        }
        else if (mComplete != null && mComplete.isVisible()) {
            int k = ke.getKeyCode();
            if (k != KeyEvent.VK_ESCAPE && k != KeyEvent.VK_ENTER && k != KeyEvent.VK_DOWN && k != KeyEvent.VK_UP && k != KeyEvent.VK_LEFT && k != KeyEvent.VK_RIGHT && k != KeyEvent.VK_TAB && !ke.isShiftDown() && !ke.isMetaDown() && !ke.isControlDown() && !ke.isAltDown() && !ke.isAltGraphDown()) {
                fun = getLastPart();
                if (fun != null) {
                    String[] result = new String[1];
                    result = RController.completeCommand(fun);
                    if (result != null && result.length > 0){
                        if (funHelpTip != null) {
                            funHelpTip.hide();
                            funHelpTip = null;
                        }
                        showCmdCompletions(result);
                    }
                    else {
                        if (cmdHelp != null) cmdHelp.hide();
                        mComplete.setVisible(false);
                    }
                }
                else {
                    if (cmdHelp != null) cmdHelp.hide();
                    mComplete.setVisible(false);
                }
            }
        }
        else if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            if (cmdHelp != null) cmdHelp.hide();
            mComplete.setVisible(false);
            if (funHelpTip != null) {
                funHelpTip.hide();
                funHelpTip = null;
            }
        }
        if (JGRPrefs.useHelpAgent && !ke.isShiftDown()) {
            if (funHelpTip != null) {
                funHelpTip.hide();
                funHelpTip = null;
            }
            showFunHelp(getLastCommand());
        }
    }
	
    private void showFunHelp(String fun) {
        try {
            funHelp = RController.getFunHelpTip(fun);
            if (fun != null && funHelp != null) {
                Tip = new JToolTip();
                Tip.setTipText(funHelp);
                Tip.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (funHelpTip != null) {
                            funHelpTip.hide();
                            funHelpTip = null;
                        }
                    }
                });
                p = getCaret().getMagicCaretPosition();
                SwingUtilities.convertPointToScreen(p,this);
                funHelpTip = PopupFactory.getSharedInstance().getPopup(this,Tip,p.x,p.y+20);
                funHelpTip.show();
                //commands.add(funHelp);
                //commands.add(p);
            }
        } catch (Exception e) {}
    }
	
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,int condition, boolean pressed) {
        if (disableEnter && e.getKeyCode() == KeyEvent.VK_ENTER) return true;
		if (mComplete.isVisible() && (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN)) return true;
        InputMap map = getInputMap(condition);
        ActionMap am = getActionMap();
		
        if(map != null && am != null && isEnabled()) {
            Object binding = map.get(ks);
            Action action = (binding == null) ? null : am.get(binding);
            if (action != null) {
                return SwingUtilities.notifyAction(action, ks, e, this, e.getModifiers());
            }
        }
        return false;
    }
	
    class SyntaxInputDocument extends SyntaxDocument {
    }
	
    public class CodeCompleteMultiple extends Panel {
		
        public java.awt.List cmds = new java.awt.List();
        private SyntaxInput parent = null;
		
        public CodeCompleteMultiple(SyntaxInput tcomp) {
            parent = tcomp;
            this.setLayout(new GridLayout(1,1));
            this.add(cmds);
            cmds.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER)
                        completeCommand();
                }
            });
            cmds.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2)
                        completeCommand();
                }
            });
            cmds.setSize(new Dimension(180,100));
            this.setSize(cmds.getWidth()+10,cmds.getHeight());
            this.setVisible(false);
        }
		
        public void refresh(String[] commands) {
            cmds.removeAll();
            for (int i = 0; i < commands.length; i++)
                cmds.add(commands[i]);
            cmds.select(0);
        }
		
        public void completeCommand() {
            parent.insertAt(parent.getCaretPosition(),cmds.getSelectedItem().replaceFirst(fun,""));
            this.setVisible(false);
            if (cmdHelp != null) cmdHelp.hide();
        }
		
        public void selectPrevios() {
            int i = cmds.getSelectedIndex();
            if (--i >= 0)
                cmds.select(i);
        }
		
        public void selectNext() {
            int i = cmds.getSelectedIndex();
            if (++i < cmds.getItemCount())
                cmds.select(i);
        }
		
        public void setVisible(boolean b) {
            if (!b && cmdHelp != null) cmdHelp.hide();
            super.setVisible(b);
        }
    }
}

