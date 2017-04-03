/* Date: 5 May, 2014
 * Author: Dr. Uzair Ahmad (uzair@ieee.org)
 * Course: Data Mining 
 * This Program is shared only for learning purpose. 
 * Any other use requires explicit approval from the author.  
 * */
package obj.share;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import obj.share.FPTree.Node;

public class FPTreeVisApp extends JFrame implements FPTreeGraph.DataSource{
	JTabbedPane appTabs = new JTabbedPane();
	// Panels
	JPanel dataPanel = new JPanel();
	JPanel fpTreePanel = new JPanel();
	//JPanel condPatternBasisPanel = new JPanel();	
	FPTreeGraph condFpTreeGraph;
	FPTreeGraph fpTreeGraph;
	// DataPanel Text Areas
	JTextArea rawDataTextArea;
	JTextArea processingOutPutTextArea;
	// Data
	private FPTree fpTree;
	private ArrayList<ContentItem> contentList = new ArrayList<ContentItem>();
	private String filterWords;
	private HashMap<FPTree.Node, ArrayList<FPTree.Node>> condFPTree;
	
	private void prepareDataPanel(){
		dataPanel.setLayout(new BorderLayout());
		// Put Two TextAreas in the textAreaPanel
		JPanel textAreaPanel = new JPanel();
		rawDataTextArea = new JTextArea(50, 50);
		JScrollPane scrollPaneRawData = new JScrollPane(rawDataTextArea);
		processingOutPutTextArea = new JTextArea(50, 50);
		JScrollPane scrollPaneProcessing = new JScrollPane();
		scrollPaneProcessing.getViewport().add(processingOutPutTextArea);
		textAreaPanel.add(scrollPaneRawData);
		textAreaPanel.add(scrollPaneProcessing);
		
		// Create SplitPane for the textAreas
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scrollPaneRawData, scrollPaneProcessing);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2);
		// Put the textAreaPanel in the Center of dataPanel
		dataPanel.add(splitPane, BorderLayout.CENTER);
		
		// Put two Buttons in btnPanel
		JPanel btnPanel = new JPanel();
		JButton btnLoadData = new JButton("Load Data");		
		btnPanel.add(btnLoadData);		
		btnLoadData.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					JFileChooser fileChooser = new JFileChooser("Select Data File");
					int returnVal = fileChooser.showOpenDialog(dataPanel);
					if (returnVal == JFileChooser.APPROVE_OPTION) {					
			            File file = fileChooser.getSelectedFile();
			            String strFile = file.getPath();
			            FileReader fileReader = new FileReader(strFile);		            
						BufferedReader br = new BufferedReader(fileReader);
						String line = br.readLine();
						contentList.clear(); // Remove Previously loaded content
						while (line != null){
							rawDataTextArea.append(line +"\n");
							ContentItem contentItem = new ContentItem();
							contentItem.setContent(line);
							contentList.add(contentItem);
							line = br.readLine();
						}					
					}
				}catch (FileNotFoundException e) {			
					e.printStackTrace();
				} catch (IOException e) {			
					e.printStackTrace();
				}				
			}
		});
		
		JButton btnLoadFilterWords = new JButton("Load Filter Words");		
		btnPanel.add(btnLoadFilterWords);		
		btnLoadFilterWords.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					JFileChooser fileChooser = new JFileChooser("Select Filter Words Data File");
					int returnVal = fileChooser.showOpenDialog(dataPanel);
					if (returnVal == JFileChooser.APPROVE_OPTION) {					
			            File file = fileChooser.getSelectedFile();
			            String strFile = file.getPath();
			            FileReader fileReader = new FileReader(strFile);		            
						BufferedReader br = new BufferedReader(fileReader);
						String line = br.readLine();
						filterWords = "";
						while (line != null){
							rawDataTextArea.append(line +"\n");
							filterWords += line;
							line = br.readLine();
						}					
					}
				}catch (FileNotFoundException e) {			
					e.printStackTrace();
				} catch (IOException e) {			
					e.printStackTrace();
				}				
			}
		});
		
		JButton btnProcessRawData = new JButton("Process FPTree");
		btnPanel.add(btnProcessRawData);
		btnProcessRawData.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (contentList.size() == 0 ){
					JOptionPane.showMessageDialog(null, "No content is found.");
				}
				String support = JOptionPane.showInputDialog("Input the minimum support threshold value.");
				try{
					Integer supportVal = Integer.parseInt(support);
					prepareTreeComponents(supportVal);
				}catch(Exception e){
					JOptionPane.showMessageDialog(null, "Invalid input. Please input only whole numbers. ");
				}					
			}
		});		
		// Put btnPane in the North
		dataPanel.add(btnPanel, BorderLayout.NORTH);
	}
	public FPTreeVisApp(){
		JMenuBar menuBar = new JMenuBar();;
		JMenu menu = new JMenu("Help");
		
		JMenuItem menuItem = new JMenuItem("About");
		menuItem.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {				 
				String msg = "FPTree Learning Tool \n" +
				" Date: 5 May, 2014 \n" +
				" Author: Dr. Uzair Ahmad (uzair@ieee.org). " +
				" College of Computer and Information Systems. King Saud University. ccis.ksu.edu.sa \n" +
				" Target Courses: Data Mining/Machine Learning \n" + 
				" This Program is shared only for learning purpose. \n" +  
				" Any other use requires explicit permission of the author.";
				JOptionPane.showMessageDialog(null, msg);
			}
		});
		menu.add(menuItem);
		menuBar.add(menu);
		setJMenuBar(menuBar);
		
		Toolkit tk = Toolkit.getDefaultToolkit();  
	    int xSize = ((int) tk.getScreenSize().getWidth());  
	    int ySize = ((int) tk.getScreenSize().getHeight());  
	    setSize(xSize,ySize);  
	    setExtendedState(JFrame.MAXIMIZED_BOTH);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setTitle("Object Share: Learning FPTree through Visualization");
	    
	    
	    prepareDataPanel();
	    appTabs.add("Data" , dataPanel);
	    
	    fpTreeGraph = new FPTreeGraph(this, FPTreeGraph.FP_TREE_MODE);
	    appTabs.add("FPTree Graph" , fpTreeGraph);
	    
//	    appTabs.add("Conditional Pattern Basis" , condPatternBasisPanel);
	    
	    condFpTreeGraph = new FPTreeGraph(this, FPTreeGraph.COND_FPTREE_MODE);
	    appTabs.add("Conditional FPTree" , condFpTreeGraph);	    
	    getContentPane().add(appTabs);
	}
	
	private void prepareTreeComponents(int support){
		fpTree = new FPTree(support, contentList, processingOutPutTextArea);
		fpTree.setFilterWords(filterWords);
		fpTree.constructFPTree();
		fpTree.constructCondPattBasis();
		condFPTree = fpTree.constructCondFpTree();
		
	}
	public static void main(String args[]){
		FPTreeVisApp fpTreeVis = new FPTreeVisApp();		
		fpTreeVis.setVisible(true);
	}
	@Override
	public HashMap<Node, ArrayList<Node>> populateCondFPTreeData() {		
		return condFPTree;
	}
	@Override
	public Node getFPTreeRootNode() {
		// TODO Auto-generated method stub
		return fpTree.getRootNode();
	}
}
