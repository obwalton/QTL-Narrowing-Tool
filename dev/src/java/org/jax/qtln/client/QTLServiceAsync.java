/*
 * QTLServiceAsync.java
 *
 * Created on February 18, 2010, 8:58 AM
 *
 */

package org.jax.qtln.client;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 *
 * @author dow
 */
public interface QTLServiceAsync {
    public void readQTLFile(AsyncCallback<List<String[]>> callback);
}
