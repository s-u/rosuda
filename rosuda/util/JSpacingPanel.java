//
//  JSpacingPanel.java
//  Klimt
//
//  Created by Simon Urbanek on 8/9/04.
//  Copyright 2004 __MyCompanyName__. All rights reserved.
//

package org.rosuda.util;

import javax.swing.JPanel;
import java.awt.Dimension;

/** tiny class implementing a spacer  - this class exists basically for compatibility with awt SpacingPanel */
public class JSpacingPanel extends JPanel {
    int spacex, spacey;
    public JSpacingPanel() { spacex=spacey=15; };
    public JSpacingPanel(int sz) { spacex=spacey=sz; };
    public JSpacingPanel(int x, int y) { spacex=x; spacey=y; };
    public Dimension getMinimumSize() { return new Dimension(spacex,spacey); }
    public Dimension getMaximumSize() { return new Dimension(spacex,spacey); }
    public Dimension getPreferredSize() { return new Dimension(spacex,spacey); }
}
