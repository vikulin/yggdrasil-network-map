package demo.more;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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

	private static final Logger log = LoggerFactory.getLogger(NetworkDHTCrawler.class);

	private static final String ADMIN_API_HOST = "172.16.0.112";

	private static final int ADMIN_API_PORT = 9001;

	private static ExecutorService threadPool;
	private static ExecutorService threadTaskPool;
	public volatile static Map<String, NodeDataPair> nodes; // node key, ip nodes
	public volatile static Set<Link> links; // node key, ip links
	public volatile static long id = 0;

	public static Gson gson = new Gson();

	private Future<String> run(String key, Class<?> class_) {

		Future<String> future = threadPool.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				log.info("found: " + nodes.size() + " records");

				final String json = new ApiRequest().getPeers(key).serialize();
				Future<Object> o = threadTaskPool.submit(apiRequest(json, class_));
				Object object = o.get(20000, TimeUnit.MILLISECONDS);
				if (object == null) {
					return null;
				}
				ApiPeersResponse peerReponse = (ApiPeersResponse) object;

				if (peerReponse.getResponse().entrySet().isEmpty()) {

					log.info("incorrect response: " + gson.toJson(peerReponse));
					return gson.toJson(peerReponse);
				}
				Iterator<Entry<String, Map<String, List<String>>>> it = peerReponse.getResponse().entrySet().iterator();
				Entry<String, Map<String, List<String>>> peer = it.next();
				if (peer.getKey().equals("error")) {
					return gson.toJson(peerReponse);
				}
				NodeDataPair ndp = new NodeDataPair(peer.getKey(), key);
				ndp.setId(id++);
				nodes.put(key, ndp);
				List<String> keys = peer.getValue().get("keys");
				for (String k : keys) {
					links.add(new Link(k, peer.getKey()));
					// duplicated values are checked by Set
					if (NetworkDHTCrawler.nodes.get(k) != null) {
						continue;
					}
					System.out.println("Total links:" + links.size());
					try {
						NetworkDHTCrawler.this.run(k, class_).get(5000, TimeUnit.MILLISECONDS);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return gson.toJson(peerReponse);
			}
		});

		return future;
	}

	private Callable<Object> apiRequest(String json, Type peers) {
		
		return new Callable<Object>() {
			@Override
			public Object call() throws Exception {
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
					System.out.println(json);
					System.out.println("Total nodes:" + NetworkDHTCrawler.nodes.size());
					int i = 0;
					is = clientSocket.getInputStream();
					while ((i = is.read(cbuf)) > 0) {
						sb.append(new String(Arrays.copyOf(cbuf, i)));
					}
					response = sb.toString();
					System.out.println(response);
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
				Gson gson = new Gson();
				Object apiReponse = null;
				try {
					apiReponse = gson.fromJson(response, peers);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
					System.err.println("error response:\n" + response);
					return null;
				}
				if (apiReponse == null) {
					System.out.println("No response received");
				}
				return apiReponse;
			}
		};
	}

	public static void run(String dataPath)
			throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		id = 0;
		threadPool = Executors.newFixedThreadPool(10);
		threadTaskPool = Executors.newFixedThreadPool(10);
		nodes = new ConcurrentHashMap<String, NodeDataPair>();
		links = new HashSet<Link>();

		String json = new ApiRequest().getPeers("2506485f72886a6729ffa4bdaf270a8801b283d30aed4ea1f14518e5e8f7e9f6")
				.serialize();
		NetworkDHTCrawler crawler = new NetworkDHTCrawler();
		Future<Object> o = threadTaskPool.submit(crawler.apiRequest(json, ApiPeersResponse.class));
		Object object = o.get();
		ApiPeersResponse peerReponse = (ApiPeersResponse) object;
		if (peerReponse == null || peerReponse.getResponse().isEmpty()) {
			return;
		}
		Entry<String, Map<String, List<String>>> keys = peerReponse.getResponse().entrySet().iterator().next();
		List<String> k = keys.getValue().get("keys");
		if (k == null) {
			return;
		}
		Iterator<String> keyIt = k.iterator();
		List<Future<String>> f = new ArrayList<Future<String>>();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			NodeDataPair ndp = new NodeDataPair(keys.getKey(), key);
			ndp.setId(id++);
			nodes.put(key, ndp);

			Future<String> future = crawler.run(key, ApiPeersResponse.class);
			f.add(future);
		}

		for (Future<String> ft : f) {
			ft.get();
		}
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
		run(".");
	}
}
