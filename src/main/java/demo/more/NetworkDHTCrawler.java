package demo.more;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import demo.node.NodeDataPair;

public class NetworkDHTCrawler {
	
	static {
		System.setProperty("org.graphstream.ui", "swing");
	}

	private static final String MAP_HISTORY_PATH = "/opt/tomcat/yggdrasil-map-history";
	//private static final String MAP_HISTORY_PATH = "C:\\Users\\Vadym\\git\\yggdrasil-network-map";
	
	private static final Logger log = LoggerFactory.getLogger(NetworkDHTCrawler.class);
	
	private static final String KEY_API_HOST = "11dbeb74048638c9532077a9b19b20cd5a8bf6f44312a1e8ba7d791e303f8e29";

	private static final String ADMIN_API_HOST = "192.168.1.106";
	private static final int ADMIN_API_PORT = 9002;

	private static SortedMap<String, NodeDataPair> nodes; // node key, ip nodes
	private static SortedSet<Link> links; // node key, ip links

	public static Gson gson = new Gson();

	private Future<String> run(String targetNodeKey, Class<ApiResponse> class_) {
		
		final ExecutorService threadPool = Executors.newFixedThreadPool(1);

		Future<String> future = threadPool.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				ExecutorService threadTaskPool = Executors.newFixedThreadPool(2);
				log.info("found: " + nodes.size() + " records");
				if (NetworkDHTCrawler.nodes.get(targetNodeKey) != null) {
					return null;
				} else {
					nodes.put(targetNodeKey, new NodeDataPair(null, null));
				}
				final String json = new ApiRequest().getPeers(targetNodeKey).serialize();
				Future<String> o = threadTaskPool.submit(apiRequest(json));
				String object = o.get(5000, TimeUnit.MILLISECONDS);
				if (object == null) {
					return null;
				}
				ApiResponse apiResponse = null;
				try {
					apiResponse = gson.fromJson(object, class_);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					System.err.println("error response:\n" + object);
					return null;
				}
				if (apiResponse == null) {
					System.out.println("No response received");
				}
				
				if(apiResponse.getStatus().equals("error")){
					System.out.println("error");
					return null;
				}
				ApiPeersResponse apiPeerResponse = gson.fromJson(object, ApiPeersResponse.class);
				if (apiPeerResponse.getResponse().entrySet().isEmpty()) {
					log.info("incorrect response: " + gson.toJson(apiPeerResponse));
					return null;
				}
				Iterator<Entry<String, Map<String, List<String>>>> it = apiPeerResponse.getResponse().entrySet().iterator();
				Entry<String, Map<String, List<String>>> peer = it.next();
				NodeDataPair ndp = new NodeDataPair(peer.getKey(), targetNodeKey);
				
				String nodeInfo = new ApiRequest().getNodeInfo(targetNodeKey).serialize();
				Future<String> nodeInfoO = threadTaskPool.submit(apiRequest(nodeInfo));
				String nodeInfoObject = nodeInfoO.get(5000, TimeUnit.MILLISECONDS);
				threadTaskPool.shutdown();
				
				ApiNodeInfoResponse nodeInfoReponse = null;
				try {
					nodeInfoReponse = gson.fromJson(nodeInfoObject, ApiNodeInfoResponse.class);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					System.err.println("error response:\n" + nodeInfoObject);
					return null;
				}
				if (nodeInfoReponse != null && !nodeInfoReponse.getResponse().isEmpty()) {
					if(nodeInfoReponse.getStatus().equals("error")){
						System.out.println("error");
						return null;
					}
					Entry<String, Map<String, Object>> keysNodeInfo = nodeInfoReponse.getResponse().entrySet().iterator().next();
					Object buildarch = keysNodeInfo.getValue().get("buildarch");
					Object buildplatform = keysNodeInfo.getValue().get("buildplatform");
					Object buildversion = keysNodeInfo.getValue().get("buildversion");
					Object name = keysNodeInfo.getValue().get("name");
					if (buildarch != null) {
						ndp.setArch(buildarch.toString());
					}
					if (buildplatform != null) {
						ndp.setPlatform(buildplatform.toString());
					}
					if (buildversion != null) {
						ndp.setVersion(buildversion.toString());
					}
					if (name != null) {
						ndp.setName(name.toString());
					}
				}
				
				nodes.put(targetNodeKey, ndp);
				List<String> peerKeys = peer.getValue().get("keys");
				ConcurrentLinkedQueue<Future<String>> tasks = new ConcurrentLinkedQueue<Future<String>>();
				for (String peerKey : peerKeys) {
					links.add(new Link(peerKey, targetNodeKey));
					// duplicated values are checked by Set
					if (NetworkDHTCrawler.nodes.get(peerKey) != null) {
						continue;
					}
					Thread.sleep(1000);
					tasks.add(NetworkDHTCrawler.this.run(peerKey, class_));
					System.out.println("Total links:" + links.size());
				}
				for(Future<String> task: tasks) {
					try {
						task.get();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				threadPool.shutdownNow();
				return gson.toJson(apiPeerResponse);
			}
		});

		return future;
	}

	private Callable<String> apiRequest(String json) {
		
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				String response = null;
				byte[] cbuf = new byte[1024*64];
				Socket clientSocket = null;
				DataOutputStream os = null;
				InputStream is = null;
				StringBuilder sb = new StringBuilder();
				try {
					clientSocket = new Socket(ADMIN_API_HOST, ADMIN_API_PORT);
					os = new DataOutputStream(clientSocket.getOutputStream());
					os.writeBytes(json);
					//System.out.println("Total nodes:" + NetworkDHTCrawler.nodes.size());
					int i = 0;
					is = clientSocket.getInputStream();
					i = is.read(cbuf);
					sb.append(new String(Arrays.copyOf(cbuf, i)));
					response = sb.toString();
					System.out.println("Request:\n"+json+"\n"+"Response:\n"+response);
					boolean exception=false;
					do {
						try {
							gson.fromJson(response, ApiResponse.class);
						} catch (JsonSyntaxException e) {
							exception = true;
							e.printStackTrace();
							i = is.read(cbuf);
							sb.append(new String(Arrays.copyOf(cbuf, i)));
							response = sb.toString();
							continue;
						}
						exception = false;
					} while(exception);
				} catch (java.net.SocketException e) {
					e.printStackTrace();
					return null;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				} finally {
					os.close();
					is.close();
					clientSocket.close();
				}
				
				return response;
			}
		};
	}

	public static void run(String dataPath)
			throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		
		nodes = new ConcurrentSkipListMap<String, NodeDataPair>();
		links = new ConcurrentSkipListSet<Link>();

		NetworkDHTCrawler crawler = new NetworkDHTCrawler();

		Future<String> future = crawler.run(KEY_API_HOST, ApiResponse.class);
		future.get();
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
		//String json = "{\"keepalive\":true,\"key\":\"39c339079f3db93d04c3c44985759f8675d038b2a282e1b2f140c58db9c6d546\",\"request\":\"debug_remotegetpeers\"}\n";
		String json = "{\"keepalive\":true,\"key\":\"085da25dc55ad61dfbf095e441cd4e7a31fef3827e0a04544c891a9aac748464\",\"request\":\"debug_remotegetpeers\"}\n";
		
		final ExecutorService threadPool = Executors.newFixedThreadPool(1);	
		Future<String> future = threadPool.submit(crawler.apiRequest(json));
		System.out.println(future.get());
		threadPool.shutdown();
		*/
	}
}
