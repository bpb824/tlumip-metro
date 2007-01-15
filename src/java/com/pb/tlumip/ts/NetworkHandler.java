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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.pb.common.datafile.DataReader;
import com.pb.common.rpc.DafNode;

import com.pb.tlumip.ts.assign.Network;

/**
 * @author   Jim Hicks  
 * @version  Nov 1, 2006
 */
public class NetworkHandler implements NetworkHandlerIF, Serializable {

    protected static transient Logger logger = Logger.getLogger(NetworkHandler.class);

    Network g = null;
    String rpcConfigFile = null;

    
    public NetworkHandler() {
    }

    
    // Factory Method to return either local or remote instance
    public static NetworkHandlerIF getInstance( String rpcConfigFile ) {
    
        if ( rpcConfigFile == null ) {

            // if rpc config file is null, then all handlers are local, so return local instance
            return new NetworkHandler();

        }
        else {
            
            // return either a local instance or an rpc instance depending on how the handler was defined.
            Boolean isLocal = DafNode.getInstance().isHandlerLocal( HANDLER_NAME );

            if ( isLocal == null )
                // handler name not found in config file, so create a local instance.
                return new NetworkHandler();
            else 
                // handler name found in config file but is not local, so create an rpc instance.
                return new NetworkHandlerRpc( rpcConfigFile );

        }
        
    }
    

    // Factory Method to return local instance only
    public static NetworkHandlerIF getInstance() {
        return new NetworkHandler();
    }

    
    
    
    
    public int setRpcConfigFileName(String configFile) {
        this.rpcConfigFile = configFile;
        return 1;
    }

    public String getRpcConfigFileName() {
        return rpcConfigFile;
    }
    
    public int getNumCentroids() {
        return g.getNumCentroids();
    }
    
    public int getMaxCentroid() {
        return g.getMaxCentroid();
    }
    
    public boolean[] getCentroid() {
        return g.getCentroid();
    }
    
    public List getCentroidRpc() {
        return Util.booleanVector( g.getCentroid() );
    }
    
    public int getNodeCount() {
        return g.getNodeCount();
    }
    
    public int getLinkCount() {
        return g.getLinkCount();
    }
    
    public int getNumUserClasses() {
        return g.getNumUserClasses();
    }
    
    public String getTimePeriod () {
        return g.getTimePeriod();
    }

    public boolean userClassesIncludeTruck() {
        return g.userClassesIncludeTruck();
    }
    
    public boolean[][] getValidLinksForAllClasses () {
        return g.getValidLinksForAllClasses ();
    }

    public List getValidLinksForAllClassesRpc() {
        return Util.boolean2Vector( g.getValidLinksForAllClasses() );
    }

    public boolean[] getValidLinksForClass ( int userClass ) {
        return g.getValidLinksForClass ( userClass );
    }

    public List getValidLinksForClassRpc( int userClass ) {
        return Util.booleanVector( g.getValidLinksForClass(userClass) );
    }

    public boolean[] getValidLinksForClass ( char modeChar ) {
        return g.getValidLinksForClass ( modeChar );
    }
    
    public List getValidLinksForClassRpc( char modeChar ) {
        return Util.booleanVector( g.getValidLinksForClass(modeChar) );
    }
    
    public int[] getNodeIndex () {
        return g.getNodeIndex();
    }

    public List getNodeIndexRpc() {
        return Util.intVector( g.getNodeIndex() );
    }

    public int[] getLinkType () {
        return g.getLinkType();
    }

    public List getLinkTypeRpc() {
        return Util.intVector( g.getLinkType() );
    }

    public char[][] getAssignmentGroupChars() {
        return g.getAssignmentGroupChars();
    }

    public List getAssignmentGroupCharsRpc() {
        return Util.char2Vector( g.getAssignmentGroupChars() );
    }

    public double[] getCongestedTime () {
        return g.getCongestedTime();
    }

    public List getCongestedTimeRpc() {
        return Util.doubleVector( g.getCongestedTime() );
    }

    public double[] getTransitTime () {
        return g.getTransitTime();
    }

    public List getTransitTimeRpc() {
        return Util.doubleVector( g.getTransitTime() );
    }

    public double[] getDist () {
        return g.getDist();
    }

    public List getDistRpc() {
        return Util.doubleVector( g.getDist() );
    }

    public String getAssignmentResultsString () {
        return g.getAssignmentResultsString();
    }
    
    public String getAssignmentResultsTimeString () {
        return g.getAssignmentResultsTimeString();
    }
    
    public double[] setLinkGeneralizedCost () {
        return g.setLinkGeneralizedCost ();
    }

    public List setLinkGeneralizedCostRpc() {
        return Util.doubleVector( g.setLinkGeneralizedCost() );
    }

    public int setFlows (double[][] flow) {
        g.setFlows( flow );
        return 1;
    }
    
    public int setFlowsRpc(ArrayList flowList) {
        double[][] flow = Util.double2Array(flowList);
        g.setFlows( flow );
        return 1;
    }
    
    public int setVolau (double[] volau) {
        g.setVolau( volau );
        return 1;
    }
    
    public int setVolauRpc(ArrayList volauList) {
        double[] volau = Util.doubleArray(volauList);
        g.setVolau( volau );
        return 1;
    }
    
    public int setTimau (double[] timau) {
        g.setTimau( timau );
        return 1;
    }
    
    public int setTimauRpc(ArrayList timauList) {
        double[] timau = Util.doubleArray(timauList);
        g.setTimau( timau );
        return 1;
    }
    
    public int setVolCapRatios () {
        g.setVolCapRatios ();
        return 1;
    }
    
    public int applyVdfs () {
        g.applyVdfs();
        return 1;
    }
    
    public double applyLinkTransitVdf (int hwyLinkIndex, int transitVdfIndex ) {
        return g.applyLinkTransitVdf(hwyLinkIndex, transitVdfIndex);
    }
    
    public int applyVdfIntegrals () {
        g.applyVdfIntegrals();
        return 1;
    }
    
    public double getSumOfVdfIntegrals () {
        return g.getSumOfVdfIntegrals();
    }
    
    public int logLinkTimeFreqs () {
        g.logLinkTimeFreqs();
        return 1;
    }
    
    public char[] getUserClasses () {
        return g.getUserClasses();
    }
    
    public List getUserClassesRpc() {
        return Util.charVector( g.getUserClasses() );
    }
    
    public String[] getMode () {
        return g.getMode();
    }

    public List getModeRpc() {
        return Util.stringVector( g.getMode() );
    }

    public int[] getIndexNode () {
        return g.getIndexNode();
    }
    
    public List getIndexNodeRpc() {
        return Util.intVector( g.getIndexNode() );
    }
    
    public double[] getNodeX () {
        return g.getNodeX();
    }
    
    public List getNodeXRpc() {
        return Util.doubleVector( g.getNodeX() );
    }
    
    public double[] getNodeY () {
        return g.getNodeY();
    }
    
    public List getNodeYRpc() {
        return Util.doubleVector( g.getNodeY() );
    }
    
    public int[] getIa() {
        return g.getIa();
    }

    public List getIaRpc() {
        return Util.intVector( g.getIa() );
    }

    public int[] getIb() {
        return g.getIb();
    }

    public List getIbRpc() {
        return Util.intVector( g.getIb() );
    }

    public int[] getIpa() {
        return g.getIpa();
    }

    public List getIpaRpc() {
        return Util.intVector( g.getIpa() );
    }

    public int[] getSortedLinkIndexA() {
        return g.getSortedLinkIndexA();
    }

    public List getSortedLinkIndexARpc() {
        return Util.intVector( g.getSortedLinkIndexA() );
    }

    public double getWalkSpeed () {
        return g.getWalkSpeed();
    }

    public int writeNetworkAttributes ( String fileName ) {
        g.writeNetworkAttributes(fileName);
        return 1;
    }
    
    public int checkForIsolatedLinks () {
        g.checkForIsolatedLinks ();
        return 1;
    }
    
    public int buildNetworkObject ( String timePeriod, String[] propertyValues  ) {
        
        try {
            
            String networkFileName = propertyValues[NETWORK_FILENAME_INDEX];
            String networkDiskObjectFileName = propertyValues[NETWORK_DISKOBJECT_FILENAME_INDEX];
            
            // if no network DiskObject file exists, no previous assignments
            // have been done, so build a new Network object which initialize 
            // the congested time field for computing time related skims.
            if ( networkDiskObjectFileName == null || networkDiskObjectFileName.equals("") ) {
                logger.info ( "building a new Network object from " + networkFileName + " for the " + timePeriod + " period." );
                g = new Network( timePeriod, propertyValues );
                return g.getLinkCount();
            }
            // otherwise, read the DiskObject file and use the congested time field
            // for computing time related skims.
            else {
                g = (Network) DataReader.readDiskObject ( networkDiskObjectFileName, "highwayNetwork_" + timePeriod );
            }
            
            return g.getLinkCount();
            
        }
        catch (Exception e){
            
            logger.info ( "error building " + timePeriod + " period highway network object in NetworkHandler.", e );
            return -1;
            
        }
        
    }

    public int buildNetworkObjectRpc( String timePeriod, ArrayList propertyValuesList ) {

        String[] propertyValues = Util.stringArray( propertyValuesList );
        return buildNetworkObject ( timePeriod, propertyValues );
        
    }
}
