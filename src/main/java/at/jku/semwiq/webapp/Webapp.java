/**
 */
package at.jku.semwiq.webapp;

import javax.servlet.ServletContext;

import org.joseki.RDFServer;
import org.joseki.Service;
import org.joseki.ServiceRegistry;
import org.joseki.processors.SPARQL;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.engine.describe.MediatorDescribeHandlerFactory;

import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry;

/**
 * @author dorgon
 *
 */
public class Webapp {
	// mediator reference handling
	public static final String MEDIATOR_REF = MediatorImpl.class.getCanonicalName();

	public static void putIntoServletContext(Mediator mediator, ServletContext context) {
		context.setAttribute(MEDIATOR_REF, mediator);
	}
	
	public static Mediator fromServletContext(ServletContext context) {
		return (Mediator) context.getAttribute(MEDIATOR_REF);
	}

//	/**
//	 * register SemWIQ into Joseki
//	 */
//	public static void registerAsJosekiService(Mediator m) {
//		
//		// describe handler
//		DescribeHandlerRegistry.get().add(new MediatorDescribeHandlerFactory(m));
//		
//		// service endpoint
//		ServiceRegistry services = new ServiceRegistry();
//		String context = m.getConfiguration().getGlobalSPARQLEndpoint();
//		Service service = new Service(new SPARQL(), context, new SemWIQDatasetDesc(m));
//		services.add(context, service);
//
//		org.joseki.Registry.remove(RDFServer.ServiceRegistryName);
//		org.joseki.Registry.add(RDFServer.ServiceRegistryName, services);
//	}

}
