package listener;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ScheduleNetworkMapListener implements ServletContextListener {
	
	private static Properties config = new Properties();
	
	static {
		try {
			config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("map.conf"));
		} catch (IOException e) {
			throw new ExceptionInInitializerError("Cannot load properties file.");
		}
	}
	
	public static Integer getPeriod(){
		return Integer.parseInt(config.getProperty("period"));
	}
	
	public static Integer getInitialDelay(){
		return Integer.parseInt(config.getProperty("initial_delay"));
	}
	

	
	 	@Override
		public void contextInitialized(ServletContextEvent arg0) {
	 		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	 		final String dataPath = arg0.getServletContext().getRealPath("mesh/data");
			final String v2Path = arg0.getServletContext().getRealPath("mesh/v2");
	 		System.out.println("saved data in:"+dataPath);
		 	Runnable task = () -> {
				try {
					org.riv.crawler.NetworkDHTCrawler.run(dataPath, v2Path);
				} catch (ClassNotFoundException | InterruptedException | ExecutionException | IOException e) {
					e.printStackTrace();
				}
			};

			ses.scheduleAtFixedRate(task, getInitialDelay(), getPeriod(), TimeUnit.SECONDS);

      arg0.getServletContext().setAttribute("timer", ses);
		}

	    @Override
	    public void contextDestroyed(ServletContextEvent arg0) {
	    	ServletContext servletContext = arg0.getServletContext();
	    	ScheduledExecutorService ses = (ScheduledExecutorService) servletContext.getAttribute("timer");
	    	ses.shutdown();
	    }

 }
