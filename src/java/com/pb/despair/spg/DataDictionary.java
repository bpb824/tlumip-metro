package com.pb.despair.spg;

import com.pb.common.util.Justify;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * The DataDictionary class in the synpop.pums package reads and defines
 * the PUMS data dictionary for reading PUMS HH and Person data records.
 *
 */
public class DataDictionary {

    protected Logger logger = Logger.getLogger("com.pb.despair.spg");

	private static final int HH_SHEET = 0;
	private static final int PERSON_SHEET = 1;
	private static final int VARIABLE_FIELD = 1;
	private static final int START_COL_FIELD = 1;
	private static final int LENGTH_FIELD = 1;

    private static final int NUMFIELDS = 4;
    private static final int NUMHHATTRIBS = 120;
    private static final int NUMPERSATTRIBS = 130;

    public ArrayList HHAttribs;
    public ArrayList PersAttribs;
    
    public static final boolean DEBUG = false;
    
    
    
    public DataDictionary (String fileName) {
        this.HHAttribs = new ArrayList(NUMHHATTRIBS);
        this.PersAttribs = new ArrayList(NUMPERSATTRIBS);

		readPUMS1990DataDictionary(fileName);

//		readPUMS2000DataDictionary(fileName);
        
        logger.info (this.HHAttribs.size() + " PUMS HH variables read.");
        logger.info (this.PersAttribs.size() + " PUMS Person variables read.");
    }
    

    public int getStartCol (ArrayList attribs, String PUMSVariable) {
    
        int i = getPUMSVariableIndex (attribs, PUMSVariable);
        if (DEBUG) {
            logger.info ("getStartCol PUMSVariable = " + PUMSVariable);
            logger.info ("getStartCol PUMSVariable Index = " + i);
            logger.info ("getStartCol PUMSVariable startCol = " + (((DataDictionaryRecord)attribs.get(i)).startCol));
        }
        return ((DataDictionaryRecord)attribs.get(i)).startCol;
    }
    
        
    public int getNumberCols (ArrayList attribs, String PUMSVariable) {
    
        int i = getPUMSVariableIndex (attribs, PUMSVariable);
        if (DEBUG) {
            logger.info ("getNumberCols PUMSVariable = " + PUMSVariable);
            logger.info ("getNumberCols PUMSVariable Index = " + i);
            logger.info ("getNumberCols PUMSVariable numberCols = " + ((DataDictionaryRecord)attribs.get(i)).numberCols);
        }
        return ((DataDictionaryRecord)attribs.get(i)).numberCols;
    }
    
        
    public int getLastCol (ArrayList attribs, String PUMSVariable) {
    
        int i = getPUMSVariableIndex (attribs, PUMSVariable);
        if (DEBUG) {
            logger.info ("getLastCol PUMSVariable = " + PUMSVariable);
            logger.info ("getLastCol PUMSVariable Index = " + i);
            logger.info ("getLastCol PUMSVariable lastCol = " + (((DataDictionaryRecord)attribs.get(i)).startCol + ((DataDictionaryRecord)attribs.get(i)).numberCols));
        }
        return ((DataDictionaryRecord)attribs.get(i)).startCol + ((DataDictionaryRecord)attribs.get(i)).numberCols;
    }
    
       
    public void printDictionary (ArrayList attribs) {
        
        Justify myFormat = new Justify();
        int blanks;
        
        if (attribs.size() > 0)
            logger.info ("Index    Variable      Start Column    Number Columns");
        for (int i=0; i < attribs.size(); i++) {
            
            logger.info (myFormat.left(i, 5));
            
            blanks = 12 - ((DataDictionaryRecord)attribs.get(i)).variable.length();
            for (int b=0; b < blanks; b++)
                logger.info (" ");
            logger.info (((DataDictionaryRecord)attribs.get(i)).variable);
            
            blanks = 6;
            for (int b=0; b < blanks; b++)
                logger.info (" ");
            logger.info (myFormat.right(((DataDictionaryRecord)attribs.get(i)).startCol, 12));
            
            blanks = 6;
            for (int b=0; b < blanks; b++)
                logger.info (" ");
            logger.info (myFormat.right(((DataDictionaryRecord)attribs.get(i)).numberCols, 12));
        }

    }
    
     
    private int getPUMSVariableIndex (ArrayList attribs, String PUMSVariable) {
    
        int index = -1;
        
        for (int i=0; i < attribs.size(); i++) {
            if (((DataDictionaryRecord)attribs.get(i)).variable.equals(PUMSVariable)) {
                index = i;
                break;
            }
        }
        
        if (index < 0) {
            logger.severe("PUMS variable: " + PUMSVariable + " not found in data dictionary.");
            logger.severe("exiting getPUMSVariableIndex(" + PUMSVariable + ") in DataDictionary.");
            logger.severe("exit (10)");
            System.exit (10);
        }
        
        return index;
    }


    
    private void readPUMS1990DataDictionary (String fileName) {
    
        String token;
        int attrib;
        int tokenCount;
        int RECTYPECount=0;
                
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String s = new String();
            
            // skip the first record (header) which defines fields
            s = in.readLine();
            while ((s = in.readLine()) != null) {

                if (s.length() > 0) {
                    StringTokenizer st = new StringTokenizer(s);
                    tokenCount = st.countTokens();

                    if (st.hasMoreTokens()) {

                        // only parse records beginning with "D"
                        token = st.nextToken();
                        if (token.equals("D")) {
                        
                            if (tokenCount != NUMFIELDS) {
                                if (RECTYPECount <= 1)
                                    attrib = HHAttribs.size();
                                else
                                    attrib = PersAttribs.size();
                                logger.severe("data definition for attrib " + attrib + " in data dictionary file " + fileName + " has " + st.countTokens() + " fields, but should have " + NUMFIELDS + ".");
                                logger.severe("exiting readPUMSDataDictionary(" + fileName + ").");
                                logger.severe("exit (11)");
                                System.exit (11);
                            }
                            else {
                                DataDictionaryRecord ddRecord = new DataDictionaryRecord();
                            
                                ddRecord.variable = st.nextToken();
                                ddRecord.numberCols = Integer.parseInt(st.nextToken());
                                ddRecord.startCol = Integer.parseInt(st.nextToken()) - 1;
                                
                                if (ddRecord.variable.equals("RECTYPE"))
                                    RECTYPECount++;
                                    
                                if (RECTYPECount == 1)
                                    HHAttribs.add(ddRecord);
                                else
                                    PersAttribs.add(ddRecord);
                            }
                        }
                    }
                }
                
            }
        } catch (Exception e) {
            logger.severe ("IO Exception caught reading data dictionary file: " + fileName);
            e.printStackTrace();
        }
        
    }



	protected void readPUMS2000DataDictionary ( String fileName ) {
    
		Workbook workbook = null;
		
		try {
			workbook = Workbook.getWorkbook( new File(fileName) );
		}
		catch (Throwable t) {
			t.printStackTrace();
		}

		
		HHAttribs = readExcelDataDictionary ( workbook, "HH" );
		PersAttribs = readExcelDataDictionary ( workbook, "PERSON" );
		
	}



	protected ArrayList readExcelDataDictionary ( Workbook workbook, String definitionType ) {
    
		ArrayList attribs = new ArrayList();
		DataDictionaryRecord ddRecord = new DataDictionaryRecord();
		
		String recordType = null;
		
		Cell cell;
		Sheet sheet = null;
		
		if ( definitionType.equalsIgnoreCase("HH") ) {
			sheet = workbook.getSheet( HH_SHEET );
			recordType = "H";
		}
		else if ( definitionType.equalsIgnoreCase("PERSON") ) {
			sheet = workbook.getSheet( PERSON_SHEET );
			recordType = "P";
		}
		else {
			return null;
		}
		
		
		int row;
		// H record indicators are in column 0
		row = findEntry(sheet, recordType, 0, 0, false);
		if (row == -1)
			//No matching data records found
			return null;

		
		while (true) {

			// record type indicators are in column 0 (excel column A)
			row = findEntry(sheet, recordType, 0, 0, false);
			if (row == -1)
				//No more matching data records found
				return attribs;
			
			// VARIABLE field is in column 5 (excel column F)
			cell = sheet.getCell( 5, row );
			ddRecord.variable = cell.getContents();

			// BEG field is in column 1 (excel column B)
			cell = sheet.getCell( 1, row );
			ddRecord.startCol = Integer.parseInt( cell.getContents() );

			// LEN field is in column 3 (excel column D)
			cell = sheet.getCell( 1, row );
			ddRecord.numberCols = Integer.parseInt( cell.getContents() );

			attribs.add(ddRecord);
			
		}
		
	}



	protected int findEntry(Sheet sheet, String keyword, int startRow, int startColumn, boolean fail) {
		int rowFound = -1;
		String str;

		try {
			for (int r=startRow; r < sheet.getRows(); r++) {
				Cell cell = sheet.getCell(startColumn, r);
				str = cell.getContents();
				if (str.equalsIgnoreCase(keyword)) {
					rowFound = r;
					break;
				}
			}
		} catch (RuntimeException e) {
			//Did not find keyword
		}

		if ( (rowFound < 0) && (fail) ) {
			String msg = "Control file does not contain a \""+keyword+"\" section";
			throw new RuntimeException(msg);
		}

		return rowFound;
	}

	
	
    private class DataDictionaryRecord {
        String variable;
        int startCol;
        int numberCols;
    }
}

