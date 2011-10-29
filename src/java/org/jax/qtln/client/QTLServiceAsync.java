/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    public void narrowQTLs(List<List> qtls, boolean doGEX, String gexExp,
            AsyncCallback<Map<String,List<Map<String,Object>>>> callback);
    public void getNarrowingStatus(AsyncCallback<String> callback);
    public void getStrains(AsyncCallback<String[]> callback);
    public void getSnpAnnotLookup(AsyncCallback<Map<Integer,String>> callback);
    public void getRegion(String chromosome, String rangeKey,
            AsyncCallback<ReturnRegion> callback);

    public void clearAnalysis(AsyncCallback<Boolean> callback);
    public void searchPhenotypesForQTLs(String searchString, 
            AsyncCallback<List<Map<String,String>>> callback);
    public void exportTable(List<String[]> rows,
            AsyncCallback<Boolean> callback);

}
