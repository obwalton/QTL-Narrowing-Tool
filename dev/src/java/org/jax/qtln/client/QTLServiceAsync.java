/*
 * QTLServiceAsync.java
 *
 * Created on February 18, 2010, 8:58 AM
 *
 */

package org.jax.qtln.client;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jax.qtln.regions.ReturnRegion;


/**
 *
 * @author dow
 */
public interface QTLServiceAsync {
    public void readQTLFile(AsyncCallback<List<String[]>> callback);

    public void narrowQTLs(List<List> qtls, boolean doGEX,
            AsyncCallback<Map<String, List<ReturnRegion>>> callback);
    public void getNarrowingStatus(AsyncCallback<String> callback);
    public void getStrains(AsyncCallback<String[]> callback);
    public void getSnpAnnotLookup(AsyncCallback<Map<Integer,String>> callback);


}
