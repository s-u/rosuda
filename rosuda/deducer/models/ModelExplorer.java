package org.rosuda.deducer.models;
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
import javax.swing.SwingConstants;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;

import org.rosuda.deducer.toolkit.OkayCancelPanel;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
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

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ModelExplorer inst = new ModelExplorer();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}
	
	public ModelExplorer() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			{
				getContentPane().setLayout(null);
				{
					topPanel = new JPanel();
					getContentPane().add(topPanel);
					topPanel.setBounds(0, 0, 700, 35);
					topPanel.setLayout(null);
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
					getContentPane().add(middlePanel);
					middlePanel.setBounds(0, 35, 700, 506);
					middlePanel.setLayout(middlePanelLayout);
					{
						tabs = new JTabbedPane();
						middlePanel.add(tabs, BorderLayout.CENTER);
						tabs.setPreferredSize(new java.awt.Dimension(700, 501));
						{
							generalTab = new JPanel();
							tabs.addTab("General", null, generalTab, null);
							generalTab.setLayout(null);
							generalTab.setPreferredSize(new java.awt.Dimension(695, 475));
							{
								previewPanel = new JPanel();
								generalTab.add(previewPanel);
								BorderLayout previewPanelLayout = new BorderLayout();
								previewPanel.setLayout(previewPanelLayout);
								previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
								previewPanel.setBounds(8, 0, 500, 475);
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
								generalTab.add(options);
								options.setText("Options");
								options.setBounds(556, 12, 91, 26);
								options.addActionListener(generalListener);
							}
							{
								plots = new JButton();
								generalTab.add(plots);
								plots.setText("Plots");
								plots.setBounds(556, 132, 91, 26);
								plots.addActionListener(generalListener);
							}
							{
								postHoc = new JButton();
								generalTab.add(postHoc);
								postHoc.setText("Post Hoc");
								postHoc.setBounds(556, 70, 91, 26);
								postHoc.addActionListener(generalListener);
							}
							{
								tests = new JButton();
								generalTab.add(tests);
								tests.setText("Tests");
								tests.setBounds(556, 101, 91, 26);
								tests.addActionListener(generalListener);
							}
							{
								means = new JButton();
								generalTab.add(means);
								means.setText("Means");
								means.setBounds(556, 163, 91, 26);
								means.addActionListener(generalListener);
							}
							{
								export = new JButton();
								generalTab.add(export);
								export.setText("Export");
								export.setBounds(556, 194, 91, 26);
								export.addActionListener(generalListener);
							}
							{
								update = new JButton();
								generalTab.add(update);
								update.setText("Update Model");
								update.setBounds(539, 422, 124, 45);
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
					getContentPane().add(bottomPanel);
					bottomPanel.setBounds(0, 541, 700, 50);
					bottomPanel.setLayout(null);
					{
						help = new JButton();
						bottomPanel.add(help);
						help.setText("Help");
						help.setBounds(12, 12, 34, 27);
						help.addActionListener(generalListener);
					}
					{
						okayCancelPanel = new OkayCancelPanel(true,true,generalListener);
						bottomPanel.add(okayCancelPanel);
						okayCancelPanel.setBounds(377, 7, 311, 32);
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
