//
//  iComboBox.java
//  iWidgets
//
//  Created by Simon Urbanek on Mon Jul 26 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import javax.swing.*;

public class iComboBox extends JComboBox {
    public iComboBox(String[] items, boolean editable) {
        super(items);
        setEditable(editable);    
    }

    public iComboBox(String item, boolean editable) {
        super();
        addItem(item);
        setEditable(editable);
    }

    public String getTextContent() {
        Object o=getSelectedItem();
        return o.toString();
    }

    public void setTextContent(String text) {
        setSelectedItem(text);
    }
}
