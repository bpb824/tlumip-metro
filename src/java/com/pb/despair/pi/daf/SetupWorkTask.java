package com.pb.despair.pi.daf;

import com.pb.common.daf.MessageProcessingTask;
import com.pb.common.daf.Message;
import com.pb.common.util.ResourceUtil;
import com.pb.despair.pi.PIPProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ResourceBundle;

/**
 * This class will calculate the composite buy and sell utilities
 * of a commodity in each zone when a message bearing a commodity
 * name is received.  It will return the buy and sell CUs in an array.
 * 
 * @author Christi Willison
 * @version Apr 27, 2004
 */
public class SetupWorkTask extends MessageProcessingTask {

    public static ResourceBundle piRb;
    public static ResourceBundle globalRb;
    private ResourceBundle pidafRb;
    String scenarioName = "pleaseWork";
    public static int timeInterval;

    public void onStart() {
        logger.info("**********************" + getName() + " has started **************************");
    }

    public void onMessage(Message msg) {
        logger.info( getName() + " received " + msg.getId() + " from" + msg.getSender() );
        logger.info("***" + getName() + " is reading the pidaf_pleaseWork.properties file");
        pidafRb = ResourceUtil.getResourceBundle("pidaf_pleaseWork");
        String runParamFilePath = ResourceUtil.getProperty(pidafRb,"run.param.file");
        logger.info("***" + getName() + " has read the properties file and is now moving on to the RunParams.txt file");
        //We need to read in the Run Parameters (timeInterval and pathToResourceBundle) from the RunParams.txt file
        //that was written by the Application Orchestrator
        BufferedReader reader = null;
        String scenarioName = null;
        String pathToPiRb = null;
        String pathToGlobalRb = null;
        try {
            logger.info("Reading RunParams.txt file: " + runParamFilePath);
            reader = new BufferedReader(new FileReader(new File(runParamFilePath)));
            scenarioName = reader.readLine();
            logger.info("\tScenario Name: " + scenarioName);
            timeInterval = Integer.parseInt(reader.readLine());
            logger.info("\tTime Interval: " + timeInterval);
            pathToPiRb = reader.readLine();
            logger.info("\tPI ResourceBundle Path: " + pathToPiRb);
            pathToGlobalRb = reader.readLine();
            logger.info("\tGlobal ResourceBundle Path: " + pathToGlobalRb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Loading pi.properties ResourceBundle");
        piRb = ResourceUtil.getPropertyBundle(new File(pathToPiRb));

        logger.info("Loading global.properties ResourceBundle");
        globalRb = ResourceUtil.getPropertyBundle(new File(pathToGlobalRb));

        logger.info("Reading data and setting up for PI run");
        long startTime = System.currentTimeMillis();
        String pProcessorClass = ResourceUtil.getProperty(piRb,"pprocessor.class");
        logger.info("PI will be using the " + pProcessorClass + " for pre and post PI processing");
        Class ppClass = null;
        PIPProcessor piReaderWriter = null;
        try {
            ppClass = Class.forName(pProcessorClass);
            piReaderWriter = (PIPProcessor) ppClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            logger.fatal("Can't create new instance of PiPProcessor of type "+ppClass.getName());
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.fatal("Can't create new instance of PiPProcessor of type "+ppClass.getName());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        piReaderWriter.setResourceBundles(piRb, globalRb);
        piReaderWriter.setTimePeriod(timeInterval);

        piReaderWriter.setUpPi();
        logger.info("Setup is complete. Time in seconds: "+((System.currentTimeMillis()-startTime)/1000));
        Message doneMsg = mFactory.createMessage();
        sendTo("SetupResultsQueue",doneMsg);
    }


}