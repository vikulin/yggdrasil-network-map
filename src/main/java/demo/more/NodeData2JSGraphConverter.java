package demo.more;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;

import demo.node.NodeDataPair;

/**
 * This call converts node data in json format in JS file ready for import to https://github.com/visjs/vis-network
 * @author Vadym
 */
public class NodeData2JSGraphConverter {
	
	public static void createJs(Map<String, NodeDataPair> nodes, Set<Link> links, String dataPath) throws IOException, ClassNotFoundException {
		
		Graph graph = new SingleGraph("Yggdrasil network");
		Layout layout = new SpringBox(false);
		graph.addSink(layout);
		graph.setStrict(true);
		layout.addAttributeSink(graph);
		BetweennessCentrality bcb = new BetweennessCentrality();
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			String nodeId = nodeEntry.getValue().getId()+"";
			Node node = graph.addNode(nodeId);
			String ip = nodeEntry.getValue().getIp();
			if(ip==null) {
				System.err.println("ip is null for node:"+nodeId);
				
			}
			node.setAttribute("ip", ip);
			node.setAttribute("key", nodeEntry.getKey());
		}
		for(Link l:links) {
			String key = l.getKey();
			String ip = l.getIp();
			NodeDataPair ndp = nodes.get(key);
			if(ndp==null) {
				continue;
			}
			Optional<Entry<String, NodeDataPair>> element = nodes.entrySet().stream().filter(n->n.getValue().getIp().equals(ip)).findFirst();
			if(element.isEmpty()) {
				continue;
			}
			Long toId = element.get().getValue().getId();
			if(ndp.getId().longValue()==toId.longValue()) {
				//skip self links
				continue;
			}
			String edgeId = ndp.getId()+"-"+toId;
			System.out.println("added adge:"+edgeId);
			try {
				graph.addEdge(edgeId , ndp.getId()+"", toId+"");
			} catch (EdgeRejectedException e1) {
				System.err.println("an existing edge");
			}
		}
		int nodesCount = graph.getNodeCount();
		System.out.println("nodesCount:"+nodesCount);
		System.out.println("edgesCount:"+graph.getEdgeCount());
		bcb.init(graph);
		bcb.compute();
		
		// iterate the compute() method a number of times
		while(layout.getStabilization() < 0.92){
		    layout.compute();
		}
		  
		StringBuilder nodesSb = new StringBuilder();
		StringBuilder edgesSb = new StringBuilder();
		String beginNodes = "var nodes = [\n";
		String rowNodes = "{id: %s, label: \"%s\", title: \"%s\", value: %d, group: %d, x: %.2f, y: %.2f},\n";
		String rowEdges = "{from: %s, to: %s, width: %.2f},\n";
		String endNodes = "];\n";
		String beginEdges = "var edges = [\n";
		String endEdges = "];\n";
		String generated = "var generated = %d;\n";
		String nodesNumber = "var nodesNumber = %d;\n";
		String linksNumber = "var linksNumber = %d;";
		nodesSb.append(beginNodes);
		
		Iterator<Node> nodeIt = graph.iterator();
		while(nodeIt.hasNext()) {
			Node node = nodeIt.next();
			Object ip = node.getAttribute("ip");
			if(ip==null) {
				ip=":";
			}
			String label = ip.toString().substring(ip.toString().lastIndexOf(':') + 1);
			long value = Double.valueOf(node.getAttribute("Cb").toString()).longValue()+5;
			long group = value;
			double[] coordinates = GraphPosLengthUtils.nodePosition(graph, node.getId());
			nodesSb.append(String.format(Locale.ROOT, rowNodes, node.getId(), label, ip, value, group, 100*coordinates[0], 100*coordinates[1]));
		}
		
		float width = 0.3f;
		
		int edgesCount = graph.getEdgeCount();	
		
		try (Writer writer = new FileWriter(new File(dataPath,"graph-data.js"))) {
			writer.append(beginEdges);
			for(int index = 0; index < edgesCount; index++) {
				Edge edge = graph.getEdge(index);
				writer.append(String.format(rowEdges, edge.getNode0().getId(), edge.getNode1().getId(), width));
			}
			writer.append(endEdges);
			writer.append(nodesSb.toString());
			writer.append(endNodes);
			
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
	}
}
