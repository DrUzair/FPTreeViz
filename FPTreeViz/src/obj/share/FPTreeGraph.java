/* Date: 5 May, 2014
 * Author: Dr. Uzair Ahmad (uzair@ieee.org)
 * Course: Data Mining 
 * This Program is shared only for learning purpose. 
 * Any other use requires explicit approval from the author.  
 * */

package obj.share;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import obj.share.FPTree.Node;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;

import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;

@SuppressWarnings("serial")
public class FPTreeGraph extends JPanel{
	private int FPTREE_GRAPH_MODE = FP_TREE_MODE;
	public static final int FP_TREE_MODE = 0;
	public static final int PATTERN_BASIS_MODE = 1;
	public static final int COND_FPTREE_MODE = 2;
	FPTree.Node rootNode;
	final Dimension layout_size = new Dimension(Toolkit.getDefaultToolkit().getScreenSize().width-100, 
			Toolkit.getDefaultToolkit().getScreenSize().height-150);
	
	public interface DataSource{
		public HashMap<FPTree.Node, ArrayList<FPTree.Node>> populateCondFPTreeData();
		public FPTree.Node getFPTreeRootNode();
	}
	public static final Font EDGE_FONT = new Font("SansSerif", Font.PLAIN, 10);	
	DataSource dataSource;
	
	private Graph<FPTree.Node, String> graph;	
	private AbstractLayout<FPTree.Node, String> frLayout = null;
	private TreeLayout<FPTree.Node, String> treeLayout = null;
	private Forest<FPTree.Node, String> forest;
	private VisualizationViewer<FPTree.Node, String> visViewer;
	HashMap<FPTree.Node, ArrayList<FPTree.Node>> condFPTreeNodesMap;
	
	private Vector<String> uniqueNodesVector = new Vector<String>();
	
	
	
	private String selectedNodeName="";
	
	private static final int EDGE_LENGTH = 50;
	private Timer timer;
	private boolean done;
	
	
	private Integer max_word_count = 0;	
	private Integer min_word_count = 10000000;
		
	public FPTreeGraph(DataSource dataSource, int mode){
		FPTREE_GRAPH_MODE = mode;
		this.dataSource = (DataSource)dataSource;				
		setLayout(new BorderLayout());		
		add(prepareLayout(), BorderLayout.NORTH);
		setBackground(Color.white);
	}   	
	private Box prepareLayout(){
		Box paramBox = Box.createHorizontalBox();		
		JPanel visParamPanel = new JPanel();
		// Prepare a JButton for FP Tree graph
		if (FPTREE_GRAPH_MODE == FP_TREE_MODE){
			JButton btnDispGraph = new JButton("Display FP Tree");
			visParamPanel.add(btnDispGraph);		
			btnDispGraph.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {	
					graph = new ObservableGraph<FPTree.Node, String>(
							Graphs.<FPTree.Node, String>synchronizedDirectedGraph(
									new DirectedSparseMultigraph<FPTree.Node, String>()));
					rootNode = dataSource.getFPTreeRootNode();
					prepareFPTreeGraph(rootNode);
					initGraphLayout();
					setUpGraphControlPanel();					
				}
			});
			JLabel selectNodeLabel = new JLabel("Select a Node");
			visParamPanel.add(selectNodeLabel);
		}
		// Prepare a JButton for Conditional FP Tree graph 
		if (FPTREE_GRAPH_MODE == COND_FPTREE_MODE){
			JButton btnDispGraph = new JButton("Dispaly Association Rules");
			visParamPanel.add(btnDispGraph);		
			btnDispGraph.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent arg0) {
					condFPTreeNodesMap = dataSource.populateCondFPTreeData();
					Set<Node> keys = condFPTreeNodesMap.keySet();				 
					for(Node node : keys){
						uniqueNodesVector.add(node.getNodeName());
					}
					uniqueNodesVector.add("*"); // Represents All Nodes
					prepareCondFPTreeGraph();
					initGraphLayout();
					setUpGraphControlPanel();
				}
			});	
			JLabel selectNodeLabel = new JLabel("Select a Node");
			visParamPanel.add(selectNodeLabel);
		}
		JComboBox<String> condStringCombo = new JComboBox<String>(uniqueNodesVector);
		condStringCombo.setSize(10, 50);
		condStringCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				String item = (String)e.getItem();
				if (e.getStateChange() == ItemEvent.SELECTED){
					selectedNodeName = item;
					if (FPTREE_GRAPH_MODE == COND_FPTREE_MODE){updateCondFPTreeGraph();}
					if (FPTREE_GRAPH_MODE == FP_TREE_MODE){updateFPTreeGraph();}
				}
			}
		});
		condStringCombo.setPrototypeDisplayValue("Choose a node.");
		JPanel wrapper = new JPanel();
		wrapper.add( condStringCombo );		
		visParamPanel.add(wrapper);
		
		paramBox.add(visParamPanel);
		
		return paramBox;
	}
		
	
	public void initTreeLayout(){
		treeLayout = new TreeLayout<FPTree.Node, String>(forest);
		visViewer = new VisualizationViewer<FPTree.Node, String>(treeLayout, layout_size);
		visViewer.setBackground(Color.WHITE);
		visViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		visViewer.getRenderContext().setVertexLabelTransformer(new VertexLabeler<FPTree.Node>());
		
      	visViewer.getRenderContext().setVertexFillPaintTransformer(new VertexColorTransformer<FPTree.Node>());
      	visViewer.getRenderContext().setVertexDrawPaintTransformer(new VertexColorTransformer<FPTree.Node>());
      	visViewer.setVertexToolTipTransformer(new VertexToolTipTransformer<FPTree.Node>());      	      	
      	Transformer<FPTree.Node, Font> vertexFont = new Transformer<FPTree.Node, Font>() {
			public Font transform(FPTree.Node node) {				
				Font font = new Font("Serif", Font.BOLD, 12);                               	
	            float scale = (node.getNodeValue()*1.0F)/(max_word_count*1.0F)*12;	                
	            return font.deriveFont(font.getSize()+scale);                			
			}
		};
		visViewer.getRenderContext().setVertexFontTransformer(vertexFont);
		Transformer<FPTree.Node, Shape> vertexShape = new Transformer<FPTree.Node, Shape>() {
				public Shape transform(FPTree.Node node){
					Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
					float scale = (float) ((node.getNodeValue()*1.0F)/(max_word_count*1.0F)*10 );
					//System.out.println(node.getNodeName() + " freq " + node.getNodeValue() + max_word_count);
					return AffineTransform.getScaleInstance(scale, scale).createTransformedShape(circle);
				}
		};
		visViewer.getRenderContext().setVertexShapeTransformer(vertexShape);       
		/*Edge Color and shape*/
		Transformer<String, Paint> edgePaint = new Transformer<String, Paint>() {
		    public Paint transform(String edge) {		    	
		        return Color.GRAY;
		    }
		};
		visViewer.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
		visViewer.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.LIGHT_GRAY));
		visViewer.getRenderContext().setArrowDrawPaintTransformer(new ConstantTransformer(Color.LIGHT_GRAY));

		DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		JComboBox modeComboBox = graphMouse.getModeComboBox();
        visViewer.setGraphMouse(graphMouse);
        visViewer.addKeyListener(graphMouse.getModeKeyListener());		
        modeComboBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);	
			
		JPanel uodVisGraphPanel = new JPanel();
		uodVisGraphPanel.add(visViewer);		
		add(uodVisGraphPanel, BorderLayout.CENTER);	
		
	}
	public void initGraphLayout() {
		frLayout = new SpringLayout<FPTree.Node, String>(graph, new ConstantTransformer(EDGE_LENGTH));
		visViewer = new VisualizationViewer<FPTree.Node, String>(frLayout, layout_size);
		visViewer.setBackground(Color.WHITE);
		visViewer.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
		Transformer<FPTree.Node, String> vertexLabeler = new Transformer<FPTree.Node, String>() {
			public String transform(FPTree.Node node){				
				return node.getNodeName()+":"+node.getNodeValue();
			}
		};
		visViewer.getRenderContext().setVertexLabelTransformer(vertexLabeler);      	
		Transformer<FPTree.Node, Paint> vertexFillColorTransformer = new Transformer<FPTree.Node, Paint>() {
			public Paint transform(FPTree.Node node) {				
				if (node.getNodeType() == Node.IS_ROOT_NODE)
					return Color.orange;
				else
					return Color.green;	                            			
			}
		};
		visViewer.getRenderContext().setVertexFillPaintTransformer(vertexFillColorTransformer);
		Transformer<FPTree.Node, Paint> vertexDrawColorTransformer = new Transformer<FPTree.Node, Paint>() {
			public Paint transform(FPTree.Node node) {				
				if (node.getNodeType() == Node.IS_ROOT_NODE)
					return Color.GREEN;
				else
					return Color.GRAY;	                            			
			}
		};
      	visViewer.getRenderContext().setVertexDrawPaintTransformer(vertexDrawColorTransformer);
      	visViewer.setVertexToolTipTransformer(new VertexToolTipTransformer<FPTree.Node>());      	      	
      	Transformer<FPTree.Node, Font> vertexFont = new Transformer<FPTree.Node, Font>() {
			public Font transform(FPTree.Node node) {				
				Font font = new Font("Serif", Font.BOLD, 12);                               	
	            float scale = (node.getNodeValue()*1.0F)/(max_word_count*1.0F)*12;	                
	            return font.deriveFont(font.getSize()+scale);                			
			}
		};
		visViewer.getRenderContext().setVertexFontTransformer(vertexFont);
		Transformer<FPTree.Node, Shape> vertexShape = new Transformer<FPTree.Node, Shape>() {
				public Shape transform(FPTree.Node node){
					Ellipse2D circle = new Ellipse2D.Double(-15, -15, 30, 30);
					float scale = (float) ((node.getNodeValue()*1.0F)/(max_word_count*1.0F)); 	
					//scale *= ((max_word_count-min_word_count)*1.0F)/((max_word_count+min_word_count)*1.0F);				
					if(node.getNodeValue() == 0)
						return AffineTransform.getScaleInstance(1, 1).createTransformedShape(circle);
					return AffineTransform.getScaleInstance(1+scale, 1+scale).createTransformedShape(circle);
				}
		};
		visViewer.getRenderContext().setVertexShapeTransformer(vertexShape);       
		/*Edge Color and shape*/
		Transformer<String, Paint> edgePaint = new Transformer<String, Paint>() {
		    public Paint transform(String edgeLabel) {		    	
		    	if(edgeLabel.contains("<--->")){	
		    		return Color.LIGHT_GRAY;
		    	}else{
		    		return Color.GRAY;
		    	}		    	
		    }
		};
		visViewer.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
		visViewer.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.LIGHT_GRAY));
		visViewer.getRenderContext().setArrowDrawPaintTransformer(new ConstantTransformer(Color.LIGHT_GRAY));
		visViewer.getRenderContext().setEdgeStrokeTransformer(new Transformer<String, Stroke>() {			
			@Override
			public Stroke transform(String edgeLabel) {
				if(edgeLabel.contains("<--->")){					
					float dash[] = { 10.0f };
					Stroke dotted = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			                BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
					return dotted;
				}else{
					Stroke normal = new BasicStroke(2.0f);
					return  normal;
				}				
			}
		});		
        JPanel uodVisGraphPanel = new JPanel();
		uodVisGraphPanel.add(visViewer);		
		add(uodVisGraphPanel, BorderLayout.CENTER);					
	}
	private void setUpGraphControlPanel(){		
		DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		JComboBox modeComboBox = graphMouse.getModeComboBox();
        visViewer.setGraphMouse(graphMouse);
        visViewer.addKeyListener(graphMouse.getModeKeyListener());		
        modeComboBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);				
		JPanel graphControlPanel = new JPanel(); 
		final JButton btnSwitchLayout = new JButton("Switch to SpringLayout");
        graphControlPanel.add(btnSwitchLayout);		
        graphControlPanel.add(modeComboBox);        
		btnSwitchLayout.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent ae) {				
				if (btnSwitchLayout.getText().indexOf("Spring") > 0) {
					btnSwitchLayout.setText("Switch to FRLayout");
					//treeLayout = new TreeLayout<FPTree.Node, String>(forest);
					frLayout = new SpringLayout<FPTree.Node, String>(graph, new ConstantTransformer(EDGE_LENGTH));
					frLayout.setSize(layout_size);
					visViewer.getModel().setGraphLayout(frLayout, layout_size);
				} else {
					btnSwitchLayout.setText("Switch to SpringLayout");
					frLayout = new FRLayout<FPTree.Node, String>(graph, layout_size);
					visViewer.getModel().setGraphLayout(frLayout, layout_size);					
				}
				
			}
		});
		add(graphControlPanel, BorderLayout.SOUTH);
	}
	private void updateCondFPTreeGraph(){
		graph = new ObservableGraph<FPTree.Node, String>(
				Graphs.<FPTree.Node, String>synchronizedDirectedGraph(
						new DirectedSparseMultigraph<FPTree.Node, String>()));				
		
		Iterator<FPTree.Node> keys = condFPTreeNodesMap.keySet().iterator();
		max_word_count = 0;		
		min_word_count = 10000000;
		if (selectedNodeName.equalsIgnoreCase("*")){
			while(keys.hasNext()){		
				FPTree.Node node = keys.next();		
				graph.addVertex(node);			        	
				ArrayList<FPTree.Node> freqPattNodes = condFPTreeNodesMap.get(node);
				for(FPTree.Node childNode : freqPattNodes){				
					graph.addEdge(node.getNodePath()+"<-->"+childNode.getNodePath(), node, childNode);
					graph.addEdge(childNode.getNodePath()+"<-->"+node.getNodePath(), childNode, node);

					if (max_word_count < node.getNodeValue())
						max_word_count = node.getNodeValue();
					if (max_word_count < childNode.getNodeValue())
						max_word_count = childNode.getNodeValue(); // childNode will never be more frequent !!!

					if (min_word_count > childNode.getNodeValue())
						min_word_count = childNode.getNodeValue();
					if (min_word_count > node.getNodeValue())
						min_word_count = node.getNodeValue(); 	// parentNode (node) will never be less frequent !!!
				}
			}			
		}else{
			while(keys.hasNext()){		
				FPTree.Node node = keys.next();		
				if (node.getNodeName().equalsIgnoreCase(selectedNodeName)){
					graph.addVertex(node);			        	
					ArrayList<FPTree.Node> freqPattNodes = condFPTreeNodesMap.get(node);
					for(FPTree.Node childNode : freqPattNodes){				
						graph.addEdge(node.getNodePath()+"<-->"+childNode.getNodePath(), node, childNode);
						graph.addEdge(childNode.getNodePath()+"<-->"+node.getNodePath(), childNode, node);
						
						if (max_word_count < node.getNodeValue())
							max_word_count = node.getNodeValue();
						if (max_word_count < childNode.getNodeValue())
							max_word_count = childNode.getNodeValue(); // childNode will never be more frequent !!!
						
						if (min_word_count > childNode.getNodeValue())
							min_word_count = childNode.getNodeValue();
						if (min_word_count > node.getNodeValue())
							min_word_count = node.getNodeValue(); 	// parentNode (node) will never be less frequent !!!
						}
				}
			}		
		}
		frLayout = new SpringLayout<FPTree.Node, String>(graph, new ConstantTransformer(EDGE_LENGTH));
		frLayout.setSize(layout_size);
		visViewer.getModel().setGraphLayout(frLayout, layout_size);		
	}
	@SuppressWarnings("unchecked")
	private void updateFPTreeGraph(){
		graph = new ObservableGraph<FPTree.Node, String>(
				Graphs.<FPTree.Node, String>synchronizedDirectedGraph(
						new DirectedSparseMultigraph<FPTree.Node, String>()));				
		max_word_count = 0;		
		min_word_count = 10000000;
		if (selectedNodeName.equalsIgnoreCase("$")){
			prepareFPTreeGraph(rootNode);			
		}else{
			FPTree.Node node = null;
			ArrayList<FPTree.Node> nodes = rootNode.getGrandChildNode(selectedNodeName);
			for(int k = 0 ; k < nodes.size() ; k++){
				node = nodes.get(k);
				do {
					if (max_word_count < node.getNodeValue())
						max_word_count = node.getNodeValue();
					if (min_word_count > node.getNodeValue())
						min_word_count = node.getNodeValue();									
					graph.addVertex(node);					
					graph.addEdge(node.getNodePath(), node, node.getParentNode());
					node = node.getParentNode();
				} while(node.getNodeType()!= FPTree.Node.IS_ROOT_NODE);				
			}
		}
		frLayout = new SpringLayout<FPTree.Node, String>(graph, new ConstantTransformer(EDGE_LENGTH));
		frLayout.setSize(layout_size);
		visViewer.getModel().setGraphLayout(frLayout, layout_size);		
	}
	public void prepareCondFPTreeGraph() {
		graph = new ObservableGraph<FPTree.Node, String>(
        								Graphs.<FPTree.Node, String>synchronizedDirectedGraph(
        										new DirectedSparseMultigraph<FPTree.Node, String>()));				
		Iterator<FPTree.Node> keys = condFPTreeNodesMap.keySet().iterator();	
		
		while(keys.hasNext()){		
			FPTree.Node node = keys.next();		
			graph.addVertex(node);			        	
			ArrayList<FPTree.Node> freqPattNodes = condFPTreeNodesMap.get(node);
			for(FPTree.Node childNode : freqPattNodes){				
				graph.addEdge(node.getNodePath()+"<-->"+childNode.getNodePath(), node, childNode);
				graph.addEdge(childNode.getNodePath()+"<-->"+node.getNodePath(), childNode, node);

				if (max_word_count < node.getNodeValue())
					max_word_count = node.getNodeValue();
				if (max_word_count < childNode.getNodeValue())
					max_word_count = childNode.getNodeValue(); // childNode will never be more frequent !!!

				if (min_word_count > childNode.getNodeValue())
					min_word_count = childNode.getNodeValue();
				if (min_word_count > node.getNodeValue())
					min_word_count = node.getNodeValue(); 	// parentNode (node) will never be less frequent !!!
			}					
		}
						
    }
	
	private void prepareFPTreeGraph(FPTree.Node rootNode){
		if (max_word_count < rootNode.getNodeValue()){
			max_word_count = rootNode.getNodeValue();
		}
		if (min_word_count > rootNode.getNodeValue()){
			min_word_count = rootNode.getNodeValue();
		}
		
		if (uniqueNodesVector.contains(rootNode.getNodeName()) == false){
			uniqueNodesVector.add(rootNode.getNodeName());
		}
		
		graph.addVertex(rootNode);
		
		FPTree.Node linkNode = rootNode.getNodeLink();
		if(linkNode != null){			
			graph.addEdge(rootNode.toString() + "<--->" + linkNode.toString(), rootNode, linkNode);						
		}
		
		ArrayList<FPTree.Node> childNodes = rootNode.getChildNodes();
		for(FPTree.Node childNode : childNodes){
			graph.addEdge(childNode.getNodePath(), rootNode, childNode);			
			prepareFPTreeGraph(childNode);			
		}
	}
	
    private final static class VertexColorTransformer<V> implements Transformer<V, Paint> {
    	@Override
    	public Paint transform(V v) {
    		String str = v.toString();
			Color color = new Color(1);
			color = Color.GREEN;
			return color;
		}	
	}    
    private final static class VertexToolTipTransformer<V> implements Transformer<V, String> 
    {		
		public String transform(V v) {	
			return v.toString();
		}
	}
	private final static class VertexLabeler<V> implements Transformer<V, String> 
    {
		@Override
		public String transform(V v) {			
			return v.toString();
		}
	}	
}