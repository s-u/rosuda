package org.rosuda.util;

import java.awt.*;

/** tiny class implementing a spacer */
public class SpacingPanel extends Panel {
    int spacex, spacey;
    public SpacingPanel() { spacex=spacey=15; };
    public SpacingPanel(int sz) { spacex=spacey=sz; };
    public SpacingPanel(int x, int y) { spacex=x; spacey=y; };
    public Dimension getMinimumSize() { return new Dimension(spacex,spacey); }
    public Dimension getMaximumSize() { return new Dimension(spacex,spacey); }
    public Dimension getPreferredSize() { return new Dimension(spacex,spacey); }
}
