package org.calgen.sparql;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
// Create a query execution over DBpedia
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");

        qef = new QueryExecutionFactoryRetry(qef, 5, 10000);

        // Add delay in order to be nice to the remote server (delay in milli seconds)
        qef = new QueryExecutionFactoryDelay(qef, 5000);

        // Set up a cache
        // Cache entries are valid for 1 day
        long timeToLive = 24l * 60l * 60l * 1000l;

        // This creates a 'cache' folder, with a database file named 'sparql.db'
        // Technical note: the cacheBackend's purpose is to only deal with streams,
        // whereas the frontend interfaces with higher level classes - i.e. ResultSet and Model

//		CacheBackend cacheBackend = CacheCoreH2.create("sparql", timeToLive, true);
//		CacheFrontend cacheFrontend = new CacheFrontendImpl(cacheBackend);
//		qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);


        QueryExecutionFactoryHttp foo = qef.unwrap(QueryExecutionFactoryHttp.class);
        System.out.println(foo);

        // Add pagination
        qef = new QueryExecutionFactoryPaginated(qef, 900);

        // Create a QueryExecution object from a query string ...
        QueryExecution qe = qef.createQueryExecution("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "PREFIX type: <http://dbpedia.org/class/yago/>\n" +
                "PREFIX prop: <http://dbpedia.org/property/>\n" +
                "SELECT DISTINCT ?country_name ?population\n" +
                "WHERE {\n" +
                "?country a <http://dbpedia.org/ontology/Country> ;\n" +
                "rdfs:label ?country_name ;\n" +
                "prop:populationEstimate ?population .\n" +
                "FILTER (?population > 950000000 && langMatches(lang(?country_name),\'en\')) .\n" +
                "}Limit 5");


        // and run it.
        ResultSet rs = qe.execSelect();
        System.out.println(ResultSetFormatter.asText(rs));

    }
}
