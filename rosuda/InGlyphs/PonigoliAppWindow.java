import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class PonigoliAppWindow extends JFrame implements ActionListener {
	
	public static JFrame frame;
    static String filename = null;
    static Dataset dataSet = null;
    static String scale = "same scale";
    static String chartType = "Scatters";
    static JPanel chartPanel = null;
    static JLabel messageLabel = new JLabel("Choose a file");

    public PonigoliAppWindow() {
		filename =
			"C:"+ File.separatorChar +
			"daniela" + File.separatorChar +
			"java" + File.separatorChar +
			"datasets" + File.separatorChar +
			"Iris2.txt";
			
		//Create a file chooser
			
		final JFileChooser fc = new JFileChooser();
			
		//Create the open button
			
		final JTextField filePath = new JTextField(20);
		filePath.setText(filename);
		
		JButton openButton = new JButton("Open a File");
		openButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					int returnVal = fc.showOpenDialog(new JFrame("Choose a file"));
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						filename = file.getPath();
						filePath.setText(filename);
					}
					else {
						messageLabel.setText("File problems");
					}
				}
			}
		);

		String[] chartTypeOptions = {"Scatters","Ponigoli","Bars","Stars","Lines","Boxes","Faces"};
		
		// Create the combobox, select the Scatterplot
		
		JComboBox chartTypeComboBox = new JComboBox(chartTypeOptions);
		
		chartTypeComboBox.setSelectedIndex(0);
		chartTypeComboBox.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox cb = (JComboBox)e.getSource();
					chartType = (String)cb.getSelectedItem();
					
				}
			}
		);
		
		String[] scaleOptions = {"same scale","std scale"};
		
		// Create the combo box, select the proportional angles
		JComboBox scaleComboBox = new JComboBox(scaleOptions);
		scaleComboBox.setSelectedIndex(0);
		scaleComboBox.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox cb = (JComboBox)e.getSource();
					scale = (String)cb.getSelectedItem();
				}
			}
		);
		
		JButton drawButton = new JButton("Draw");
		
		drawButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						
						// Set file path and name				
						dataSet = new Dataset(filePath.getText());
						Common.initStatic();
						
						// 100 cases
						SMarker m = new SMarker(100);

						// create a frame
						TFrame polygonFrame = new TFrame("test plot",TFrame.clsUser);

						polygonFrame.dataSet = dataSet;
						polygonFrame.chartType = chartType;
						polygonFrame.scale = scale;

						Common.mainFrame = polygonFrame;
						polygonFrame.addWindowListener(Common.defaultWindowListener);
						ChartCanvas cCanvas = new ChartCanvas(polygonFrame,m);
						m.addDepend(cCanvas);
						cCanvas.setSize(new Dimension(400,300));
						polygonFrame.add(cCanvas);
						polygonFrame.pack();
						polygonFrame.setVisible(true);
						
						messageLabel.setText("File loaded");
					}
					catch (Exception exc) {
						messageLabel.setText("File problem");
						System.out.println(exc);
					}
				}
			}
		);
		
		//For layout purposes, put the buttons in a separate panel
		
		JPanel filePanel = new JPanel();
		filePanel.setLayout(new FlowLayout());
		//filePanel.setBackground(Color.);
		filePanel.add(openButton);
		filePanel.add(filePath);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(chartTypeComboBox);
		buttonPanel.add(scaleComboBox);
		buttonPanel.add(drawButton);
		chartPanel = new JPanel();
		chartPanel.setBackground(Color.lightGray);
		getContentPane().add(filePanel, BorderLayout.NORTH);
		getContentPane().add(chartPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(new Dimension(400,300));
		setTitle("Ponigoli");
		setVisible(true);
	}
	public static void main(String[] args) {
		frame = new PonigoliAppWindow();
	}
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
	}
}