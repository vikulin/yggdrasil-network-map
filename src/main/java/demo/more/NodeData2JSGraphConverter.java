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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;

import demo.node.NodeDataPair;

/**
 * This call converts node data in json format in JS file ready for import to https://github.com/visjs/vis-network
 * @author Vadym
 */
public class NodeData2JSGraphConverter {
	
	private static GraphModel graphModel;
	private static AppearanceController appearanceController;
	private static AppearanceModel appearanceModel;
	private static Workspace workspace;

	static {
		 //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get and model
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        appearanceModel = appearanceController.getModel();
	}
	
	public static void createJs(Map<String, NodeDataPair> nodes, Set<Link> links, String dataPath) throws IOException, ClassNotFoundException {
		
		UndirectedGraph graph = graphModel.getUndirectedGraph();
		
		long id=0;
        for (Column col : graphModel.getNodeTable()) {
            System.out.println(col);
        }
		for(Entry<String, NodeDataPair> nodeEntry:nodes.entrySet()) {
			id++;
			nodeEntry.getValue().setId(id);
			String nodeId = id+"";
			Node node = graphModel.factory().newNode(nodeId);
			String ip = nodeEntry.getValue().getIp();
			if(ip==null) {
				System.err.println("ip is null for node:"+nodeId);
				
			}
			if(!node.getAttributeKeys().contains("ip")) {
				Column ipc = graphModel.getNodeTable().addColumn("ip", String.class);
				node.setAttribute(ipc, ip);
			} else {
				Column ipc = graphModel.getNodeTable().getColumn("ip");
				node.setAttribute(ipc, ip);
			}
			
			if(!node.getAttributeKeys().contains("key")) {
				Column ipc = graphModel.getNodeTable().addColumn("key", String.class);
				node.setAttribute(ipc, nodeEntry.getKey());
			} else {
				Column ipc = graphModel.getNodeTable().getColumn("key");
				node.setAttribute(ipc, nodeEntry.getKey());
			}
			
			if(!node.getAttributeKeys().contains("os")) {
				Column ipc = graphModel.getNodeTable().addColumn("os", String.class);
				node.setAttribute(ipc, nodeEntry.getValue().getPlatform());
			} else {
				Column ipc = graphModel.getNodeTable().getColumn("os");
				node.setAttribute(ipc, nodeEntry.getValue().getPlatform());
			}
			
			if(!node.getAttributeKeys().contains("arch")) {
				Column ipc = graphModel.getNodeTable().addColumn("arch", String.class);
				node.setAttribute(ipc, nodeEntry.getValue().getArch());
			} else {
				Column ipc = graphModel.getNodeTable().getColumn("arch");
				node.setAttribute(ipc, nodeEntry.getValue().getArch());
			}
			
			if(!node.getAttributeKeys().contains("version")) {
				Column ipc = graphModel.getNodeTable().addColumn("version", String.class);
				node.setAttribute(ipc, nodeEntry.getValue().getVersion());
			} else {
				Column ipc = graphModel.getNodeTable().getColumn("version");
				node.setAttribute(ipc, nodeEntry.getValue().getVersion());
			}
			
			if(!node.getAttributeKeys().contains("name")) {
				Column ipc = graphModel.getNodeTable().addColumn("name", String.class);
				node.setAttribute(ipc, nodeEntry.getValue().getName());
			} else {
				Column ipc = graphModel.getNodeTable().getColumn("name");
				node.setAttribute(ipc, nodeEntry.getValue().getName());
			}
			graph.addNode(node);
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
				Node n1 = graph.getNode(ndp.getId()+"");
				Node n2 = graph.getNode(toId+"");
				Edge e1 = graphModel.factory().newEdge(n1, n2, 0, 1.0, false);
				graph.addEdge(e1);
				System.out.println("added adge:"+edgeId);
			} else {
				String reversedEdgeId = toId+"-"+ndp.getId();
				if(graph.getEdge(reversedEdgeId)!=null) {
					continue;
				}
				Node n1 = graph.getNode(toId+"");
				Node n2 = graph.getNode(ndp.getId()+"");
				Edge e1 = graphModel.factory().newEdge(n1, n2, 0, 1.0, false);
				graph.addEdge(e1);
				System.out.println("added adge:"+reversedEdgeId);
			}
		}
		int nodesCount = graph.getNodeCount();
		System.out.println("nodesCount:"+nodesCount);
		System.out.println("edgesCount:"+graph.getEdgeCount());

		ForceAtlas2Builder lb = new ForceAtlas2Builder();
		ForceAtlas2 layout = new ForceAtlas2(lb);
		layout.resetPropertiesValues();
		layout.setGravity(20d);
		layout.setLinLogMode(true);
		layout.setScalingRatio(30d);
		layout.setThreadsCount(11);
		layout.setBarnesHutTheta(0.5d);
		layout.setEdgeWeightInfluence(1.0d);
		//layout.setAdjustSizes(true);
        layout.setGraphModel(graphModel);
        
        layout.initAlgo();
        for (int i = 0; i < 1200 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
        
        //Get Centrality
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);
        
      //Rank size by centrality
        Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn, RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking.getTransformer();
        centralityTransformer.setMinSize(3);
        centralityTransformer.setMaxSize(10);
        appearanceController.transform(centralityRanking);
        
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
			
		try (Writer writer = new FileWriter(new File(dataPath,"graph-data.js"))) {
			//writer.append(preTitle);
			writer.append(beginEdges);
			Iterator<Edge> edges = graph.getEdges().iterator();
			while(edges.hasNext()) {
				Edge edge = edges.next();
				String edgeString = String.format(Locale.ROOT, rowEdges, edge.getSource().getId(), edge.getTarget().getId(), width);
				System.out.println(edgeString);
				writer.append(edgeString);
			}
			
			writer.append(endEdges);
			writer.append(beginNodes);
			Iterator<Node> nodeIt = graph.getNodes().iterator();
			while(nodeIt.hasNext()) {
				Node node = nodeIt.next();
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
				
				long value = Double.valueOf(node.getAttribute(centralityColumn).toString()).longValue();
				long group = value;
				//double[] coordinates = GraphPosLengthUtils.nodePosition(graph, node.getId());
				//String title = ip+"\\n"+os+" "+arch+" "+version;
				float x = node.x();
				float y = node.y();
				String title = ip.toString();
				if(name==null) {
					writer.append(String.format(Locale.ROOT, rowNodes, node.getId(), label, title, value, group, x, y));
				} else {
					writer.append(String.format(Locale.ROOT, rowNodes, node.getId(), name, title, value, group, x, y));
				}
			}
			writer.append(endNodes);
			writer.append(String.format(generated, new Date().getTime()));
			writer.append(String.format(nodesNumber, graph.getNodeCount()));
			writer.append(String.format(linksNumber, graph.getEdgeCount()));
			
			 //Export full graph
	        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
	        try {
	            ec.exportFile(new File("io_gexf.gexf"));
	        } catch (IOException ex) {
	            ex.printStackTrace();
	            return;
	        }
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
