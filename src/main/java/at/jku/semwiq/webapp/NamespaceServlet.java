package at.jku.semwiq.webapp;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorImpl;

/**
 * based on D2R NamespaceServlet
 * (C) FU Berlin, http://www4.wiwiss.fu-berlin.de/bizer/d2r-server/
 * 
 * @author dorgon, Andreas Langegger, al@jku.at
 */
public class NamespaceServlet extends HttpServlet {

	private static final long serialVersionUID = -8120427087150998738L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException {
    	Mediator mediator = Webapp.fromServletContext(getServletContext());
    	if (!(mediator != null && mediator.isReady())) {
			getServletContext().getRequestDispatcher("/not-available.jsp").forward(request, response);
			return;
		}

		response.setContentType("text/javascript");
		ServletOutputStream out = response.getOutputStream();
		Map<String, String> map = mediator.getConfig().getGuiConfig().getPrefixMapping().getNsPrefixMap();
		out.println("// Generated dynamically from the mapping file");
		out.println("var D2R_namespacePrefixes = {");
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			out.print("\t\"" + key + "\": \"" + map.get(key) + "\"");
			if (it.hasNext())
				out.print(",");
			out.println();
		}
		out.println("};");
	}
	
}
