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
package com.pb.tlumip.ts.transit;

import com.pb.tlumip.ts.NetworkHandlerIF;

import com.pb.common.datafile.CSVFileReader;
import com.pb.common.datafile.TableDataSet;
import com.pb.common.matrix.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;



public class OptimalStrategy {


	protected static Logger logger = Logger.getLogger(OptimalStrategy.class);

	int IVT = -1;
	int FWT = -1;
	int TWT = -1;
    int ACC = -1;
    int AUX = -1;
    int EGR = -1;
	int BRD = -1;
    int FAR = -1;
    int FRQ = -1;
	int NUM_SKIMS = 9;

	static final double COMPARE_EPSILON = 1.0e-07;

	static final double MIN_ALLOCATED_FLOW = 0.00001;
	
	static final int MAX_BOARDING_LINKS = 100;

//	static final double LATITUDE_PER_FEET  = 2.7;
//	static final double LONGITUDE_PER_FEET = 3.6;
	static final double LATITUDE_PER_FEET  = 1.0;
	static final double LONGITUDE_PER_FEET = 1.0;


	NetworkHandlerIF nh;

    Matrix[] skimMatrices = new Matrix[NUM_SKIMS];
    HashMap skimTablesOrder = null;
    
	int dest;
    
    int auxNodeCount;
    int auxLinkCount;
	
	Heap candidateHeap;
	int[] heapContents;

	double[] nodeLabel, nodeFreq, linkLabel;
	boolean[] inStrategy;
	int[] orderInStrategy;
	int[] strategyOrderForLink;
	
	double[] nodeFlow;

    int[] alphaNumberArray = null;
    int[] zonesToSkim = null;
    int[] externalToAlphaInternal = null;
    int[] alphaExternalNumbers = null;
    
    int[] ia = null;
    int[] ib = null;
    int[] ipa = null;
    int[] ipb = null;
    int[] indexa = null;
    int[] indexb = null;
    int[] hwyLink = null;
    int[] trRoute = null;
    double[] accessTime = null;
    double[] walkTime = null;
    double[] waitTime = null;
    double[] dwellTime = null;
    double[] layoverTime = null;
    double[] invTime = null;
    double[] freq = null;
    double[] flow = null;
    int[] linkType = null;
    char[] rteMode = null;
	
	int[] gia;
	int[] gib;
	int[] indexNode;
	int[] nodeIndex;
	double[] gNodeX;
	double[] gNodeY;
	double[] gDist;

	int inStrategyCount;
    double tripsNotLoaded;
	
	boolean classDebug = false;
	
	
	
	public OptimalStrategy ( NetworkHandlerIF nh ) {

		this.nh = nh;
        
        
        auxNodeCount = nh.getAuxNodeCount();
        auxLinkCount = nh.getAuxLinkCount();
        
        
		nodeFlow = new double[auxNodeCount+1];
		nodeLabel = new double[auxNodeCount+1];
		nodeFreq = new double[auxNodeCount+1];
		linkLabel = new double[auxLinkCount+1];
		inStrategy = new boolean[auxLinkCount+1];
		orderInStrategy = new int[auxLinkCount+1];
		strategyOrderForLink = new int[auxLinkCount+1];
		
		//Create a new heap structure to sort candidate node labels
        //candidateHeap = new Heap(auxNodeCount+1);  // old Heap 
        candidateHeap = new Heap( auxLinkCount ); // new SortedSet
		heapContents = new int[auxNodeCount+1];

        ia = nh.getAuxIa();
        ib = nh.getAuxIb();
        ipa = nh.getAuxIpa();
        ipb = nh.getAuxIpb();
        indexa = nh.getAuxIndexa();
        indexb = nh.getAuxIndexb();
        hwyLink = nh.getAuxHwyLink();
        trRoute = nh.getLinkTrRoute();
        rteMode = nh.getRteMode();
        linkType = nh.getAuxLinkType();
        dwellTime = nh.getDwellTime();
        layoverTime = nh.getLayoverTime();
        waitTime = nh.getWaitTime(); 
        walkTime = nh.getWalkTime(); 
        invTime = nh.getInvTime();
        freq = nh.getAuxLinkFreq();
        flow = nh.getAuxLinkFlow();
        
		gia = nh.getIa();
		gib = nh.getIb();
		indexNode = nh.getIndexNode();
		nodeIndex = nh.getNodeIndex();
		gNodeX = nh.getNodeX();
		gNodeY = nh.getNodeY();
		gDist = nh.getDist();
		
	}


	private void initData() {
		Arrays.fill(nodeLabel, AuxTrNet.INFINITY);
		Arrays.fill(nodeFlow, 0.0);
		Arrays.fill(nodeFreq, 0.0);
		Arrays.fill(linkLabel, 0.0);
		Arrays.fill(inStrategy, false);
		Arrays.fill(orderInStrategy, 0);
		Arrays.fill(strategyOrderForLink, -1);

		inStrategyCount = 0;
		candidateHeap.clear();
	}



    
    public void initSkimMatrices ( String zoneCorrespondenceFile, HashMap skimTablesOrder ) {

        this.skimTablesOrder = skimTablesOrder;

        IVT = (Integer)skimTablesOrder.get("ivt");
        FWT = (Integer)skimTablesOrder.get("fwt");
        TWT = (Integer)skimTablesOrder.get("twt");
        ACC = (Integer)skimTablesOrder.get("acc");
        AUX = (Integer)skimTablesOrder.get("egr");
        EGR = (Integer)skimTablesOrder.get("aux");
        BRD = (Integer)skimTablesOrder.get("brd");
        FAR = (Integer)skimTablesOrder.get("far");
        FRQ = (Integer)skimTablesOrder.get("frq");
        
        
        // take a column of alpha zone numbers from a TableDataSet and puts them into an array for
        // purposes of setting external numbers.         */
        try {
            CSVFileReader reader = new CSVFileReader();
            TableDataSet table = reader.readFile(new File(zoneCorrespondenceFile));
            alphaNumberArray = table.getColumnAsInt( 1 );
        } catch (IOException e) {
            logger.fatal("Can't get zone numbers from zonal correspondence file");
            e.printStackTrace();
        }

        // get the list of externals from the NetworkHandler.
        int[] externals = nh.getExternalZoneLabels();

    
        // define which of the total set of centroids are within the Halo area and should have skim trees built
        // include external zones (5000s)
        zonesToSkim = new int[nh.getMaxCentroid()+1];
        externalToAlphaInternal = new int[nh.getMaxCentroid()+1];
        alphaExternalNumbers = new int[nh.getNumCentroids()+1];
        Arrays.fill ( zonesToSkim, 0 );
        Arrays.fill ( externalToAlphaInternal, -1 );
        for (int i=0; i < alphaNumberArray.length; i++) {
            zonesToSkim[alphaNumberArray[i]] = 1;
            externalToAlphaInternal[alphaNumberArray[i]] = i;
            alphaExternalNumbers[i+1] = alphaNumberArray[i];
        }
        for (int i=0; i < externals.length; i++) {
            zonesToSkim[alphaNumberArray.length+i] = 1;
            externalToAlphaInternal[externals[i]] = alphaNumberArray.length+i;
            alphaExternalNumbers[alphaNumberArray.length+i+1] = externals[i];
        }

    }


    
	// This method builds the optimal strategy sub-network for the destination taz passed in.
	// The sub-network is represented by the boolean link field inStrategy[] where true indicates
	// the link is part of the strategy and false indicates it is not.
	public int buildStrategy (int dest) {
		// dest is an internally numbered centroid number from highway network (g).

		int j, k, m, start, end;
        
        boolean debug = classDebug;
		
		double linkImped = 0.0;

		this.dest = dest;
		initData();

		
		nodeLabel[dest] = 0;
		nodeFreq[dest] = 0;
		updateEnteringLabels (dest);

		
		// set the access time array based on access mode
		if (nh.getAccessMode().equalsIgnoreCase("walk")) {
			accessTime = walkTime; 
		}
		else {
			accessTime = nh.getDriveAccTime(); 
		}


		if (debug)
		    logger.info ("building optimal strategy to " + dest + "(" + indexNode[dest] + ")");
		
        
        //while ((k = candidateHeap.remove()) != -1) {  //old Heap
        while ( candidateHeap.size() > 0 ) {

            HeapElement he = candidateHeap.getFirst();
            k = he.getIndex();
            
            if (ia[k] != dest && !inStrategy[k]) {

    			// do not include links into centroids in strategy unless its going into dest.
    			if ( ib[k] < nh.getNumCentroids() && ib[k] != dest ) {
    				inStrategy[k] = false;
    				continue;
    			}
    			
    			
				linkImped = nh.getLinkImped(k);
				
				// log some information about the starting condition of the candidate link being examined
				if ( debug ) {
					logger.info ("");
					
                    // get the highway network link index for the given transit network link index
                    m = hwyLink[k];
                

					logger.info ("k=" + k + ", ag.ia[k]=" + ia[k] + "(g.an=" + (m>=0 ? indexNode[gia[m]] : -1) + "), ag.ib[k]=" + ib[k] + "(g.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + "), linkType=" + linkType[k] + ", trRoute=" + trRoute[k] + "(" + (trRoute[k] >= 0 ? nh.getRouteName(trRoute[k]) : "aux") + ")" );
					logger.info ("nodeLabel[ag.ia=" + ia[k] + "]=" + nodeLabel[ia[k]]);
					logger.info ("nodeLabel[ag.ib=" + ib[k] + "]=" + nodeLabel[ib[k]]);
					logger.info ("nodeFreq[ag.ia=" + ia[k] + "]=" + nodeFreq[ia[k]]);
					logger.info ("nodeFreq[ag.ib=" + ib[k] + "]=" + nodeFreq[ib[k]]);
					logger.info ("ag.freq[k=" + k + "]=" + freq[k]);
					logger.info ("linkImped(k=" + k + ")=" + linkImped);
					
				}

				
				// if the anode's label is at least as big as bnode's label + link's utility, the link is a candidate to be added to strategy; otherwise get next link. 
				if (nodeLabel[ia[k]] >= (nodeLabel[ib[k]] + linkImped)) {
					
					// alighting link
					if (linkType[k] == AuxTrNet.ALIGHTING_TYPE) {

						nodeLabel[ia[k]] = nodeLabel[ib[k]] + linkImped;

						// the in-vehicle transit segment preceding this alighting link has index = k - 1.
						// the in-vehicle transit segment preceding this alighting link which has a dwell time factor has index = k - 4.
						// if the dwellTime for the in-vehicle segment is negative, this segment is the last one for the route,
						// so the dwell time for the in-vehicle segment will be determined by the following auxiliary link.
						if ( dwellTime[k-1] < 0 ) {
							
							// find the auxilliary transit link following this alighting link in the strategy.
							// assign the dwell time for the in-vehicle link to the dwell time calculated for the auxiliary link
							start = ipa[ib[k]];
							j = ib[k] + 1;
							while (ipa[j] == -1)
								j++;
							end = ipa[j];
							for (int i=start; i < end; i++) {
								j = indexa[i];
								if (linkType[j] == AuxTrNet.AUXILIARY_TYPE && inStrategy[j]) {
									// use dwell time factor from in-vehicle link preceding last in-vehicle link in route.
									dwellTime[k-1] = gDist[hwyLink[k-1]]*(-dwellTime[k-1]);
									break;
								}
							}
							if ( dwellTime[k-1] < 0 )
								dwellTime[k-1] = 0;
							
						}

						inStrategy[k] = true;
						strategyOrderForLink[k] = inStrategyCount;
                        orderInStrategy[inStrategyCount++] = k;
						updateEnteringLabels(ia[k]);

					}
					// boarding link
					else if (linkType[k] == AuxTrNet.BOARDING_TYPE) {

						if ( nodeFreq[ia[k]] == 0.0 ) {
							
							// first transit boarding link considered from the current node
							nodeLabel[ia[k]] = (AuxTrNet.ALPHA + freq[k]*(nodeLabel[ib[k]] + linkImped))/(nodeFreq[ia[k]] + freq[k]);
							nodeFreq[ia[k]] = freq[k];

						}
						else {
							
							// at least one transit boarding link from the current node exists in optimal strategy
							nodeLabel[ia[k]] = (nodeFreq[ia[k]]*nodeLabel[ia[k]] + freq[k]*(nodeLabel[ib[k]] + linkImped))/(nodeFreq[ia[k]] + freq[k]);
							nodeFreq[ia[k]] += freq[k];

                        }
						
						inStrategy[k] = true;
						strategyOrderForLink[k] = inStrategyCount;
						orderInStrategy[inStrategyCount++] = k;
						updateEnteringLabels (ia[k]);
							
					}
					// non-boarding link - either in-vehicle or auxilliary
					else {
						
						nodeLabel[ia[k]] = nodeLabel[ib[k]] + linkImped;

						inStrategy[k] = true;
						strategyOrderForLink[k] = inStrategyCount;
						orderInStrategy[inStrategyCount++] = k;
						updateEnteringLabels(ia[k]);
					}
				}
				else {
					
					if (debug) logger.info ("link not included in strategy");
					inStrategy[k] = false;
					
				}


				// log some information about the ending condition of the candidate link being examined
				if ( debug && inStrategy[k] ) {
					
                    // get the highway network link index for the given transit network link index
                    m = hwyLink[k];
                

					logger.info ("");
					logger.info ("k=" + k + ", linkType=" + linkType[k] + ", trRoute=" + trRoute[k]);
					logger.info ("ag.ia[k]=" + ia[k] + "(g.an=" + (m >= 0 ? indexNode[gia[m]] : -1) + "), ag.ib[k]=" + ib[k] + "(g.bn=" + ( m>=0 ? indexNode[gib[m]] : -1) + ")");
					logger.info ("nodeLabel[ag.ia=" + ia[k] + "]=" + nodeLabel[ia[k]]);
					logger.info ("nodeLabel[ag.ib=" + ib[k] + "]=" + nodeLabel[ib[k]]);
					logger.info ("nodeFreq[ag.ia=" + ia[k] + "]=" + nodeFreq[ia[k]]);
					logger.info ("nodeFreq[ag.ib=" + ib[k] + "]=" + nodeFreq[ib[k]]);
					logger.info ("ag.freq[k=" + k + "]=" + freq[k]);
					logger.info ("inStrategy[k=" + k + "]=" + inStrategy[k]);
					
				}
				
			}

		} // end of while heap not empty

		return 0;

	}



    private int updateEnteringLabels (int currentNode) {
        // calculate linkLabels[] for use in ordering the contents of the heap.
        // linkLabel[k] is the cumulative utility from ia[k] to dest.

        int i, j, k, m;
        int start, end;
        boolean debug = classDebug;
//      boolean debug = true;
        double linkImped = 0.0;

        if (debug) {
            logger.info ("");
            logger.info ("updateEnteringLabels(): currentNode = " + currentNode);
        }

        start = ipb[currentNode];
        if (start == -1) {
            return -1;
        }


        
        if (debug)
              logger.info ("start=" + start + ", indexb[start]=" + indexb[start] + ", ia=" + ia[indexb[start]] + ", ib=" + ib[indexb[start]] + ", an=" + (ia[indexb[start]] < indexNode.length ? indexNode[ia[indexb[start]]] : 0) + ", bn=" + (ib[indexb[start]] < indexNode.length ? indexNode[ib[indexb[start]]] : 0));
        j = currentNode + 1;
        while (ipb[j] == -1)
            j++;
        end = ipb[j];
        if (debug) {
            logger.info ("end=" + end + ", j=" + j);
            logger.info ("end=" + end + ", indexb[end]=" + (end < indexb.length ? Integer.toString(indexb[end]) : "null") + ", ia=" + (end < indexb.length ? Integer.toString(ia[indexb[end]]) : "null") + ", ib=" + (end < indexb.length ? Integer.toString(ib[indexb[end]]) : "null"));
            logger.info ("");
        }
        for (i=start; i < end; i++) {
            k = indexb[i];

            // if link k is a boarding link, but the in-vehicle link that follows it (link k+1) is not in the strategy,
            // don't add link k to the heap.
            if ( linkType[k] == AuxTrNet.BOARDING_TYPE && !inStrategy[k+1] )
                continue;
                
            linkImped = nh.getLinkImped(k);
            linkLabel[k] = nodeLabel[ib[k]] + linkImped;

            // if the anode's label is already smaller than the bnode's label plus the link impedance,
            // no need to add the link to the heap. 
            if ( nodeLabel[ia[k]] < (nodeLabel[ib[k]] + linkImped) )
                continue;

            if (debug) {
                m = hwyLink[k];
                logger.info ("adding   " + i + ", indexb[i] or k=" + k + ", linkType=" + linkType[k] + ", ia=" + ia[k] + "(" + (m>=0 ? indexNode[gia[m]] : -1) + "), ib=" + ib[k] + "(" + (m>=0 ? indexNode[gib[m]] : -1) + "), linkLabel[k]=" + String.format("%15.6f", linkLabel[k]) + ", nodeLabel[ag.ib[k]]=" + nodeLabel[ib[k]] + ", linkImped=" + linkImped);
            }

            HeapElement he = new HeapElement(k, linkType[k], linkLabel[k]);
            
            if ( candidateHeap.contains(k))
                candidateHeap.remove(he);
            
            candidateHeap.add(he);

        }

        if (debug) candidateHeap.dataPrintSorted();
            
        return 0;
    }



    private double sumBoardingFlow (int inA) {
        // add up the flow allocated to boarding links exiting this internally numbered transit network node.

        int i, j, k;
        int start, end;
        double boardingFlow = 0.0;
        boolean debug = classDebug;


        // start is the pointer array index for links exiting ia.
        start = ipa[inA];
        if (start == -1) {
            return -1;
        }
        if (debug)
            logger.info ("start=" + start + ", ia=" + ia[indexa[start]] + ", ib=" + ib[indexa[start]] + ", an=" + (ia[indexa[start]] < indexNode.length ? indexNode[ia[indexa[start]]] : 0) + ", bn=" + (ib[indexa[start]] < indexNode.length ? indexNode[ib[indexa[start]]] : 0));


        j = inA + 1;
        while (ipa[j] == -1)
            j++;
        end = ipa[j];
        if (debug) {
            logger.info ("end=" + end + ", j=" + j);
            logger.info ("end=" + end + ", indexa[end]=" + (end < indexa.length ? Integer.toString(indexa[end]) : "null") + ", ia=" + (end < indexa.length ? Integer.toString(ia[indexa[end]]) : "null") + ", ib=" + (end < indexa.length ? Integer.toString(ib[indexa[end]]) : "null"));
            logger.info ("");
        }
        
        
        for (i=start; i < end; i++) {

            k = indexa[i];

            // if link k is a boarding link and it is in the strategy, sum its link flow.
            if ( linkType[k] == AuxTrNet.BOARDING_TYPE && inStrategy[k] )
                boardingFlow += flow[k];
                
        }

        return boardingFlow;
        
    }



    private double sumAlightingFlow (int inB) {
        // add up the flow allocated to alighting links entering this internally numbered transit network node.

        int i, j, k;
        int start, end;
        double alightingFlow = 0.0;
        boolean debug = classDebug;


        // start is the pointer array index for links exiting ia.
        start = ipb[inB];
        if (start == -1) {
            return -1;
        }
        if (debug)
            logger.info ("start=" + start + ", ia=" + ia[indexb[start]] + ", ib=" + ib[indexb[start]] + ", an=" + (ia[indexb[start]] < indexNode.length ? indexNode[ia[indexb[start]]] : 0) + ", bn=" + (ib[indexb[start]] < indexNode.length ? indexNode[ib[indexb[start]]] : 0));


        j = inB + 1;
        while (ipb[j] == -1)
            j++;
        end = ipb[j];
        if (debug) {
            logger.info ("end=" + end + ", j=" + j);
            logger.info ("end=" + end + ", indexb[end]=" + (end < indexb.length ? Integer.toString(indexb[end]) : "null") + ", ia=" + (end < indexb.length ? Integer.toString(ia[indexb[end]]) : "null") + ", ib=" + (end < indexb.length ? Integer.toString(ib[indexb[end]]) : "null"));
            logger.info ("");
        }
        
        
        for (i=start; i < end; i++) {

            k = indexb[i];

            // if link k is a boarding link and it is in the strategy, sum its link flow.
            if ( linkType[k] == AuxTrNet.ALIGHTING_TYPE && inStrategy[k] )
                alightingFlow += flow[k];
                
        }

        return alightingFlow;
        
    }


    public double[] loadOptimalStrategyDest (double[] tripColumn) {

        // tripColumn is the column of the trip table for the destination zone for this optimal strategy 
        int k, m;
        int count;
        double linkFlow;
        boolean debug = false;
        
        int[] originsNotLoaded = new int[tripColumn.length];
        ArrayList origList = new ArrayList();
        
        tripsNotLoaded = 0;
        
        // the trips are loaded onto the network at the origin zone centroid nodes.
        for (int origTaz=0; origTaz < tripColumn.length; origTaz++) {
            
            // no intra-zonal trips assigned, so go to next orig if orig==dest.
            if ( origTaz == dest) continue;

            nodeFlow[origTaz] = tripColumn[origTaz];
            
            if ( tripColumn[origTaz] > 0.0 ) {
                origList.add(origTaz);
                originsNotLoaded[origTaz] = 1;
            }
        }


        // allocate an array to store boardings by route to be passed back to calling method.
        double[] routeBoardingsToDest = new double[nh.getMaxRoutes()];
        
        
        // loop through links in optimal strategy in reverse order and allocate
        // flow at the nodes to exiting links in the optimal strategy
        count = 0;
        for (int i=inStrategyCount; i >= 0; i--) {
            
            k = orderInStrategy[i];
            m = hwyLink[k];

            if ( linkType[k] == AuxTrNet.BOARDING_TYPE) {
                
                linkFlow = (freq[k]/nodeFreq[ia[k]])*nodeFlow[ia[k]];

                if ( linkFlow > 0 ) {
                    flow[k] = linkFlow;
                    nodeFlow[ib[k]] += linkFlow;
                    routeBoardingsToDest[trRoute[k]] += linkFlow;
                }

            }
            else {

                linkFlow = nodeFlow[ia[k]];

                if ( linkFlow > 0 ) {
                    if ( nodeLabel[ib[k]] != AuxTrNet.INFINITY ) {
                        flow[k] = linkFlow;
                        nodeFlow[ib[k]] += linkFlow;
                        nodeFlow[ia[k]] -= linkFlow;
                        
                        if ( ia[k] < nh.getNumCentroids() )
                            originsNotLoaded[ia[k]] = 0;
                    }
                }

            }
            
            
            
            if (debug) {
                logger.info ( "count=" + count + ", i=" + i + ", k=" + k + ", m=" + m + ", trRoute=" + trRoute[k] + ", ag.ia=" + ia[k] + ", ag.ib="  + ib[k] + ", nh.an=" + (m>=0 ? indexNode[gia[m]] : -1) + ", nh.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + ", linkType=" + linkType[k] + ", ag.walkTime=" + walkTime[k] + ", invTime=" + invTime[k] + ", ag.waitTime=" + waitTime[k] + ", flow[k]=" + flow[k] + ", nodeLabel[ia[k]]=" + nodeLabel[ia[k]] + ", nodeLabel[ib[k]]=" + nodeLabel[ib[k]] );
            }
            
            count++;
        
        }

        for (int origTaz=0; origTaz < tripColumn.length; origTaz++) {
            if ( originsNotLoaded[origTaz] == 1 )
                tripsNotLoaded += tripColumn[origTaz];
        }
        
        return routeBoardingsToDest;

    }


    public double getTripsNotLoaded () {
        return tripsNotLoaded;
    }
    

//    public double[] getOptimalStrategyWtSkimsOrigDest (int startFromNode, int startToNode) {
//
//        // startFromNode and startToNode are externally numbered.
//        
//        int k, m;
//        int fromNodeIndex=-1;
//        int count;
//        
//        double linkFlow;
//        
//        boolean debug = classDebug;
////        boolean debug = true;
//        
//
//        double[] results = new double[6];
//        Arrays.fill (results, AuxTrNet.UNCONNECTED);
//        Arrays.fill (nodeFlow, 0.0);
//        Arrays.fill (flow, 0.0);
//
//        
//        if (startFromNode == indexNode[dest]) {
//            return results;
//        }
//
//
//        // find the link index of the first optimal strategy link exiting fromNode
//        // allocate 1 trip to routes between fromNode and dest to track proportions allocated to multiple paths in strategy
//        for (int i=inStrategyCount - 1; i >= 0; i--) {
//            k = orderInStrategy[i];
//            m = hwyLink[k];
//            if ( ia[k] == nodeIndex[startFromNode] ) {
//                fromNodeIndex = i;
//                nodeFlow[ia[k]] = 1.0;
//                if (debug) {
//                    logger.info ("");
//                    logger.info ( "startFromNode=" + startFromNode + "(" + nodeIndex[startFromNode] + "), startToNode=" + startToNode + "(" + nodeIndex[startToNode] + "), fromNodeIndex=" + fromNodeIndex + ", i=" + i + ", k=" + k + ", m=" + m + ", ag.ia=" + ia[k] + ", ag.ib=" + ib[k] + ", nh.an=" + (m>=0 ? indexNode[gia[m]] : -1) + ", nh.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + ", linkType=" + linkType[k] );
//                }
//                break;
//            }
//        }
//        
//        if ( fromNodeIndex < 0 ) 
//            return results;
//
//        
//        // set one trip starting at the startFromNode
//        nodeFlow[nodeIndex[startFromNode]] = 1.0;
//        
//        
//        // loop through links in optimal strategy starting at the index where the startFromNode was found and assign flow to links in the strategy
//        count = 0;
//        for (int i=fromNodeIndex; i >= 0; i--) {
//            
//            k = orderInStrategy[i];
//            m = hwyLink[k];
//            
//            if (nodeFlow[ia[k]] > 0.0) {
//
//                
//                if ( linkType[k] == AuxTrNet.BOARDING_TYPE ) {
//                    linkFlow = (freq[k]/nodeFreq[ia[k]])*nodeFlow[ia[k]];
//                    flow[k] = linkFlow;
//                    nodeFlow[ib[k]] += linkFlow;
//                }
//                else {
//                    linkFlow = nodeFlow[ia[k]];
//                    if ( nodeLabel[ib[k]] != AuxTrNet.INFINITY ) {
//                        flow[k] += linkFlow;
//                        nodeFlow[ib[k]] += linkFlow;
//                    }
//                }
//
//                if ( debug )
//                    logger.info ( "count=" + count + ", i=" + i + ", k=" + k + ", m=" + m + ", trRoute=" + trRoute[k] + ", ag.ia=" + ia[k] + ", ag.ib="  + ib[k] + ", nh.an=" + (m>=0 ? indexNode[gia[m]] : -1) + ", nh.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + ", linkType=" + linkType[k] + ", ag.walkTime=" + walkTime[k] + ", invTime=" + invTime[k] + ", waitTime=" + waitTime[k] + ", flow[k]=" + flow[k] + ", nodeLabel[ag.ia[k]]=" + nodeLabel[ia[k]] + ", nodeLabel[ib[k]]=" + nodeLabel[ib[k]] + ", nodeFreq[ag.ia[k]]=" + nodeFreq[ia[k]] + ", ag.freq[k]=" + freq[k] + ", nodeFlow[agia]]=" + nodeFlow[ia[k]] + ", nodeFlow[ag.ib[k]]=" + nodeFlow[ib[k]] );
//                
//                count++;
//
//            }
//
//        }
//        
//
//        // loop through links in optimal strategy that received flow and log some information 
//        Integer tempNode = new Integer(0);
//        ArrayList boardingNodes = new ArrayList();
//        double inVehTime = 0.0;
//        double dwellTm = 0.0;
//        double walkTm = 0.0;
//        double wtAccTime = 0.0;
//        double wtEgrTime = 0.0;
//        double firstWait = 0.0;
//        double totalWait = 0.0;
//        double boardings = 0.0;
//        double alightings = 0.0;
//        double totalBoardings = 0.0;
//        double fare = 0.0;
//        
//        
//        count = 0;
//        if (debug)
//            logger.info ( "\n\n\nlinks in strategy with flow from origin toward destination:" );
//        for (int i=inStrategyCount; i >= 0; i--) {
//            
//            k = orderInStrategy[i];
//            m = hwyLink[k];
//            
//            if (nodeFlow[ia[k]] == 0.0)
//                continue;
//
//            if ( debug )
//                logger.info ( "count=" + count + ", i=" + i + ", k=" + k + ", m=" + m + ", trRoute=" + trRoute[k] + ", ag.ia=" + ia[k] + ", ag.ib="  + ib[k] + ", nh.an=" + (m>=0 ? indexNode[gia[m]] : -1) + ", nh.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + ", linkType=" + linkType[k] + ", ag.walkTime=" + walkTime[k] + ", invTime=" + invTime[k] + ", ag.waitTime=" + waitTime[k] + ", flow[k]=" + flow[k] + ", nodeLabel[ag.ia[k]]=" + nodeLabel[ia[k]] + ", nodeLabel[ag.ib[k]]=" + nodeLabel[ib[k]] + ", nodeFreq[ag.ia[k]]=" + nodeFreq[ia[k]] );
//            
//            
//            if ( linkType[k] == AuxTrNet.BOARDING_TYPE ) {
//                
//                tempNode = new Integer(ia[k]);
//
//                // since we loaded 1 trip for computing skims, the fraction of the trip loading transit lines at a node
//                // is also a weight used to computed average waiting time at the node.
//                if ( firstWait == 0.0 ) {
//                    boardings = sumBoardingFlow (ia[k]);
//                    totalBoardings = boardings;
//                    boardingNodes.add(tempNode);
//                    firstWait = boardings*AuxTrNet.ALPHA/nodeFreq[ia[k]];
//                    totalWait = firstWait;
//                    fare = boardings*AuxTrNet.FARE;
//                }
//                else {
//                    if ( !boardingNodes.contains(tempNode) ) {
//                        boardings = sumBoardingFlow (ia[k]);
//                        totalBoardings += boardings;
//                        totalWait += boardings*AuxTrNet.ALPHA/nodeFreq[ia[k]];
//                        fare += boardings*AuxTrNet.TRANSFER_FARE;
//                        boardingNodes.add(tempNode);
//                    }
//                }
//                
//            }
//            else if ( linkType[k] == AuxTrNet.IN_VEHICLE_TYPE ) {
//                
//                inVehTime += invTime[k]*flow[k];
//                dwellTm += dwellTime[k]*flow[k];
//                
//            }
//            else if ( linkType[k] == AuxTrNet.AUXILIARY_TYPE ) {
//
//                // accumulate access walk time and total walk time
//                if ( firstWait == 0.0 )
//                    wtAccTime += walkTime[k]*flow[k];
//                else
//                    walkTm += walkTime[k]*flow[k];
//                
//            }
//                
//            count++;
//
//        }
//
//        // linkFreqs were weighted by WAIT_COEFF, so unweight them to get actual first and total wait values
//        firstWait /= AuxTrNet.WAIT_COEFF;
//        totalWait /= AuxTrNet.WAIT_COEFF;
//
//
//        
//
//        // loop through links in optimal strategy starting at the startToNode to accumulate egress time
//        count = 0;
//        double totalEgressFlow = 0.0;
//        if (debug)
//            logger.info ( "\n\n\nlinks in strategy with flow from destination toward origin:" );
//        for (int i=0; i < inStrategyCount; i++) {
//            
//            k = orderInStrategy[i];
//            m = hwyLink[k];
//            
//            if (nodeFlow[ia[k]] == 0.0)
//                continue;
//
//            if ( debug )
//                logger.info ( "count=" + count + ", i=" + i + ", k=" + k + ", m=" + m + ", trRoute=" + trRoute[k] + ", ag.ia=" + ia[k] + ", ag.ib="  + ib[k] + ", nh.an=" + (m>=0 ? indexNode[gia[m]] : -1) + ", nh.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + ", linkType=" + linkType[k] + ", ag.walkTime=" + walkTime[k] + ", invTime=" + invTime[k] + ", ag.waitTime=" + waitTime[k] + ", flow[k]=" + flow[k] + ", nodeLabel[ag.ia[k]]=" + nodeLabel[ia[k]] + ", nodeLabel[ag.ib[k]]=" + nodeLabel[ib[k]] + ", nodeFreq[ag.ia[k]]=" + nodeFreq[ia[k]] );
//            
//            
//            if ( linkType[k] == AuxTrNet.AUXILIARY_TYPE ) {
//
//                // get total flow alighting to this node
//                alightings = sumAlightingFlow(ia[k]);
//                totalEgressFlow += alightings;
//                
//                // accumulate access walk time and total walk time
//                wtEgrTime += alightings*walkTime[k];
//                
//                
//                //break out of loop when all flow to destination is accounted for.
//                if ( 1.0 - totalEgressFlow < COMPARE_EPSILON )
//                    break;
//                
//            }
//                
//            count++;
//
//        }
//
//        walkTm -= wtEgrTime;
//        
//        
//        if ( debug ) {
//            logger.info ( "\n\n\ntransit skims from " + startFromNode + " to " + startToNode + ":" );
//            logger.info ( "in-vehicle time  = " + inVehTime );
//            logger.info ( "dwell time       = " + dwellTm );
//            logger.info ( "firstWait time   = " + firstWait );
//            logger.info ( "totalWait time   = " + totalWait );
//            logger.info ( "wt access time   = " + wtAccTime );
//            logger.info ( "wt egress time   = " + wtEgrTime );
//            logger.info ( "other walk time  = " + walkTm );
//            logger.info ( "total boardings  = " + totalBoardings );
//        }
//            
//        
//        results[0] = inVehTime;
//        results[1] = firstWait;
//        results[2] = totalWait + dwellTm;
//        results[3] = wtAccTime;
//        results[4] = totalBoardings;
//        results[5] = fare;
//
//        
//        return results;
//        
//    }



    public double[][] getOptimalStrategySkimsDest () {

        int k, m;
        int count;
        
        //double flow = 0.0;
        
        boolean debug = classDebug;
        

        
        
        double[][] nodeSkims = new double[NUM_SKIMS][auxNodeCount];
        double[][] skimResults = new double[NUM_SKIMS][nh.getNumCentroids()];
        for (k=0; k < NUM_SKIMS; k++) {
            Arrays.fill (nodeSkims[k], AuxTrNet.UNCONNECTED);
            Arrays.fill (skimResults[k], AuxTrNet.UNCONNECTED);
        }

//        Arrays.fill (flow, 0.0);
//        for (int i=0; i < nh.getNumCentroids(); i++)
//            nodeFlow[i] = 1.0;
//        nodeFlow[dest] = 0.0;
        

        
        
        // check links entering dest.  If none are access links in the strategy, then dest is not transit accessible.
        boolean walkAccessAtDestination = false;
        int start = ipb[dest];
        if (start >= 0) {

            int j = dest + 1;
            while (ipb[j] == -1)
                j++;
            int end = ipb[j];
            
            for (int i=start; i < end; i++) {
                k = indexb[i];
                if ( linkType[k] == AuxTrNet.AUXILIARY_TYPE && inStrategy[k] ) {
                    walkAccessAtDestination = true;
                    break;
                }
            }
        }

        if ( !walkAccessAtDestination )
            return skimResults;
        
        
        
        // loop through links in optimal strategy in reverse order and propagate 1 trip from each accessible origin through the optimal strategy to the destination. 
//        for (int i=inStrategyCount; i >= 0; i--) {
//            
//            k = orderInStrategy[i];
//            m = ag.hwyLink[k];
//            
//
//            if (nodeFlow[ag.ia[k]] > 0.0) {
//                
//                if ( linkType[k] == AuxTrNet.BOARDING_TYPE ) {
//                    flow = (ag.freq[k]/nodeFreq[ag.ia[k]])*nodeFlow[ag.ia[k]];
//                    flow[k] = flow;
//                    nodeFlow[ag.ib[k]] += flow;
//                }
//                else {
//                    flow = nodeFlow[ag.ia[k]];
//                    if ( nodeLabel[ag.ib[k]] != AuxTrNet.INFINITY ) {
//                        flow[k] += flow;
//                        nodeFlow[ag.ib[k]] += flow;
//                    }
//                }
//
//            }
//            
//        }            
            
            

        // loop through links in optimal strategy in ascending order and accumulate skim component values at nodes weighted by link flows
        count = 0;
        for (int i=0; i < inStrategyCount; i++) {
            
            k = orderInStrategy[i];
            m = hwyLink[k];
            
            if ( debug )
                logger.info ( "count=" + count + ", i=" + i + ", k=" + k + ", m=" + m + ", trRoute=" + trRoute[k] + ", ag.ia=" + ia[k] + ", ag.ib="  + ib[k] + ", nh.an=" + (m>=0 ? indexNode[gia[m]] : -1) + ", nh.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + ", linkType=" + linkType[k] + ", ag.walkTime=" + walkTime[k] + ", invTime=" + invTime[k] + ", ag.waitTime=" + waitTime[k] + ", flow[k]=" + flow[k] + ", nodeLabel[ag.ia[k]]=" + nodeLabel[ia[k]] + ", nodeLabel[ag.ib[k]]=" + nodeLabel[ib[k]] + ", nodeFreq[ag.ia[k]]=" + nodeFreq[ia[k]] + ", ag.freq[k]=" + freq[k] + ", nodeFlow[ag.ia[k]]=" + nodeFlow[ia[k]] + ", nodeFlow[ag.ib[k]]=" + nodeFlow[ib[k]] );
            
        
        
            if ( linkType[k] == AuxTrNet.BOARDING_TYPE ) {

                if ( nodeSkims[FWT][ib[k]] == 0.0 ) {
                    nodeSkims[FWT][ia[k]] = freq[k]*(AuxTrNet.ALPHA/nodeFreq[ia[k]])/nodeFreq[ia[k]];
                    nodeSkims[TWT][ia[k]] = freq[k]*(AuxTrNet.ALPHA/nodeFreq[ia[k]])/nodeFreq[ia[k]];
                    nodeSkims[BRD][ia[k]] = freq[k]/nodeFreq[ia[k]];
                    nodeSkims[FAR][ia[k]] = freq[k]*AuxTrNet.FARE/nodeFreq[ia[k]];
                }
                else {
                    nodeSkims[FWT][ia[k]] = nodeSkims[FWT][ib[k]] + freq[k]*AuxTrNet.ALPHA/nodeFreq[ia[k]];
                    nodeSkims[TWT][ia[k]] = nodeSkims[TWT][ib[k]] + freq[k]*AuxTrNet.ALPHA/nodeFreq[ia[k]];
                    nodeSkims[BRD][ia[k]] = nodeSkims[BRD][ib[k]] + freq[k]/nodeFreq[ia[k]];
                    nodeSkims[FAR][ia[k]] = nodeSkims[FAR][ib[k]] + freq[k]*AuxTrNet.TRANSFER_FARE/nodeFreq[ia[k]];
                }
                nodeSkims[IVT][ia[k]] = nodeSkims[IVT][ib[k]];
                nodeSkims[ACC][ia[k]] = nodeSkims[ACC][ib[k]];
                nodeSkims[AUX][ia[k]] = nodeSkims[AUX][ib[k]];
                nodeSkims[EGR][ia[k]] = nodeSkims[EGR][ib[k]];
                
//                nodeSkims[HSR][ia[k]] = nodeSkims[HSR][ib[k]];
                
            }
            else if ( linkType[k] == AuxTrNet.IN_VEHICLE_TYPE ) {
                
                nodeSkims[IVT][ia[k]] = nodeSkims[IVT][ib[k]] + invTime[k];
                nodeSkims[FWT][ia[k]] = nodeSkims[FWT][ib[k]];
                nodeSkims[TWT][ia[k]] = nodeSkims[TWT][ib[k]] + dwellTime[k];
                nodeSkims[ACC][ia[k]] = nodeSkims[ACC][ib[k]];
                nodeSkims[AUX][ia[k]] = nodeSkims[AUX][ib[k]];
                nodeSkims[EGR][ia[k]] = nodeSkims[EGR][ib[k]];
                nodeSkims[BRD][ia[k]] = nodeSkims[BRD][ib[k]];
                nodeSkims[FAR][ia[k]] = nodeSkims[FAR][ib[k]];
                
//                if ( rteMode[k] == 'm' )
//                    nodeSkims[HSR][ia[k]] = nodeSkims[HSR][ib[k]] + invTime[k];
//                else
//                    nodeSkims[HSR][ia[k]] = nodeSkims[HSR][ib[k]];
                    
            }
            else if ( linkType[k] == AuxTrNet.LAYOVER_TYPE ) {
                
                nodeSkims[IVT][ia[k]] = nodeSkims[IVT][ib[k]];
                nodeSkims[FWT][ia[k]] = nodeSkims[FWT][ib[k]];
                nodeSkims[TWT][ia[k]] = nodeSkims[TWT][ib[k]] + layoverTime[k];
                nodeSkims[ACC][ia[k]] = nodeSkims[ACC][ib[k]];
                nodeSkims[AUX][ia[k]] = nodeSkims[AUX][ib[k]];
                nodeSkims[EGR][ia[k]] = nodeSkims[EGR][ib[k]];
                nodeSkims[BRD][ia[k]] = nodeSkims[BRD][ib[k]];
                nodeSkims[FAR][ia[k]] = nodeSkims[FAR][ib[k]];
                    
            }
            else if ( linkType[k] == AuxTrNet.ALIGHTING_TYPE ) {
                
                nodeSkims[IVT][ia[k]] = nodeSkims[IVT][ib[k]];
                nodeSkims[FWT][ia[k]] = nodeSkims[FWT][ib[k]];
                nodeSkims[TWT][ia[k]] = nodeSkims[TWT][ib[k]];
                nodeSkims[ACC][ia[k]] = nodeSkims[ACC][ib[k]];
                nodeSkims[AUX][ia[k]] = nodeSkims[AUX][ib[k]];
                nodeSkims[EGR][ia[k]] = nodeSkims[EGR][ib[k]];
                nodeSkims[BRD][ia[k]] = nodeSkims[BRD][ib[k]];
                nodeSkims[FAR][ia[k]] = nodeSkims[FAR][ib[k]];
//                nodeSkims[HSR][ia[k]] = nodeSkims[HSR][ib[k]];
                
            }
            else if ( linkType[k] == AuxTrNet.AUXILIARY_TYPE ) {

                // if bnode is dest, initialize anode's egress walk time to that walk egress time and other skims to zero.
                if ( ib[k] < nh.getNumCentroids() ) {
                    nodeSkims[IVT][ia[k]] = 0.0;
                    nodeSkims[FWT][ia[k]] = 0.0;
                    nodeSkims[TWT][ia[k]] = 0.0;
                    nodeSkims[ACC][ia[k]] = 0.0;
                    nodeSkims[AUX][ia[k]] = 0.0;
                    nodeSkims[EGR][ia[k]] = walkTime[k];
                    nodeSkims[BRD][ia[k]] = 0.0;
                    nodeSkims[FAR][ia[k]] = 0.0;
//                    nodeSkims[HSR][ia[k]] = 0.0;
                }
                // anode is an origin node, set final skim values for that origin and add the access time
                else if ( ia[k] < nh.getNumCentroids() ) {
                    skimResults[IVT][ia[k]] = nodeSkims[IVT][ib[k]];
                    skimResults[FWT][ia[k]] = nodeSkims[FWT][ib[k]];
                    skimResults[TWT][ia[k]] = nodeSkims[TWT][ib[k]];
                    skimResults[ACC][ia[k]] = nodeSkims[ACC][ib[k]] + accessTime[k];
                    skimResults[AUX][ia[k]] = nodeSkims[AUX][ib[k]];
                    skimResults[EGR][ia[k]] = nodeSkims[EGR][ib[k]];
                    skimResults[BRD][ia[k]] = nodeSkims[BRD][ib[k]];
                    skimResults[FAR][ia[k]] = nodeSkims[FAR][ib[k]];
//                    skimResults[HSR][ia[k]] = nodeSkims[HSR][ib[k]];
                }
                // link is a walk link, not connected to a centroid
                else {
                    nodeSkims[IVT][ia[k]] = nodeSkims[IVT][ib[k]];
                    nodeSkims[FWT][ia[k]] = nodeSkims[FWT][ib[k]];
                    nodeSkims[TWT][ia[k]] = nodeSkims[TWT][ib[k]];
                    nodeSkims[ACC][ia[k]] = nodeSkims[ACC][ib[k]];
                    nodeSkims[AUX][ia[k]] = nodeSkims[AUX][ib[k]] + walkTime[k];
                    nodeSkims[EGR][ia[k]] = nodeSkims[EGR][ib[k]];
                    nodeSkims[BRD][ia[k]] = nodeSkims[BRD][ib[k]];
                    nodeSkims[FAR][ia[k]] = nodeSkims[FAR][ib[k]];
//                    nodeSkims[HSR][ia[k]] = nodeSkims[HSR][ib[k]];
                }
                
            }

        }
        
        count++;


        
        
        // linkFreqs were weighted by WAIT_COEFF, so unweight them to get actual first and total wait values
        for (int i=0; i < nh.getNumCentroids(); i++) {
            skimResults[FWT][i] /= AuxTrNet.WAIT_COEFF;
            skimResults[TWT][i] /= AuxTrNet.WAIT_COEFF;
        }

        
        

        return skimResults;
        
    }



	public void computeOptimalStrategySkimMatrices (String period, String accessMode, String routeType) {

        // get skim values into 0-based double[][] dimensioned to number of actual zones including externals (2983)
        float[][][] zeroBasedFloatArrays = new float[NUM_SKIMS][][];
        double[][][] zeroBasedDoubleArray = new double[NUM_SKIMS][nh.getNumCentroids()][nh.getNumCentroids()];

		
		for (int dest=0; dest < nh.getNumCentroids(); dest++) {
		    
		    if ( dest % 100 == 0 ) {
		        logger.info ( "generating " + period + " " + accessMode + " " + routeType + " skims to zone " + dest + "." );
		    }
		    
		    
			// build an optimal strategy for the specified destination node.
			if ( buildStrategy(dest) >= 0 ) {
			    
			    double[][] odSkimValues = getOptimalStrategySkimsDest();
                    
                for (int k=0; k < NUM_SKIMS; k++) {
                    for (int orig=0; orig < nh.getNumCentroids(); orig++)
                        zeroBasedDoubleArray[k][orig][dest] = odSkimValues[k][orig];
                    
				}
				
			}
			
		}
        

        for (int k=0; k < NUM_SKIMS; k++) {
            zeroBasedFloatArrays[k] = getZeroBasedFloatArray ( zeroBasedDoubleArray[k] );
        }
        

        
        for (int o=0; o < nh.getNumCentroids(); o++)
            for (int d=0; d < nh.getNumCentroids(); d++)
                zeroBasedFloatArrays[FRQ][o][d] = 1.0f;
        
        
        String nameQualifier = null;
        String descQualifier = null;
        if ( nh.getTimePeriod().equalsIgnoreCase("peak") ) {
            nameQualifier = "p";
            descQualifier = "peak";
        }
        else {
            nameQualifier = "o";
            descQualifier = "offpeak";
        }

        if ( nh.getAccessMode().equalsIgnoreCase("walk") ) {
            nameQualifier = nameQualifier.concat("wt");
            descQualifier = descQualifier.concat(" walk-transit");
        }
        else {
            nameQualifier = nameQualifier.concat("dt");
            descQualifier = descQualifier.concat(" drive-transit");
        }
        

        skimMatrices[IVT] = new Matrix( nameQualifier + "ivt", descQualifier + " in-vehicle time skims", zeroBasedFloatArrays[IVT] );
        skimMatrices[FWT] = new Matrix( nameQualifier + "fwt", descQualifier + " first wait time skims", zeroBasedFloatArrays[FWT] );
        skimMatrices[TWT] = new Matrix( nameQualifier + "twt", descQualifier + " total wait time skims", zeroBasedFloatArrays[TWT] );
        skimMatrices[ACC] = new Matrix( nameQualifier + "acc", descQualifier + " access time skims", zeroBasedFloatArrays[ACC] );
        skimMatrices[AUX] = new Matrix( nameQualifier + "aux", descQualifier + " other walk time skims", zeroBasedFloatArrays[AUX] );
        skimMatrices[EGR] = new Matrix( nameQualifier + "egr", descQualifier + " egress walk time skims", zeroBasedFloatArrays[EGR] );
        skimMatrices[BRD] = new Matrix( nameQualifier + "brd", descQualifier + " boardings skims", zeroBasedFloatArrays[BRD] );
        skimMatrices[FAR] = new Matrix( nameQualifier + "far", descQualifier + " fare skims", zeroBasedFloatArrays[FAR] );
//        skimMatrices[HSR] = new Matrix( nameQualifier + "hsr", descQualifier + " high speed rail ivt skims", zeroBasedFloatArrays[HSR] );
        skimMatrices[FRQ] = new Matrix( nameQualifier + "hsr", descQualifier + " high speed rail ivt skims", zeroBasedFloatArrays[FRQ] );
        
        for (int k=0; k < NUM_SKIMS; k++)
            skimMatrices[k].setExternalNumbers( alphaExternalNumbers );
        
	}

    
    
    public Matrix[] getSkimMatrices() {
        return skimMatrices;
    }
    
    
    private float[][] getZeroBasedFloatArray ( double[][] zeroBasedDoubleArray ) {

        int[] skimsInternalToExternal = indexNode;

        // convert the zero-based double[alphas+externals][alphas+externals] produced by the skimming procedure, with network centroid/zone index mapping
        // to a zero-based float[alphas+externals][alphas+externals] with indexZone mapping to be written to skims file.
        float[][] zeroBasedFloatArray = new float[nh.getNumCentroids()][nh.getNumCentroids()];
        
        int exRow;
        int exCol;
        int inRow;
        int inCol;
        for (int i=0; i < zeroBasedDoubleArray.length; i++) {
            exRow = skimsInternalToExternal[i];
            if ( zonesToSkim[exRow] == 1 ) {
                inRow = externalToAlphaInternal[exRow];
                for (int j=0; j < zeroBasedDoubleArray[i].length; j++) {
                    exCol = skimsInternalToExternal[j];
                    if ( zonesToSkim[exCol] == 1 ) {
                        inCol = externalToAlphaInternal[exCol];
                        zeroBasedFloatArray[inRow][inCol] = (float)zeroBasedDoubleArray[i][j];
                    }
                }
            }
        }

        zeroBasedDoubleArray = null;

        return zeroBasedFloatArray;

    }



	double airlineDistance(int fromNode, int toNode) {

	  double horizMiles, vertMiles, distMiles;
	  boolean debug = classDebug;


	  horizMiles = ((gNodeX[toNode] - gNodeX[fromNode])/LONGITUDE_PER_FEET)/5280.0;
	  vertMiles  = ((gNodeY[toNode] - gNodeY[fromNode])/LONGITUDE_PER_FEET)/5280.0;
	  distMiles = Math.sqrt(horizMiles*horizMiles + vertMiles*vertMiles);


	  if (debug) {
	      logger.info ("fromNode=" + fromNode + ", toNode=" + toNode +
	              ", ax=" + gNodeX[fromNode] + ", ay=" + gNodeY[fromNode] + ", bx=" + gNodeX[toNode] + ", by=" + gNodeY[toNode] +
	              ", horizMiles=" + horizMiles + ", vertMiles=" + vertMiles + ", distMiles=" + distMiles);
	  }


	  return distMiles;
	}



    
    

    // Inner classes

    public class HeapElement implements Comparable {
        
        int index;
        int type;
        double label;
        
        public HeapElement (int index, int type, double label) {
            this.index = index;
            this.type = type;
            this.label = label;
        }
        
        
        public int compareTo(Object obj) {
            
            int returnValue = 0;
            HeapElement el = (HeapElement)obj;
            
            if ( label > el.getLabel() )
                returnValue = 1;
            else if ( label < el.getLabel() )
                returnValue = -1;
            else {
                if ( type > el.getType() )
                    returnValue = 1;
                else if ( type < el.getType() )
                    returnValue = -1;
                else {
                    if ( index > el.getIndex() )
                        returnValue = 1;
                    else if ( index < el.getIndex() )
                        returnValue = -1;
                }
            }
            
            return returnValue;
        }

        public int getIndex() {
            return index;
        }
        
        public int getType() {
            return type;
        }
        
        public double getLabel() {
            return label;
        }
        
    }

    
    
    public class Heap {
        
        boolean[] inHeap = null;
        SortedSet elements = null;
        
        public Heap( int numLinks ) {
            elements = new TreeSet();
            inHeap = new boolean[numLinks];
        }

        public void clear() {
            elements.clear();
            Arrays.fill ( inHeap, false );
        }

        public int size() {
            return elements.size();
        }

        public void add( HeapElement el ) {
            int k = el.getIndex();
            inHeap[k] = true;
            elements.add(el);
        }
        
        public boolean remove( HeapElement el ) {
            int k = el.getIndex();
            inHeap[k] = false;
            return elements.remove(el);
        }
        
        public boolean contains( int k ) {
            return inHeap[k];
        }
        
        public HeapElement getFirst() {
            HeapElement el = (HeapElement)elements.first();
            if ( el != null )
                remove(el);
            return el;
        }

        public void dataPrintSorted() {

            logger.info( "Heap contents sorted by linklabel" );
            Iterator it = elements.iterator();

            int i=0;
            while ( it.hasNext() ) {
                HeapElement h = (HeapElement)it.next();
                int k = h.getIndex();
                int m = hwyLink[k];
                logger.info ("i=" + (i++) + ",k=" + k + ", ag.ia[k]=" + ia[k] + "(g.an=" + (m>=0 ? indexNode[gia[m]] : -1) + "), ag.ib[k]=" + ib[k] + "(g.bn=" + (m>=0 ? indexNode[gib[m]] : -1) + "), linkType=" + linkType[k] + ", Route=" + trRoute[k] + ", linkLabel[k]=" + String.format("%10.6f", linkLabel[k]) );
            }
        }

    }

}	