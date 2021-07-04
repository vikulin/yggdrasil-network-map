package demo.more;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
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
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import demo.node.NodeDataPair;

public class NetworkDHTCrawler {
	
	private static final String MAP_HISTORY_PATH = "/opt/tomcat/yggdrasil-map-history";
	
	private static final Logger log = LoggerFactory.getLogger(NetworkDHTCrawler.class);
	
	private static final String ADMIN_API_HOST="192.168.1.108";
	
	private static final int ADMIN_API_PORT=9001;
	
	private static ExecutorService threadPool;
	public volatile static Map<String, NodeDataPair> nodes; //node key, ip nodes
	public volatile static Set<Link> links; //node key, ip links
	public volatile static long id=0;
	
	public static Gson gson = new Gson();
	
	private Future<String> run(String key, Class<?> class_) {
		
		Future<String> future = threadPool.submit(new Callable<String>() {
			
			@Override
			public String call() throws Exception {
				log.info("found: "+nodes.size()+" records");
				final String json = new ApiRequest().getDHT(key).serialize();
				Object object = apiRequest(json, class_);
				if(object==null) {
					return null;
				}
				ApiPeersResponse peerReponse = (ApiPeersResponse)object;
				
				if(peerReponse.getResponse().entrySet().isEmpty()) {
					
					log.info("incorrect response: "+gson.toJson(peerReponse));
					return gson.toJson(peerReponse);
				}
				Iterator<Entry<String, Map<String, List<String>>>> it = peerReponse.getResponse().entrySet().iterator();
				Entry<String, Map<String, List<String>>> peer = it.next();
				if(peer.getKey().equals("error")) {
					return gson.toJson(peerReponse);
				}
				NodeDataPair ndp = new NodeDataPair(peer.getKey(), key);
				ndp.setId(id++);
				nodes.put(key, ndp);
				List<String> keys = peer.getValue().get("keys");
				for(String k:keys) {
					links.add(new Link(k, peer.getKey()));
					System.out.println("Total links:"+links.size());
					//duplicated values are checked by Set
					if(NetworkDHTCrawler.nodes.get(k)!=null) {
						continue;
					}
					NetworkDHTCrawler.this.run(k, class_).get(100l, TimeUnit.SECONDS);
				}
				return gson.toJson(peerReponse);
			}    
		});
		
		return future;
	}
	
	
	private Object apiRequest(String json, Type peers) {
		
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
			apiReponse = gson.fromJson(response, peers);
		} catch(JsonSyntaxException e) {
			e.printStackTrace();
			System.err.println("error response:\n"+response);
			return null;
		}
		if(apiReponse==null) {
			System.out.println("No response received");
		}
		return apiReponse;
	}
	
	public static void run(String dataPath) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		id = 0;
		threadPool = Executors.newFixedThreadPool(20);
		nodes = new ConcurrentHashMap<String, NodeDataPair>();
		links = new HashSet<Link>();
		
		String json = new ApiRequest().getDHT("2506485f72886a6729ffa4bdaf270a8801b283d30aed4ea1f14518e5e8f7e9f6").serialize();
		NetworkDHTCrawler crawler = new NetworkDHTCrawler();
		
		ApiPeersResponse peerReponse = (ApiPeersResponse) crawler.apiRequest(json, ApiPeersResponse.class);
		if(peerReponse==null || peerReponse.getResponse().isEmpty()) {
			return;
		}
		Entry<String, Map<String, List<String>>> keys = peerReponse.getResponse().entrySet().iterator().next();
		List<String> k = keys.getValue().get("keys");
		if(k==null) {
			return;
		}
		Iterator<String> keyIt = k.iterator();
		while(keyIt.hasNext()) {
			String key = keyIt.next();
			NodeDataPair ndp = new NodeDataPair(keys.getKey(), key);
			ndp.setId(id++);
			nodes.put(key, ndp);

			try {
				crawler.run(key, ApiPeersResponse.class).get(1200l, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				e1.printStackTrace();
			} catch (TimeoutException e1) {
				e1.printStackTrace();
			}
		}
		/*history part
		long timestamp = new Date().getTime();
		new File(dataPath, "nodes.json").renameTo(new File(MAP_HISTORY_PATH, "nodes-"+timestamp+".json"));
		try (Writer writer = new FileWriter(new File(dataPath, "nodes.json"))) {
		    Gson gson = new GsonBuilder().setPrettyPrinting().create();
		    gson.toJson(nodes, writer);
		}
		new File(dataPath, "links.json").renameTo(new File(MAP_HISTORY_PATH, "links-"+timestamp+".json"));
		try (Writer writer = new FileWriter(new File(dataPath, "links.json"))) {
		    Gson gson = new GsonBuilder().setPrettyPrinting().create();
		    gson.toJson(links, writer);
		}*/
		System.out.println("done");
		threadPool.shutdownNow();
		try {
			System.out.println("Nodes:"+nodes.size()+" Links:"+links.size());
			NodeData2JSGraphConverter.createJs(nodes, links, dataPath);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("JS file created");
	}
	
	public static void main(String args[]) throws InterruptedException, ExecutionException, IOException, ClassNotFoundException {
		run(".");
	}
}
