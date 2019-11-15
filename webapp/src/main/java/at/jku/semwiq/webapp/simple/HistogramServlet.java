/**
 */
package at.jku.semwiq.webapp.simple;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.jku.rdfstats.RDFStatsDataset;
import at.jku.rdfstats.RDFStatsModel;
import at.jku.rdfstats.RDFStatsModelException;
import at.jku.rdfstats.html.GenerateHTML;
import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.webapp.Webapp;

/**
 * @author dorgon
 *
 */
public class HistogramServlet extends HttpServlet {
	protected static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			Mediator mediator = Webapp.fromServletContext(getServletContext());
			RDFStatsModel stats = mediator.getDataSourceRegistry().getRDFStatsModel();
			String uri = req.getParameter("endpointUri");
			RDFStatsDataset ds;
			if (uri != null && uri.length() > 0) {
				ds = stats.getDataset(uri);
				if (ds != null)
					res.setHeader("Last-Modified", httpDateFormat.format(ds.getDate()));
			}
			
			String html = GenerateHTML.generateHTML(mediator.getDataSourceRegistry().getRDFStatsModel(), uri);
			res.setContentType("text/html");
			res.getOutputStream().println(html);
			res.flushBuffer();
		} catch (RDFStatsModelException e) {
			throw new ServletException("Failed to generate statistics.", e);
		}
	}

}
