package demo.more;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.concurrent.ForkJoinTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import demo.node.NodeDataPair;

public class NetworkDHTCrawler {

	private static final String MAP_HISTORY_PATH = "/opt/tomcat/yggdrasil-map-history";
	//private static final String MAP_HISTORY_PATH = "C:\\Users\\Vadym\\git\\yggdrasil-network-map";
	
	private static final Logger log = LoggerFactory.getLogger(NetworkDHTCrawler.class);
	
	private static final String KEY_API_HOST = "323e321939b1b08e06b89b0ed8c57b09757f2974eba218887fdd68a45024d4c1";

	private static final String ADMIN_API_HOST = "192.168.1.106";
	private static final int ADMIN_API_PORT = 9002;

	private static SortedMap<String, NodeDataPair> nodes; // node key, ip nodes
	private static SortedSet<Link> links; // node key, ip links

	public static Gson gson = new Gson();
	
	public static class CountingTask extends RecursiveTask<Integer> {

		private static final long serialVersionUID = 1L;

		private ExecutorService threadTaskPool = Executors.newFixedThreadPool(2);
		
	    private String targetNodeKey;
		private Class<ApiResponse> class_;

		public CountingTask(String targetNodeKey, Class<ApiResponse> class_) {
	    	this.targetNodeKey = targetNodeKey;
	    	this.class_ = class_;
	    }

	    @Override
	    protected Integer compute() {
	    	
	    	log.info("found: " + nodes.size() + " records");
			if (NetworkDHTCrawler.nodes.get(targetNodeKey) != null) {
				return 0;
			} else {
				nodes.put(targetNodeKey, new NodeDataPair(null, null));
			}
			final String json = new ApiRequest().getPeers(targetNodeKey).serialize();
			Future<String> o = threadTaskPool.submit(apiRequest(json));
			String object = null;
			try {
				object = o.get(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e1) {
				e1.printStackTrace();
			}
			if (object == null) {
				return 0;
			}
			ApiResponse apiResponse = null;
			try {
				apiResponse = gson.fromJson(object, class_);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				System.err.println("error response:\n" + object);
				return 0;
			}
			if (apiResponse == null) {
				System.out.println("No response received");
			}
			
			if(apiResponse.getStatus().equals("error")){
				System.out.println("error");
				return 0;
			}
			ApiPeersResponse apiPeerResponse = gson.fromJson(object, ApiPeersResponse.class);
			if (apiPeerResponse.getResponse().entrySet().isEmpty()) {
				log.info("incorrect response: " + gson.toJson(apiPeerResponse));
				return 0;
			}
			Iterator<Entry<String, Map<String, List<String>>>> it = apiPeerResponse.getResponse().entrySet().iterator();
			Entry<String, Map<String, List<String>>> peer = it.next();
			NodeDataPair ndp = new NodeDataPair(peer.getKey(), targetNodeKey);
			
			String nodeInfo = new ApiRequest().getNodeInfo(targetNodeKey).serialize();
			Future<String> nodeInfoO = threadTaskPool.submit(apiRequest(nodeInfo));
			String nodeInfoObject = null;
			try {
				nodeInfoObject = nodeInfoO.get(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e1) {
				e1.printStackTrace();
			}
			threadTaskPool.shutdown();
			ApiNodeInfoResponse nodeInfoReponse = null;
			try {
				nodeInfoReponse = gson.fromJson(nodeInfoObject, ApiNodeInfoResponse.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				System.err.println("error response:\n" + object);
				return 0;
			}
			if (nodeInfoReponse != null && !nodeInfoReponse.getResponse().isEmpty()) {
				if(nodeInfoReponse.getStatus().equals("error")){
					System.out.println("error");
					return 0;
				}
				Entry<String, Map<String, String>> keysNodeInfo = nodeInfoReponse.getResponse().entrySet().iterator().next();
				String buildarch = keysNodeInfo.getValue().get("buildarch");
				String buildplatform = keysNodeInfo.getValue().get("buildplatform");
				String buildversion = keysNodeInfo.getValue().get("buildversion");
				String name = keysNodeInfo.getValue().get("name");
				if (buildarch != null) {
					ndp.setArch(buildarch);
				}
				if (buildplatform != null) {
					ndp.setPlatform(buildplatform);
				}
				if (buildversion != null) {
					ndp.setVersion(buildversion);
				}
				if (name != null) {
					ndp.setName(name);
				}
			}
			
			nodes.put(targetNodeKey, ndp);
			List<String> peerKeys = peer.getValue().get("keys");
			for (String peerKey : peerKeys) {
				links.add(new Link(peerKey, targetNodeKey));
				// duplicated values are checked by Set
				//if (NetworkDHTCrawler.nodes.get(peerKey) != null) {
				//	continue;
				//}
			}
			return 1 + peerKeys.stream().filter(peerKey->NetworkDHTCrawler.nodes.get(peerKey) == null).map(peerKey-> new CountingTask(peerKey, class_).fork()).collect(Collectors.summingInt(ForkJoinTask::join));
	    }
	}
	
	private static Callable<String> apiRequest(String json) {
		
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				String response = null;
				char[] cbuf = new char[1024];
				Socket clientSocket = null;
				DataOutputStream os = null;
				InputStream is = null;
				StringBuilder sb = new StringBuilder();
				try {
					SocketAddress sa = new InetSocketAddress(InetAddress.getByName(ADMIN_API_HOST), ADMIN_API_PORT);
					clientSocket = new Socket();
					clientSocket.connect(sa, 5000);
					os = new DataOutputStream(clientSocket.getOutputStream());
					os.writeBytes(json);
					System.out.println("Total nodes:" + NetworkDHTCrawler.nodes.size());
					//int i = 0;
					is = clientSocket.getInputStream();
					Reader in = new InputStreamReader(is, "UTF-8");
					while(true) {
						int rsz = in.read(cbuf, 0, cbuf.length);
					    if (rsz < cbuf.length) {
					    	if(rsz>0) {
					    		sb.append(cbuf, 0, rsz);
					    	}
					        break;
					    } else {
					    	//System.out.println(rsz);
					    }
						sb.append(cbuf, 0, rsz);
					}
					response = sb.toString();
					System.out.println("Request:\n"+json+"\n"+"Response:\n"+response);
				} catch (java.net.SocketException e) {
					e.printStackTrace();
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				try {
					os.close();
					is.close();
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return response;
			}
		};
	}

	public static void run(String dataPath)
			throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		
		
		nodes = new TreeMap<String, NodeDataPair>();
		links = new TreeSet<Link>();

		//NetworkDHTCrawler crawler = new NetworkDHTCrawler();
		
		ForkJoinPool forkJoinPool = new ForkJoinPool(10).commonPool();
		int sum = forkJoinPool.invoke(new CountingTask(KEY_API_HOST, ApiResponse.class));
		System.out.println("sum="+sum);

		/*
		 * history part long timestamp = new Date().getTime(); new File(dataPath,
		 * "nodes.json").renameTo(new File(MAP_HISTORY_PATH,
		 * "nodes-"+timestamp+".json")); try (Writer writer = new FileWriter(new
		 * File(dataPath, "nodes.json"))) { Gson gson = new
		 * GsonBuilder().setPrettyPrinting().create(); gson.toJson(nodes, writer); } new
		 * File(dataPath, "links.json").renameTo(new File(MAP_HISTORY_PATH,
		 * "links-"+timestamp+".json")); try (Writer writer = new FileWriter(new
		 * File(dataPath, "links.json"))) { Gson gson = new
		 * GsonBuilder().setPrettyPrinting().create(); gson.toJson(links, writer); }
		 */
		System.out.println("done");
		try {
			System.out.println("Nodes:" + nodes.size() + " Links:" + links.size());
			NodeData2JSGraphConverter.createJs(nodes, links, dataPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("JS file created");
	}

	public static void main(String args[])
			throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		System.out.println(new File(".").toPath());
		run(".");
		
		/*
		NetworkDHTCrawler crawler = new NetworkDHTCrawler();
		//String json = "{\"keepalive\":true,\"key\":\"fb370bd6ec82c46f57973ec0d4e26d9c1af8692107cb9a0936f5e258775a014f\",\"request\":\"debug_remotegetpeers\"}\n";
		String json = "{\"keepalive\":true,\"key\":\"39c339079f3db93d04c3c44985759f8675d038b2a282e1b2f140c58db9c6d546\",\"request\":\"debug_remotegetpeers\"}\n";
		
		final ExecutorService threadPool = Executors.newFixedThreadPool(1);	
		Future<String> future = threadPool.submit(crawler.apiRequest(json));
		System.out.println(future.get());
		threadPool.shutdown();
		*/
	}
}
