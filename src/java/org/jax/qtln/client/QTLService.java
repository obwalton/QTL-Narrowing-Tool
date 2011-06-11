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
    public Map<String,List<Map<String,Object>>> narrowQTLs(List<List> qtls,
            boolean doGEX, String gexExp)
            throws Throwable;
    public String getNarrowingStatus() throws Throwable;
    public String[] getStrains() throws Throwable;
    public Map<Integer,String> getSnpAnnotLookup() throws Throwable;
    public ReturnRegion getRegion(String chromosome, String rangeKey)
            throws Throwable;
    public Boolean clearAnalysis() throws Throwable;
    public List<Map<String,String>> searchPhenotypesForQTLs(String searchString) 
            throws Throwable;

}
