import java.awt.*;
import java.awt.event.*;

/** Simple help frame that displays list of available shortcuts and their descriptions.
    @version $Id$
*/
public class HelpFrame extends TFrame implements ActionListener {
    TextArea t;
    
    public HelpFrame() {
	super("Help");
	setLayout(new BorderLayout());
	t=new TextArea();
	t.setText("Shortcuts for commands in tree window:\n\nTool modes:\n"+
		 "<e>        - sElect cases\n"+
		 "<z>        - Zoom\n"+
		 "<v>        - moVe (or hold <space> key for temporary pan mode)\n"+
		 "<n>        - Node select\n\nOther commands:\n"+
		 "<c>        - toggle type of Connecting lines\n"+
		 "<d>        - toggle Deviance display\n"+
                 "<shift><d> - show deviance plot\n"+
		 "<f>        - toggle Final node alignment\n"+
		 "<h>        - Help\n"+
		 "<l>        - toggle Labels\n"+
		 "<m>        - tree Map\n"+
		 "<shift><n> - new tree\n"+
		 "<o>        - Open file\n"+
		 "<p>        - Prune\n"+
		 "<shift><p> - Print\n"+
		 "<q>        - Quit\n"+
		 "<r>        - Re-arrange nodes\n"+
		 "<shift><r> - Rotate\n"+
		 "<s>        - toggle node Size (fixed/porportional)\n"+
                 "<shift><x> - export to PoGraSS meta file\n"+
		 "<+>/<->    - change deviance zoom\n\n"+
           "In most plots:\n"+
           "<shift><p> - save as EPS\n"+
           "<shift><x> - export as PoGraSS meta file\n"+
           "<shift><c> - export selected cases\n\n"+
           "Treemap:\n"+
           "<a>        - toggle between treemap and spineplot of leaves\n"+
           "<shift><r> - rotate\n\n"+
           "Deviance plot:\n"+
           "<c>        - switch between cumulative(=absolute deviance) and gain display\n"+
           "<l>        - switch between bars and lines\n\n"+
           "Scatterplot:\n"+
           "<e>        - toggle enhanced highlighting\n"+
           "<l>        - toggle labels\n"+
           "<shift><r> - rotate, i.e. swap axes\n\n"+
           "Barchart:\n"+
           "<s>        - switch between spineplot and barchart mode\n"
           );
		 
	add(t);
        t.setEditable(false); t.setFont(new Font("Monospaced",Font.PLAIN,10));
	t.setBackground(Color.white);
	t.setSize(400,300);
	Panel p=new Panel();	
	add(p,BorderLayout.SOUTH);
	p.setLayout(new FlowLayout());
	Button b=new Button("Close");
	p.add(b);
	b.addActionListener(this);	
	addWindowListener(Common.defaultWindowListener);
	pack();
    };

    public void actionPerformed(ActionEvent e) {	
	dispose();
	removeAll();
	WinTracker.current.rm(this);
    };
};
