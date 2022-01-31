package org.riv.crawler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

import org.riv.NodeData2JSGraphConverter;
import org.riv.node.Link;
import org.riv.node.NodeDataPair;
import org.rivmesh.api.ApiNodeInfoResponse;
import org.rivmesh.api.ApiPeersResponse;
import org.rivmesh.api.ApiRequest;
import org.rivmesh.api.ApiResponse;
import org.rivmesh.api.ApiSelfResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class NetworkDHTCrawler {
	
	private static Properties config = new Properties();
	
	static {
		System.setProperty("org.graphstream.ui", "swing");
		try {
			config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("map.conf"));
		} catch (IOException e) {
			throw new ExceptionInInitializerError("Cannot load properties file.");
		}
	}
	
	public static String getKeyApiHost(){
		return config.getProperty("KEY_API_HOST");
	}
	
	public static String getAdminApiHost(){
		return config.getProperty("ADMIN_API_HOST");
	}
	
	public static String getAdminApiPort(){
		return config.getProperty("ADMIN_API_PORT");
	}
	
	private static final String MAP_HISTORY_PATH = "/opt/tomcat/yggdrasil-map-history";
	//private static final String MAP_HISTORY_PATH = "C:\\Users\\Vadym\\git\\yggdrasil-network-map";
	
	private static final Logger log = LoggerFactory.getLogger(NetworkDHTCrawler.class);
	
	private static final String KEY_API_HOST = getKeyApiHost();// "323e321939b1b08e06b89b0ed8c57b09757f2974eba218887fdd68a45024d4c1";

	private static final String ADMIN_API_HOST = getAdminApiHost();
	
	private static final int ADMIN_API_PORT = Integer.parseInt(getAdminApiPort());

	private static SortedMap<String, NodeDataPair> nodes; // node key, ip nodes
	private static SortedSet<Link> links; // node key, ip links
	public static Gson gson = new Gson();
	
	private static ExecutorService threadPool = null;

	private Future<String> runTask(String targetNodeKey) {
		
		Future<String> future = threadPool.submit(new Callable<String>() {
			
			@Override
			public String call() throws Exception {
				
				log.info("found: " + nodes.size() + " records");
				if (NetworkDHTCrawler.nodes.get(targetNodeKey) != null) {
					return null;
				} else {
					nodes.put(targetNodeKey, new NodeDataPair(null, null));
				}

				Entry<String, Map<String, List<String>>> peer = getPeerInfo(targetNodeKey);
				Entry<String, Map<String, Object>> nodeInfoReponse = getNodeInfo(targetNodeKey);
				Entry<String, Map<String, Object>> selfNodeInfo = getSelf(targetNodeKey);
				
				String ip = null;
				if(peer!=null) {
					ip = peer.getKey();
				} else {
					if(selfNodeInfo!=null) {
						ip = selfNodeInfo.getKey();
					} else {
						if(nodeInfoReponse!=null) {
							ip = nodeInfoReponse.getKey();
						}
					}
				}
				NodeDataPair ndp = nodes.get(targetNodeKey);//new NodeDataPair(ip, targetNodeKey);
				ndp.setIp(ip);
				ndp.setKey(targetNodeKey);
				
				if (nodeInfoReponse != null) {
					
					Object buildarch = nodeInfoReponse.getValue().get("buildarch");
					Object buildplatform = nodeInfoReponse.getValue().get("buildplatform");
					Object buildversion = nodeInfoReponse.getValue().get("buildversion");
					Object name = nodeInfoReponse.getValue().get("name");
					Object icon = nodeInfoReponse.getValue().get("icon");
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
					if (icon != null) {
						ndp.setIcon(icon.toString());
					}
				}
				
				if(selfNodeInfo!=null) {
					String coords = selfNodeInfo.getValue().get("coords").toString();
					String coordsString = coords.substring(1, coords.length()-1);
					ndp.setCoords(coordsString);
				}
				
				//nodes.put(targetNodeKey, ndp);
				if(peer==null) {
					return null;
				}
				List<String> peerKeys = peer.getValue().get("keys");
				if(peerKeys==null) {
					System.err.println(peer.getKey()+" returned no peers");
					return null;
				}
				ConcurrentLinkedQueue<Future<String>> tasks = new ConcurrentLinkedQueue<Future<String>>();
				for (String peerKey : peerKeys) {
					links.add(new Link(peerKey, targetNodeKey));
					// duplicated values are checked by Set
					if (NetworkDHTCrawler.nodes.get(peerKey) != null) {
						continue;
					}
					//Thread.sleep(1000);
					tasks.add(NetworkDHTCrawler.this.runTask(peerKey));
					//System.out.println("Total links:" + links.size());
				}
				for(Future<String> task: tasks) {
					try {
						task.get();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});

		return future;
	}
	
	private Entry<String, Map<String, Object>> getSelf(String targetNodeKey) throws IOException {
		String selfInfo = new ApiRequest().getSelf(targetNodeKey).serialize();
		String selfInfoObject = socketRequest(selfInfo);
		
		ApiSelfResponse selfInfoReponse = null;
		try {
			selfInfoReponse = gson.fromJson(selfInfoObject, ApiSelfResponse.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			System.err.println("error response:\n" + selfInfoObject);
			return null;
		}
		Entry<String, Map<String, Object>> selfNodeInfo = null;
		if (selfInfoReponse != null && !selfInfoReponse.getResponse().isEmpty()) {
			if(selfInfoReponse.getStatus().equals("error")){
				System.out.println("error");
				return null;
			}
			selfNodeInfo = selfInfoReponse.getResponse().entrySet().iterator().next();
		}
		return selfNodeInfo;
	}
	
	private Entry<String, Map<String, Object>> getNodeInfo(String targetNodeKey) throws IOException {
		String nodeInfo = new ApiRequest().getNodeInfo(targetNodeKey).serialize();
		String nodeInfoObject = socketRequest(nodeInfo);
		
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
			return nodeInfoReponse.getResponse().entrySet().iterator().next();
		}
		return null;
	}
	
	private Entry<String, Map<String, List<String>>> getPeerInfo(String targetNodeKey) throws IOException {

		final String json = new ApiRequest().getPeers(targetNodeKey).serialize();
		
		String object = socketRequest(json);
		if (object == null) {
			return null;
		}
		ApiResponse apiResponse = null;
		try {
			apiResponse = gson.fromJson(object, ApiResponse.class);
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
		return peer;
	}

	private String socketRequest(String json) throws IOException {
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
			//System.out.println("Request:\n"+json+"\n"+"Response:\n"+response);
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
		} catch (java.net.ConnectException e) {
			e.printStackTrace();
		} catch (java.net.SocketException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(os!=null) {
				os.close();
			}
			if(is!=null) {
				is.close();
			}
			if(clientSocket!=null) {
				clientSocket.close();
			}
		}
		
		return response;
	}

	public static void run(String dataPath)
			throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		threadPool = Executors.newWorkStealingPool(12);
		nodes = new ConcurrentSkipListMap<String, NodeDataPair>();
		links = new ConcurrentSkipListSet<Link>();

		NetworkDHTCrawler crawler = new NetworkDHTCrawler();

		Future<String> future = crawler.runTask(KEY_API_HOST);
		future.get();
		threadPool.shutdownNow();
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
			NodeData2JSGraphConverter.createPeerGraphJs(nodes, links, dataPath);
			NodeData2JSGraphConverter.createSpanningTreeGraphJs(nodes, dataPath);
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
