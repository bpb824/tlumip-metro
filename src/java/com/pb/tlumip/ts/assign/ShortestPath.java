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
package com.pb.tlumip.ts.assign;

import java.util.Arrays;
import org.apache.log4j.Logger;

import com.pb.tlumip.ts.NetworkHandlerIF;


/**
 * Class for node to node shortest paths.
 *
 */

public class ShortestPath {

	protected static Logger logger = Logger.getLogger(ShortestPath.class);

    static final int MAX_PATH_LENGTH = 500;
    static final double COMPARE_EPSILON = 1.0e-07;


    int inOrigin;
    int inDestination;

    int[] pathLinks;
    int[] nodeLabeled;
    double[] nodeLabels;
    double[] aonFlow;
    int[] predecessorLink;

    int[] ia;
    int[] ib;
	int[] ip;
    int[] indexNode;
	int[] nodeIndex;
    int[] sortedLinkIndex;
	boolean[] centroid;
	double[] congestedTime;
    
    int numNodes;
    int numZones;

    Heap candidateHeap;
	int[] heapContents;


    public ShortestPath (NetworkHandlerIF nh) {

        numNodes = nh.getNodeCount();
        numZones = nh.getNumCentroids();
        
        // store network fields in local arrays
        ia = nh.getIa();
        ib = nh.getIb();
        ip = nh.getIpa();
        indexNode = nh.getIndexNode();
        nodeIndex = nh.getNodeIndex();
        centroid = nh.getCentroid();
        congestedTime = nh.getCongestedTime();
        sortedLinkIndex = nh.getSortedLinkIndexA();
        
        
        pathLinks = new int[MAX_PATH_LENGTH];
        nodeLabeled = new int[nh.getNodeCount()+1];
        nodeLabels = new double[nh.getNodeCount()+1];
        aonFlow = new double[nh.getLinkCount()];
        predecessorLink = new int[nh.getNodeCount()+1];

        //Create a new heap structure to sort canidate node labels
        candidateHeap = new Heap(nh.getNodeCount()+1);
        heapContents = new int[nh.getNodeCount()];
    }

    
    private void initData() {

        Arrays.fill(nodeLabeled, 0);
        Arrays.fill(nodeLabels, 1.0e+99);
        nodeLabels[inOrigin] = 0.0;
        nodeLabeled[inOrigin] = 1;

        predecessorLink = new int[numNodes+1];
        Arrays.fill(predecessorLink, -1);

        candidateHeap.clear();

    }


    public boolean buildPath(int inOrigin, int inDestination) {
        int k;
        boolean debug = false;

        this.inOrigin = inOrigin;
        this.inDestination = inOrigin;

        if (debug) System.out.println ("building path from " + inOrigin + "(" + indexNode[inOrigin] + ")" + " to " + inDestination + "(" + indexNode[inDestination] + ")");
        initData();

        // set labels for links eminating from the origin node
        setRootLabels (inOrigin);
        if (debug) candidateHeap.dataPrint();

        // continue labeling until candidateHeap is empty
        k = candidateHeap.remove();
        if (debug) System.out.println ("removed k=" + k + ", ia=" + ia[k] + "(" + indexNode[ia[k]] + ")" + ", ib=" + ib[k] + "(" + indexNode[ib[k]] + ")");
        while (ib[k] != inDestination) {
            setRootLabels (ib[k]);
            if (debug) candidateHeap.dataPrint();
            nodeLabeled[ib[k]] = 1;
            k = candidateHeap.remove();
            if (k == -1)
                return false;
            if (debug) System.out.println ("removed k=" + k + ", ia=" + ia[k] + "(" + indexNode[ia[k]] + ")" + ", ib=" + ib[k] + "(" + indexNode[ib[k]] + ")");
        }

        return true;
    }


    private void setRootLabels (int rootNode) {
        int i;
        double label;
        boolean debug = false;

        if (debug) System.out.println ("rootNode=" + indexNode[rootNode] +"(external node label)" + ", ip[" + rootNode + "]=" + ip[rootNode] + ", ip[" + (rootNode+1) + "]=" + ip[(rootNode+1)]);
        for (i=ip[rootNode]; i < ip[rootNode+1]; i++) {
            
            int k = sortedLinkIndex[i];
            
            if (debug) System.out.println ("considering link k=" + k + ": ia[k]=" + ia[k] + " " + indexNode[ia[k]] + "(external a-node)" + ", ib[k]=" + ib[k] + " " + indexNode[ib[k]] + "(external b-node), nodeLabeled[ib[k]]=" + nodeLabeled[ib[k]] + ", nodeLabels[ia[k]]=" + nodeLabels[ia[k]] + ", congestedTime[k]=" + congestedTime[k]);
            if (nodeLabeled[ib[k]] == 0) {
                label = congestedTime[k] + nodeLabels[ia[k]];
                if (label - nodeLabels[ib[k]] < -COMPARE_EPSILON) {
                    nodeLabels[ib[k]] = label;
                    if (!centroid[k] || rootNode == inOrigin || ib[k] == inDestination) {
                        candidateHeap.add(k);
                    }
                    predecessorLink[ib[k]] = k;
                }
            }
        }
    }


    public void printCurrentPath (int origin, int destination, double[] congestedTime) {
        // origin and destination are external numbers
        int i, k, count;
        double cumTime=0.0;

        k = predecessorLink[nodeIndex[destination]];
        count = 0;
        while (ia[k] != nodeIndex[origin]) {
            pathLinks[count++] = k;
            k = predecessorLink[ia[k]];
        }
        pathLinks[count++] = k;


        for (i=count-1; i >= 0; i--) {
            k = pathLinks[i];
            cumTime += congestedTime[k];
            System.out.printf ( "%d,%d   %12.4f  %12.4f\n", Integer.toString(indexNode[ia[k]]), Integer.toString(indexNode[ib[k]]), congestedTime[k], cumTime );
        }
    }


    public double getPathCost (int inOrigin, int inDest) {
        int k;
        double cumTime=0.0;

        k = predecessorLink[inDest];
        cumTime += congestedTime[k];
        while (ia[k] != inOrigin) {
            k = predecessorLink[ia[k]];
            cumTime += congestedTime[k];
        }

        return cumTime;
    }


    public int[] getPredecessorLink () {
        return predecessorLink;
    }


    public int[] getNodeList (int inOrigin, int inDestination) {

        int i, j, k, count;
        boolean debug = false;

        if (debug) {
            System.out.println ("");
            System.out.println (inDestination + "(" + indexNode[inDestination] + ")");

            k = predecessorLink[inDestination];
            System.out.println (ia[k] + "(" + indexNode[ia[k]] + ")");
            count = 0;
            while (ia[k] != inOrigin) {
                count++;
                k = predecessorLink[ia[k]];
                System.out.println (ia[k] + "(" + indexNode[ia[k]] + ")");
            }
            System.out.println (ia[k] + "(" + indexNode[ia[k]] + ")");
            if (count >= MAX_PATH_LENGTH)
                System.out.println (count + " links in path from " + inOrigin + "(" + indexNode[inOrigin] + ") to " + inDestination + "(" + indexNode[inDestination] + ")");
        }

        k = predecessorLink[inDestination];
        count = 0;
        while (ia[k] != inOrigin) {
            pathLinks[count++] = k;
            k = predecessorLink[ia[k]];
        }
        pathLinks[count++] = k;

        int[] nodes = new int[count + 1];

        j = 0;
        for (i=count-1; i >= 0; i--) {
            k = pathLinks[i];
            nodes[j++] = ia[k];
        }
        nodes[j] = ib[k];

        return nodes;
    }


    /*-------------------- Inner class --------------------*/

    public class Heap {

//        public static final boolean DEBUG = false;


        private int data[];
        private int last;


        public Heap(int size) {
            data = new int[size];
            last = -1;
        }


        public Heap(int initData[]) {
            data = new int[100];
            for (int i = 0; i < initData.length; i++) {
                data[i] = initData[i];
            }
            last = initData.length - 1;
            heapify();
        }


        public int peek() {
            if (last == -1) return -1;   // no item left
            return data[0];              // return element at top of heap
        }


        public void clear() {
            last = -1;
            for (int i=0; i < heapContents.length; i++)
                heapContents[i] = 0;
        }


        //Rearrange current data into a heap
        public void heapify() {
            for (int i = (last - 1) / 2; i >= 0; i--) {
                percolateDown(i);
            }
        }


        public void add(int x) {
            
            if(logger.isDebugEnabled()) {
                logger.debug("adding " + x + ", last=" + last + "   " + indexNode[ia[x]] + "   " + indexNode[ib[x]] + "   " + nodeLabels[ib[x]]);
            }
  
            if (heapContents[ib[x]] == 1) {
				for (int i = last; i >= 0; i--) {
				    if ( ib[data[i]] == ib[x] )
				        percolateUp(i);
				}
            }
            else {
				data[++last] = x;
				percolateUp(last);

				heapContents[ib[x]] = 1;
            }

            
			
        }


		public int remove() {
			if (last == -1) return -1;   // no item left
			int min = data[0];           // remove element at top of heap
			data[0] = data[last];        // move last element to top of heap

			if (last == 0) {
				last = -1;
				if(logger.isDebugEnabled()) {
                    logger.debug("remove " + min + ", last=" + last);
                }
				return min;
			}

			last--;                      // reduce heap size
			percolateDown(0);            // move element at top down

			if(logger.isDebugEnabled()) {
                logger.debug("remove " + min + ", last=" + last);
            }
			return min;
		}


		/**
		 * remove element i from the heap. 
		 * 
		 */
		public int remove(int i) {
			if (last == -1) return -1;   // no item left
			int min = data[i];           // remove element at top of heap
			data[i] = data[last];        // move last element to top of heap

			if (last == 0) {
				last = -1;
				if(logger.isDebugEnabled()) {
                    logger.debug("remove " + min + ", last=" + last);
                }
				return min;
			}

			last--;                      // reduce heap size
			percolateDown(i);            // move element at top down

			if(logger.isDebugEnabled()) {
                logger.debug("remove " + min + ", last=" + last);
            }
			return min;
		}


        //Let element move up and settle
        public void percolateUp(int idx) {
            if(logger.isDebugEnabled()) {
                logger.debug("pu " + idx);
            }
            if (idx == 0) return;
            int parentIdx = (idx - 1) / 2;
            int k = data[idx];                                  // added
            int kParent = data[parentIdx];          // added
			if(logger.isDebugEnabled()) {
				logger.debug("k=" + k + ", ib[k]=" + ib[k] + ", an[k]=" + indexNode[ia[k]] + ", bn[k]=" + indexNode[ib[k]] + ", nodeLabels[ib[k]]=" + nodeLabels[ib[k]]);
				logger.debug("kParent=" + kParent + ", ib[kParent]=" + ib[kParent] + ", an[kParent]=" + indexNode[ia[kParent]] + ", bn[kParent]=" + indexNode[ib[kParent]] + ", nodeLabels[ib[kParent]]=" + nodeLabels[ib[kParent]]);
				logger.debug("nodeLabels[ib[k]] - nodeLabels[ib[kParent]]=" + (nodeLabels[ib[k]] - nodeLabels[ib[kParent]]) );
			}

			if (nodeLabels[ib[k]] - nodeLabels[ib[kParent]] < -COMPARE_EPSILON) {
				swap(parentIdx, idx);           // move larger parent down
				percolateUp(parentIdx);
            }
			else if ((nodeLabels[ib[k]] - nodeLabels[ib[kParent]] <= COMPARE_EPSILON) && (indexNode[ib[k]] < indexNode[ib[kParent]])) {           // added
                swap(parentIdx, idx);           // move larger parent down
                percolateUp(parentIdx);
            }
        }


        public void percolateDown(int idx) {
            if(logger.isDebugEnabled()) {
                logger.debug("pd " + idx);
            }
            int childIdx = idx * 2 + 1;
            if (childIdx > last) return;
            int k = data[idx];
            int kChild = data[childIdx];
            int kChildp1 = data[childIdx+1];

			if(logger.isDebugEnabled()) {
				logger.debug("idx=" + idx + ", childIdx=" + childIdx + ", last=" + last);
				logger.debug("k=" + k + ", ib[k]=" + ib[k] + ", an[k]=" + indexNode[ia[k]] + ", bn[k]=" + indexNode[ib[k]] + ", nodeLabels[ib[k]]=" + nodeLabels[ib[k]]);
				logger.debug("kChild=" + kChild + ", ib[kChild]=" + ib[kChild] + ", an[kChild]=" + indexNode[ia[kChild]] + ", bn[kChild]=" + indexNode[ib[kChild]] + ", nodeLabels[ib[kChild]]=" + nodeLabels[ib[kChild]]);
				logger.debug("kChildp1=" + kChildp1 + ", ib[kChildp1]=" + ib[kChildp1] + ", an[kChildp1]=" + indexNode[ia[kChildp1]] + ", bn[kChildp1]=" + indexNode[ib[kChildp1]] + ", nodeLabels[ib[kChildp1]]=" + nodeLabels[ib[kChildp1]]);
				logger.debug("nodeLabels[ib[kChildp1]] - nodeLabels[ib[kChild]]=" + (nodeLabels[ib[kChildp1]] - nodeLabels[ib[kChild]]) );
				logger.debug("nodeLabels[ib[kChild]] - nodeLabels[ib[k]]=" + (nodeLabels[ib[kChild]] - nodeLabels[ib[k]]) );
			}
			if ((childIdx + 1) <= last && (nodeLabels[ib[kChildp1]] - nodeLabels[ib[kChild]] < -COMPARE_EPSILON)) {
                childIdx = childIdx + 1;
                kChild = data[childIdx];
            }
            else if ((childIdx + 1) <= last && (nodeLabels[ib[kChildp1]] - nodeLabels[ib[kChild]] <= COMPARE_EPSILON) && (indexNode[ib[kChildp1]] < indexNode[ib[kChild]])) {
                childIdx = childIdx + 1;
                kChild = data[childIdx];
            }
            
			if (nodeLabels[ib[kChild]] - nodeLabels[ib[k]] < -COMPARE_EPSILON) {
				swap(idx, childIdx);
				percolateDown(childIdx);
			}
            else if ((nodeLabels[ib[kChild]] - nodeLabels[ib[k]] <= COMPARE_EPSILON) && (indexNode[ib[kChild]] < indexNode[ib[k]])) {
                swap(idx, childIdx);
                percolateDown(childIdx);
            }
        }


        public void swap(int idx1, int idx2) {
            int temp = data[idx1];
            data[idx1] = data[idx2];
            data[idx2] = temp;
        }


		//Print heap contents to console (note in sorted order)
		public void dataPrint() {
			int k;

			for (int i = 0; i <= last; i++) {
				k = data[i];
				logger.info("i=" + i + ", k=" + k + ", ib[k]=" + ib[k] + ", an=" + indexNode[ia[k]] + ", bn=" + indexNode[ib[k]] + ", nodeLabels[ib]=" + nodeLabels[ib[k]] + ", nodeLabeled[ib]=" + nodeLabeled[ib[k]]);
			}
			logger.info("");
		}


        /*
        //Used to test as a stand alone class
        static final int initData[] =  { 10, 3, 5, 6, 2, 7, 1 };

        public static void main(String argv[]) {
        	logger.info("Initial data:");
            for (int i = 0; i < initData.length; i++) {
                logger.info(initData[i] + ", ");
            }
            logger.info();

            Heap heap = new Heap(initData);
            heap.dataPrint();

            heap.add(8);
            heap.dataPrint();

            int min = heap.removeMin();
            heap.dataPrint();

            heap.add(9);
            heap.dataPrint();

            heap.add(4);
            heap.dataPrint();

            min = heap.removeMin();
            heap.dataPrint();
        }
    */
    }

}
