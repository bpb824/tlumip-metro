/* Generated by Together */

package com.pb.despair.ts.old;
import com.pb.despair.model.Mode;
import com.pb.despair.model.TravelUtilityCalculatorInterface;
import com.pb.despair.model.UnitOfLand;

import java.util.Vector;
//import com.pb.despair.model.TravelUtilityCalculatorInterface;
//import com.pb.despair.model.UnitOfLand;
//import com.pb.despair.pt.Mode;

/**
 * A ZPAttribute is a class that contains the information about the route choice between zone pairs.  A FatZPattribute (this class) contains references to the origin and destination zones, the loaded network (AssignmentPeriod) that the route occured on, the route itself as a list of links, the Mode that was used in the route choice, the TravelPreferences that were used in route choice, and the parent object TravelCharacteristicMatrix (if any).  This is a lot of extra references, and with 9million or more zone pairs it certainly is a waste of storage to keep all these references for all zone pairs for all trips.  Thus this should be considered a "working" class that encapsulates a lot of redundent information.  When the "work" is done only the essential information should be stored, in the ZPAttribute class.
 */
public class FatZPAttribute extends ZPAttribute {
    public FatZPAttribute(TravelUtilityCalculatorInterface tp ,Mode m, AssignmentPeriod ap, UnitOfLand o, UnitOfLand d) {
      super(0);
      myTravelPreferences = tp;
      myMode = m;
      myAssignmentPeriod = ap;
      origin = o;
      destination = d;
    }

    public FatZPAttribute(ZPAttribute thin) {
      super(thin);
    }

    public synchronized void findMinimumPath(    ) {
      findMinimumPath(myTravelPreferences,myMode,myAssignmentPeriod,origin,destination);
    }

    /**
     * @param m the mode that the route choice should use
     * @param tp the route choice parameters that determine the unique choice of route
     * @param ntwrk the congested network that the route flows through
     * @param o origin zone
     * @param d destination zone
     */
    public synchronized void findMinimumPath(TravelUtilityCalculatorInterface tp, Mode m, AssignmentPeriod ntwrk, UnitOfLand o, UnitOfLand d) {
      myTravelPreferences=tp;
      myMode=m;
      myAssignmentPeriod = ntwrk;
      origin = o;
      destination = d;
      findMinimumPath();
    }

    /**
     *@label origin
     */
    protected UnitOfLand origin;

    /**
     *@label destination
     */
    protected UnitOfLand destination;

    /**
     * Reference to the loaded path that describes the route.
     * @associates <{LinkLoad}>
     */
    protected Vector myPathLoad;

    /**
     * Reference to the parent object that "owns" this object.
     */
    public TravelCharacteristicMatrix myTravelCharacteristicMatrix;
    public TravelUtilityCalculatorInterface myTravelPreferences;
    public Mode myMode;
    public AssignmentPeriod myAssignmentPeriod;
    public String toString() {return "o:"+origin+" d:"+destination+" by "+myMode+" at "+myAssignmentPeriod;};

    public UnitOfLand getOrigin(){
            return origin;
        }

    public UnitOfLand getDestination(){
            return destination;
        }
}
