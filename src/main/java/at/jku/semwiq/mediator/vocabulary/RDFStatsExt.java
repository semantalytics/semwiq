/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Andreas Langegger, Johannes Kepler University Linz, Austria
 * Author email       al@jku.at
 * Package            @package@
 * Web site           @website@
 * Created            24 Aug 2009 16:38
 * @copyright@
 *****************************************************************************/

// Package
///////////////////////////////////////
package at.jku.semwiq.mediator.vocabulary;

// Imports
///////////////////////////////////////
import com.hp.hpl.jena.rdf.model.*;


/**
 * Vocabulary definitions from file:vocabulary/purl.org/semwiq/mediator/rdfstats-ext.n3
 * @author Auto-generated by schemagen on 24 Aug 2009 16:38
 */
public class RDFStatsExt {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/semwiq/mediator/rdfstats-ext#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    



    /* Vocabulary properties */

    /** <p>specifies the timestamp of the last time when the statistics have been downloaded 
     *  from a remote server</p>
     */
    public static final Property lastDownload = m_model.createProperty( "http://purl.org/semwiq/mediator/rdfstats-ext#lastDownload" );
    

    /* Vocabulary classes */


    /* Vocabulary individuals */

}

/*
@footer@
*/

