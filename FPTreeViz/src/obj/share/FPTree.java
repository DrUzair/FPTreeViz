/* Date: 5 May, 2014
 * Author: Dr. Uzair Ahmad (uzair@ieee.org)
 * Course: Data Mining 
 * This Program is shared only for learning purpose. 
 * Any other use requires explicit approval from the author.  
 * */
package obj.share;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JTextArea;

public class FPTree {
	private JTextArea textArea;
	int freqThreshold = 0;	
//	public FPTree(int freqThresholdVal, ArrayList<ContentItem> contentList){
//		rootNode = new Node();
//		this.freqThreshold = freqThresholdVal;
//		this.contentList = contentList;
//	}
	public FPTree(int freqThresholdVal, ArrayList<ContentItem> contentList, JTextArea textArea){
		rootNode = new Node();
		this.freqThreshold = freqThresholdVal;
		this.contentList = contentList;
		this.textArea = textArea;
	}
	private String words_2b_filtered;
	private boolean filterWordsNeeded = false;
	public void setFilterWords(String filterWords){
		words_2b_filtered = filterWords;
		filterWordsNeeded = true;
	}
	// protected Map<String, Node> itemsList = new HashMap<String, Node>();
	//public Map<String, Node> getItemsList(){return itemsList;}
	public java.util.List<Node> getUniqueNodesList(){return uniqueNodesList;}
	private Node rootNode;
	// Make a wordList and sort the words w.r.t occurrence_count		
	java.util.List<Node> uniqueNodesList = new java.util.ArrayList<Node>();	
	private ArrayList<ContentItem> contentList = new ArrayList<ContentItem>();	
	ArrayList<String> frequencyOrderedContentList = new ArrayList<String>();
	Map<String, Node> nodeFreqMap = new HashMap<String, Node>();
	public Node getRootNode(){		
		return rootNode;
	}
	
	
	class Node{
		private Node parentNode;
		public static final int IS_ROOT_NODE 	= 1;
		public static final int IS_CHILD_NODE 	= 0;
		private int nodeType;
		public int getNodeType(){
			return nodeType;
		}
		public Node(){			
			nodeType = IS_ROOT_NODE;
			nodeName = "$";
		}
		private Node(Node parentNode){ // private: should only be called by addChildNode()
			nodeType = IS_CHILD_NODE;
			this.parentNode = parentNode;			
		}
		@Override
		public String toString(){
			return getNodePath();//getNodeName()+":"+getNodeValue();
		}
		@Override
	    public boolean equals(Object object) {

	        if (object != null && object instanceof Node) {
	            Node node = (Node) object;	            
	            if(this.nodePath.equalsIgnoreCase(node.getNodePath()))
	            	return true;
	        }
	        return false;
	    }

		public Node getParentNode(){return parentNode;}
		
		public String getNodeName() {
			return nodeName;
		}
		private void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}
		public int getNodeValue() {
			return nodeFreq;
		}
		private void setNodeValue(int nodeValue) {
			this.nodeFreq = nodeValue;
		}
		private void incrementNodeValue() {
			this.nodeFreq++;
		}
		private ArrayList<FPTree.Node>	childNodes = new ArrayList<FPTree.Node>();
		public Node addChildNode(String name) {
			Node childNode = new Node(this);
			childNode.setNodeName(name);
			childNode.setNodeValue(childNode.getNodeValue()+1);
			childNodes.add(childNode);			
			childNode.getNodePath();
			// Update the list of unique items in the fpTree
//			for(Node node : uniqueNodesList){
//				if (node.getNodeName().equalsIgnoreCase(this.nodeName) == false)
//					uniqueNodesList.add(childNode);
//			}			
			return childNodes.get(childNodes.size()-1);
		}		
		public Node getDirectChildNode(String name){			
			Node childNodeNull = null;
			Iterator<FPTree.Node> childNodesIter = childNodes.iterator();
			while(childNodesIter.hasNext()){
				Node node = childNodesIter.next();
				if(node.getNodeName().equalsIgnoreCase(name)){					
					return node;
				}
			}
			return childNodeNull;			
		}
		public ArrayList<Node> getChildNodes(){return childNodes;}
		
		public ArrayList<Node> getGrandChildNode(String name){			
			ArrayList<Node> nodesList = new ArrayList<Node>();			
			Iterator<FPTree.Node> childNodesIter = childNodes.iterator();
			while(childNodesIter.hasNext() ){
				Node node = childNodesIter.next();
				if(node.getNodeName().equalsIgnoreCase(name)){					
					nodesList.add(node);
				}else{
					// depth first
					ArrayList<Node> nodesList2 = node.getGrandChildNode(name);
					if ( nodesList2.size() > 0 )
						nodesList.addAll(nodesList2);
				}
			}
			return nodesList;			
		}
		public boolean hasChildNodes(){
			boolean hasChildNodes = false;
			if(childNodes.size()>0)
				hasChildNodes = true;
			return hasChildNodes;
		}
		public String getNodePath(){
			Node parent = parentNode;			
			nodePath = "{"+getNodeValue()+"}"+getNodeName();
			
			if(parent !=null){
				do{
					nodePath += "-->"+parent.getNodeName();
					parent = parent.getParentNode();
				}while(parent != null);
			}
			
			return nodePath;
		}
		public void addNodeLink(FPTree.Node linkNode){			
			nodeLink = linkNode;						
		}
		public FPTree.Node getNodeLink(){return nodeLink;}
		
		private String 	nodeName;
		private int		nodeFreq;
		private Node 	nodeLink;
		private String nodePath = "";
	}
	public class NodeValueComparator implements Comparator<Node>{
		@Override    public int compare(Node o1, Node o2) {		
			return (o1.getNodeValue()>o2.getNodeValue() ? -1 : (o1.getNodeValue()==o2.getNodeValue() ? 0 : 1));    
			}
	}
	public PatternBasis createNewPatternBasis(String condItem){
		return new PatternBasis(condItem);
	}
	
	private void buildItemFrequencyMap(){
		// 3. Load Data	
		int dataItemNumber = 0;		
		
		Iterator<ContentItem> contentListIterator = contentList.iterator();
		textArea.append("\t Parsing Words for : \n ");
		while(contentListIterator.hasNext()){
			ContentItem contentItem =  contentListIterator.next();			
						
			String author = contentItem.getAuthor().replace(" ", ""); // Remove WhiteSpaces, Required for RelevanceMeasure Algorithm
			String content  = author + " " + contentItem.getContent().replaceAll("[^\\u0000-\\uFFFF]",""); // Replace non-alphanumeric characters
			String delim = " \t\n\r.,:;?؟`~!@#$%^&*+-=_/|{}()[]<>\"\'";
			String strWord;	
			Node newNode;	
			textArea.append("\t" + ++dataItemNumber+". " + (content.length() > 60 ? content.substring(0, 60) : content) + "... \n" );			
			StringTokenizer st = new StringTokenizer(content, delim); 
			while (st.hasMoreTokens()) {
				strWord = st.nextToken().toLowerCase().trim();	
				if (strWord.length() > 2){
					newNode = (Node) nodeFreqMap.get(strWord); 
					if (newNode == null) {
						newNode = new Node();
						newNode.setNodeName(strWord);
						newNode.setNodeValue(1);
						nodeFreqMap.put(strWord, newNode); 						
					} else {						
						newNode.incrementNodeValue();						
					}						
				}
			}						
		}
	}
	private void removeFilterWords(){
		// 2. Load words_2b_filtered		
		StringTokenizer st = new StringTokenizer(words_2b_filtered, ","); 
		//uod_param.uodOutputTextArea.setText("Words before removal of filterwords.. " + wordsMap.entrySet().size());		
		ArrayList<String> filterWordsList = new ArrayList<String>(); 
		while (st.hasMoreTokens()) {
			String strFilterWord = st.nextToken().toLowerCase();
			filterWordsList.add(strFilterWord);
			Node wordObj = nodeFreqMap.get(strFilterWord);
			if (wordObj != null){  
				nodeFreqMap.remove(strFilterWord);
			}
		}		
	}
	private void removeInFrequentWords(){
		// 5. Remove Word with less occurrence_count than this.wordCountLimit | this.coWordCountLimit
		Set<Map.Entry<String, Node>> wordsSet = nodeFreqMap.entrySet(); 
		Iterator<Map.Entry<String, Node>> wordsSetIter = wordsSet.iterator();
		textArea.append("\n Removing less frequent words than wordCountLimit ... \n");
			
		int i = 1;
		while(wordsSetIter.hasNext()){		
			Node wordObj = wordsSetIter.next().getValue();			
			if(wordObj.getNodeValue() < this.freqThreshold){
				try{
					textArea.append("\n\t " + i++ + ". Word: " + wordObj.getNodeName() + " WordCount: " + wordObj.getNodeValue() + " \t removed ...");					
				}catch(Exception e){
					e.printStackTrace();
				}
				nodeFreqMap.remove(wordObj.getNodeName());
				wordsSet = nodeFreqMap.entrySet(); 
				wordsSetIter = wordsSet.iterator();
			}
		}
		textArea.append("\n Words after removal of less frequent than limit ... " + nodeFreqMap.entrySet().size() + ".\n");		
	}
	private void sortItems(){
		Set<Map.Entry<String, Node>> wordsSet = nodeFreqMap.entrySet(); 
		Iterator<Map.Entry<String, Node>> wordsSetIter = wordsSet.iterator();		
		wordsSet = nodeFreqMap.entrySet(); 
		wordsSetIter = wordsSet.iterator();
		textArea.append("\n Sorting content wrt Frequency ... ");
		while(wordsSetIter.hasNext()){			
			Node wordObj = wordsSetIter.next().getValue();
			uniqueNodesList.add(wordObj);
		}		
		nodeFreqMap.clear();
		nodeFreqMap = null;		
		Collections.sort(uniqueNodesList, new NodeValueComparator());	
		for(Node node : uniqueNodesList){
			textArea.append("\n Word: " + node.getNodeName() + "\t Freq: " + node.getNodeValue());
		}
	}
	private void reorderContentsList(){
		// Modify contentList to contain frequency ordered contents		
		Iterator<ContentItem> contentListIterator = contentList.iterator();		
		int itemCount = 1;
		textArea.append("\n\n Ordering content items ...  ("+ itemCount + " of "+ contentList.size() +") \n");
		while(contentListIterator.hasNext()){
			ContentItem contentItem =  contentListIterator.next();	
			String author = contentItem.getAuthor().replace(" ", ""); // Remove WhiteSpaces, Required for RelevanceMeasure Algorithm
			String content  = author.toLowerCase() + " " + contentItem.getContent().replaceAll("[^\\u0000-\\uFFFF]","").toLowerCase(); // Replace non-alphanumeric characters
			String frequencyOrderedContent = "";
			Iterator<Node> orderedWordListIterator = uniqueNodesList.iterator();
			while(orderedWordListIterator.hasNext()){
				Node nextMostFrequentNode = orderedWordListIterator.next();
				if(content.contains(nextMostFrequentNode.getNodeName())){
					frequencyOrderedContent += nextMostFrequentNode.getNodeName() + " ";
				}
			}
			textArea.append(itemCount++ + ". " +content +"\t\t --> is ordered as --> \t" + frequencyOrderedContent + "\n");			
//			if (uod_param.uodOutputTextArea.getLineCount()> 50){
//				uod_param.uodOutputTextArea.setText("Ordering content items ...  ("+ itemCount + " of "+ contentList.size() +") \n");
//			}
			
			if (frequencyOrderedContent.length() > 1)
				frequencyOrderedContentList.add(frequencyOrderedContent);
		}		
	}
	public void constructFPTree(){	
		buildItemFrequencyMap();		
		if (filterWordsNeeded) removeFilterWords();
		removeInFrequentWords();
		sortItems();				
		reorderContentsList();
		// Construct FPTree		
		FPTree.Node rootNode = getRootNode();	
		
		Iterator<String> iterator = frequencyOrderedContentList.iterator();
		int i = 1;
		textArea.append("\n Populating Tree for --> \n");
		while(iterator.hasNext()){
			String frequencyOrderedContent = iterator.next();
			textArea.append("\n "+ i++ + ". " +frequencyOrderedContent);			
			// First string of the content
			FPTree.Node childNode = null;
			if (frequencyOrderedContent.contains(" ")){
				String firstWord = frequencyOrderedContent.substring(0, frequencyOrderedContent.indexOf(" "));
				childNode = getRootNode().getDirectChildNode(firstWord);
				if (childNode == null){
					childNode = rootNode.addChildNode(firstWord);		
					ArrayList<FPTree.Node> preExistingNodes = getRootNode().getGrandChildNode(firstWord);
					for(int k = 0; k < preExistingNodes.size()-1 ; k++){
							preExistingNodes.get(k).addNodeLink(preExistingNodes.get(k+1));							
					}
				}else{
					childNode.incrementNodeValue();
				}
			}
			// Rest of the strings
			frequencyOrderedContent = frequencyOrderedContent.substring(frequencyOrderedContent.indexOf(" ")+1, frequencyOrderedContent.length());
			if (frequencyOrderedContent.length() == 0)
				continue;
 			do {
 				String nextWord = frequencyOrderedContent.substring(0, frequencyOrderedContent.indexOf(" "));
				FPTree.Node childNode2 = childNode.getDirectChildNode(nextWord);
				if (childNode2 == null){					
					childNode = childNode.addChildNode(nextWord);
					ArrayList<FPTree.Node> preExistingNodes = getRootNode().getGrandChildNode(nextWord);
					for(int k = 0; k < preExistingNodes.size()-1 ; k++){
							preExistingNodes.get(k).addNodeLink(preExistingNodes.get(k+1));							
					}					
				}else{
					childNode2.incrementNodeValue();
					childNode = childNode2;
				}						
				frequencyOrderedContent = frequencyOrderedContent.substring(frequencyOrderedContent.indexOf(" ")+1, frequencyOrderedContent.length());
			}while (frequencyOrderedContent.contains(" "));			
		}		
	}	
	
	public HashMap<String, PatternBasis> constructCondPattBasis(){
		// For each unique node(item) in the itemsList... 
		for(Node node : uniqueNodesList){
			FPTree.PatternBasis pattBasis = createNewPatternBasis(node.getNodeName());
			ArrayList<Node> nodeList = getRootNode().getGrandChildNode(node.getNodeName());
			for(Node node_i : nodeList ){
				if (node_i.getParentNode().getNodeName().equalsIgnoreCase("$"))
					continue; // It takes at least two items to make a pattern
				pattBasis.addNewPattBaseBranch(node_i.getNodePath());				
			}			
			condPatternBasisMap.put(node.getNodeName(), pattBasis);
		}
		return condPatternBasisMap;
	}
	public HashMap<Node, ArrayList<Node>> constructCondFpTree(){
		ConditonalFPTree condFpTree = new ConditonalFPTree();
		return condFpTree.processCondPatternBasis();
	}
	private HashMap<String, PatternBasis> condPatternBasisMap = new HashMap<String, PatternBasis>();
	private HashMap<Node, ArrayList<Node>> condPatternsMap = new HashMap<Node, ArrayList<Node>>();
	class PatternBasis{
		public PatternBasis(String condItem){
			this.condItem = condItem;
		}
		private String condItem;
		public String getCondItem(){return condItem;}
		private ArrayList<String> pattBaseBranches = new ArrayList<String>();		
		public ArrayList<String> getPatternBaseBranches(){return pattBaseBranches;}		
		public void addNewPattBaseBranch(String pattBase){
			pattBaseBranches.add(pattBase);
		}		
	}
	class ConditonalFPTree{
		public ConditonalFPTree(){			
			
		}
		private HashMap<Node, ArrayList<Node>> processCondPatternBasis(){
			// For each unique node(item) in the itemsList... 
			for(Node node : uniqueNodesList){										
				ArrayList<Node> freqPatternItems = processPattBasis(condPatternBasisMap.get(node.getNodeName()));
				if (freqPatternItems.size() > 0){
					node.setNodeValue(freqPatternItems.get(0).getNodeValue()); // change node value for drawing purposes/not part of algorithm
					condPatternsMap.put(node, freqPatternItems);
				}
			}
			textArea.append("\n\n\t\t****** CONDITIONAL FPTree (Association Rules)******\n");
			Set<Node> keys = condPatternsMap.keySet();
			int i = 1;
			for(Node node: keys){
				textArea.append(i++ + ". " + node.getNodeName() + "\t");
				for (Node nodePatt: condPatternsMap.get(node)){
					textArea.append(nodePatt.getNodeName()+":"+nodePatt.getNodeValue()+ "  ");
				}
				textArea.append("\n");
			}
			return condPatternsMap;
		}
		private int processCondItemFreq(String pattBaseStr){			
			short endIndex = (short) pattBaseStr.indexOf("}");
			return Integer.parseInt(pattBaseStr.substring(1, endIndex));		
		}
		private ArrayList<Node> processPattBasis(PatternBasis pattBase){
			ArrayList<Node> freqPatternItems = new ArrayList<Node>();			
			if (pattBase.getPatternBaseBranches().size() == 0)
				return freqPatternItems;	
			StringTokenizer st;
			if (pattBase.getPatternBaseBranches().size() == 1){//Only one branch captures whole pattern
				String pattBaseStr = pattBase.getPatternBaseBranches().get(0);
				int condItemFreq = processCondItemFreq(pattBaseStr);
				pattBaseStr = pattBaseStr.substring(pattBaseStr.indexOf("}")+1);
				st = new StringTokenizer(pattBaseStr, "-->");
				while(st.hasMoreTokens()){
					String strWord = st.nextToken();
					if (strWord.equalsIgnoreCase("$") || strWord.equalsIgnoreCase(pattBase.getCondItem()))
						continue; // Ignore self & root
					Node newNode = new Node();
					newNode.setNodeName(strWord);
					newNode.setNodeValue(condItemFreq);
					freqPatternItems.add(newNode);
				}
			}else{//More than one branches capture the pattern
				int condItemFreq = 0;
				Map<String, Node> nodesMap = new HashMap<String, Node>();
				for(String pattBase_i: pattBase.getPatternBaseBranches()){
					condItemFreq = processCondItemFreq(pattBase_i);
					pattBase_i = pattBase_i.substring(pattBase_i.indexOf("}")+1);					
					st = new StringTokenizer(pattBase_i, "-->");
					Node newNode;
					while(st.hasMoreTokens()){
						String strWord = st.nextToken();
						if (strWord.equalsIgnoreCase("$") || strWord.equalsIgnoreCase(pattBase.getCondItem()))
							continue;  // Ignore self & root
						newNode = (Node) nodesMap.get(strWord); 
						if (newNode == null) {
							newNode = new Node();
							newNode.setNodeName(strWord);
							newNode.setNodeValue(condItemFreq);
							nodesMap.put(strWord, newNode); 						
						} else {							
							newNode.setNodeValue(newNode.getNodeValue()+condItemFreq);						
						}
					}
				}
				for(Node node: nodesMap.values()){
					if(node.getNodeValue() >= freqThreshold)
						freqPatternItems.add(node);
				}				
			}
			return freqPatternItems;
		}
	}
}
class ContentItem {
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	private String name = "";
	private String content = "";
	private String author = "";
}
