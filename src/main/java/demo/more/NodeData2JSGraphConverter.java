package demo.more;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;

import demo.comparator.NodeDataPairSortByCoords;
import demo.node.NodeDataPair;

/**
 * This call converts node data in json format in JS file ready for import to https://github.com/visjs/vis-network
 * @author Vadym
 */
public class NodeData2JSGraphConverter {
	
	public static void createJs(Set<NodeDataPair> nodes) throws IOException, ClassNotFoundException {
		
		Set<NodeDataPair> unknownNodes = new TreeSet<NodeDataPair>(new NodeDataPairSortByCoords());

		Map<String, Long> idByCoordinates = new HashMap<String, Long>();
		Long unknownIdStartFrom = Long.valueOf(nodes.size()+100);
		
		Graph graph = new SingleGraph("Yggdrasil network");
		Layout layout = new SpringBox(false);
		graph.addSink(layout);
		layout.addAttributeSink(graph);
		BetweennessCentrality bcb = new BetweennessCentrality();
		
		for(NodeDataPair n:nodes) {
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length()-1);
			idByCoordinates.put(coords, n.getId());
		}
		for(NodeDataPair n:nodes) {
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length()-1);
			if(coords.equals("")) {
				continue;
			}
			int index = coords.lastIndexOf(' ');
			if(index<0) {
				continue;
			}
			String from = coords.substring(0, index);
			Long idFrom = idByCoordinates.get(from);
			//skip null. it occurs when parent coords are unknown
			if(idFrom==null) {
				//special condition for unknown parent nodes
				String recoverCoords = "["+from+"]";
				NodeDataPair nodeDataPair = new NodeDataPair(null, new NodeData(null, recoverCoords));
				nodeDataPair.setId(unknownIdStartFrom);
				idByCoordinates.put(from, unknownIdStartFrom);
				unknownNodes.add(nodeDataPair);
				unknownIdStartFrom++;
			}
		}
		nodes.addAll(unknownNodes);
		for(NodeDataPair n:nodes) {
			graph.addNode(n.getId().toString());
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length()-1);
			if(coords.equals("")) {
				continue;
			}
			int index = coords.lastIndexOf(' ');
			if(index<0) {
				String fromId = idByCoordinates.get("").toString();
				String toId =  idByCoordinates.get(coords).toString();
				graph.addEdge(fromId+"-"+toId, fromId, toId);
				continue;
			}
			
			String from = coords.substring(0, index);
			Long idFrom = idByCoordinates.get(from);
			//skip null. it occurs when parent coords are unknown
			if(idFrom!=null) {
				String fromId = idFrom.toString();
				String toId =  idByCoordinates.get(coords).toString();
				String edgeId = fromId+"-"+toId;
				if(graph.getEdge(edgeId)==null) {
					graph.addEdge(edgeId, fromId, toId);
				}
			}
		}
		bcb.init(graph);
		bcb.compute();
		
		// iterate the compute() method a number of times
		while(layout.getStabilization() < 0.9){
		    layout.compute();
		}
		  
		//StringBuilder sb = new StringBuilder();
		StringBuilder nodesSb = new StringBuilder();
		StringBuilder edgesSb = new StringBuilder();
		String beginNodes = "var nodes = [\n";
		String rowNodes = "{id: %d, label: \"%s\", title: \"%s\", value: %d, group: %d, x: %.2f, y: %.2f},\n";
		String rowEdges = "{from: %d, to: %d, value: %d},\n";
		String endNodes = "];\n";
		String beginEdges = "var edges = [\n";
		String endEdges = "];";
		nodesSb.append(beginNodes);
		edgesSb.append(beginEdges);
		
		for(NodeDataPair n:nodes) {
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length()-1);
			int group = 0;
			if(!coords.equals("")) {
				group = coords.split(" ").length;
			}
			long value = Double.valueOf(graph.getNode(n.getId().toString()).getAttribute("Cb").toString()).longValue()+5;
			double[] coordinates = GraphPosLengthUtils.nodePosition(graph, n.getId().toString());
			nodesSb.append(String.format(Locale.ROOT, rowNodes, n.getId(), coords, n.getIp(), value, group, 100*coordinates[0], 100*coordinates[1]));

			if(coords.equals("")) {
				continue;
			}
			int index = coords.lastIndexOf(' ');
			if(index<0) {
				edgesSb.append(String.format(rowEdges, idByCoordinates.get(""), idByCoordinates.get(coords), value));
				continue;
			}
			
			String from = coords.substring(0, index);
			Long idFrom = idByCoordinates.get(from);
			//skip null. it occurs when parent coords are unknown
			if(idFrom!=null) {
				edgesSb.append(String.format(rowEdges, idFrom, idByCoordinates.get(coords), value));
			}
		}
		
		nodesSb.append(endNodes);
		edgesSb.append(endEdges);
		
		try (Writer writer = new FileWriter("graph-data.js")) {
			writer.append(nodesSb.toString());
			writer.append(edgesSb.toString());
		}
		
	}
	
	public static void main(String[] args) {
		String coords = "1 2";
		int index = coords.lastIndexOf(' ');
		System.out.println(coords.substring(0, index));
		System.out.println(coords.substring(0, index).length());
		System.out.println(coords.substring(index+1));
		int value;
		int group = 3; 
		value = (int) (128/Math.pow(2,group));
		System.out.println(value);
	}
	
	public static final String STYLE = "node {" + "fill-mode: dyn-plain;"
			+ "fill-color: blue,yellow;" + "size-mode: dyn-size;"
			+ "stroke-color: black;" + "stroke-width: 1px;"
			+ "stroke-mode: plain;" + "}";
	
}
