//
//  TextFrame.java
//  InGlyphs
//
//  Created by Daniela DiBenedetto on Tue Nov 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.InGlyphs;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.TFrame;
import org.rosuda.ibase.toolkit.WinTracker;

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
