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
package com.pb.tlumip.pt.daf;

import com.pb.tlumip.pt.PTOccupation;
import com.pb.models.pt.daf.WorkplaceLocationWorkerTask;

/**
 * TLUMIPWorkplaceLocationTask is a class that ...
 *
 * @author Kimberly Grommes
 * @version 1.0, Feb 15, 2007
 *          Created by IntelliJ IDEA.
 */
public class TLUMIPWorkplaceLocationTask extends WorkplaceLocationWorkerTask {
    
    public void onStart(){
        super.onStart(PTOccupation.NONE);
    }
}