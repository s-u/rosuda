import java.awt.*;

/** tiny class implementing a spacer */
public class SpacingPanel extends Panel {
    int spacex, spacey;
    SpacingPanel() { spacex=spacey=15; };
    SpacingPanel(int sz) { spacex=spacey=sz; };
    SpacingPanel(int x, int y) { spacex=x; spacey=y; };
    public Dimension getMinimumSize() { return new Dimension(spacex,spacey); }
    public Dimension getMaximumSize() { return new Dimension(spacex,spacey); }
    public Dimension getPreferredSize() { return new Dimension(spacex,spacey); }
}
