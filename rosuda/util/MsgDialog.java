import java.awt.*;
import java.awt.event.*;

/** Implements a simple modal message dialog with an OK button.
    @version $Id$
*/
public class MsgDialog extends Dialog implements ActionListener
{
    public String lastResult;
    public static final String[] okCancel = {"OK","Cancel"};
    public static final String[] yesNoCancel = {"Yes","No","Cancel"};
    public static final String[] yesNo = {"Yes","No"};
    
    /** Creates and displays the dialog box
	@param par parent frame (or <code>null</code> if none)
 	@param Cap caption title of the dialog box
	@param Msg message to be displayed */
    public MsgDialog(Frame par, String Cap, String Msg) {
        this(par,Cap,Msg,null);
    }

    public MsgDialog(Frame par, String Cap, String Msg, String[] buts)
    {
	super(par,true);
	setLayout(new BorderLayout());
	setTitle(Cap);
	add(new Label(Msg));
	Panel p=new Panel();	
	p.setLayout(new FlowLayout());
	Button b;
        if (buts==null || buts.length==0) {
            p.add(b=new Button("OK"));
            b.addActionListener(this);
        } else {
            int i=0;
            while (i<buts.length) {
                p.add(b=new Button(buts[i++]));
                b.addActionListener(this);
            }
        }
	add(p,"South");
	add(new SpacingPanel(),"West"); 
	add(new SpacingPanel(),"East");
        add(new SpacingPanel(50,10),"North");
	pack();
	setResizable(false);
	//b.setSize(new Dimension(60,20));
	show();
    };

    public void actionPerformed(ActionEvent e)
    {
        lastResult=e.getActionCommand();
        dispose();
    };
};
