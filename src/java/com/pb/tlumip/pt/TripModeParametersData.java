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
import com.pb.common.util.ResourceUtil;
import com.pb.common.datafile.CSVFileReader;
import com.pb.common.datafile.TableDataSet;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

/**  
 * A class to add trip mode parameters to a TableDataSet from a csv file
 * 
 * @author Joel Freedman
 * @version 1.0 12/01/2003
 * 
 */

public class TripModeParametersData {
    final static Logger logger = Logger.getLogger("com.pb.tlumip.pt.default");
     String tripModeParametersTableName="TripModeParameters";
     
     //a hashtable of taz objects
     private TripModeParameters[] tripModeParameters = new TripModeParameters[ActivityPurpose.ACTIVITY_PURPOSES.length];
     
     public TripModeParameters getTripModeParameters(int activityPurpose){
         return tripModeParameters[activityPurpose];  
     }

     public static TableDataSet loadTableDataSet(ResourceBundle rb, String fileName) {
        
        try {
            String tazFile = ResourceUtil.getProperty(rb, fileName);
            CSVFileReader reader = new CSVFileReader();
            TableDataSet table = reader.readFile(new File(tazFile));
            return table;
        } catch (IOException e) {
            logger.fatal("Can't find TourDestinationParameters file " + fileName);
            e.printStackTrace();
        } 
        return null;
    }
    
     public void readData(ResourceBundle rb, String fileName){
         if(logger.isDebugEnabled()) {
             logger.debug("Getting table: "+fileName);
         }
         TableDataSet table = loadTableDataSet(rb, fileName);
        try {
            for(int rowNumber = 1; rowNumber<=table.getRowCount(); rowNumber++) {
                 
                TripModeParameters thisPurpose = new TripModeParameters();
                
                thisPurpose.purpose =     table.getStringValueAt(rowNumber,table.getColumnPosition("purpose")); 
                thisPurpose.ivt               =      table.getValueAt(rowNumber,table.getColumnPosition("ivt"));
                thisPurpose.opclow          =     table.getValueAt(rowNumber,table.getColumnPosition("opclow"));
                thisPurpose.opcmed          =     table.getValueAt(rowNumber,table.getColumnPosition("opcmed"));
                thisPurpose.opchi          =     table.getValueAt(rowNumber,table.getColumnPosition("opchi"));
                thisPurpose.pkglow          =     table.getValueAt(rowNumber,table.getColumnPosition("pkglow"));
                thisPurpose.pkgmed          =     table.getValueAt(rowNumber,table.getColumnPosition("pkgmed"));
                thisPurpose.pkghi          =     table.getValueAt(rowNumber,table.getColumnPosition("pkghi"));
                thisPurpose.opcpas          =     table.getValueAt(rowNumber,table.getColumnPosition("opcpas"));
                thisPurpose.sr2hh2          =     table.getValueAt(rowNumber,table.getColumnPosition("sr2hh2"));
                thisPurpose.sr2hh3p          =     table.getValueAt(rowNumber,table.getColumnPosition("sr2hh3p"));
                thisPurpose.sr3hh3p          =     table.getValueAt(rowNumber,table.getColumnPosition("sr3hh3p"));
                thisPurpose.driverSr2     =     table.getValueAt(rowNumber,table.getColumnPosition("driverSr2"));
                thisPurpose.driverSr3p     =     table.getValueAt(rowNumber,table.getColumnPosition("driverSr3p"));
                thisPurpose.passSr3p     =     table.getValueAt(rowNumber,table.getColumnPosition("passSr3p"));
                
                int purposeNumber = ActivityPurpose.getActivityPurposeValue(thisPurpose.purpose.charAt(0));
                tripModeParameters[purposeNumber] = thisPurpose;    
                                                   
            }                                                            
        } catch (Exception e) {            
            System.err.println("Error: printTable()");                   
            e.printStackTrace();                                         
        }                                                                
     };                                                                   
                                                                          
    public static void main (String[] args) throws Exception {           
          ResourceBundle rb = ResourceUtil.getResourceBundle("pt");
         TripModeParametersData tmp = new TripModeParametersData();                                     
         tmp.readData(rb,"tripModeparameters.file");
                                                                          
         System.exit(1);                                                  
    };                                                                   
                                                                         
}                                                                        