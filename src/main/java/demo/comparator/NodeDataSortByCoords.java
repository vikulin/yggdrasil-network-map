package demo.comparator;

import java.util.Comparator;

import demo.more.NodeData;

public class NodeDataSortByCoords implements Comparator<NodeData> {

	@Override
	public int compare(NodeData n1, NodeData n2) {
		int delta = n2.getCoords().split(" ").length - n1.getCoords().split(" ").length;
		if(delta!=0) {
			return delta;
		}
		delta = n2.getCoords().length() - n1.getCoords().length();
		if(delta!=0) {
			return delta;
		}
		String[] array2 = n2.getCoords().substring(1, n2.getCoords().length()-1).split(" ");
		String[] array1 = n1.getCoords().substring(1, n1.getCoords().length()-1).split(" ");
		int size = array2.length;
		for(int index = 0;index<size; index++) {
			int value = Integer.parseInt(array2[index])-Integer.parseInt(array1[index]);
			if(value>0) {
				return value;
			}
		}
		return 0;
	}
	
	public static void main(String[] args) {
		System.out.println("[1 1 2]".substring(1,"[1 1 2]".length()-1));
	}

}
