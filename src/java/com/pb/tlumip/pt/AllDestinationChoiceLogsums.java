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
package com.pb.tlumip.pt;

import com.pb.common.matrix.Matrix;
import com.pb.common.matrix.MatrixCollection;
import com.pb.common.matrix.MatrixReader;
import com.pb.common.matrix.MatrixType;
import com.pb.common.util.ResourceUtil;
import java.util.ResourceBundle;
import java.io.File;
import org.apache.log4j.Logger;

/** 
 * A class that contains all destination choice logsums
 * for all purposes and market segments; 
 * readFromJDataStore puts them in Hashtable
 * 
 * @author Joel Freedman
 * @version 1.0 12/01/2003
 */

public class AllDestinationChoiceLogsums {
    final static Logger logger = Logger.getLogger("com.pb.tlumip.pt.default");
    
    static final int TOTALSEGMENTS = PTHousehold.NUM_WORK_SEGMENTS;

    static final String dcTableName="DC LOGSUMS";
    MatrixCollection mc;

    public AllDestinationChoiceLogsums(){}
     
     public void readDCLogsums(ResourceBundle rb){
         String dcLogsumFile = ResourceUtil.getProperty(rb, "dcLogsum.path");
         if(logger.isDebugEnabled()) {
             logger.debug("dcLogsumFile: "+dcLogsumFile);
         }

         mc = new MatrixCollection();
         for(int purpose=0;purpose<ActivityPurpose.DC_LOGSUM_PURPOSES.length;purpose++){
             for(int segment=0;segment<TOTALSEGMENTS;segment++){
                 MatrixReader reader = MatrixReader.createReader(MatrixType.ZIP, new File(dcLogsumFile
                                                                +ActivityPurpose.DC_LOGSUM_PURPOSES[purpose]+segment+"dcls.zip"));
                 Matrix m = reader.readMatrix();
                 mc.addMatrix(m);
                 if(logger.isDebugEnabled()) {
                     logger.debug("matrix name: "+m.getName()+".zip");
                 }
             }
         }
     }

    public void readBinaryDCLogsums(ResourceBundle rb){
         String dcLogsumFile = ResourceUtil.getProperty(rb, "dcLogsum.path");
         if(logger.isDebugEnabled()) {
             logger.debug("dcLogsumFile: "+dcLogsumFile);
         }

         mc = new MatrixCollection();
         for(int purpose=0;purpose<ActivityPurpose.DC_LOGSUM_PURPOSES.length;purpose++){
             for(int segment=0;segment<TOTALSEGMENTS;segment++){
                 MatrixReader reader = MatrixReader.createReader(MatrixType.BINARY, new File(dcLogsumFile
                                                                +ActivityPurpose.DC_LOGSUM_PURPOSES[purpose]+segment+"dcls.binary"));
                 Matrix m = reader.readMatrix();
                 mc.addMatrix(m);
                 if(logger.isDebugEnabled()) {
                     logger.debug("matrix name: "+m.getName()+".binary");
                 }
             }
         }
     }

     /**
     *
     * This class sets DC Logsums for all persons in the hh
     *
     */  
     
     public void setDCLogsums(PTHousehold thisHousehold){
          
          //calculate household market segments
          int workHouseholdSegment = thisHousehold.calcWorkLogsumSegment();               
             int nonWorkHouseholdSegment = thisHousehold.calcNonWorkLogsumSegment();
             int studentPersonSegment=0;
        
             for(int i=0;i<thisHousehold.persons.length;++i){

                  //calculate person market segments for workers, students (logsums will be set to 0 for non-workers, non-students)
                  studentPersonSegment=thisHousehold.persons[i].calcStudentLogsumSegment();
        
                  //now grab the logsums
                  if(thisHousehold.persons[i].employed && thisHousehold.persons[i].workTaz!=0){
                      /*thisHousehold.persons[i].workDCLogsum = mc.getValue(thisHousehold.homeTaz, 0, 
                                                                          "w"+new Integer(workPersonSegment).toString()
                                                                             +new Integer(workHouseholdSegment).toString()
                                                                             +"dcls");*/
                      thisHousehold.persons[i].workBasedDCLogsum = mc.getValue(thisHousehold.homeTaz, 0, 
                                                                          "b"+new Integer(workHouseholdSegment).toString()
                                                                             +"dcls");
                       //thisHousehold.persons[i].workDCLogsum=((Float)logsums.get("w"+new Integer(workPersonSegment).toString()
                       //  +new Integer(workHouseholdSegment).toString()+new Integer(thisHousehold.homeTaz).toString())).doubleValue();
                      //thisHousehold.persons[i].workBasedDCLogsum=((Float)logsums.get("b"+new Integer(nonWorkHouseholdSegment).toString()
                      //     +new Integer(thisHousehold.persons[i].workTaz).toString())).doubleValue();
                  }
                 if(thisHousehold.persons[i].student && thisHousehold.persons[i].schoolTaz!=0){
                     thisHousehold.persons[i].schoolDCLogsum = mc.getValue(thisHousehold.homeTaz, 0, 
                                                                           "c"+new Integer(studentPersonSegment).toString()
                                                                           +new Integer(nonWorkHouseholdSegment).toString()
                                                                           +"dcls");
                      //thisHousehold.persons[i].schoolDCLogsum=((Float)logsums.get("c"+new Integer(studentPersonSegment).toString()
                    //+new Integer(nonWorkHouseholdSegment).toString()+new Integer(thisHousehold.homeTaz).toString())).doubleValue();
                  }
                 
                 thisHousehold.persons[i].shopDCLogsum=mc.getValue(thisHousehold.homeTaz, 0, 
                                                                   "s"
                                                                   +new Integer(nonWorkHouseholdSegment).toString()
                                                                   +"dcls");
                 thisHousehold.persons[i].recreateDCLogsum=mc.getValue(thisHousehold.homeTaz, 0, 
                                                                       "r"
                                                                       +new Integer(nonWorkHouseholdSegment).toString()
                                                                        +"dcls");
                 thisHousehold.persons[i].otherDCLogsum=mc.getValue(thisHousehold.homeTaz, 0, 
                                                                    "o"
                                                                    +new Integer(nonWorkHouseholdSegment).toString()
                                                                    +"dcls");
 
          }

     }
        
         
}