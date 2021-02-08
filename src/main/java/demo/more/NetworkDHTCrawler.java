package demo.more;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import demo.comparator.NodeDataPairSortByCoords;
import demo.node.NodeDataPair;

public class NetworkDHTCrawler {
	
	private static final Logger log = LoggerFactory.getLogger(NetworkDHTCrawler.class);
	
	private static final String ADMIN_API_HOST="localhost";
	
	private static final int ADMIN_API_PORT=9001;
	
	private static ExecutorService threadPool;
	private static Queue<Future<NodeDataPair>> queue;	
	public static Set<NodeDataPair> nodes;
	
	private void run(NodeData nodeData, Class<?> class_) {

		queue.add(threadPool.submit(new Callable<NodeDataPair>() {
			
			@Override
			public NodeDataPair call() throws Exception {
				log.info("found: "+nodes.size()+" records");
				final String json = new ApiRequest().dhtPing(nodeData).serialize();
				final ApiNodesResponse nodesReponse = (ApiNodesResponse)apiRequest(json, class_);
				if(nodesReponse==null) {
					return null;
				}
				if(nodesReponse.getResponse()==null) {
					Gson gson = new Gson();
					log.info("incorrect response: "+gson.toJson(nodesReponse));
					return null;
				}
				if(nodesReponse.getResponse().getNodes()==null) {
					Gson gson = new Gson();
					log.info("incorrect response: "+gson.toJson(nodesReponse));
					return null;
				}
				final Map<String, NodeData> nodes = nodesReponse.getResponse().getNodes();
				for(final Entry<String, NodeData> nodeEntry:nodes.entrySet()) {
					//duplicated values are checked by Set
					if(NetworkDHTCrawler.nodes.contains(new NodeDataPair(nodeEntry.getKey(), nodeEntry.getValue()))) {
						continue;
					}
					NetworkDHTCrawler.nodes.add(new NodeDataPair(nodeEntry.getKey(), nodeEntry.getValue()));
					NetworkDHTCrawler.this.run(nodeEntry.getValue(), ApiNodesResponse.class);
				}
				return null;
			}    
		}));
	}
	
	
	private Object apiRequest(String json, Class<?> class_) {
		
		String response = null;
		byte[] cbuf = new byte[1024];
		Socket clientSocket = null;
		DataOutputStream os = null;
        InputStream is = null;
		StringBuilder sb = new StringBuilder();
		try {
			clientSocket = new Socket(ADMIN_API_HOST, ADMIN_API_PORT);
			os = new DataOutputStream(clientSocket.getOutputStream());
			os.writeBytes(json);
			System.out.println(json);
			System.out.println("Total nodes:"+NetworkDHTCrawler.nodes.size());
			int i = 0;
			is = clientSocket.getInputStream();
			while((i = is.read(cbuf))>0) {
				sb.append(new String(Arrays.copyOf(cbuf, i)));
			}
			response = sb.toString();
			System.out.println(response);
		} catch (java.net.SocketException e) {
			//System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			os.close();
			is.close();
            clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Gson gson = new Gson();
		Object apiReponse = null;
		try {
			apiReponse = gson.fromJson(response, class_);
		} catch(JsonSyntaxException e) {
			e.printStackTrace();
			System.out.println(response);
		}
		if(apiReponse==null) {
			System.out.println("No response received");
		}
		return apiReponse;
	}
	
	public static void run(String dataPath) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		
		threadPool = Executors.newFixedThreadPool(10);
		queue = new ConcurrentLinkedQueue<Future<NodeDataPair>>();
		nodes = new HashSet<NodeDataPair>();
		
		String json = new ApiRequest().getDHT().serialize();
		NetworkDHTCrawler crawler = new NetworkDHTCrawler();
		ApiDHTResponse dhtReponse = (ApiDHTResponse)crawler.apiRequest(json, ApiDHTResponse.class);
		if(dhtReponse==null) {
			return;
		}
		//String json = new ApiRequest().dhtPing("5db525ea8fa6d3f20b5bb3d6d810f047ca447987e177532f78ed01713f459414", "[1 13]").serialize();
		Map<String, NodeData> localDHT = dhtReponse.getResponse().getDht();
		
		for(Entry<String, NodeData> dhtEntry:localDHT.entrySet()) {
			nodes.add(new NodeDataPair(dhtEntry.getKey(), dhtEntry.getValue()));
			crawler.run(dhtEntry.getValue(), ApiNodesResponse.class);
		}
		for(Future<NodeDataPair> item:queue) {
			try {
				NodeDataPair map = item.get();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		long id = 1;
		for(NodeDataPair ndp:NetworkDHTCrawler.nodes) {
			ndp.setId(id);
			id++;
		}
		try (Writer writer = new FileWriter(new File(dataPath, "nodes.json"))) {
		    Gson gson = new GsonBuilder().setPrettyPrinting().create();
		    gson.toJson(nodes, writer);
		}
		System.out.println("done");
		threadPool.shutdownNow();
		NodeData2JSGraphConverter.createJs(NetworkDHTCrawler.nodes, dataPath);
		System.out.println("JS file created");
	}
	
	public static void main(String args[]) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		run("");
	}
}
