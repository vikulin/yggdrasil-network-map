package listener;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.riv.node.PeerInfo;

public class SchedulePeerReaderListener implements ServletContextListener {

	private static final String PEERS_REPO_PATH = "/opt/tomcat/public-peers";//"C:\\Users\\Vadym.Server-PC\\git\\public-peers.git";

	public static final Map<String, LinkedHashMap<String, PeerInfo>> peersPerCountry = new TreeMap<String, LinkedHashMap<String, PeerInfo>>();

	Pattern IPv4_PEER_REGEXP = Pattern.compile(
			"(((tcp)|(tls)|(sctp)|(mpath))\\://(((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d))\\:\\d{1,5})");
	Pattern IPv6_PEER_REGEXP = Pattern.compile(
			"((tcp)|(tls)|(sctp)|(mpath))\\://\\[(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))\\]\\:\\d{1,5}");
	Pattern DOMAIN_NAME_PEER_REGEXP = Pattern.compile("(((tcp)|(tls)|(sctp)|(mpath))\\://([\\w\\-]+\\.)+[a-zA-Z]+\\:\\d{1,5})");

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
		Runnable task2 = () -> {

			try {
				Iterator<File> it = FileUtils.iterateFilesAndDirs(new File(PEERS_REPO_PATH), new IOFileFilter() {

					@Override
					public boolean accept(File file) {
						return false;
					}

					@Override
					public boolean accept(File dir, String name) {
						return false;
					}

				}, TrueFileFilter.INSTANCE);
				//
				while (it.hasNext()) {
					File f = it.next();
					if (f.isDirectory()) {
						Iterator<File> country = FileUtils.iterateFiles(f,

								new IOFileFilter() {

									@Override
									public boolean accept(File file) {
										return file.getName().endsWith(".md");
									}

									@Override
									public boolean accept(File dir, String name) {
										return false;
									}

								}, null);
						while (country.hasNext()) {
							LinkedHashMap<String, PeerInfo> peerInfoMap = new LinkedHashMap<String, PeerInfo>();
							// parse peers in md file
							File countryNext = country.next();
							try {
								List<String> content = FileUtils.readLines(countryNext, "UTF-8");
								for (String c : content) {
									Matcher mIpv4 = IPv4_PEER_REGEXP.matcher(c);
									while (mIpv4.find()) {
										String peer = mIpv4.group(0);
										PeerInfo pi = new PeerInfo(true, null, null, 0);
										peerInfoMap.put(peer, pi);
									}
									Matcher mIpv6 = IPv6_PEER_REGEXP.matcher(c);
									while (mIpv6.find()) {
										String peer = mIpv6.group(0);
										PeerInfo pi = new PeerInfo(true, null, null, 0);
										peerInfoMap.put(peer, pi);
									}
									Matcher mDomain = DOMAIN_NAME_PEER_REGEXP.matcher(c);
									while (mDomain.find()) {
										String peer = mDomain.group(0);
										PeerInfo pi = new PeerInfo(true, null, null, 0);
										peerInfoMap.put(peer, pi);
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (peerInfoMap.size() > 0) {
								peersPerCountry.put(countryNext.getName(), peerInfoMap);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		ses.scheduleAtFixedRate(task2, 5, 600, TimeUnit.SECONDS);
		arg0.getServletContext().setAttribute("timer", ses);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		ServletContext servletContext = arg0.getServletContext();
		ScheduledExecutorService ses = (ScheduledExecutorService) servletContext.getAttribute("timer");
		ses.shutdown();
	}

}