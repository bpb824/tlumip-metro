package com.pb.despair.pt.daf;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.Date;

import com.pb.common.daf.Message;
import com.pb.common.daf.MessageProcessingTask;
import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
import com.pb.despair.model.ModeChoiceLogsums;
import com.pb.despair.pt.CreateDestinationChoiceLogsums;
import com.pb.despair.pt.PTModelInputs;

/**
 * AggregateDestinationChoiceLogsumsTask
 *
 * @author Freedman
 * @version Aug 10, 2004
 * 
 */
public class AggregateDestinationChoiceLogsumsTask  extends MessageProcessingTask {
    protected static Logger logger = Logger.getLogger("com.pb.despair.pt.daf");
    protected static Object lock = new Object();
    protected static ResourceBundle rb;
    protected static boolean initialized = false;
    boolean firstDCLogsum = true;
    CreateDestinationChoiceLogsums dcLogsumCalculator = new CreateDestinationChoiceLogsums();
//    protected static WorkLogsumMap logsumMap = new WorkLogsumMap();
    String fileWriterQueue = "FileWriterQueue";

    /**
     * Onstart method sets up model
     */
    public void onStart() {
        synchronized (lock) {
            logger.info( "***" + getName() + " started");
            //in cases where there are multiple tasks in a single vm, need to make sure only initilizing once!
            if (!initialized) {
                //We need to read in the Run Parameters (timeInterval and pathToResourceBundle) from the RunParams.txt file
                //that was written by the Application Orchestrator
                BufferedReader reader = null;
                int timeInterval = -1;
                String pathToRb = null;
                try {
                    logger.info("Reading RunParams.txt file");
                    reader = new BufferedReader(new FileReader(new File("/models/tlumip/daf/RunParams.txt")));
                    timeInterval = Integer.parseInt(reader.readLine());
                    logger.info("\tTime Interval: " + timeInterval);
                    pathToRb = reader.readLine();
                    logger.info("\tResourceBundle Path: " + pathToRb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rb = ResourceUtil.getPropertyBundle(new File(pathToRb));

                PTModelInputs ptInputs = new PTModelInputs(rb);
                logger.info("Setting up the aggregate mode choice model");
                ptInputs.setSeed(2002);
                ptInputs.getParameters();
                ptInputs.readSkims();
                ptInputs.readTazData();

                initialized = true;
            }

            logger.info( "***" + getName() + " finished onStart()");
        }
    }
    /**
     * A worker bee that will process a block of households.
     *
     */
    public void onMessage(Message msg) {
        logger.info("********" + getName() + " received messageId=" + msg.getId() +
            " message from=" + msg.getSender() + " at " + new Date());

         if (msg.getId().equals(MessageID.CREATE_DC_LOGSUMS)) 
            createDCLogsums(msg);
    }
    /**
     * Create destination choice aggregate logsums
     * @param msg
     */
    public void createDCLogsums(Message msg){
        if (firstDCLogsum) {
            logger.info(getName() + " is setting population, school occupation and collapsing employment " +
                    "in the ptModel tazs");
            dcLogsumCalculator.buildModel(PTModelInputs.tazs);
            firstDCLogsum=false;
        }

        String purpose = String.valueOf(msg.getValue("purpose"));
        Integer segment = (Integer) msg.getValue("segment");

        String path = ResourceUtil.getProperty(rb, "mcLogsum.path");
        ModeChoiceLogsums mcl = new ModeChoiceLogsums(rb);
        mcl.readLogsums(purpose.charAt(0),segment.intValue());
        Matrix modeChoiceLogsum =mcl.getMatrix();

        if (purpose.equals("c")) {
            for (int i = 1; i <= 3; i++) {
                logger.info(getName() + " is calculating the DC Logsums for purpose c, market segment " + segment + " subpurpose " + i);
                
                //create a message to store the dc logsum vector
                Message dcLogsumMessage = createMessage();
                dcLogsumMessage.setId(MessageID.DC_LOGSUMS_CREATED);

                String dcPurpose = "c" + i;
                Matrix dcLogsumMatrix = (Matrix) dcLogsumCalculator.getDCLogsumVector(PTModelInputs.tazs,
                PTModelInputs.tdpd, dcPurpose, segment.intValue(), modeChoiceLogsum);
                dcLogsumMessage.setValue("matrix", dcLogsumMatrix);
                sendTo(fileWriterQueue, dcLogsumMessage);
                
                //get the exponentiated utilities matrix and put it in another message
                Message dcExpUtilitiesMessage = createMessage();
                dcExpUtilitiesMessage.setId(MessageID.DC_EXPUTILITIES_CREATED);
                Matrix expUtilities = dcLogsumCalculator.getExpUtilities();
                dcExpUtilitiesMessage.setValue("matrix", expUtilities);
                sendTo(fileWriterQueue, dcExpUtilitiesMessage);
                
            }
        } else if (!purpose.equals("w")) {
            logger.info(getName() + " is calculating the DC Logsums for purpose " + purpose + ", market segment " + segment + " subpurpose 1");
            Message dcLogsumMessage = createMessage();
            dcLogsumMessage.setId(MessageID.DC_LOGSUMS_CREATED);
            Matrix dcLogsumMatrix = (Matrix) dcLogsumCalculator.getDCLogsumVector(PTModelInputs.tazs,
            PTModelInputs.tdpd, purpose, segment.intValue(), modeChoiceLogsum);
            dcLogsumMessage.setValue("matrix", dcLogsumMatrix);
            sendTo(fileWriterQueue, dcLogsumMessage);
            
            //get the exponentiated utilities matrix and put it in another message
            Message dcExpUtilitiesMessage = createMessage();
            dcExpUtilitiesMessage.setId(MessageID.DC_EXPUTILITIES_CREATED);
            Matrix expUtilities = dcLogsumCalculator.getExpUtilities();
            dcExpUtilitiesMessage.setValue("matrix", expUtilities);
            sendTo(fileWriterQueue, dcExpUtilitiesMessage);

        }

        modeChoiceLogsum = null;
    }

}
