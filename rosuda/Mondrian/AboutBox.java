//
//	File:		AboutBox.java
//

import java.awt.*;
import java.awt.event.*;

public class AboutBox extends Frame
					  implements ActionListener
{
	protected Button okButton;
	protected Label aboutText;

	public AboutBox()
	{
		super();
		this.setLayout(new BorderLayout(15, 15));
		this.setFont(new Font ("SansSerif", Font.BOLD, 14));

		aboutText = new Label ("About Mondrian");
		Panel textPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 15));
		textPanel.add(aboutText);
		this.add (textPanel, BorderLayout.NORTH);
		
		okButton = new Button("OK");
		Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 15));
		buttonPanel.add (okButton);
		okButton.addActionListener(this);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}
	

	public void actionPerformed(ActionEvent newEvent) 
	{
		setVisible(false);
	}	
	
}