package demo.more;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
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
	
	public static void createPeerGraphJs(Map<String, NodeDataPair> nodes, Set<Link> links, String dataPath) throws IOException, ClassNotFoundException {
		
		Graph graph = new SingleGraph("RiV-mesh network");
		Layout layout = new SpringBox(false, new Random(100001));
		graph.addSink(layout);
		graph.setStrict(true);
		layout.addAttributeSink(graph);

		BetweennessCentrality bcb = new BetweennessCentrality();
		long id=0;
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			id++;
			nodeEntry.getValue().setId(id);
			String nodeId = id+"";
			Node node = graph.addNode(nodeId);
			String ip = nodeEntry.getValue().getIp();
			if(ip==null) {
				System.err.println("ip is null for node:"+nodeId);
				
			}
			node.setAttribute("ip", ip);
			node.setAttribute("key", nodeEntry.getKey());
			node.setAttribute("coords", nodeEntry.getValue().getCoords());
			node.setAttribute("os", nodeEntry.getValue().getPlatform());
			node.setAttribute("arch", nodeEntry.getValue().getArch());
			node.setAttribute("version", nodeEntry.getValue().getVersion());
			node.setAttribute("name", nodeEntry.getValue().getName());
		}
		for(Link l:links) {
			String keyFrom = l.getKeyFrom();
			String keyTo = l.getKeyTo();
			NodeDataPair ndp = nodes.get(keyFrom);
			if(ndp==null) {
				continue;
			}
			Optional<Entry<String, NodeDataPair>> element = nodes.entrySet().stream().filter(n->(n.getValue().getKey()!=null && n.getValue().getKey().equals(keyTo))).findFirst();
			if(element.isEmpty()) {
				continue;
			}
			Long toId = element.get().getValue().getId();
			if(ndp.getId().longValue()==toId.longValue()) {
				//skip self links
				continue;
			}
			if(ndp.getId()<toId) {
				String edgeId = ndp.getId()+"-"+toId;
				if(graph.getEdge(edgeId)!=null) {
					continue;
				}
				Edge e = graph.addEdge(edgeId , ndp.getId()+"", toId+"");
				e.setAttribute("layout.weight", 5);
				System.out.println("added adge:"+edgeId);
			} else {
				String reversedEdgeId = toId+"-"+ndp.getId();
				if(graph.getEdge(reversedEdgeId)!=null) {
					continue;
				}
				Edge e = graph.addEdge(reversedEdgeId , toId+"", ndp.getId()+"");
				e.setAttribute("layout.weight", 5);
				System.out.println("added adge:"+reversedEdgeId);
			}
			
			/*try {
				graph.addEdge(edgeId , ndp.getId()+"", toId+"");
			} catch (EdgeRejectedException e1) {
				System.err.println("an existing edge");
			}*/
		}
		int nodesCount = graph.getNodeCount();
		System.out.println("nodesCount:"+nodesCount);
		System.out.println("edgesCount:"+graph.getEdgeCount());
		bcb.init(graph);
		bcb.compute();
		layout.setQuality(0.99d);

		// iterate the compute() method a number of times
		while(layout.getStabilization() < 0.93){
		    layout.compute();
		}
		
		String preTitle = "function preTitle(text) {\r\n"
				+ "		  const container = document.createElement(\"pre\");\r\n"
				+ "		  container.innerText = text;\r\n"
				+ "		  return container;\r\n"
				+ "		};\n";
		String beginNodes = "var nodes = [\n";
		String rowNodes = "{id: %s, label: \"%s\", title: \"%s\", value: %d, group: %d, x: %.2f, y: %.2f},\n";
		String rowEdges = "{from: %s, to: %s, width: %.2f},\n";
		String endNodes = "];\n";
		String beginEdges = "var edges = [\n";
		String endEdges = "];\n";
		String generated = "var generated = %d;\n";
		String nodesNumber = "var nodesNumber = %d;\n";
		String linksNumber = "var linksNumber = %d;";
		float width = 0.7f;
		int edgesCount = graph.getEdgeCount();	
		try (Writer writer = new FileWriter(new File(dataPath,"graph-peer-data.js"))) {
			//writer.append(preTitle);
			writer.append(beginEdges);
			for(int index = 0; index < edgesCount; index++) {
				Edge edge = graph.getEdge(index);
				String edgeString = String.format(Locale.ROOT, rowEdges, edge.getNode0().getId(), edge.getNode1().getId(), width);
				System.out.println(edgeString);
				writer.append(edgeString);
			}
			
			writer.append(endEdges);
			writer.append(beginNodes);
			Iterator<Node> nodeIt = graph.iterator();
			while(nodeIt.hasNext()) {
				Node node = nodeIt.next();
				node.setAttribute("layout.weight", 200);
				Object ip = node.getAttribute("ip");
				if(ip==null) {
					ip=":";
				}
				Object os = node.getAttribute("os");
				if(os==null) {
					os="";
				}
				Object arch = node.getAttribute("arch");
				if(arch==null) {
					arch="";
				}
				Object version = node.getAttribute("version");
				if(version==null) {
					version="";
				}
				Object name = node.getAttribute("name");

				String label = ip.toString().substring(ip.toString().lastIndexOf(':') + 1);
				long value = Double.valueOf(node.getAttribute("Cb").toString()).longValue()+5;
				long group = value;
				double[] coordinates = GraphPosLengthUtils.nodePosition(graph, node.getId());
				//String title = ip+"\\n"+os+" "+arch+" "+version;
				String title = ip.toString();
				if(name==null) {
					writer.append(String.format(Locale.ROOT, rowNodes, node.getId(), label, title, value, group, 100*coordinates[0], 100*coordinates[1]));
				} else {
					writer.append(String.format(Locale.ROOT, rowNodes, node.getId(), name, title, value, group, 100*coordinates[0], 100*coordinates[1]));
				}
			}
			writer.append(endNodes);
			writer.append(String.format(generated, new Date().getTime()));
			writer.append(String.format(nodesNumber, graph.getNodeCount()));
			writer.append(String.format(linksNumber, graph.getEdgeCount()));
			//graph.display(true);
		}	
	}
	
public static void createSpanningTreeGraphJs(Map<String, NodeDataPair> nodes, String dataPath) throws IOException, ClassNotFoundException {
		
		Set<NodeDataPair> unknownNodes = new HashSet<NodeDataPair>();

		Map<String, Long> idByCoordinates = new HashMap<String, Long>();
		Long unknownIdStartFrom = Long.valueOf(nodes.size()+100);
		
		Graph graph = new SingleGraph("RiV-mesh network");
		Layout layout = new SpringBox(false, new Random(100001));
		graph.addSink(layout);
		layout.addAttributeSink(graph);
		BetweennessCentrality bcb = new BetweennessCentrality();
		
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			String coords = nodeEntry.getValue().getCoords();
			idByCoordinates.put(coords, nodeEntry.getValue().getId());
		}
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			String coords = nodeEntry.getValue().getCoords();
			if(coords==null || coords.equals("")) {
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
						NodeDataPair nodeDataPair = new NodeDataPair("?", "?");
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
		for(NodeDataPair n:unknownNodes) {
			nodes.put(n.getId()+"?", n);
		}
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			Node node = graph.addNode(nodeEntry.getValue().getId().toString());
			node.setAttribute("layout.weight", 50);
		}
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			String coords = nodeEntry.getValue().getCoords();
			if(coords==null || coords.equals("")) {
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
						Edge e = graph.addEdge(edgeId, fromId, toId);
						e.setAttribute("layout.weight", 3);
					} catch (ElementNotFoundException e) {
						e.printStackTrace();
						System.out.println("fromId="+fromId+" toId="+toId);
					}
				}
			}
		}
		bcb.init(graph);
		bcb.compute();
		
		layout.setQuality(0.99d);

		// iterate the compute() method a number of times
		while(layout.getStabilization() < 0.93){
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
			String coords = nodeEntry.getValue().getCoords();
			if(coords==null) {
				continue;
			}
			int group = 0;
			if(!coords.equals("")) {
				group = coords.split(" ").length;
			}
			long value = Double.valueOf(graph.getNode(nodeEntry.getValue().getId().toString()).getAttribute("Cb").toString()).longValue()+5;
			double[] coordinates = GraphPosLengthUtils.nodePosition(graph, nodeEntry.getValue().getId().toString());
			String ip = nodeEntry.getValue().getIp();
			String label = "";
			if(nodeEntry.getValue().getName()!=null) {
				label = nodeEntry.getValue().getName();
			} else {
				label = ip.toString().substring(ip.toString().lastIndexOf(':') + 1);
			}
			
			nodesSb.append(String.format(Locale.ROOT, rowNodes, nodeEntry.getValue().getId(), label, ip, value, group, 120*coordinates[0], 120*coordinates[1]));

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
		
		try (Writer writer = new FileWriter(new File(dataPath,"graph-tree-data.js"))) {
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
