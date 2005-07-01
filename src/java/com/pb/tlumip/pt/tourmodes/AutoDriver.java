package com.pb.tlumip.pt.tourmodes;
import com.pb.tlumip.model.Mode;
import com.pb.tlumip.model.ModeType;
import com.pb.tlumip.model.TravelTimeAndCost;
import com.pb.tlumip.pt.PersonTourModeAttributes;
import com.pb.tlumip.pt.TourModeParameters;
import com.pb.tlumip.pt.ZoneAttributes;

import org.apache.log4j.Logger;
/** Driver mode
 * 
 * @author Joel Freedman
 * @version 1.0 12/01/2003
 */

public class AutoDriver extends Mode {
    final static Logger logger = Logger.getLogger("com.pb.tlumip.pt.default");
      
//     public boolean isAvailable=true;
//     public boolean hasUtility=false;
     
//     double utility=0;

    public AutoDriver() {
        isAvailable = true;
        hasUtility = false;
        utility = 0.0D;
        alternativeName = new String("AutoDriver");
        type = ModeType.AUTODRIVER;
    }
     /** Calculates utility of being an auto driver
      * 
      * @param inbound - In-bound TravelTimeAndCost
      * @param outbound - Outbound TravelTimeAndCost
      * @param z - ZoneAttributes (Currently only parking cost)
      * @param c - TourModeParameters
      * @param p - PersonTourModeAttributes
      */
     public void calcUtility(TravelTimeAndCost inbound, TravelTimeAndCost outbound,
          ZoneAttributes z,TourModeParameters c, PersonTourModeAttributes p){
               
               if(p.age<16) isAvailable=false;
               if(p.autos==0) isAvailable=false;
               
               if(isAvailable){
                    time = inbound.driveAloneTime + outbound.driveAloneTime;
                    utility=(
                      c.ivt*(inbound.driveAloneTime + outbound.driveAloneTime)
                 + c.opclow*((inbound.driveAloneCost+outbound.driveAloneCost)*p.inclow)
                 + c.opcmed*((inbound.driveAloneCost+outbound.driveAloneCost)*p.incmed)
                 + c.opchi* ((inbound.driveAloneCost+outbound.driveAloneCost)*p.inchi)
                 + c.pkglow*((z.parkingCost*(p.primaryDuration/60))*p.inclow)
                 + c.pkgmed*((z.parkingCost*(p.primaryDuration/60))*p.incmed)
                 + c.pkghi*((z.parkingCost*(p.primaryDuration/60))*p.inchi)
                    );
               hasUtility=true;
               };
     };
     
    /** Get auto driver utility */
     public double getUtility(){
          if(!hasUtility){
               logger.fatal("Error: Utility not calculated for "+alternativeName+"\n");
              //TODO - log this exception to the node exception file
               System.exit(1);
          };
          return utility;
     };
     


}

