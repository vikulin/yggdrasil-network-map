package demo.more;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import demo.node.NodeDataPair;

/**
 * This call converts node data in json format in JS file ready for import to https://github.com/visjs/vis-network
 * @author Vadym
 */
public class NodeData2JSGraphConverter {
	
	private List<EdgeData> edgeData = new ArrayList<EdgeData>();
	
	private Map<String, Long> coordinatesById = new HashMap<String, Long>();
	
	public void createJs(Set<NodeDataPair> nodes) {
		StringBuilder sb = new StringBuilder();
		String beginNodes = "var nodes = [\n";
		String rowNodes = "{id: %d, label: %s, title: %s, value: %d, group: %d},\n";
		String rowEdges = "{from: %d, to: %d},\n";
		String endNodes = "];\n";
		sb.append(beginNodes);
		
		for(NodeDataPair n:nodes) {
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length());
			int group = coords.split(" ").length;
			int value;
			if(group>=20) {
				value = 10;
			} else {
				value = 30-group;
			}
			sb.append(String.format(rowNodes, n.getId(), n.getNodeData().getBox_pub_key(), coords, value, group));
			coordinatesById.put(coords, n.getId());
		}
		sb.append(endNodes);
		String beginEdges = "var edges = [\n";
		sb.append(beginEdges);
		for(NodeDataPair n:nodes) {
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length());
			int index = coords.lastIndexOf(' ');
			if(index<0 && !coords.equals("")) {
				sb.append(String.format(rowEdges, coordinatesById.get(""), coordinatesById.get(coords)));
				continue;
			}
			String from = coords.substring(0, index);
			sb.append(String.format(rowEdges, coordinatesById.get(from), coordinatesById.get(coords)));
		}
		String endEdges = "];";
		sb.append(endEdges);
	}
	
	public static void main(String[] args) {
		int index = "1 2 3 4".lastIndexOf(' ');
		System.out.println("1 2 3 4".substring(index+1));
		System.out.println("1 2 3 4".substring(0, index));
	}
	
}
