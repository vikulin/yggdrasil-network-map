package listener;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ScheduleYggdrasilNetworkMapListener implements ServletContextListener {
	
	
	 	@Override
		public void contextInitialized(ServletContextEvent arg0) {
	 		ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	 		final String dataPath = arg0.getServletContext().getRealPath("/data");
		 	Runnable task2 = () -> {
				try {
					demo.more.NetworkDHTCrawler.run(dataPath);
				} catch (ClassNotFoundException | InterruptedException | ExecutionException | IOException e) {
					e.printStackTrace();
				}
			};
			ses.scheduleAtFixedRate(task2, 1, 60, TimeUnit.MINUTES);
			arg0.getServletContext().setAttribute ("timer", ses);
		}

	    @Override
	    public void contextDestroyed(ServletContextEvent arg0) {
	    	ServletContext servletContext = arg0.getServletContext();
	    	ScheduledExecutorService ses = (ScheduledExecutorService) servletContext.getAttribute("timer");
	    	ses.shutdown();
	    }

 }