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


/**
 *
 * @author dow
 */
public interface QTLServiceAsync {
    public void readQTLFile(AsyncCallback<List<String[]>> callback);

    public void getSmallestCommonRegions(List<List> qtls, AsyncCallback<Map<String, List<GWTRegion>>> callback);

}
