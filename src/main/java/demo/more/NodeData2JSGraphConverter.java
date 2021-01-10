package demo.more;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This call converts node data in json format in JS file ready for import to https://github.com/visjs/vis-network
 * @author Vadym
 */
public class NodeData2JSGraphConverter {
	
	private List<EdgeData> nodesByCoordinates = new ArrayList<EdgeData>();
	
	public void createJs(Map<String, NodeData> nodes) {
		StringBuilder sb = new StringBuilder();
		String beginNodes = "var nodes = [\n";
		String rowNodes = "{id: %d, label: %s, title: %s, value: %d, group: %d},\n";
		String rowEdges = "{from: %d, to: %d},\n";
		String endNodes = "];\n";
		sb.append(beginNodes);
		
		for(Entry<String, NodeData> n:nodes.entrySet()) {
			String coords = n.getValue().getCoords();
			String coordString = "";
			if(coords.equals("[]")) {
				coordString = "0";
			} else {
				coordString = "0 "+coords.substring(1, coords.length()-1);
			}
			int group = n.getValue().getCoords().split(" ").length;
			int value;
			if(group>=20) {
				value = 10;
			} else {
				value = 30-group;
			}
			
			sb.append(String.format(rowNodes, coordString, n.getKey(), coords, value, group));
			//put all nodes in set by coords
			
			int index = coordString.lastIndexOf(' ');
			String from = coordString.substring(0, index);
			String to = coordString.substring(index+1);
			nodesByCoordinates.add(new EdgeData(from, to));
		}
		sb.append(endNodes);
		String beginEdges = "var edges = [\n";
		sb.append(beginEdges);
		for(EdgeData ed:nodesByCoordinates) {
			ed.getFrom();
			
		}
		String endEdges = "];";
	}
	
	public static void main(String[] args) {
		int index = "boo and foo".lastIndexOf(' ');
		System.out.println("boo and foo".substring(index+1));
		System.out.println("boo and foo".substring(0, index));
	}
	
}
