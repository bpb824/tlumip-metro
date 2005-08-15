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
/* Generated by Together */

package com.pb.tlumip.ha;

import com.pb.models.pecas.LogitModel;
import com.pb.models.pecas.Alternative;
import com.pb.models.pecas.AbstractTAZ;

public class VacationLocationLogit extends LogitModel {
    
    class VacationLocation implements Alternative {
        AbstractTAZ z;

        /* (non-Javadoc)
         * @see com.pb.common.old.model.Alternative#getUtility()
         */
        public double getUtility(double higherLevelDispersionParameter) {
            return h.utilityOfVacationAlternative(z);
        }
        
        private VacationLocationLogit getVacationLocationLogit() {
            return VacationLocationLogit.this;
        }
    }
    
    void addAlternatives(AbstractTAZ[] zones) {
        for (int z=0;z<zones.length;z++) {
            VacationLocation h = new VacationLocation();
            h.z = zones[z];
            addAlternative(h);
        }
    }
    
    Household h;

    public void addAlternative(Alternative a) {
        if (! (a instanceof VacationLocation)) {
            throw new Error("Can only add VacationLocation objects to this logit model");
        }
        VacationLocation v = (VacationLocation) a;
        if (v.getVacationLocationLogit() !=this) throw new Error ("Can only add association VacationLocation objects to the VacationLocationLogitModel");
        super.addAlternative(a);
    }

    public void setHousehold(Household h) {
        this.h = h;
    }
}
