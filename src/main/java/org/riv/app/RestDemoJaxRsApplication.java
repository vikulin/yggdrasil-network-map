package org.riv.app;

import org.glassfish.jersey.server.ResourceConfig;
import org.riv.filter.CORSResponseFilter;

/**
 * Registers the components to be used by the JAX-RS application
 * 
 * @author Vadym Vikulin <vadym.vikulin@rivchain.org>
 * 
 */
public class RestDemoJaxRsApplication extends ResourceConfig {


	/**
	 * Register JAX-RS application components.
	 */
	public RestDemoJaxRsApplication() {
        packages("org.riv.mesh.rest");
        // register application resources
        register(CORSResponseFilter.class);
		//EncodingFilter.enableFor(this, GZipEncoder.class);
	}
}
