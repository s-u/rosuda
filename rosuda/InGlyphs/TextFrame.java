//
//  TextFrame.java
//  InGlyphs
//
//  Created by Daniela DiBenedetto on Tue Nov 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.InGlyphs;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

import java.awt.*;
import java.awt.event.*;

/** Simple help frame that displays list of available shortcuts and their descriptions.
@version $Id$
*/
public class TextFrame extends TFrame implements ActionListener {
    TextArea t;

    public TextFrame() {
        super("Help",clsHelp);
        setLayout(new BorderLayout());
        t=new TextArea();
	t.setText("\n");
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
        addWindowListener(Common.getDefaultWindowListener());
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        dispose();
        removeAll();
        WinTracker.current.rm(this);
    }
}
