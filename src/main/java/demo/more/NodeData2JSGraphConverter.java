package demo.more;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import demo.node.NodeDataPair;

/**
 * This call converts node data in json format in JS file ready for import to https://github.com/visjs/vis-network
 * @author Vadym
 */
public class NodeData2JSGraphConverter {
	
	public static void createJs(Set<NodeDataPair> nodes) throws IOException {

		Map<String, Long> idByCoordinates = new HashMap<String, Long>();
		
		StringBuilder sb = new StringBuilder();
		String beginNodes = "var nodes = [\n";
		String rowNodes = "{id: %d, label: \"%s\", title: \"%s\", value: %d, group: %d},\n";
		String rowEdges = "{from: %d, to: %d},\n";
		String endNodes = "];\n";
		sb.append(beginNodes);
		
		for(NodeDataPair n:nodes) {
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length()-1);
			int group = coords.split(" ").length;
			int value;
			if(group>=20) {
				value = 10;
			} else {
				value = 30-group;
			}
			sb.append(String.format(rowNodes, n.getId(), n.getNodeData().getBox_pub_key(), coords, value, group));
			idByCoordinates.put(coords, n.getId());
		}
		sb.append(endNodes);
		String beginEdges = "var edges = [\n";
		sb.append(beginEdges);
		for(NodeDataPair n:nodes) {
			String coords = n.getNodeData().getCoords().substring(1, n.getNodeData().getCoords().length()-1);
			if(coords.equals("")) {
				continue;
			}
			int index = coords.lastIndexOf(' ');
			if(index<0) {
				sb.append(String.format(rowEdges, idByCoordinates.get(""), idByCoordinates.get(coords)));
				continue;
			}
			
			String from = coords.substring(0, index);
			Long idFrom = idByCoordinates.get(from);
			//skip null. it occurs when parent coords are unknown
			if(idFrom!=null) {
				sb.append(String.format(rowEdges, idFrom, idByCoordinates.get(coords)));
			}
		}
		String endEdges = "];";
		sb.append(endEdges);
		
		try (Writer writer = new FileWriter("graph-data.js")) {
		    writer.append(sb.toString());
		}
	}
	
	public static void main(String[] args) {
		String coords = "1 2";
		int index = coords.lastIndexOf(' ');
		System.out.println(coords.substring(0, index));
		System.out.println(coords.substring(0, index).length());
		System.out.println(coords.substring(index+1));
		
	}
	
}
