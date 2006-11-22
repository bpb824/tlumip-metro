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

package com.pb.tlumip.ts.assign.tests;

/**
 *
 * @author    Jim Hicks
 * @version   1.0, 6/14/2006
 */

import com.pb.tlumip.ts.NetworkHandler;
import com.pb.tlumip.ts.NetworkHandlerIF;
import com.pb.tlumip.ts.transit.AuxTrNet;
import com.pb.tlumip.ts.transit.OpStrategy;
import com.pb.tlumip.ts.transit.TrRoute;
import com.pb.common.datafile.DataReader;
import com.pb.common.datafile.DataWriter;
import com.pb.common.util.ResourceUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.io.File;
import java.text.DateFormat;

import org.apache.log4j.Logger;



public class OpStrategyTest {

	protected static Logger logger = Logger.getLogger(OpStrategyTest.class);

	
	static final boolean CREATE_NEW_NETWORK = true;
//    static final int START_NODE = 24944;
    static final int START_NODE = 854;
	static final int END_NODE = 4;
	
	
	public static final String AUX_TRANSIT_NETWORK_LISTING = "c:\\jim\\projects\\tlumip\\debugTransit\\aux_transit_net.listing";

    NetworkHandlerIF nh = null;
	
	HashMap tsPropertyMap = null;
    HashMap globalPropertyMap = null;
    ResourceBundle rb = null;
    ResourceBundle globalRb = null;
    

    
	public OpStrategyTest( ResourceBundle rb, ResourceBundle globalRb, HashMap tsPropertyMap, HashMap globalPropertyMap ) {
		
        this.tsPropertyMap = tsPropertyMap;
        this.globalPropertyMap = globalPropertyMap;
        this.rb = rb;
        this.globalRb = globalRb;

	}
    
	
	
    public static void main (String[] args) {

    	ResourceBundle rb = ResourceUtil.getPropertyBundle( new File("/jim/util/svn_workspace/projects/tlumip/config/debugTransit.properties") );
    	HashMap propertyMap = ResourceUtil.changeResourceBundleIntoHashMap(rb);

        ResourceBundle globalRb = ResourceUtil.getPropertyBundle( new File("/jim/util/svn_workspace/projects/tlumip/config/globalTest.properties") );
    	HashMap globalPropertyMap = ResourceUtil.changeResourceBundleIntoHashMap(globalRb);

    	logger.info ("building transit network");
        OpStrategyTest test = new OpStrategyTest(rb, globalRb, propertyMap, globalPropertyMap);
		
		/*
		 * specify period and accessmode for the optimal strategy to be built:
		 * 
		 * period: "peak" or "offpeak"
		 * accessMode: "walk" or "drive"
		 *
		 * 0: in-vehicle time Matrix
		 * 1: first wait Matrix
		 * 2: total wait Matrix
		 * 3: access time Matrix
		 * 4: boardings Matrix
		 * 5: cost Matrix
		 *  
		 */
        
        test.buildStrategyTest ( "peak", "walk" );
		
    }

    
    
    private AuxTrNet getTransitNetwork( String period, String accessMode ) {
        
        AuxTrNet ag = null; 
        String diskObjectFileName = null;
        
        // create a new transit network from d211 highway network file and d221 transit routes file, or read it from DiskObject.
        String key = period + accessMode + "TransitNetwork";
        String path = (String) tsPropertyMap.get( "diskObject.pathName" );
        if ( path.endsWith("/") || path.endsWith("\\") )
            diskObjectFileName = path + key + ".diskObject";
        else
            diskObjectFileName = path + "/" + key + ".diskObject";
        
        if ( CREATE_NEW_NETWORK ) {
            ag = createTransitNetwork ( period, accessMode );
            DataWriter.writeDiskObject ( ag, diskObjectFileName, key );
        }
        else {
            ag = (AuxTrNet) DataReader.readDiskObject ( diskObjectFileName, key );
        }

        return ag;
        
    }
    
    
    
	private void buildStrategyTest ( String period, String accessMode ) {
        
        AuxTrNet ag = getTransitNetwork( period, accessMode );
        
		// create an optimal strategy object for this highway and transit network
		OpStrategy os = new OpStrategy( ag );

		int[] nodeIndex = ag.getHighwayNetworkNodeIndex();
		os.buildStrategy( nodeIndex[END_NODE] );
        
		os.getOptimalStrategyWtSkimsOrigDest( START_NODE, END_NODE );
		
	}
    

	
	private AuxTrNet createTransitNetwork ( String period, String accessMode ) {
        

		// create a highway network oject
        logger.info ("creating peak Highway Network object for transit assignment.");
        nh = NetworkHandler.getInstance();
        nh.setup( rb, globalRb, period );
        logger.info (nh.getLinkCount() + " highway links");
        logger.info (nh.getNodeCount() + " highway nodes");


        // get the filename for the route files
        String d221File = null;
        if ( period.equalsIgnoreCase( "peak" ) )
            d221File = (String) tsPropertyMap.get( "d221.pk.fileName" );
        else if ( period.equalsIgnoreCase( "offpeak" ) )
            d221File = (String) tsPropertyMap.get( "d221.op.fileName" );

        if ( d221File == null ) {
            RuntimeException e = new RuntimeException();
            logger.error ( "Error reading routes file for specified " + period + " period.", e );
            throw e;
        }
        
        // read parameter for maximum number of transit routes
        int maxRoutes = Integer.parseInt ( (String)tsPropertyMap.get("MAX_TRANSIT_ROUTES") );

		// create transit routes object
		TrRoute tr = new TrRoute ( maxRoutes );

		//read transit route info from Emme/2 for d221 file for the specified time period
	    tr.readTransitRoutes ( d221File );
		    
		// associate transit segment node sequence with highway link indices
		tr.getLinkIndices (nh.getNetwork());



		// create an auxilliary transit network object
        AuxTrNet ag = new AuxTrNet(nh, tr);

		// build the auxilliary links for the given transit routes object
		ag.buildAuxTrNet ( accessMode );
		
		// define the forward star index arrays, first by anode then by bnode
		ag.setForwardStarArrays ();
		ag.setBackwardStarArrays ();

        
//		ag.printAuxTrLinks (24, tr);
		ag.printAuxTranNetwork( AUX_TRANSIT_NETWORK_LISTING );
//		ag.printTransitNodePointers();

		String myDateString = DateFormat.getDateTimeInstance().format(new Date());
		logger.info ("done creating transit network AuxTrNetTest: " + myDateString);

		return ag;
	}
    
}
