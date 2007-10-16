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
package com.pb.tlumip.ao;

import com.pb.common.datafile.TableDataSet;
import com.pb.common.util.ResourceUtil;
import com.pb.models.pecas.PIModel;
import com.pb.models.reference.ModelComponent;
import com.pb.tlumip.ald.ALDModel;
import com.pb.tlumip.ct.CTModel;
import com.pb.tlumip.ed.EDControl;
import com.pb.tlumip.et.ETModel;
import com.pb.tlumip.spg.SPGnew;
import com.pb.tlumip.ts.TSModelComponent;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author  Christi Willison
 * @version Jan 6, 2004
 * Created by IntelliJ IDEA.
 */
public class ApplicationOrchestrator {

    private static Logger logger = Logger.getLogger(ApplicationOrchestrator.class);
    private String rootDir;
    private String baseScenarioName;
    private String scenarioName;
    private int t;
    private int baseYear;
    ResourceBundle rb;
    BufferedWriter runLogWriter;
    File runLogPropFile;
    HashMap<String,String> runLogHashmap;
    

    public ApplicationOrchestrator(ResourceBundle rb){
        this.rb = rb;
    }

    public ApplicationOrchestrator(String rootDir, String baseScenarioName, String scenarioName, int timeInterval, int baseYear){
        this.rootDir = rootDir;
        this.baseScenarioName = baseScenarioName;
        this.scenarioName = scenarioName;
        this.t = timeInterval;
        this.baseYear = baseYear;
    }

    public void setRb(ResourceBundle rb) {
        this.rb = rb;
    }

    public ResourceBundle getRb() {
        return rb;
    }
    
    private void createBaseYearPropFile(){
        runLogPropFile = new File(rootDir + "/scenario_" + scenarioName + "/t0/runLog.properties");
        if(!runLogPropFile.exists()){
            logger.info("Creating the base year run log in " + rootDir + "/scenario_" + scenarioName + "/t0/");
            try {
                runLogWriter = new BufferedWriter(new FileWriter(runLogPropFile, false));
                runLogWriter.write("# Filter values for properties files");
                runLogWriter.newLine();
                runLogWriter.write("BASEDIR=" + rootDir);
                runLogWriter.newLine();
                runLogWriter.write("BASE_SCENARIO_NAME=" + baseScenarioName);
                runLogWriter.newLine();
                runLogWriter.write("SCENARIO_NAME=" + scenarioName);
                runLogWriter.newLine();
                runLogWriter.write("BASE_YEAR=" + baseYear);
                runLogWriter.newLine();
                runLogWriter.write("CURRENT_INTERVAL=0");
                runLogWriter.newLine();
                runLogWriter.write("GLOBAL_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("AO_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("ED_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("ALD_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("SPG_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("PI_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("CT_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("ET_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("PT_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("TS_TEMPLATE_INTERVAL=0");
	            runLogWriter.newLine();
                runLogWriter.write("ED_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("ALD_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("SPG1_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("PI_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("SPG2_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("CT_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("ET_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("PT_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.write("TS_LAST_RUN=0");
                runLogWriter.newLine();
                runLogWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }// else return - base year prop file has already been created.
    }

    private void createRunLogPropFile(){
        runLogPropFile = new File(rootDir + "/scenario_" + scenarioName + "/t" + t + "/runLog.properties");
        logger.info("Looking for the run log in " + rootDir + "/scenario_" + scenarioName + "/t" + t + "/");
        if(!runLogPropFile.exists()){ 
	        try { //create the file and write the current year into it.
	            logger.info("Writing the current year into the run log");
	            runLogWriter = new BufferedWriter(new FileWriter(runLogPropFile, false));
	            runLogWriter.write("# Filter values for properties files");
	            runLogWriter.newLine();
	            runLogWriter.write("BASEDIR=" + rootDir);
	            runLogWriter.newLine();
                runLogWriter.write("BASE_SCENARIO_NAME=" + baseScenarioName);
	            runLogWriter.newLine();
                runLogWriter.write("SCENARIO_NAME=" + scenarioName);
	            runLogWriter.newLine();
	            runLogWriter.write("BASE_YEAR=" + baseYear);
	            runLogWriter.newLine();
	            runLogWriter.write("CURRENT_INTERVAL=" + t);
	            runLogWriter.newLine();
	            runLogWriter.flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }else { // the file may have already been created by a call from an earlier app.
            	// so just attach a writer to it that can append the appLog info.
            try {
                runLogWriter = new BufferedWriter(new FileWriter(runLogPropFile, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateRunLogPropertiesHashmap (int t){
        //Get the base year runLog and read in all of the properties into a hashmap.
        String initialRunLogPath = rootDir + "/scenario_" + scenarioName + "/t0/runLog.properties";
        ResourceBundle runLogRb = ResourceUtil.getPropertyBundle(new File(initialRunLogPath));
        runLogHashmap = ResourceUtil.changeResourceBundleIntoHashMap(runLogRb);
        
        //Now update the hashmap with the most current values by looking for runLog.properties
        //files in the t1, t2, up to tn directories and replacing any of the runLog properties
        //with the current values.
        int i = 1;
        File propFile;
        String keyName;
        String value;
        
        while (i<=t){
            String propPath = rootDir + "/scenario_" + scenarioName + "/t" + i + "/runLog.properties";
            propFile = new File(propPath);
            if(propFile.exists()){
                logger.info("Reading in Run Log from year " + i + " and updating the RunLog HashMap: ");
                runLogRb = ResourceUtil.getPropertyBundle(new File(propPath));
                Enumeration rbEnum = runLogRb.getKeys();
                while (rbEnum.hasMoreElements()) {
                    //get the name and value pair from the current run log and
                    keyName = (String) rbEnum.nextElement();
                    value = runLogRb.getString(keyName);
                  
                    //if the key already exists in the hashmap (which it should) replace
                    //the matching value with the runLog value.
                    if(runLogHashmap.containsKey(keyName)){
                        runLogHashmap.put(keyName,value);
                    }else {
                        logger.info("The RunLog HashMap doesn't contain the key " + keyName);
                        logger.info("Adding key to HashMap");
                        runLogHashmap.put(keyName,value);
                    }
                }
                i++;
            }else {
                if(t==1){
                    logger.info("No applications have been run yet so no run log exists");
                }else logger.warn("NO RUN LOG EXISTS IN t" + i + " - PROPERTY FILES COULD BE AFFECTED" );
                i++;
            }
        }
       
    }
    
    private String createAppRb(String appName){
        //First read in the template properties file.  This will have default values and
        //tokens (surrounded by @ symbols).  The tokens will be replaced with the values
        //in the runLogHashMap

        //Get Template File
        File appPropertyTemplate = findTemplateFileRecursively(appName);
        String appPropertyTemplateName = appPropertyTemplate.getName();

        //The property file will have the same name as the template file minus the 'Template' part.
        //Keep in mind that the 'appName' passed in will be spg1 or pidaf but the template and properties
        //files are spg.properties or pi.properties.  The 'findTemplateFileRecursively' method will strip off the
        //extra characters (like '1' or 'daf') and so the appPropFileName != appName but the appPropFileName
        //will be the one we want to use to locate and name the properties file.
        String appPropFileName = appPropertyTemplateName.substring(0, appPropertyTemplateName.indexOf("Temp"));
        String outputPath = rootDir + "/scenario_" + scenarioName + "/t" + t + "/";
        File appPropertyFile;

        if (appPropFileName.equalsIgnoreCase("global"))  appPropertyFile = new File(outputPath + "global.properties");
        else appPropertyFile = new File(outputPath + appPropFileName + "/" + appPropFileName + ".properties");

        logger.info("Creating " + appPropertyFile.getAbsolutePath());

        Properties appDefaultProps = new Properties();
        try {
            appDefaultProps.load(new FileInputStream(appPropertyTemplate));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Iterator keys = runLogHashmap.keySet().iterator();
	    while (keys.hasNext()) {
	        String keyName = (String) keys.next();
	        String hashmapValue = runLogHashmap.get(keyName);
	        
	        //Build a pattern and compile it
	        String patternStr = "@" + keyName + "@";
	        Pattern pattern = Pattern.compile(patternStr);
	
	        // Replace all occurrences of pattern in input string
	        Enumeration propNames = appDefaultProps.propertyNames();
	        while(propNames.hasMoreElements()){
	            String propName = (String)propNames.nextElement();
	            String tempStr = appDefaultProps.getProperty(propName);
	            Matcher matcher = pattern.matcher(tempStr);
	            tempStr = matcher.replaceAll(hashmapValue);
	            appDefaultProps.setProperty(propName, tempStr);
	        }
        }
	    //write the properties file to the output stream in a format suitable for loading into a 
	    //properties table using the "load" method.
	    try {
            appDefaultProps.store(new FileOutputStream(appPropertyFile), appPropFileName.toUpperCase() + " Properties File for Interval " + t);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (appPropFileName.equalsIgnoreCase("global"))
            return (outputPath + "global.properties");
        else
            return (outputPath + appPropFileName + "/" + appPropFileName + ".properties");
    }



    public File findTemplateFileRecursively(String appName) {
        String templateSuffix;
        if(appName.startsWith("ct") || appName.startsWith("et") || appName.startsWith("pi") || appName.startsWith("pt")){
            if(t==1 || t==0) templateSuffix = "TemplateBase.properties";
            else templateSuffix = "Template.properties";
        }else{
            templateSuffix = "Template.properties";
        }
        File templateFile = null;
        String editedName;
        int i = t;  // look thru all of the tn directories for a property template file
                    // starting with the most recent t.
        while(i >= 0){
            String templatePath = rootDir + "/scenario_" + scenarioName + "/t" + i + "/";
            //Deal with SPG exception (appName=spg1 or spg2 but template file is spgTemplate.properties for both)
            if(appName.startsWith("spg")){
                editedName = "SPG";
                templateFile = new File(templatePath + "spg/spg" + templateSuffix);
            //Deal with the PTDAF and PIDAF exceptions
            }else if (appName.endsWith("daf")) {
                editedName = appName.substring(0,(appName.length()-3)).toUpperCase();
                templateFile = new File(templatePath + appName.substring(0,(appName.length()-3)) +
                        "/" + appName.substring(0,(appName.length()-3)) + templateSuffix);//subtract off the 'daf' part
            }else if (appName.endsWith("constrained")){ //for the piConstrained runs.
                editedName = appName.substring(0,(appName.length()-11)).toUpperCase();
                templateFile = new File(templatePath + appName.substring(0,(appName.length()-11)) +
                        "/" + appName.substring(0,(appName.length()-11)) + templateSuffix);
            //Deal with the global exception
            } else if (appName.equalsIgnoreCase("global")) {
                editedName="GLOBAL";
                templateFile = new File(templatePath + "global" + templateSuffix);
            } else{
                editedName = appName.toUpperCase();
                templateFile = new File(templatePath + appName + "/" + appName + templateSuffix);
            }

            if(templateFile.exists()){
                logger.info("Full Path to Template File: " + templateFile.getAbsolutePath());
                String currentTemplate = runLogHashmap.get(editedName + "_TEMPLATE_INTERVAL");
                if(currentTemplate == null){ // no previous template was found.
                    runLogHashmap.put(editedName + "_TEMPLATE_INTERVAL", Integer.toString(i));
                    try {
                        logger.info("Writing the initial template location for this app into the run log");
                        runLogWriter.write(editedName + "_TEMPLATE_INTERVAL=" + Integer.toString(i));
                        runLogWriter.newLine();
                        runLogWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                 } else if(!currentTemplate.equals(Integer.toString(i))){  //a more recent template file was found
                    try {
                        logger.info("Writing the new template location for this app into the run log");
                        runLogWriter.write(editedName + "_TEMPLATE_INTERVAL=" + Integer.toString(i));
                        runLogWriter.newLine();
                        runLogWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } //if template file is the same as the last one found don't write it into the log.
                return templateFile;
            }else {
                i--;
            }
        }
        // if you get to here it means a template file couldn't be found in any of the tn directories
        // so templateFile = null
        logger.fatal("Couldn't find Template File for " + appName + ".  Returning a file that does not exist");
        return templateFile;
    }



    public ResourceBundle findResourceBundle(String pathToRb) {
        File propFile = new File(pathToRb);
        ResourceBundle rb = ResourceUtil.getPropertyBundle(propFile);
        if(rb == null ) logger.fatal("Problem loading resource bundle: " + pathToRb);
        return rb;
    }



    
    public void writeRunParamsToPropertiesFile(String pathToAppRb, String pathToGlobalRb, String moduleName){
        File runParams = new File(rootDir + "/scenario_" + scenarioName + "/daf/RunParams.properties");
        logger.info("Writing 'scenarioName', 'baseYear, 'timeInterval', 'pathToRb' and 'pathToGlobalRb' into " + runParams.getAbsolutePath());
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter(runParams));
            writer.println("scenarioName=" + scenarioName);
            writer.println("baseYear=" + baseYear);
            writer.println("timeInterval=" + t);
            writer.println("pathToAppRb=" + pathToAppRb);
            writer.println("pathToGlobalRb=" + pathToGlobalRb);
            writer.println("pecasName=" + moduleName);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open the RunParams.txt file", e);
        }
        writer.close();
    }
    
    public void writeRunParamsToPropertiesFile(String pathToAppRb, String pathToGlobalRb){
        File runParams = new File(rootDir + "/scenario_" + scenarioName + "/daf/RunParams.properties");
        logger.info("Writing 'scenarioName', 'baseYear, 'timeInterval', 'pathToRb' and 'pathToGlobalRb' into " + runParams.getAbsolutePath());
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter(runParams));
            writer.println("scenarioName=" + scenarioName);
            writer.println("baseYear=" + baseYear);
            writer.println("timeInterval=" + t);
            writer.println("pathToAppRb=" + pathToAppRb);
            writer.println("pathToGlobalRb=" + pathToGlobalRb);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open the RunParams.txt file", e);
        }
        writer.close();
    }
    
    
    public void runEDModel(int baseYear, int timeInterval, ResourceBundle appRb){

        ModelComponent comp = new EDControl(baseYear,timeInterval,appRb);
        comp.startModel(baseYear, timeInterval);

    }

    public void runALDModel(int baseYear, int timeInterval, ResourceBundle appRb){
        ModelComponent comp = new ALDModel();
        comp.setApplicationResourceBundle(appRb);
        comp.startModel(baseYear, timeInterval);
    }

    public void runSPG1Model(int baseYear, int timeInterval, ResourceBundle appRb, ResourceBundle globalRb){

        String baseYearS = Integer.toString(baseYear);
        String currentYear = Integer.toString(baseYear + timeInterval);

        SPGnew spg = new SPGnew( appRb, globalRb, baseYearS, currentYear );

        spg.getHHAttributesFromPUMS(baseYearS);
        spg.spg1(currentYear);
        //spg.writeFrequencyTables();
        TableDataSet table = spg.sumHouseholdsByIncomeSize();
        spg.writePiInputFile(table);
        
    }

    public void runPIModel(int baseYear, int timeInterval, ResourceBundle appRb, ResourceBundle globalRb){
        PIModel pi = new PIModel();
        pi.setResourceBundles(appRb, globalRb);
        pi.startModel(baseYear, timeInterval);
    }

    public void runPIDAFModel(int timeInterval, String pathToAppRb, String pathToGlobalRb, String nodeName){
        //Since AO doesn't communicate directly with PI we need to write the absolute
        //path to the resource bundle and the time interval into a file, "RunParams.txt"
        //that will be read by the PIServer Task when the PIDAF application is launched.
        writeRunParamsToPropertiesFile(pathToAppRb, pathToGlobalRb,"pi");
        StartDafApplication appRunner = new StartDafApplication("pidaf", nodeName, timeInterval, rb);
        appRunner.run();
    }

    public void runSPG2Model(int baseYr, int timeInterval, ResourceBundle appRb, ResourceBundle globalRb){

        String baseYear = Integer.toString(baseYr);
        String currentYear = Integer.toString(baseYr + timeInterval);

        SPGnew spg = new SPGnew( appRb, globalRb, baseYear, currentYear );

        spg.spg2();
        spg.writeZonalSummaryToCsvFile();
        spg.writeHHOutputAttributesFromPUMS(baseYear);

    }

    public void runPTDAFModel(int timeInterval, String pathToAppRb, String pathToGlobalRb,String nodeName){
        writeRunParamsToPropertiesFile(pathToAppRb, pathToGlobalRb);
        StartDafApplication appRunner = new StartDafApplication("ptdaf", nodeName, timeInterval, rb);
        appRunner.run();
    }

    public void runCTModel(int baseYear, int timeInterval, ResourceBundle appRb, ResourceBundle globalRb){
        ModelComponent comp = new CTModel(appRb, globalRb);
        comp.startModel(baseYear, timeInterval);

    }

    public void runETModel(int baseYear, int timeInterval, ResourceBundle appRb, ResourceBundle globalRb){
        ETModel et = new ETModel(appRb, globalRb);

        et.startModel(baseYear, timeInterval);
    }

    public void runTSModel(int baseYear, int timeInterval, ResourceBundle appRb, ResourceBundle globalRb){
        ModelComponent tsModel = new TSModelComponent(appRb, globalRb, null);
        tsModel.startModel(baseYear, timeInterval);
    }

    public void runTSDAFModel(int baseYear, int timeInterval, String pathToAppRb, String pathToGlobalRb, String configFileName){
        ResourceBundle appRb = findResourceBundle(pathToAppRb);
        ResourceBundle globalRb = findResourceBundle(pathToGlobalRb);
        ModelComponent tsModel = new TSModelComponent(appRb, globalRb, configFileName);
        tsModel.startModel(baseYear, timeInterval);
    }




    private void logAppRun(String appName){
        if (appName.endsWith("daf")) {
            appName = appName.substring(0,(appName.length()-3));
        }
        try {
            logger.info("Writing the application name and the timeInterval for run into the run log");
            runLogWriter.write(appName.toUpperCase() + "_LAST_RUN=" + t);
            runLogWriter.newLine();
            runLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {
            //Get command line arguments: Root Directory, Scenario Name, Application Name,
            //                            Time Interval and Start Node (for daf applications)
            String rootDir = args[0];
            String baseScenarioName = args[1];
            String scenarioName = args[2];
            String appName = args[3];
            int baseYear = Integer.parseInt(args[4]);
            int t = Integer.parseInt(args[5]);

            logger.info("Root Directory: " + rootDir);
            logger.info("Base Scenario Name: " + baseScenarioName);
            logger.info("Scenario Name: " + scenarioName);
            logger.info("App Name: " + appName);
            logger.info("Base Year: " + baseYear);
            logger.info("Time Interval: " + t);
            String configFileOrNodeName = null; //nodeName is for daf2, configFile is for daf3
            if(args.length == 7 ){
                configFileOrNodeName = args[6];
                logger.info("Daf Property: "+ configFileOrNodeName);
            }

            //Create an ApplicationOrchestrator object that will handle creating the
            //resource bundle for the appropriate app, retrieve that resource bundle and
            //start the application, passing along the property bundle
            ApplicationOrchestrator ao = new ApplicationOrchestrator(rootDir,baseScenarioName, scenarioName,t,baseYear);

            //Before starting, AO needs to create the base year run log
            ao.createBaseYearPropFile();

            //For each t interval that the model runs in,  AO needs to create the runLogProperty file and write in the current year
            // if the file does not already exist in that year.  This file will then be updated by any application that runs.
            ao.createRunLogPropFile();
            //We need to read in the runLog.properties files starting
            //in year interval=0 and moving up to interval=t.  We will fill
            //up a hashmap with the appropriate values.
            ao.updateRunLogPropertiesHashmap(t);

            //Create an ao.properties file for the current 't' directory
            // using the most recent aoTemplate.properties file
            String pathToAoRb = ao.createAppRb("ao");

            //Get the ao.properties file that was just created and read in the "base.year"
            //value from the ao.properties so that you can pass it to the application that you are starting.
            ResourceBundle aoRb = ao.findResourceBundle(pathToAoRb);
            ao.setRb(aoRb);

            //Create a global.properties file for the current 't' directory
            // using the most recent globalTemplate.properties file
            String pathToGlobalRb = ao.createAppRb("global");

            //Get the global properties file that was just created so that you can pass it to the current application
            ResourceBundle globalRb = null;
            if(!appName.endsWith("daf")){
                globalRb = ao.findResourceBundle(pathToGlobalRb);
            }

            //Read in the appNameTemplate.properties and replace
            //all patterns with values from the RunLogHashMap and write properties file
            //to the appropriate directory.
            String pathToAppRb = ao.createAppRb(appName);

            //Unless AO is being asked to start a daf-application, it should
            //find the actual application resource bundle and pass it to the app
            //on start-up.  Daf applications will be passed a path to a runProperties file
            //instead of the actual file.
            ResourceBundle appRb = null;
            if(! appName.endsWith("daf")){
                appRb = ao.findResourceBundle(pathToAppRb);
            }
            if(appName.equalsIgnoreCase("ED")){
                logger.info("AO will now start ED for simulation year " + (baseYear+t));
                ao.runEDModel(baseYear, t, appRb);
            }else if(appName.equalsIgnoreCase("ALD")){
                logger.info("AO will now start ALD for simulation year " + (baseYear+t));
                ao.runALDModel(baseYear, t, appRb);
            }else if(appName.equalsIgnoreCase("SPG1")){
                logger.info("AO will now start SPG1 for simulation year " + (baseYear+t));
                ao.runSPG1Model(baseYear,t,appRb, globalRb);
            }else if (appName.equalsIgnoreCase("PI")){
                logger.info("AO will now start PI for simulation year " + (baseYear+t));
                ao.runPIModel(baseYear,t,appRb,globalRb);
            }else if(appName.equalsIgnoreCase("PIDAF")){
                logger.info("AO will now start PIDAF for simulation year " + (baseYear+t));
                ao.runPIDAFModel(t,pathToAppRb,pathToGlobalRb,configFileOrNodeName);
            }else if (appName.equalsIgnoreCase("SPG2")){ //not a daf application
                logger.info("AO will now start SPG2 for simulation year " + (baseYear+t));
                ao.runSPG2Model(baseYear,t,appRb, globalRb);
            }else if(appName.equalsIgnoreCase("PTDAF")){
                logger.info("AO will now start PTDAF for simulation year " + (baseYear+t));
                ao.runPTDAFModel(t, pathToAppRb,pathToGlobalRb,configFileOrNodeName);
            }else if(appName.equalsIgnoreCase("CT")){
                logger.info("AO will now start CT for simulation year " + (baseYear+t));
                ao.runCTModel(baseYear, t, appRb, globalRb);
            }else if(appName.equalsIgnoreCase("ET")){
                logger.info("AO will now start ET for simulation year " + (baseYear+t));
                ao.runETModel(baseYear, t, appRb, globalRb);
            }else if(appName.equalsIgnoreCase("TS")){
                logger.info("AO will now start TS for simulation year " + (baseYear+t));
                ao.runTSModel(baseYear, t, appRb, globalRb);
            }else if(appName.equalsIgnoreCase("TSDAF")){
                logger.info("AO will now start TS DAF for simulation year " + (baseYear+t));
                ao.runTSDAFModel(baseYear, t, pathToAppRb, pathToGlobalRb, configFileOrNodeName);
            }else {
                logger.fatal("AppName not recognized");
            }

            ao.logAppRun(appName);
            logger.info(appName + " is complete");
            
            
        } catch (Exception e) {
            logger.fatal("An application threw the following error");
            throw new RuntimeException("An application threw the following error", e);
        }
    }




}
