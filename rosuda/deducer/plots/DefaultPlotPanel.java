package org.rosuda.deducer.plots;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


public class DefaultPlotPanel extends javax.swing.JPanel implements ActionListener {
	private JLabel label;
	private JButton histogram;
	private JPanel buttonPanel;
	private JButton boxplot;
	private JButton line;
	private JButton scatterPlot;
	private JButton pie;
	private JButton time;
	private JLabel jLabel1;
	private JLabel orLabel;
	private JButton bar;
	private JButton bubble;

	PlotBuilder builder;
	
	public DefaultPlotPanel(PlotBuilder builder) {
		super();
		this.builder = builder;
		initGUI();
	}
	
	private void initGUI() {
		try {
			JPanel p ;
			setPreferredSize(new Dimension(400, 300));
			BoxLayout thisLayout = new BoxLayout(this, javax.swing.BoxLayout.Y_AXIS);
			this.setLayout(thisLayout);
			this.add(Box.createRigidArea(new Dimension(0, 20)));

			{
				label = new JLabel();
				label.setAlignmentX(Component.CENTER_ALIGNMENT);
				this.add(label);
				label.setText("Drag a component from above");
				label.setFont(new java.awt.Font("Dialog",0,18));
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setPreferredSize(new java.awt.Dimension(376, 38));
			}
			{
				this.add(Box.createRigidArea(new Dimension(0, 5)));
				orLabel = new JLabel();
				orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				this.add(orLabel);
				orLabel.setText("OR");
				orLabel.setHorizontalAlignment(SwingConstants.CENTER);
				orLabel.setFont(new java.awt.Font("Dialog",2,26));
				orLabel.setPreferredSize(new java.awt.Dimension(376, 32));
			}
			{
				this.add(Box.createRigidArea(new Dimension(0, 5)));
				jLabel1 = new JLabel();
				jLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
				this.add(jLabel1);
				jLabel1.setText("Select a plot type:");
				jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
				jLabel1.setFont(new java.awt.Font("Dialog",0,18));
				jLabel1.setPreferredSize(new java.awt.Dimension(376, 38));
			}
			{
				this.add(Box.createRigidArea(new Dimension(0, 20)));
				buttonPanel = new JPanel();
				buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
				GridLayout buttonPanelLayout = new GridLayout(2, 4);
				//buttonPanelLayout.setColumns(1);
				buttonPanelLayout.setHgap(5);
				buttonPanelLayout.setVgap(5);
				buttonPanel.setPreferredSize(new java.awt.Dimension(300, 160));
				buttonPanel.setMaximumSize(new java.awt.Dimension(300, 160));
				this.add(buttonPanel);
				buttonPanel.setLayout(buttonPanelLayout);

				{
					histogram = new JButton();
					buttonPanel.add(histogram);
					histogram.setBounds(64, 125, 66, 80);
					p = PlottingElement.createElement("template","histogram").makeComponent();
					p.setBorder(null);
					histogram.add(p);
					histogram.addActionListener(this);
				}				
				{
					boxplot = new JButton();
					buttonPanel.add(boxplot);
					boxplot.setPreferredSize(new java.awt.Dimension(70, 80));
					boxplot.setBounds(132, 126, 70, 80);
					p = PlottingElement.createElement("template","boxplot").makeComponent();
					p.setBorder(null);
					boxplot.add(p);
					boxplot.addActionListener(this);
				}
				{
					pie = new JButton();
					buttonPanel.add(pie);
					pie.setPreferredSize(new java.awt.Dimension(70, 80));
					pie.setBounds(202, 126, 70, 80);
					p = PlottingElement.createElement("template","pie").makeComponent();
					p.setBorder(null);
					pie.add(p);
					pie.addActionListener(this);
				}
				{
					bar = new JButton();
					buttonPanel.add(bar);
					bar.setPreferredSize(new java.awt.Dimension(70, 80));
					bar.setBounds(272, 126, 70, 80);
					p = PlottingElement.createElement("template","bar").makeComponent();
					p.setBorder(null);
					bar.add(p);
					bar.addActionListener(this);
				}

				{
					scatterPlot = new JButton();
					buttonPanel.add(scatterPlot);
					scatterPlot.setPreferredSize(new java.awt.Dimension(70, 80));
					scatterPlot.setBounds(62, 206, 70, 80);
					p = PlottingElement.createElement("template","scatter").makeComponent();
					p.setBorder(null);
					scatterPlot.add(p);
					scatterPlot.addActionListener(this);
				}
				{
					line = new JButton();
					buttonPanel.add(line);
					line.setPreferredSize(new java.awt.Dimension(70, 80));
					line.setBounds(132, 206, 70, 80);
					p = PlottingElement.createElement("template","line").makeComponent();
					p.setBorder(null);
					line.add(p);
					line.addActionListener(this);
				}
				{
					time = new JButton();
					buttonPanel.add(time);
					time.setPreferredSize(new java.awt.Dimension(70, 80));
					time.setBounds(202, 206, 70, 80);
					p = PlottingElement.createElement("template","series").makeComponent();
					p.setBorder(null);
					time.add(p);
					time.addActionListener(this);
				}				
				{
					bubble = new JButton();
					buttonPanel.add(bubble);
					bubble.setPreferredSize(new java.awt.Dimension(70, 80));
					bubble.setBounds(272, 206, 70, 80);
					p = PlottingElement.createElement("template","bubble").makeComponent();
					p.setBorder(null);
					bubble.add(p);
					bubble.addActionListener(this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		PlottingElement p;
		if(o == histogram)
			p = (PlottingElement) PlotController.getTemplates().get("histogram");
		else if(o == boxplot)
			p = (PlottingElement) PlotController.getTemplates().get("boxplot");
		else if(o == pie)
			p = (PlottingElement) PlotController.getTemplates().get("pie");
		else if(o == bar)
			p = (PlottingElement) PlotController.getTemplates().get("bar");
		else if(o == time)
			p = (PlottingElement) PlotController.getTemplates().get("series");
		else if(o == scatterPlot)
			p = (PlottingElement) PlotController.getTemplates().get("scatter");
		else if(o == line)
			p = (PlottingElement) PlotController.getTemplates().get("line");
		else if(o == bubble)
			p = (PlottingElement) PlotController.getTemplates().get("bubble");
		else
			return;
		
		builder.addElement((PlottingElement) p.clone());
		
	}

}
