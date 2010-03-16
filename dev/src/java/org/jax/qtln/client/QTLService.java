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

/**
 *
 * @author dow
 */
@RemoteServiceRelativePath("qtl")
public interface QTLService extends RemoteService{
    public List<String[]> readQTLFile() throws Throwable;
}
