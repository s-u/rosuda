
import java.awt.*;

/** Implementation of a simple progress dialog */
public class ProgressDlg extends Frame {
    int progress=0;
    String ptxt="Processing...";
    
    public ProgressDlg(Frame f, String tt) {
        super(tt);
        Dimension sr=Toolkit.getDefaultToolkit().getScreenSize();
        setSize(200,100); setLocation(sr.width/2-100,sr.height/2-50);
        setResizable(false);
    };

    public void paint(Graphics g) {
        g.setColor(Color.black);
        g.drawString(ptxt,20,40);
        g.drawRect(20,50,160,18);
        g.setColor(new Color(32,32,255));
        g.fillRect(20,50,160*progress/100,18);
        g.setColor(new Color(0,0,0));
        g.drawString(""+progress+"%",81,63);
        g.setColor(new Color(255,255,192));
        g.drawString(""+progress+"%",80,62);
    };

    public void setProgress(int p) {
        progress=p;
        repaint();
    };

    public void setText(String t) {
        ptxt=t;
        repaint();
    };
}
