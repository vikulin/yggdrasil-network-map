package demo.comparator;

import java.util.Comparator;

import demo.node.NodeDataPair;

public class NodeDataPairSortByCoords implements Comparator<NodeDataPair> {

	@Override
	public int compare(NodeDataPair n1, NodeDataPair n2) {
		int delta = n1.getNodeData().getCoords().split(" ").length - n2.getNodeData().getCoords().split(" ").length;
		if(delta!=0) {
			return delta;
		}
		if(n1.getNodeData().getCoords().equals("[]")) {
			return n1.getNodeData().getCoords().compareTo(n2.getNodeData().getCoords());
		} 
		if(n2.getNodeData().getCoords().equals("[]")) {
			return n1.getNodeData().getCoords().compareTo(n2.getNodeData().getCoords());
		}

		String[] array2 = n2.getNodeData().getCoords().substring(1, n2.getNodeData().getCoords().length()-1).split(" ");
		String[] array1 = n1.getNodeData().getCoords().substring(1, n1.getNodeData().getCoords().length()-1).split(" ");
		int size = array2.length;
		for(int index = 0; index<size; index++) {
			int value = Integer.parseInt(array1[index])-Integer.parseInt(array2[index]);
			if(value!=0) {
				return value;
			}
		}
		return 0;
	}
	
	public static void main(String[] args) {
		System.out.println("[1 1 2]".substring(1,"[1 1 2]".length()-1));
	}

}
