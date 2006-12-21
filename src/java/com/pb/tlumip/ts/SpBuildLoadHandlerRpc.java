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
package com.pb.tlumip.ts;

import java.util.Vector;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.pb.common.rpc.DafNode;
import com.pb.common.rpc.RpcClient;
import com.pb.common.rpc.RpcException;

/**
 * @author   Jim Hicks  
 * @version  Sep 20, 2005
 */
public class SpBuildLoadHandlerRpc implements SpBuildLoadHandlerIF {

    protected static transient Logger logger = Logger.getLogger(SpBuildLoadHandlerRpc.class);

    RpcClient rc = null;
    String handlerName = null;



    public SpBuildLoadHandlerRpc( String rpcConfigFileName, String handlerName ) {
        
        this.handlerName = handlerName;
        
        try {
            
            // Need a config file to initialize a Daf node
            DafNode.getInstance().initClient(rpcConfigFileName);
            
            rc = new RpcClient(handlerName);
        }
        catch (MalformedURLException e) {
            logger.error( "MalformedURLException caught in SpBuildLoadHandlerRpc() while defining RpcClient for " + handlerName + ".", e);
        }
        catch (Exception e) {
            logger.error( "Exception caught in SpBuildLoadHandlerRpc() while defining RpcClient for " + handlerName + ".", e);
        }
    }
    
    
    
    public int setup(double[][][] tripTables ) {

        int returnValue = -1;
        try {
            Vector params = new Vector();
            params.add(tripTables);
            returnValue = (Integer)rc.execute(handlerName+".setup", params );
        } catch (RpcException e) {
            logger.error( e );
        } catch (IOException e) {
            logger.error(  e );
        }
        return returnValue;
    }
    
    
    public int start() {
        int returnValue = -1;
        try {
            returnValue = (Integer)rc.execute(handlerName+".start", new Vector() );
        } catch (RpcException e) {
            logger.error( e );
        } catch (IOException e) {
            logger.error(  e );
        }
        return returnValue;
    }

    
    public double[][] getResults() {
        double[][] returnValue = null;
        try {
            returnValue = (double[][])rc.execute(handlerName+".getResults", new Vector() );
        } catch (RpcException e) {
            logger.error( e );
        } catch (IOException e) {
            logger.error(  e );
        }
        return returnValue;
    }
    
    
    public boolean handlerIsFinished() {
        boolean returnValue = false;
        try {
            returnValue = (Boolean)rc.execute(handlerName+".handlerIsFinished", new Vector() );
        } catch (RpcException e) {
            logger.error( e );
        } catch (IOException e) {
            logger.error(  e );
        }
        return returnValue;
    }

}
