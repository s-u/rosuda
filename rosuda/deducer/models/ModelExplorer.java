package org.rosuda.deducer.models;
import org.rosuda.JGR.layout.AnchorConstraint;
import org.rosuda.JGR.layout.AnchorLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.rosuda.deducer.toolkit.OkayCancelPanel;


public class ModelExplorer extends JFrame {
	protected JPanel topPanel;
	protected JPanel bottomPanel;
	protected JTextArea preview;
	protected JPanel generalTab;
	protected JButton plots;
	protected JButton postHoc;
	protected JButton means;
	protected JButton export;
	protected JButton update;
	protected JPanel interactionPanel;
	protected JPanel otherPanel;
	protected JPanel includedPanel;
	protected JPanel influencePanel;
	protected JPanel residPanel;
	protected JButton assumpHomo;
	protected JButton assumpFunc;
	protected JButton assumpN;
	protected JLabel model;
	protected JPanel okayCancelPanel;
	protected JButton help;
	protected JButton tests;
	protected JButton options;
	protected JTabbedPane tabs;
	protected JScrollPane previewScroller;
	protected JPanel previewPanel;
	protected JPanel middlePanel;
	protected ActionListener generalListener = new ModelListener();

	
	public ModelExplorer(ModelModel mod) {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			{
				AnchorLayout thisLayout = new AnchorLayout();
				getContentPane().setLayout(thisLayout);
				{
					topPanel = new JPanel();
					getContentPane().add(topPanel, new AnchorConstraint(0, 989, 58, 0, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL));
					topPanel.setLayout(null);
					topPanel.setPreferredSize(new java.awt.Dimension(700, 35));
					{
						model = new JLabel();
						topPanel.add(model);
						model.setText("a ~ b + c");
						model.setBounds(282, -1, 418, 36);
						model.setFont(new java.awt.Font("Tahoma",0,18));
					}
					{
						assumpN = new JButton();
						topPanel.add(assumpN);
						assumpN.setBounds(12, 8, 27, 27);
						assumpN.setText("Large Sample");
					}
					{
						assumpFunc = new JButton();
						topPanel.add(assumpFunc);
						assumpFunc.setBounds(44, 8, 27, 27);
						assumpFunc.setText("Correct Functional Form");
					}
					{
						assumpHomo = new JButton();
						topPanel.add(assumpHomo);
						assumpHomo.setText("Homoskedasticity");
						assumpHomo.setBounds(76, 8, 27, 27);
					}
				}
				{
					middlePanel = new JPanel();
					BorderLayout middlePanelLayout = new BorderLayout();
					getContentPane().add(middlePanel, new AnchorConstraint(35, 989, 62, 0, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL));
					middlePanel.setLayout(middlePanelLayout);
					middlePanel.setPreferredSize(new java.awt.Dimension(700, 506));
					{
						tabs = new JTabbedPane();
						middlePanel.add(tabs, BorderLayout.CENTER);
						tabs.setPreferredSize(new java.awt.Dimension(700, 501));
						{
							generalTab = new JPanel();
							AnchorLayout generalTabLayout = new AnchorLayout();
							tabs.addTab("General", null, generalTab, null);
							generalTab.setLayout(generalTabLayout);
							generalTab.setPreferredSize(new java.awt.Dimension(695, 475));
							{
								previewPanel = new JPanel();
								generalTab.add(previewPanel, new AnchorConstraint(1, 731, 992, 12, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
								BorderLayout previewPanelLayout = new BorderLayout();
								previewPanel.setLayout(previewPanelLayout);
								previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
								previewPanel.setPreferredSize(new java.awt.Dimension(500, 475));
								{
									previewScroller = new JScrollPane();
									previewPanel.add(previewScroller, BorderLayout.CENTER);
									{
										preview = new JTextArea();
										previewScroller.setViewportView(preview);
										preview.setFont(new java.awt.Font("Monospaced",0,12));
									}
								}
							}
							{
								options = new JButton();
								generalTab.add(options, new AnchorConstraint(12, 931, 80, 800, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
								options.setText("Options");
								options.setPreferredSize(new java.awt.Dimension(91, 26));
								options.addActionListener(generalListener);
							}
							{
								plots = new JButton();
								generalTab.add(plots, new AnchorConstraint(132, 931, 330, 800, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
								plots.setText("Plots");
								plots.setPreferredSize(new java.awt.Dimension(91, 26));
								plots.addActionListener(generalListener);
							}
							{
								postHoc = new JButton();
								generalTab.add(postHoc, new AnchorConstraint(70, 931, 201, 800, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
								postHoc.setText("Post Hoc");
								postHoc.setPreferredSize(new java.awt.Dimension(91, 26));
								postHoc.addActionListener(generalListener);
							}
							{
								tests = new JButton();
								generalTab.add(tests, new AnchorConstraint(101, 931, 266, 800, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
								tests.setText("Tests");
								tests.setPreferredSize(new java.awt.Dimension(91, 26));
								tests.addActionListener(generalListener);
							}
							{
								means = new JButton();
								generalTab.add(means, new AnchorConstraint(163, 931, 395, 800, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
								means.setText("Means");
								means.setPreferredSize(new java.awt.Dimension(91, 26));
								means.addActionListener(generalListener);
							}
							{
								export = new JButton();
								generalTab.add(export, new AnchorConstraint(194, 931, 460, 800, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
								export.setText("Export");
								export.setPreferredSize(new java.awt.Dimension(91, 26));
								export.addActionListener(generalListener);
							}
							{
								update = new JButton();
								generalTab.add(update, new AnchorConstraint(393, 954, 915, 776, AnchorConstraint.ANCHOR_ABS, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE));
								update.setText("Update Model");
								update.setPreferredSize(new java.awt.Dimension(124, 45));
								update.addActionListener(generalListener);
							}
						}
						{
							residPanel = new JPanel();
							tabs.addTab("Residuals", null, residPanel, null);
						}
						{
							influencePanel = new JPanel();
							tabs.addTab("Infulence & outliers", null, influencePanel, null);
						}
						{
							includedPanel = new JPanel();
							tabs.addTab("Included variables", null, includedPanel, null);
						}
						{
							otherPanel = new JPanel();
							tabs.addTab("Other variables", null, otherPanel, null);
						}
						{
							interactionPanel = new JPanel();
							tabs.addTab("Interactions", null, interactionPanel, null);
						}
					}
				}
				{
					bottomPanel = new JPanel();
					AnchorLayout bottomPanelLayout = new AnchorLayout();
					getContentPane().add(bottomPanel, new AnchorConstraint(898, 989, 1000, 0, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
					bottomPanel.setLayout(bottomPanelLayout);
					bottomPanel.setPreferredSize(new java.awt.Dimension(700, 62));
					{
						help = new JButton();
						bottomPanel.add(help, new AnchorConstraint(379, 66, 814, 17, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL));
						help.setText("Help");
						help.setPreferredSize(new java.awt.Dimension(34, 27));
						help.addActionListener(generalListener);
					}
					{
						okayCancelPanel = new OkayCancelPanel(true,true,generalListener);
						bottomPanel.add(okayCancelPanel, new AnchorConstraint(120, 983, 814, 539, AnchorConstraint.ANCHOR_NONE, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_REL, AnchorConstraint.ANCHOR_NONE));
						okayCancelPanel.setPreferredSize(new java.awt.Dimension(311, 43));
					}
				}
			}
			this.setSize(708, 625);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void refresh(){}
	
	public void optionsClicked(ModelModel mod){}
	public void postHocClicked(ModelModel mod){}
	public void testsClicked(ModelModel mod){}
	public void plotsClicked(ModelModel mod){}
	public void meansClicked(ModelModel mod){}
	public void exportClicked(ModelModel mod){}
	public void updateClicked(ModelModel mod){}
	
	class ModelListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
