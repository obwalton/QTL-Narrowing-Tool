/*
 * QTLService.java
 *
 * Created on February 18, 2010, 8:56 AM
 *
 */

package org.jax.qtln.client;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.Map;
import org.jax.qtln.regions.ReturnRegion;

/**
 *
 * @author dow
 */
@RemoteServiceRelativePath("qtl")
public interface QTLService extends RemoteService{
    public List<String[]> readQTLFile() throws Throwable;
    public Map<String, List<ReturnRegion>> narrowQTLs(List<List> qtls)
            throws Throwable;
    public String getNarrowingStatus() throws Throwable;
    public String[] getStrains() throws Throwable;
}
