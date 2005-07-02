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
package com.pb.tlumip.reports;

import com.pb.common.util.ResourceUtil;
import com.pb.common.datafile.TableDataSet;
import com.pb.common.datafile.CSVFileWriter;

import java.util.ResourceBundle;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;

/**
 * Author: willison
 * Date: Dec 14, 2004
 * This class will be called from AO when
 * calibration files need to be written.
 *
 * The actual methods to create the
 * module specific output
 * files will be in the respective
 * module code as a static class.
 *
 *
 * Created by IntelliJ IDEA.
 */
public class CalibrationFileWriter {

    protected static Logger logger = Logger.getLogger("com.pb.tlumip.ao");
    ResourceBundle rb;
    String calibrationOutputPath;
    String scenarioName;
    int timeInterval;

    public CalibrationFileWriter(ResourceBundle rb, String scenarioName, int timeInterval){
        this.rb = rb;
        this.calibrationOutputPath = ResourceUtil.getProperty(this.rb, "calibration.output.path");
        this.scenarioName = scenarioName;
        this.timeInterval = timeInterval;
    }

    public void writeCalibrationFiles(TableDataSet[] dataSets, String appName){
        //first create output path by concatinating the output path + scenarioName + timeInterval + application
        String outputPath = calibrationOutputPath + "scenario_"+ scenarioName + "/calibration/outputs/t" +
                timeInterval + "/" + appName + "/";

        for(int i=0; i< dataSets.length; i++){
            String fileName = ResourceUtil.getProperty(rb, (dataSets[i].getName()+ ".file"));
            String fullPath = outputPath+fileName;
            if(logger.isDebugEnabled()) {
                logger.debug("Full path to output file is: " + fullPath);
            }
            writeCalibrationOutputFile(fullPath, dataSets[i]);
        }

    }

    private void writeCalibrationOutputFile(String outputFilePath, TableDataSet tableData){
        CSVFileWriter writer = new CSVFileWriter();
        try {
            writer.writeFile(tableData, new File(outputFilePath));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
