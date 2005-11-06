/*
 * Copyright  2005 PB Consult Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.pb.tlumip.ts;

/**
 *
 * @author    Jim Hicks
 * @version   1.0, 6/30/2004
 */


import com.pb.common.rpc.RpcClient;
import com.pb.common.rpc.RpcException;
import com.pb.common.rpc.RpcHandler;
import com.pb.common.util.ResourceUtil;

//import com.pb.tlumip.ts.AonFlowResults;
import com.pb.tlumip.ts.assign.NoPathFoundException;

import java.util.HashMap;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;



public class AonFlowHandler implements RpcHandler {

    public static String remoteHandlerName = "aonFlowHandler";
    
	protected static Logger logger = Logger.getLogger("com.pb.tlumip.ts.ShortestPathTreeHandler");

//    private AonFlowResults flowResults = AonFlowResults.getInstance();

    RpcClient demandHandlerClient;    
    RpcClient networkHandlerClient;
    RpcClient shortestPathHandlerClient;

    int numLinks;
    int numCentroids;
    int numUserClasses;
    int lastOriginTaz;
    int startOriginTaz;
    int[] ia;
    int[] indexNode;

    String componentPropertyName;
    String globalPropertyName;
    
	HashMap componentPropertyMap;
    HashMap globalPropertyMap;

    ResourceBundle appRb;
    ResourceBundle globalRb;
    


	public AonFlowHandler() {

        String handlerName = null;
        
        try {
            
            //Create RpcClients this class connects to
            try {

                handlerName = NetworkHandler.remoteHandlerName;
                networkHandlerClient = new RpcClient( handlerName );

                handlerName = DemandHandler.remoteHandlerName;
                demandHandlerClient = new RpcClient( handlerName );
                
                handlerName = ShortestPathTreeHandler.remoteHandlerName;
                shortestPathHandlerClient = new RpcClient( handlerName );
                
            }
            catch (MalformedURLException e) {
            
                logger.error ( "MalformedURLException caught in ShortestPathTreeH() while defining RpcClients.", e );
            
            }

        }
        catch ( Exception e ) {
            logger.error ( "Exception caught in ShortestPathTreeH().", e );
            System.exit(1);
        }

    }
    


    
    public Object execute (String methodName, Vector params) throws Exception {
                  
        if ( methodName.equalsIgnoreCase( "setup" ) ) {
            HashMap componentPropertyMap = (HashMap)params.get(0);
            HashMap globalPropertyMap = (HashMap)params.get(1);
            setup( componentPropertyMap, globalPropertyMap );
            return 0;
        }
        else if ( methodName.equalsIgnoreCase( "getMulticlassAonLinkFlows" ) ) {
            return getMulticlassAonLinkFlows();
        }
        else {
            logger.error ( "method name " + methodName + " called from remote client is not registered for remote method calls.", new Exception() );
            return 0;
        }
        
    }
    

    
    
    public void setup( HashMap componentPropertyMap, HashMap globalPropertyMap ) {
        
        this.componentPropertyMap = componentPropertyMap;
        this.globalPropertyMap = globalPropertyMap; 
        
        getNetworkParameters ();
    }
    
    
    public void setup( ResourceBundle componentRb, ResourceBundle globalRb ) {
        

        this.appRb = componentRb;
        this.globalRb = globalRb;
        
        this.componentPropertyMap = ResourceUtil.changeResourceBundleIntoHashMap( componentRb );
        this.globalPropertyMap = ResourceUtil.changeResourceBundleIntoHashMap( globalRb );

        getNetworkParameters ();
    }
    
    
    
    private void getNetworkParameters () {
        
        try {
            
            startOriginTaz = 0;
            lastOriginTaz = networkHandlerGetNumCentroidsRpcCall();
            numLinks = networkHandlerGetLinkCountRpcCall();
            numCentroids = networkHandlerGetNumCentroidsRpcCall();
            numUserClasses = networkHandlerGetNumUserClassesRpcCall();
            
            ia = networkHandlerGetIaRpcCall();
            indexNode = networkHandlerGetIndexNodeRpcCall();

        }
        catch ( RpcException e ) {
            logger.error ( "RpcException caught.", e );
            System.exit(1);
        }
        catch ( IOException e ) {
            logger.error ( "IOException caught.", e );
            System.exit(1);
        }
        catch ( Exception e ) {
            logger.error ( "Exception caught.", e );
            System.exit(1);
        }
        
    }
    
    

    private double[][] getMulticlassAonLinkFlows () {

        int m=0;
        int origin=0;
        
        // initialize the AON Flow arrays to zero
        double[] aon;
        double[][] aonFlow = new double[numUserClasses][numLinks];

        double[][] tripTableRowSums = null;
        
        
        
        // get the trip table row sums by user class
        try {
            tripTableRowSums = demandHandlerGetTripTableRowSumsRpcCall();
        }
        catch ( Exception e ) {
            logger.error ( "Exception caught.", e );
            System.exit(1);
        }
        

        
        try {
            
            for (m=0; m < numUserClasses; m++) {
                
                for (origin=startOriginTaz; origin < lastOriginTaz; origin++) {
                
                    if (tripTableRowSums[m][origin] > 0.0) {
                        
                        double[] tripTableRow = demandHandlerGetTripTableRowRpcCall(m, origin);

                        int[] predecessorLink = shortestPathHandlerGetPredecessorLinkArrayRpcCall(m, origin);

                        aon = loadTree ( numCentroids, numLinks, origin, predecessorLink, tripTableRow );
                        for (int k=0; k < numLinks; k++)
                            aonFlow[m][k] += aon[k];
                        
                    }
                    
                }
                
            }
            
        }
        catch ( NoPathFoundException e ) {
            logger.error ("no path from " + indexNode[origin] + " to " + indexNode[e.code] + " for userClass " + m + ".", e );
            System.exit(1);
        }
        catch ( Exception e ) {
            logger.error ( "Exception caught.", e );
            System.exit(1);
        }
        
        return aonFlow;
        
    }


    
    
    /**
     * Load trips from the trip table row associated with the shortest
     * path tree origin
     */
    public double[] loadTree ( int numZones, int numLinks, int inOrigin, int[] predecessorLink, double[] tripRow ) throws NoPathFoundException {

        double[] aonFlow = new double[numLinks];
        
        int k;
        for (int j=0; j < numZones; j++) {
            if ( tripRow[j] > 0 && j != inOrigin ) {
                
                k = predecessorLink[j];
                
                if (k == -1) {
                    throw new NoPathFoundException( j, "no path found exception" );
                }
                
                aonFlow[k] += tripRow[j];
                
                while (ia[k] != inOrigin) {
                    k = predecessorLink[ia[k]];
                    aonFlow[k] += tripRow[j];
                }
            }
        }
        
        return aonFlow;
        
    }
    


    private int networkHandlerGetNumCentroidsRpcCall() throws Exception {
        // g.getNumCentroids()
        return (Integer)networkHandlerClient.execute("networkHandler.getNumCentroids", new Vector());
    }

    private int networkHandlerGetLinkCountRpcCall() throws Exception {
        // g.getLinkCount()
        return (Integer)networkHandlerClient.execute("networkHandler.getLinkCount", new Vector() );
    }

    private int networkHandlerGetNumUserClassesRpcCall() throws Exception {
        // g.getNumUserClasses()
        return (Integer)networkHandlerClient.execute("networkHandler.getNumUserClasses", new Vector() );
    }

    private int[] networkHandlerGetIaRpcCall() throws Exception {
        // g.getIa()
        return (int[])networkHandlerClient.execute("networkHandler.getIa", new Vector() );
    }

    private int[] networkHandlerGetIndexNodeRpcCall() throws Exception {
        // g.getIndexNode()
        return (int[])networkHandlerClient.execute("networkHandler.getIndexNode", new Vector() );
    }

    
    
    
    private double[] demandHandlerGetTripTableRowRpcCall( int userClass, int origin ) throws Exception {
        Vector params = new Vector();
        params.add( userClass );
        params.add( origin );
        return (double[])demandHandlerClient.execute("demandHandler.getTripTableRow", params );
    }
    
    private double[][] demandHandlerGetTripTableRowSumsRpcCall() throws Exception {
        return (double[][])demandHandlerClient.execute("demandHandler.getTripTableRowSums", new Vector() );
    }
    
    
    
    
    private int[] shortestPathHandlerGetPredecessorLinkArrayRpcCall( int userClass, int origin ) throws Exception {
        Vector params = new Vector();
        params.add( userClass );
        params.add( origin );
        return (int[])shortestPathHandlerClient.execute("shortestPathTreeHandler.getPredecessorLinkArray", params );
    }
    
}
