package org.jax.qtln.server;



import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.jax.qtln.client.GWTRegion;
import org.jax.qtln.client.QTLService;
import org.jax.qtln.client.SMSException;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class QTLServiceImpl extends RemoteServiceServlet implements
        QTLService {

    public List<String[]> readQTLFile()
            throws SMSException {

        System.out.println("IN readQTLFile");
        // Check the HttpSession to see if a sequence file was uploaded
        HttpSession session = this.getSession();
        HttpServletRequest hsr = this.getThreadLocalRequest();
        System.out.println("Got session info");

        //  The actual logic for running the analysis is in a separate
        //  class from the actual servlet.  I did this because the servlet
        //  class is shared by all users, and I wanted to use class attributes
        //  that were not shared.
        QTLFileReader qfr = new QTLFileReader();
        System.out.println("Got reader, now read");
        return qfr.readQTLFile(session, hsr);

    }

    public Map<String, List<GWTRegion>> getSmallestCommonRegions(List<List> qtls)
            throws SMSException 
    {
        System.out.print("In getSmallestCommonRegions");
        //  The actual logic for running the analysis is in a separate
        //  class from the actual servlet.  I did this because the servlet
        //  class is shared by all users, and I wanted to use class attributes
        //  that were not shared.
        QTLSet qtlSet = new QTLSet();
        for (List qtl : qtls) {
            try {
                qtlSet.addQTL(qtl);
            } catch (InvalidChromosomeException e) {
                throw new SMSException(e.getMessage());
            }
        }
        SmallestCommonRegion scr = new SmallestCommonRegion(qtlSet);
        Map<String, List<Region>> regions = scr.getRegions();
        Set<String> keys = regions.keySet();
        HashMap<String, List<GWTRegion>> results =
                new HashMap<String, List<GWTRegion>>();
        for (String key : keys) {
            ArrayList gwtRegions = new ArrayList<GWTRegion>();
            for (Region region: regions.get(key)) {
                gwtRegions.add(regionToGWTRegion(region));
            }
            results.put(key, (List)gwtRegions);
        }
        Map<String, List<GWTRegion>> generic_results = (Map<String, List<GWTRegion>>)results;
        return generic_results;

    }

    private GWTRegion regionToGWTRegion (Region region) {
        GWTRegion gwtr = new GWTRegion();
        gwtr.setBuild(region.getBuild());
        gwtr.setChromosome(region.getChromosome());
        gwtr.setEnd(region.getEnd());
        gwtr.setStart(region.getStart());
        return gwtr;
    }

    /**
     * Returns the current session
     *
     * @return  The current Session
     */
    private HttpSession getSession() {
        // Get the current request and then return its session
        return this.getThreadLocalRequest().getSession();
    }

}