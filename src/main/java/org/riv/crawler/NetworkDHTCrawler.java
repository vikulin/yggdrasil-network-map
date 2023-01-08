package org.riv.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.riv.node.RemoteNodeInfoReponse;
import org.riv.node.RemotePeerResponse;
import org.riv.node.RemoteSelfReponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

				Map<String, RemotePeerResponse> peer = getRemotePeers(targetNodeKey);
				Map<String, RemoteNodeInfoReponse> nodeInfoReponse = getRemoteNodeInfo(targetNodeKey);
				Map<String, RemoteSelfReponse> selfNodeInfo = getRemoteSelf(targetNodeKey);
				if(peer==null) {
					System.err.println("RemotePeerResponse is null, target key="+targetNodeKey);
					return null;
				}	
				if(nodeInfoReponse==null) {
					System.err.println("RemoteNodeInfoReponse is null, target key="+targetNodeKey);
					return null;
				}
				if(selfNodeInfo==null) {
					System.err.println("RemoteSelfReponse is null, target key="+targetNodeKey);
					return null;
				}
				String ip = peer.entrySet().iterator().next().getKey();
				NodeDataPair ndp = nodes.get(targetNodeKey);//new NodeDataPair(ip, targetNodeKey);
				ndp.setIp(ip);
				ndp.setKey(targetNodeKey);
				
				String buildarch = nodeInfoReponse.entrySet().iterator().next().getValue().getBuildarch();
				String buildplatform = nodeInfoReponse.entrySet().iterator().next().getValue().getBuildplatform();
				String buildversion = nodeInfoReponse.entrySet().iterator().next().getValue().getBuildversion();
				String name = nodeInfoReponse.entrySet().iterator().next().getValue().getName();
				String icon = nodeInfoReponse.entrySet().iterator().next().getValue().getIcon();
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
				if (icon != null) {
					ndp.setIcon(icon);
				}
				
				String coords = selfNodeInfo.entrySet().iterator().next().getValue().getCoords();
				String coordsString = coords.substring(1, coords.length()-1);
				ndp.setCoords(coordsString);
				
				List<String> peerKeys = peer.values().iterator().next().getKeys();
				if(peerKeys==null) {
					System.err.println(peer.keySet().iterator().next()+" returned no peers");
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
	
	private Map<String, RemoteSelfReponse> getRemoteSelf(String targetNodeKey) throws IOException {
		Type collectionType = new TypeToken<Map<String, RemoteSelfReponse>>(){}.getType();
		
		Map<String, RemoteSelfReponse> apiResponse = (Map<String, RemoteSelfReponse>) restRequest("api/remote/self/"+targetNodeKey, collectionType);
		
		if (apiResponse == null) {
			return null;
		}
		if (apiResponse.values().isEmpty()) {
			log.info("incorrect response: " + gson.toJson(apiResponse));
			return null;
		}
		return apiResponse;
	}
	
	private Map<String, RemoteNodeInfoReponse> getRemoteNodeInfo(String targetNodeKey) throws IOException {
		Type collectionType = new TypeToken<Map<String, RemoteNodeInfoReponse>>(){}.getType();
		
		Map<String, RemoteNodeInfoReponse> apiResponse = (Map<String, RemoteNodeInfoReponse>) restRequest("api/remote/nodeinfo/"+targetNodeKey, collectionType);
		
		if (apiResponse == null) {
			return null;
		}
		if (apiResponse.values().isEmpty()) {
			log.info("incorrect response: " + gson.toJson(apiResponse));
			return null;
		}
		return apiResponse;
	}
	
	private Map<String, RemotePeerResponse> getRemotePeers(String targetNodeKey) throws IOException {

		Type collectionType = new TypeToken<Map<String, RemotePeerResponse>>(){}.getType();
		
		Map<String, RemotePeerResponse> apiResponse = (Map<String, RemotePeerResponse>) restRequest("api/remote/peers/"+targetNodeKey, collectionType);
		if (apiResponse == null) {
			return null;
		}
		if (apiResponse.values().isEmpty()) {
			log.info("incorrect response: " + gson.toJson(apiResponse));
			return null;
		}
		return apiResponse;
	}

	private Object restRequest(String uri, Type collectionType) throws IOException {
		String result = "";
		InputStream is = null;
		HttpURLConnection con = null;
		try {
			URL url = new URL("http://"+ADMIN_API_HOST+":"+ADMIN_API_PORT+"/"+uri);
			con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(20000);
			con.setReadTimeout(10000);
			HttpURLConnection.setFollowRedirects(false);
			con.setRequestMethod("GET");
			con.setDoOutput(true);

			is = con.getInputStream();
			
			BufferedReader in = new BufferedReader(
					  new InputStreamReader(is));
					String inputLine;
					StringBuffer content = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
					    content.append(inputLine);
					}
			result = content.toString();
			return gson.fromJson(result, collectionType);
			
		} catch (java.net.ConnectException e) {
			e.printStackTrace();
			System.out.println(result);
		} catch (java.net.SocketException e) {
			e.printStackTrace();
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(result);
		} finally {
			if(is!=null) {
				is.close();
			}
			if(con!=null) {
				con.disconnect();
			}
		}
		
		return null;
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
