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

import java.io.PrintWriter;


/** 
 * Person Attributes for Trip Mode Choice
 * 
 * @author Joel Freedman
 * @version 1.0 12/01/2003
 * 
 */

public class PersonTripModeAttributes {

   public int age;
   public int autos;


   public int size2;
   public int size3p;
   public int inclow;
   public int incmed;
   public int inchi;

   public PersonTripModeAttributes(){
        
             age=0;
             autos=0;

             size2=0;
             size3p=0;
             inclow=0;
             incmed=0;
             inchi=0;
        }
   public PersonTripModeAttributes(PTHousehold thisHousehold, PTPerson thisPerson){
        
       age=0;
       autos=0;

       size2=0;
       size3p=0;
       inclow=0;
       incmed=0;
       inchi=0;
            
        age=thisPerson.age;
       autos=thisHousehold.autos;
     
          if(thisHousehold.size==2)
                  size2=1;
             else if(thisHousehold.size>=3)
                  size3p=1;
        
             if(thisHousehold.income<15000)
                  inclow=1;
             else if(thisHousehold.income>=15000 && thisHousehold.income<30000)
                  incmed=1;
             else
                  inchi=1;
                  
        }

    public void print(PrintWriter file){
        file.println("PersonTripModeAttributes:");
        file.println("\tage = " + age);
        file.println("\tautos = " + autos);
        file.println("\tsize2 = " + size2);
        file.println("\tsize3p = " + size3p);
        file.println("\tinclow = " + inclow);
        file.println("\tincmed = " + incmed);
        file.println("\tinchi = " + inchi);
        file.println();
        file.println();

        file.flush();
    }

}
     
     