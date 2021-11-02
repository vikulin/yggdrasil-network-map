package org.riv;

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
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.riv.node.Link;
import org.riv.node.NodeDataPair;

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
			node.setAttribute("icon", nodeEntry.getValue().getIcon());
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
		//String testIcon=" data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAACXBIWXMAAAsSAAALEgHS3X78AAAPPUlEQVR42u2beYxW1RnGD8swwLAIM4DA4LAoIAYIyiIUBMZgiEFqI9YaQoKt2hQNaaMkJpKQRhYTp/xR/9DQNNgiAUwaDVYgCso6soyyOWwDiOyIgCA7zJw+v5Pv/Xrn+g0wnTugKSe5ud/c5Zz3fc67POc9d5y73f6/W52bOdipU6danD9/PvfixYstWrdunbV37153zz33uG+++cbdcccdxy9cuHCujlqLFi3cwYMH3X333ee//PLLMw888MDZnwUA3nu3b9++BhUVFc3z8vIKjh492rdjx459vv3225Y5OTl5eqSznmlRt27detLTlZeXu/r164ez2hUdV/mt9129evWcAPHNmjU79d1335Xp/IGurxQwh5IEpEYArFixounAgQOb7Ny5s09+fv5QXRp49erVFlIqt2HDhiicpXMAhoZi9pszINg5CJP6265dvnzZZWdnu3PnzrkGDRo4Wc65K1euHBZIH+fm5r47ePDg9WoVNxUACdfs+++/f16mPEIz2U0z01iCtmrevHlaMRozab9rPEspYDRe+M1ZY1754Ycfik+fPv1PWcu/OnXqdPqmACBz/E2jRo3mIZDNrh213eLWkpWVhZu4S5culcgyJiqufF7dPutW5+HPPvssSwP+jhnAJGXulcy61iN2nTqVwJDiIYY0bty4r8CYe+zYsf61CsCdd975sGZ9OAEKxaMC3ZIUpvGRgyaZOgmMRXKLsbUGwF133TVG0b3eT0H5eHwgWOqce/LkyWmy0laJA6DOGyrw9E2lrIyC1DYo8THM9QyEJk2auKZNmxbs2rXr4cQB2LRpUy8p391MLprGCESKxu7MmTPhNyAlBYaNoRToZN5hHGWgMAaxKB6AFQ/c3Xff/eTSpUtvSLf6NypI586dh6nzbLMABCM/KytUUhqhiBHKFA5Gh0DxCH6jkZ4gp5Trzp49WyngMgYmT/+Wfk0mnpNMPfr27dtIl84lBoBmIEfmlUYe5Tdv3uxWr17ttm7dGkDABAWU69Onj+vevXt4DuERkqxxPRDsPkow0xAhpTd34MABJ0rsduzYEQABYCj0oEGDXP/+/V3Lli3TlkBWID0fPnw4WX8Uqn/RDHjNipfyfubMmb59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";
		String rowNodesIcons = "{id: %s, label: \"%s\", title: \"%s\", value: %d, group: %d, x: %.2f, y: %.2f, shape: \"circularImage\", image: \"%s\"},\n";
		String rowEdges = "{from: %s, to: %s, width: %.2f},\n";
		String endNodes = "];\n";
		String beginEdges = "var edges = [\n";
		String endEdges = "];\n";
		String generated = "var generated = %d;\n";
		String nodesNumber = "var nodesNumber = %d;\n";
		String linksNumber = "var linksNumber = %d;\n";
		
		String versionMapStart = "(function() {\n"
				+ "	window.demo = {};\n"
				+ "	window.demo.data = [];\n"
				+ "var data = getData();\n"
				+ "for(var t=0;t<1;t++) {\n"
				+ "for(var j = 0;j < data.length; j++) {\n"
				+ "window.demo.data.push(data[j]);\n"
				+ "}\n"
				+ "}\n"
				+ "function getData() {\n"
				+ "return [\n";
		//ip, name, version, os, arch
		String versionMapValue = "['%s', '%s', '%s', '%s', '%s'],\n";
		String versionMapEnd = "];\n"
				+ "}\n"
				+ "}());";
		
				float width = 0.7f;
		int edgesCount = graph.getEdgeCount();
		try (Writer statistic = new FileWriter(new File(dataPath,"statistic-peer-data.js"))) {
			statistic.append(versionMapStart);
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
					os="unknown";
				}
				Object arch = node.getAttribute("arch");
				if(arch==null) {
					arch="unknown";
				}
				Object version = node.getAttribute("version");
				if(version==null) {
					version="unknown";
				}
				String label = ip.toString().substring(ip.toString().lastIndexOf(':') + 1);
				Object name = node.getAttribute("name");
				if(name==null) {
					name=StringEscapeUtils.escapeEcmaScript(label);
				} else {
					name=StringEscapeUtils.escapeEcmaScript(name.toString());
				}
				Object icon = node.getAttribute("icon");
				
				//ip, name, version, os, arch
				//String versionMapValue = "['%s', '%s', '%s', '%s', '%s'],\n";
				statistic.append(String.format(Locale.ROOT, versionMapValue, ip, name, version, os, arch));
				
				long value = Double.valueOf(node.getAttribute("Cb").toString()).longValue()+5;
				long group = value;
				double[] coordinates = GraphPosLengthUtils.nodePosition(graph, node.getId());
				//String title = ip+"\\n"+os+" "+arch+" "+version;
				String title = ip.toString();
				if(icon==null) {
					writer.append(String.format(Locale.ROOT, rowNodes, node.getId(), name, title, value, group, 100*coordinates[0], 100*coordinates[1]));
				} else {
					String str = String.format(Locale.ROOT, rowNodesIcons, node.getId(), name, title, value, group, 100*coordinates[0], 100*coordinates[1], icon);
					writer.append(str);
				}
			}
			
			statistic.append(versionMapEnd);
			writer.append(endNodes);
			writer.append(String.format(generated, new Date().getTime()));
			writer.append(String.format(nodesNumber, graph.getNodeCount()));
			writer.append(String.format(linksNumber, graph.getEdgeCount()));
			//graph.display(true);
		}
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
