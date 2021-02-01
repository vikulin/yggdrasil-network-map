package demo.node;

import java.io.IOException;
import java.util.Iterator;

import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;

public class GroupNodeDataPair implements ViewerListener {
	
	public static final String STYLE = "node {" + "fill-mode: dyn-plain;"
			+ "fill-color: blue,yellow;" + "size-mode: dyn-size;"
			+ "stroke-color: black;" + "stroke-width: 1px;"
			+ "stroke-mode: plain;" + "}";

	public static void main(String[] args) throws ElementNotFoundException, IOException, GraphParseException, ClassNotFoundException, InterruptedException {
		Class.forName("org.graphstream.ui.swing.util.Display");

		System.setProperty("org.graphstream.ui", "demo.ui.DisplayStub");
		//System.setProperty("org.graphstream.ui", "org.graphstream.ui.swing.util.Display");
		//org.graphstream.ui.swing.util.Display
		(new GroupNodeDataPair()).test1();
	}
	
	public void test() throws ElementNotFoundException, IOException, GraphParseException {
		Graph graph = new MultiGraph("test");
		SpriteManager sm = new SpriteManager(graph);
		Sprite C = sm.addSprite("C");
		C.setPosition(0, 0, 0);
		C.setAttribute("ui.label", "(0,0)");
		graph.display();
		graph.setAttribute("ui.stylesheet", styleSheet);
		graph.setAttribute("layout.stabilization-limit", 1);
		graph.setAttribute("layout.quality", 3);
		graph.setAttribute("layout.gravity", 0.01);
//		int steps = 50;
//		Generator gen = new BarabasiAlbertGenerator(2);
//		int steps = 6;
//		Generator gen = new GridGenerator();
		
//		gen.addSink(graph);
//		gen.begin();
//		for(int i=0; i<steps; i++) {
//			gen.nextEvents();
//			sleep(10);
//		}
//		gen.end();
//		graph.read("src-test/org/graphstream/ui/layout/test/data/fourComponents.dgs");
		graph.read("polbooks.gml");
//		graph.read("src-test/org/graphstream/ui/layout/test/data/dolphins.gml");
	}
	
	public void test1() throws InterruptedException {

		Graph graph = new SingleGraph("g");
        SpringBox box = new SpringBox();
        Viewer v = graph.display(false);
        ViewerPipe pipe = v.newViewerPipe();
        pipe.addAttributeSink(graph);
        v.enableAutoLayout(box);
        
		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.antialias");
		graph.setAttribute("ui.stylesheet", STYLE);

		BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
		gen.addSink(graph);
		gen.begin();
		for (int i = 0; i < 200; i++)
			gen.nextEvents();
		gen.end();

		BetweennessCentrality bcb = new BetweennessCentrality();
		bcb.init(graph);
		bcb.compute();

		for (int i = 0; i < graph.getNodeCount(); i++) {
			graph.getNode(i).setAttribute("ui.size",
					Double.parseDouble(graph.getNode(i).getAttribute("Cb").toString())/2000 + 5);
		}
		
        Thread.sleep(5000);
        pipe.pump();
        
	    Iterator<Node> it = graph.iterator();
	    while(it.hasNext()) {
	    	Node n = it.next();
	    	double[] coordinates = GraphPosLengthUtils.nodePosition(graph, n.getId().toString());
	    	System.out.println("id="+n.getId()+" "+"x="+coordinates[0]+" y="+coordinates[1]);
	    }
		System.out.println("Graph calculated");
	}
	
	public void test2() throws InterruptedException {
		Graph graph = new SingleGraph("Clicks");
		Viewer viewer = graph.display();
		ProxyPipe fromViewer = viewer.newThreadProxyOnGraphicGraph();
		fromViewer.addSink(graph);
		System.out.println();
		boolean loop = true;
		while (loop) {
			fromViewer.blockingPump();
			//System.out.println("loop");
			if (graph.hasAttribute("ui.viewClosed")) {
				loop = false;
				System.out.println("ui.viewClosed");
			}
		}
	}
	
	public static void sleep(long ms) {
		try { Thread.sleep(ms); } catch(Exception e) {}
	}
	
	protected static final String styleSheet = 
			"sprite#C { fill-color: red; text-color: red; }";

	@Override
	public void viewClosed(String viewName) {
		System.out.println("Closed view");
	}

	@Override
	public void buttonPushed(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buttonReleased(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseOver(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseLeft(String id) {
		// TODO Auto-generated method stub
		
	}
}
