package com.pb.despair.pt;

import com.pb.common.util.ResourceUtil;
import com.pb.common.datafile.CSVFileReader;
import com.pb.common.datafile.TableDataSet;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import org.apache.log4j.Logger;

/**  
 * A class to access Duration Model Parameters from TableDataSet
 * 
 * @author Christi Willison
 * @version 1.0 1/21/05
 * 
 */
public class DurationModelParametersData {

    final static Logger logger = Logger.getLogger(DurationModelParametersData.class);

    public DurationModelParameters[] parameters;
    public String[] purposeIndex;

    public DurationModelParametersData(){

    }

    public void readData(ResourceBundle rb, String fileName){
        
        if(logger.isDebugEnabled()) {
            logger.debug("Getting table: "+fileName);
        }

        TableDataSet table = loadTableDataSet(rb, fileName);

        parameters = new DurationModelParameters[table.getRowCount()];  //there will be as many durationModelParameters object
                                                                        //as there are rows in the table.

        purposeIndex = new String[table.getRowCount()];
        try {
            for(int rowNumber = 1; rowNumber<=table.getRowCount(); rowNumber++) {

                DurationModelParameters parameterObject = new DurationModelParameters();
                parameterObject.purpose                                = table.getStringValueAt(rowNumber,table.getColumnPosition("purpose"));
                parameterObject.IStopsInPatternEquals1                 = table.getValueAt(rowNumber,table.getColumnPosition("IStopsInPatternEquals1"));
                parameterObject.IStopsInPatternEquals2                 = table.getValueAt(rowNumber,table.getColumnPosition("IStopsInPatternEquals2"));
                parameterObject.IStopsInPatternEquals2Plus             = table.getValueAt(rowNumber,table.getColumnPosition("IStopsInPatternEquals2Plus"));
                parameterObject.IStopsInPatternEquals3                 = table.getValueAt(rowNumber,table.getColumnPosition("IStopsInPatternEquals3"));
                parameterObject.IStopsInPatternEquals3Plus             = table.getValueAt(rowNumber,table.getColumnPosition("IStopsInPatternEquals3Plus"));
                parameterObject.IStopsInPatternEquals4Plus             = table.getValueAt(rowNumber,table.getColumnPosition("IStopsInPatternEquals4Plus"));
                parameterObject.IStopsOnTourEquals1                    = table.getValueAt(rowNumber,table.getColumnPosition("IStopsOnTourEquals1"));
                parameterObject.IStopsOnTourEquals2                    = table.getValueAt(rowNumber,table.getColumnPosition("IStopsOnTourEquals2"));
                parameterObject.toursEquals2                           = table.getValueAt(rowNumber,table.getColumnPosition("toursEquals3"));
                parameterObject.toursEquals3                           = table.getValueAt(rowNumber,table.getColumnPosition("toursEquals3"));
                parameterObject.toursEquals3Plus                       = table.getValueAt(rowNumber,table.getColumnPosition("toursEquals3Plus"));
                parameterObject.toursEquals4                           = table.getValueAt(rowNumber,table.getColumnPosition("toursEquals4"));
                parameterObject.toursEquals5Plus                       = table.getValueAt(rowNumber,table.getColumnPosition("toursEquals5Plus"));
                parameterObject.singleAdultWithOnePlusChildren         = table.getValueAt(rowNumber,table.getColumnPosition("singleAdultWithOnePlusChildren"));
                parameterObject.householdSize3Plus                     = table.getValueAt(rowNumber,table.getColumnPosition("householdSize3Plus"));
                parameterObject.female                                 = table.getValueAt(rowNumber,table.getColumnPosition("female"));
                parameterObject.income60Plus                           = table.getValueAt(rowNumber,table.getColumnPosition("income60Plus"));
                parameterObject.worksTwoJobs                           = table.getValueAt(rowNumber,table.getColumnPosition("worksTwoJobs"));
                parameterObject.age18to20                              = table.getValueAt(rowNumber,table.getColumnPosition("age18to20"));
                parameterObject.age25Plus                              = table.getValueAt(rowNumber,table.getColumnPosition("age25Plus"));
                parameterObject.autos0                                 = table.getValueAt(rowNumber,table.getColumnPosition("autos0"));
                parameterObject.industryEqualsRetail                   = table.getValueAt(rowNumber,table.getColumnPosition("industryEqualsRetail"));
                parameterObject.industryEqualsPersonServices           = table.getValueAt(rowNumber,table.getColumnPosition("industryEqualsPersonServices"));
                parameterObject.schDummy                               = table.getValueAt(rowNumber,table.getColumnPosition("schDummy"));
                parameterObject.wkDummy                                = table.getValueAt(rowNumber,table.getColumnPosition("wkDummy"));
                parameterObject.amActivityStartTime                    = table.getValueAt(rowNumber,table.getColumnPosition("amActivityStartTime"));
                parameterObject.mdActivityStartTime                    = table.getValueAt(rowNumber,table.getColumnPosition("mdActivityStartTime"));
                parameterObject.pmActivityStartTime                    = table.getValueAt(rowNumber,table.getColumnPosition("pmActivityStartTime"));
                parameterObject.evActivityStartTime                    = table.getValueAt(rowNumber,table.getColumnPosition("evActivityStartTime"));
                parameterObject.isWeekend                              = table.getValueAt(rowNumber,table.getColumnPosition("isWeekend"));
                parameterObject.activityIsPrimaryDestination           = table.getValueAt(rowNumber,table.getColumnPosition("activityIsPrimaryDestination"));
                parameterObject.activityIsIntermediateStopOnShopTour   = table.getValueAt(rowNumber,table.getColumnPosition("activityIsIntermediateStopOnShopTour"));
                parameterObject.activityIsIntermediateStopOnWorkTour   = table.getValueAt(rowNumber,table.getColumnPosition("activityIsIntermediateStopOnWorkTour"));
                parameterObject.activityIsIntermediateStopOnSchoolTour = table.getValueAt(rowNumber,table.getColumnPosition("activityIsIntermediateStopOnSchoolTour"));
                parameterObject.shopOnlyInPattern                      = table.getValueAt(rowNumber,table.getColumnPosition("shopOnlyInPattern"));
                parameterObject.recreateOnlyInPattern                  = table.getValueAt(rowNumber,table.getColumnPosition("recreateOnlyInPattern"));
                parameterObject.otherOnlyInPattern                     = table.getValueAt(rowNumber,table.getColumnPosition("otherOnlyInPattern"));
                parameterObject.tour1IsWork                            = table.getValueAt(rowNumber,table.getColumnPosition("tour1IsWork"));
                parameterObject.tour1IsSchool                          = table.getValueAt(rowNumber,table.getColumnPosition("tour1IsSchool"));
                parameterObject.constant                               = table.getValueAt(rowNumber,table.getColumnPosition("constant"));
                parameterObject.shape                                  = table.getValueAt(rowNumber,table.getColumnPosition("shape"));

                purposeIndex[rowNumber-1] = parameterObject.purpose;
                parameters[rowNumber-1] = parameterObject;
            }                                                            
        } catch (Exception e) {            
            logger.fatal("Error reading DurationModelParameters");
            //TODO - log this exception to the node exception file
            e.printStackTrace();
        }                                                                

     }

    private static TableDataSet loadTableDataSet(ResourceBundle rb, String fileName) {

        try {
            String durationModelParameterFile = ResourceUtil.getProperty(rb, fileName);
            CSVFileReader reader = new CSVFileReader();
            TableDataSet table = reader.readFile(new File(durationModelParameterFile));
            return table;
        } catch (IOException e) {
            logger.fatal("Can't find DurationModelParameters file " + fileName);
            //TODO - log this exception to the node exception log
            e.printStackTrace();
            System.exit(10);
        }
        return null;
    }


    public static void main (String[] args) throws Exception {
        ResourceBundle rb = ResourceUtil.getPropertyBundle(new File("/models/tlumip/scenario_pleaseWork/t1/pt/pt.properties"));
//        ResourceBundle rb = ResourceUtil.getResourceBundle("pt");
//         DurationModelParametersData tdpd = new DurationModelParametersData();
//         tdpd.readData(rb, "tourDestinationParameters.file");
//         for(int i=1;i<ActivityPurpose.ACTIVITY_PURPOSES.length;i++){
//            int length = tdpd.tourDestinationParameters[i].length;
//            System.out.println("purpose " + ActivityPurpose.getActivityPurposeChar((short)i));
//            for(int j=1;j<=length;j++){
//                System.out.println("\tsegment " + j);
//                System.out.println("\t\tlogsum  = "+tdpd.getParameters(i,j).logsum);
//                System.out.println("\t\tdistance  = "+tdpd.getParameters(i,j).distance);
//                System.out.println("\t\tdistancePower  = "+tdpd.getParameters(i,j).distancePower);
//            }
//         }
//         System.exit(1);
    }                                       
}                                                                        

