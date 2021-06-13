package demo.more;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
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
			graph.addNode(nodeEntry.getValue().getId()+"");
		}
		for(Link l:links) {
			String key = l.getKey();
			String ip = l.getIp();
			NodeDataPair ndp = nodes.get(key);
			if(ndp==null) {
				continue;
			}
			Long toId = nodes.entrySet().stream().filter(n->n.getValue().getIp().equals(ip)).findFirst().get().getValue().getId();
			String edgeId = ndp.getId()+"-"+toId;
			try {
				graph.addEdge(edgeId , ndp.getId()+"", toId+"");
			} catch (EdgeRejectedException e1) {
				System.err.println("an existing edge");
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
		
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			
			String ip = nodeEntry.getValue().getIp();
			String label = ip.substring(ip.lastIndexOf(':') + 1);
			long value = Double.valueOf(graph.getNode(nodeEntry.getValue().getId().toString()).getAttribute("Cb").toString()).longValue()+5;
			long group = value;
			double[] coordinates = GraphPosLengthUtils.nodePosition(graph, nodeEntry.getValue().getId().toString());
			nodesSb.append(String.format(Locale.ROOT, rowNodes, nodeEntry.getValue().getId(), label, ip, value, group, 100*coordinates[0], 100*coordinates[1]));
		}
		
		int value = 1;
		
		for(Link l:links) {
			String key = l.getKey();
			String ip = l.getIp();
			NodeDataPair ndp = nodes.get(key);
			if(ndp==null) {
				continue;
			}
			Long toId = nodes.entrySet().stream().filter(n->n.getValue().getIp().equals(ip)).findFirst().get().getValue().getId();
			String edgeId = ndp.getId()+"-"+toId;
			if(graph.getEdge(edgeId)!=null) {
				continue;
			}
			//Long toId = nodes.entrySet().stream().filter(n->n.getValue().getIp().equals(ip)).findFirst().get().getValue().getId();
			
			edgesSb.append(String.format(rowEdges, ndp.getId(), toId, value));
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
	}
}
