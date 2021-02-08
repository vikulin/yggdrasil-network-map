package demo.more;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.ElementNotFoundException;
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
	
	public static void createJs(Set<NodeDataPair> nodes, String dataPath) throws IOException, ClassNotFoundException {
		
		Set<NodeDataPair> unknownNodes = new HashSet<NodeDataPair>();

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
			
			String[] array = from.split(" ");
			List<String> list = Arrays.asList(array);
			try {
				for(int i=0;i<list.size();i++) {
					String parentCoords = list.subList(0, list.size()-i).stream().collect(Collectors.joining(" "));
					Long idFrom = idByCoordinates.get(parentCoords);
					//skip null. it occurs when parent coords are unknown
					if(idFrom==null) {
						//special condition for unknown parent nodes
						String recoverCoords = "["+parentCoords+"]";
						NodeDataPair nodeDataPair = new NodeDataPair(null, new NodeData(null, recoverCoords));
						nodeDataPair.setId(unknownIdStartFrom);
						idByCoordinates.put(parentCoords, unknownIdStartFrom);
						unknownNodes.add(nodeDataPair);
						unknownIdStartFrom++;
					}
				}
			} catch (Exception e) {
				System.out.println("from:"+from);
				e.printStackTrace();
			}
		}
		nodes.addAll(unknownNodes);
		//sort out here
		for(NodeDataPair n:nodes) {
			graph.addNode(n.getId().toString());
		}
		for(NodeDataPair n:nodes) {
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
					try {
						graph.addEdge(edgeId, fromId, toId);
					} catch (ElementNotFoundException e) {
						e.printStackTrace();
						System.out.println("fromId="+fromId+" toId="+toId);
					}
				}
			}
		}
		bcb.init(graph);
		bcb.compute();
		
		// iterate the compute() method a number of times
		while(layout.getStabilization() < 0.92){
		    layout.compute();
		}
		  
		StringBuilder nodesSb = new StringBuilder();
		StringBuilder edgesSb = new StringBuilder();
		String beginNodes = "var nodes = [\n";
		String rowNodes = "{id: %d, label: \"%s\", title: \"%s\", value: %d, group: %d, x: %.2f, y: %.2f},\n";
		String rowEdges = "{from: %d, to: %d, value: %d},\n";
		String endNodes = "];\n";
		String beginEdges = "var edges = [\n";
		String endEdges = "];\n";
		String generated = "var generated = %d;\n";
		String nodesNumber = "var nodesNumber = %d;\n";
		String linksNumber = "var linksNumber = %d;";
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
		
		try (Writer writer = new FileWriter(new File(dataPath,"graph-data.js"))) {
			writer.append(nodesSb.toString());
			writer.append(edgesSb.toString());
			writer.append(String.format(generated, new Date().getTime()));
			writer.append(String.format(nodesNumber, graph.getNodeCount()));
			writer.append(String.format(linksNumber, graph.getEdgeCount()));
		}
		
	}
	
	public static void main(String[] args) {
		String coords = "1 2 3";
		
		int index = coords.lastIndexOf(' ');
		if(index>0) {
			String[] array = coords.split(" ");
			List<String> list = Arrays.asList(array);
			for(int i=0;i<list.size();i++) {
				System.out.println("["+list.subList(0, list.size()-i).stream().collect(Collectors.joining(" "))+"]");
				//System.out.println(coords.substring(0, index));
				//System.out.println(coords.substring(0, index).length());
				//System.out.println(coords.substring(index+1));
			}
		}
		//int value;
		//int group = 3; 
		//value = (int) (128/Math.pow(2,group));
		//System.out.println(value);
	}
}
