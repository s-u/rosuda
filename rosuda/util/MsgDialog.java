import java.awt.*;
import java.awt.event.*;

/** Implements a simple modal message dialog with an OK button.
    @version $Id$
*/
public class MsgDialog extends Dialog implements ActionListener
{
    /** Creates and displays the dialog box
	@param par parent frame (or <code>null</code> if none)
 	@param Cap caption title of the dialog box
	@param Msg message to be displayed */
    public MsgDialog(Frame par, String Cap, String Msg)
    {
	super(par,true);
	setLayout(new BorderLayout());
	setTitle(Cap);
	add(new Label(Msg));
	Panel p=new Panel();	
	p.setLayout(new FlowLayout());
	Button b;
	p.add(b=new Button("OK"));
	add(p,"South");
	add(new SpacingPanel(),"West"); 
	add(new SpacingPanel(),"East");
        add(new SpacingPanel(50,10),"North");
	b.addActionListener(this);
	pack();
	setResizable(false);
	//b.setSize(new Dimension(60,20));
	show();
    };

    public void actionPerformed(ActionEvent e)
    {
        dispose();
    };
};
