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

	private static final String MAP_HISTORY_PATH = "/opt/tomcat/yggdrasil-map-history";
	//private static final String MAP_HISTORY_PATH = "C:\\Users\\Vadym\\git\\yggdrasil-network-map";
	
	private static final Logger log = LoggerFactory.getLogger(NetworkDHTCrawler.class);

	private static final String ADMIN_API_HOST = "192.168.1.104";
	
	private static final String KEY_API_HOST = "323e321939b1b08e06b89b0ed8c57b09757f2974eba218887fdd68a45024d4c1";

	private static final int ADMIN_API_PORT = 9001;

	private static ExecutorService threadPool;
	private static ExecutorService threadTaskPool;
	private static SortedMap<String, NodeDataPair> nodes; // node key, ip nodes
	private static SortedSet<Link> links; // node key, ip links

	public static Gson gson = new Gson();

	private Future<String> run(String targetNodeKey, Class<ApiResponse> class_) {

		Future<String> future = threadPool.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				log.info("found: " + nodes.size() + " records");
				if (NetworkDHTCrawler.nodes.get(targetNodeKey) != null) {
					return null;
				} else {
					nodes.put(targetNodeKey, new NodeDataPair(null, null));
				}
				final String json = new ApiRequest().getPeers(targetNodeKey).serialize();
				Future<String> o = threadTaskPool.submit(apiRequest(json));
				String object = o.get(2000, TimeUnit.MILLISECONDS);
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
				String nodeInfoObject = nodeInfoO.get(2000, TimeUnit.MILLISECONDS);
				ApiNodeInfoResponse nodeInfoReponse = null;
				try {
					nodeInfoReponse = gson.fromJson(nodeInfoObject, ApiNodeInfoResponse.class);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					System.err.println("error response:\n" + object);
					return null;
				}
				if (nodeInfoReponse != null && !nodeInfoReponse.getResponse().isEmpty()) {
					if(nodeInfoReponse.getStatus().equals("error")){
						System.out.println("error");
						return null;
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
					if (NetworkDHTCrawler.nodes.get(peerKey) != null) {
						continue;
					}
					System.out.println("Total links:" + links.size());
					try {
						NetworkDHTCrawler.this.run(peerKey, class_).get();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
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
				byte[] cbuf = new byte[1024*15];
				Socket clientSocket = null;
				DataOutputStream os = null;
				InputStream is = null;
				StringBuilder sb = new StringBuilder();
				try {
					clientSocket = new Socket(ADMIN_API_HOST, ADMIN_API_PORT);
					os = new DataOutputStream(clientSocket.getOutputStream());
					os.writeBytes(json);
					System.out.println("Total nodes:" + NetworkDHTCrawler.nodes.size());
					int i = 0;
					is = clientSocket.getInputStream();
					i = is.read(cbuf);
					sb.append(new String(Arrays.copyOf(cbuf, i)));
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
		threadPool = Executors.newFixedThreadPool(10);
		threadTaskPool = Executors.newFixedThreadPool(10);
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
		threadPool.shutdownNow();
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
	}
}
