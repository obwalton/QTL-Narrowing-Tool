package org.jax.qtln.server;



import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
       // Check the HttpSession to see if a sequence file was uploaded
       HttpSession session = this.getSession();
       HttpServletRequest hsr = this.getThreadLocalRequest();

       //  The actual logic for running the analysis is in a separate
       //  class from the actual servlet.  I did this because the servlet
       //  class is shared by all users, and I wanted to use class attributes
       //  that were not shared.
       QTLFileReader qfr = new QTLFileReader();
       return qfr.readQTLFile(session, hsr);

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