package com.pb.despair.pt.daf;


/**
 * List of MessageIDs used in PTDaf
 * 
 * @author hansens
 *

 */
public final class MessageID{
	
    //Messages sent from PTDafMaster
    public static final String CREATE_MC_LOGSUMS = "CreateMCLogsums";
    public static final String CREATE_DC_LOGSUMS = "CreateDCLogsums";
    
    //public static final String CREATE_LABOR_FLOWS = "CreateLaborFlows";
    public static final String CALCULATE_WORKPLACE_LOCATIONS = "CalculateWorkplaceLocations";
    public static final String PROCESS_HOUSEHOLDS = "ProcessHouseholds";
    public static final String LOAD_DC_LOGSUMS = "LoadDCLogsums";
    
    //Messages sent from HouseholdWorker
    
    public static final String SKIMS = "Skims";
    public static final String MC_LOGSUMS_CREATED = "MCLogsumsCreated";
    public static final String MC_LOGSUMS_COLLAPSED = "MCLogsumsCollapsed";
    
    public static final String DC_LOGSUMS_CREATED = "DCLogsumsCreated";
    public static final String DC_EXPUTILITIES_CREATED = "DCExpUtilitiesCreated";
    
    //public static final String LABOR_FLOWS_CREATED = "LaborFlowsCreated";
    public static final String WORKPLACE_LOCATIONS_CALCULATED = "WorkplaceLocationsCalculated";
    public static final String HOUSEHOLDS_PROCESSED = "HouseholdsProcessed";
    public static final String UPDATE_TAZDATA = "UpdateTazData";
    public static final String TAZDATA_UPDATED = "TazDataUpdated";
    public static final String NODE_SETUP_DONE = "NodeSetupDone";
    public static final String NUM_OF_HHS = "NumOfHHs";
    public static final String NUM_OF_PERSONS = "NumOfPersons";
    public static final String NUM_OF_WORK_QUEUES = "NumOfWorkQueues";
    public static final String ALL_HOUSEHOLDS_PROCESSED = "AllHHsDone";
    public static final String ALL_FILES_WRITTEN = "AllFilesWritten";
}
