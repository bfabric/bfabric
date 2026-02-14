/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric;

import java.time.format.DateTimeFormatter;

import javax.inject.Named;

@Named
public class Constants extends JSTLConstants {

    public static final String ACKNOWLEDGEDBY = "acknowledgedBy";

    public static final String ADD = "add";

    public static final String ADMIN = "admin";

    public static final String ALL = "all";

    public static final String ANALYSIS_REASONS = "analysisReasons";

    public static final String ANCESTOR = "ancestor";

    public static final String APPLICATION = "application";

    public static final String ASSIGN = "assign";

    public static final String AUTOCOMPLETE_CLIENT_ID = "autocompleteClientId";

    public static final String BACKGROUND_COLOR_CALCULATED_SAMPLE_ATTRIBUTE_WARNING = "background-color: #ffceab !important;";

    public static final String BACKGROUND_COLOR_BLUE = "background-blue";

    public static final String BACKGROUND_COLOR_BLUE_LIGHT = "background-blue-light";

    public static final String BACKGROUND_COLOR_BROWN = "background-brown";

    public static final String BACKGROUND_COLOR_GREEN = "background-green";

    public static final String BACKGROUND_COLOR_RED = "background-red";

    public static final String BACKGROUND_COLOR_ORANGE = "background-orange";

    public static final String BACKGROUND_COLOR_YELLOW = "background-yellow";

    public static final String BASIC_PRICE = "basicPrice";

    public static final String BOOKER_ETH = "ETH";

    public static final String BOOKING_TYPE_UMBUCHUNG = "Umbuchung";

    public static final String BOOKING_TYPE_INVOICE = "Invoice";

    public static final String BUTTON = "button";

    public static final String BUTTONS = "buttons";

    public static final String CANCELED = "canceled";

    public static final String CHARGER = "charger";

    public static final String CHECK_BOX = "checkBox";

    public static final String CLONE = "clone";

    public static final String CLONE_MODE_SAMPLES_NONE = "cloneModeSamplesNone";

    public static final String CLONE_MODE_SAMPLES_REFERENCE = "cloneModeSamplesReference";

    public static final String COLOR_GREEN = "green";

    public static final String COLOR_RED = "red";

    public static final String COLUMN = "Column";

    public static final String COMMENT_CATEGORY_COMMENT = "Comment";

    public static final String COMMENT_CATEGORY_NOTE = "Note";

    public static final String COMMENT_CATEGORY_RESULT = "Result";

    public static final String COMPANY = "company";

    public static final String CONSUMABLE = "consumable";

    public static final String CONTAINER = "container";

    public static final String CREATEDBY = "createdBy";

    public static final String CREATION_FROM_INPUT_DATASET = "creationFromInputDataset";

    public static final String CREATION_FROM_INPUT_RESOURCES = "creationFromInputResources";

    public static final String CREATION_FROM_RERUNNING = "creationFromRerunning";

    public static final String CREATION_FROM_SCRATCH = "creationFromScratch";

    public static final String CREATION_FROM_SELECTED_WORKUNIT = "creationFromSelectedWorkunit";

    public static final String CSV_SEPARATOR = ";";

    public static final String CUSTOM_ORDER_STATE_LIBRARY_PREP = "Library Prep";

    public static final String CUSTOM_ORDER_STATE_LIBRARY_PREP_DONE = "Library Prep Done";

    public static final String CUSTOM_ORDER_STATE_QUEUED_FOR_SEQUENCING = "Queued For Sequencing";

    public static final String CUSTOM_ORDER_STATE_QUEUED_FOR_SEQUENCING_QC = "Queued For Sequencing QC";

    public static final String CUSTOM_ORDER_STATE_SEQUENCING = "Sequencing";

    public static final String CUSTOM_ORDER_STATE_SEQUENCING_DONE = "Sequencing Done";

    public static final String CUSTOM_ORDER_STATE_SEQUENCING_QC = "Sequencing QC";

    public static final String CUSTOM_ORDER_STATE_WAITING_FOR_SAMPLE_QC = "Waiting For Sample QC";

    public static final String DATA_PRODUCED = "dataProduced";

    public static final String DATASET = "dataset";

    public static final String DATE_PATTERN_ETH = "yyyyMMdd";

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    public static final String DATE_PATTERN_EU = "dd.MM.yyyy";

    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String TIME_PATTERN = "HH:mm:ss";

    public static final String DATETIME_PATTERN_ISO = "yyyy-MM-ddTHH:mm:ss+-HH:mm";

    public static final String DATETIME_PATTERN_MM = "yyyy-MM-dd HH:mm";

    public static final String DATETIME_PATTERN_N = "yyyy-MM-dd HH:mm:ss[.SSS][.SS][.S]";

    public static final String DATETIME_PATTERN_TIMESTAMP = "EEE MMM dd HH:mm:ss zzz yyyy";

    public static final String DATETIME_PATTERN_NAME = "yyyyMMdd_HHmmss";

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

    public static final DateTimeFormatter DATETIME_FORMATTER_N = DateTimeFormatter.ofPattern(DATETIME_PATTERN_N);

    public static final DateTimeFormatter DATETIME_FORMATTER_MM = DateTimeFormatter.ofPattern(DATETIME_PATTERN_MM);

    public static final DateTimeFormatter DATETIME_FORMATTER_NAME = DateTimeFormatter.ofPattern(DATETIME_PATTERN_NAME);

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static final DateTimeFormatter DATE_FORMATTER_ETH = DateTimeFormatter.ofPattern(DATE_PATTERN_ETH);

    public static final DateTimeFormatter DATE_FORMATTER_EU = DateTimeFormatter.ofPattern(DATE_PATTERN_EU);

    public static final String DEMULTIPLEXING = "demultiplexing";

    public static final String DEPLOYER_FGCZ = "FGCZ";

    public static final String DEPARTMENT = "department";

    public static final String DESCENDANT = "descendant";

    public static final String DESCRIPTION = "description";

    public static final String DETAILS = "details";

    public static final String DISABLED = "disabled";

    public static final String DISPLAY_MESSAGES = "displayMessages";

    public static final String DOWNLOAD_METADATA_FILE_NAME = "metadata.xml";

    public static final String EDIT = "edit";

    public static final String EMPTY_STRING = "";

    public static final String ENABLED = "enabled";

    public static final String ERROR = "error";

    public static final String ERROR_MESSAGES = "errorMessages";

    public static final String ETH_ZURICH = "ETH Zurich";

    public static final String ETHZ_UZH = "ETHZ/UZH";

    public static final String EXTRACTION_PROTOCOL = "extractionProtocol";

    public static final String FAILED = "failed";

    public static final String FALSE = "false";

    public static final String FOLDER = "folder";

    public static final String GROUP = "group";

    public static final String HEADER = "header";

    public static final String HEADER_INPUT = "HeaderInput";

    public static final String HOURS_REQUESTED = "hoursRequested";

    @SuppressWarnings("HttpUrlsUsage")
    public static final String HTTP = "http://";

    public static final String HTTPS = "https://";

    public static final String ID = "id";

    public static final String INDEXER_COL_SUFFIX = "_COL";

    public static final String INDEX_CLASS = "indexClass";

    public static final String INDEX_DELETE_ENTITIES = "indexDeleteEntities";

    public static final String INDEX_ENTITIES = "indexEntities";

    public static final String INDEX_ENTITIES_COUNT = "indexEntitiesCount";

    public static final String INDEX_QUERY = "indexQueryResults";

    public static final String INDEX_QUERY_COUNT = "indexQueryResultsCount";

    public static final String INDEXMAP_CONTENT = "indexMapContent";

    public static final String INDEXMAP_CREATED = "indexMapCreated";

    public static final String INDEXMAP_CREATEDBY = "indexMapCreatedBy";

    public static final String INDEXMAP_DOI_CREATED = "indexMapDoiCreated";

    public static final String INDEXMAP_GROUP = "indexMapGroup";

    public static final String INDEXMAP_ID = "indexMapId";

    public static final String INDEXMAP_MODIFIED = "indexMapModified";

    public static final String INDEXMAP_MODIFIEDBY = "indexMapModifiedBy";

    public static final String INDEXMAP_STATUS = "indexMapStatus";

    public static final String INDEXMAP_TYPE = "indexMapType";

    public static final String INPUT = "inputSuffix";

    public static final String INPUT_QC_PLATE = "inputQcPlate";

    public static final String INPUT_QC_SAMPLE = "inputQcSample";

    public static final String INSTITUTE = "institute";

    public static final String INSTRUMENT = "instrument";

    public static final String INSTRUMENT_RESERVATION = "instrumentReservation";

    public static final String INSTRUMENT_DATA_DELIVERY = "instrumentDataDelivery";

    public static final String INSTRUMENT_DATA_PACKAGES = "instrumentDataPackage";

    public static final String KITS_USED = "kitsUsed";

    public static final String LEFT_OUTER = "left outer";

    public static final String LANE = "lane";

    public static final String ILLUMINA = "Illumina";

    public static final String ILLUMINA_LIBRARY = "Library - Illumina";

    public static final String ILLUMINA_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER = "libraryIlluminaPlateSampleTableColumnOrder";

    public static final String NANOPORE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER = "libraryNanoporePlateSampleTableColumnOrder";

    public static final String ONT_READY_MADE_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER = "libraryONTReadyMadePlateSampleTableColumnOrder";

    public static final String PACBIO_LIBRARY_PLATE_SAMPLE_TABLE_COLUMN_ORDER = "libraryPacBioPlateSampleTableColumnOrder";

    public static final String IN_MULTIPLEX_SAMPLE_NAME_SEPARATOR = "-";

    public static final String MOLARITY_PLATE = "molarityPlate";

    public static final String MOLARITY_SAMPLE = "molaritySample";

    public static final String NANOPORE = "Nanopore";

    public static final String NANOPORE_LIBRARY = "Library - Nanopore";

    public static final String ONT_READY_MADE = "ONT Ready-Made";

    public static final String ONT_READY_MADE_LIBRARY = "Library - ONT Ready-Made";

    public static final String PACBIO = "PacBio";

    public static final String PACBIO_LIBRARY = "Library - PacBio";

    public static final String LIBRARY_PROTOCOL = "libraryProtocol";

    public static final String MULTIPLEXED = "multiplexed";

    public static final String LIST = "list";

    public static final String LOCAL_EXTERNAL_STORAGE = "Local External Storage";

    public static final String LOCAL_INTERNAL_STORAGE = "Local Internal Storage";

    public static final String LOCAL_TEMPORARY_STORAGE = "Local Temporary Storage";

    public static final String LOGIN = "login";

    public static final String LOGOUT = "logout";

    public static final String MAIL_STYLE = "<style>body{font-family: Arial, Helvetica, sans-serif;}p{margin-top:12px!important;margin-bottom:12px!important;}</style>";

    public static final String MAIL_TARGET_COACHES = "Coach,CoachBackup";

    public static final String MAIL_TARGET_ORDER_INTERNALS = "Coach,CoachBackup,Bioinformatician,Tracker,ServiceTypeAssociate,ReplyToUser,InternalMember";

    public static final String MAIL_TARGET_ORDER_MEMBERS = "Requester,Contact,BudgetOfficer,Member,Coach,CoachBackup,Bioinformatician,Tracker,ServiceTypeAssociate,ReplyToUser";

    public static final String MAIL_TARGET_PROJECT_INTERNALS = "Coach,CoachBackup,Bioinformatician,Tracker,ReplyToUser,InternalMember";

    public static final String MAIL_TARGET_PROJECT_MEMBERS = "Requester,Contact,BudgetOfficer,Leader,Member,Coach,CoachBackup,Bioinformatician,Tracker,ReplyToUser";

    public static final String MAIL_TARGET_REQUESTER = "Requester";

    public static final int MAX_LENGTH_FILECHECKSUM = 1024;

    public static final int MAX_LENGTH_NAME = 256;

    public static final int MAX_LENGTH_RELATIVE_PATH = 1024;

    public static final int MAX_LENGTH_URL = 1024;

    public static final String MESSAGE = "Message";

    public static final String MESSAGE_COMPONENT_ROW_IDENTIFIER = "__::MESSAGE_COMPONENT_ROW_IDENTIFIER::__";

    public static final String MESSAGE_COMPONENT_SEPARATOR = "::__MESSAGE_COMPONENT_SEPARATOR__::";

    public static final String MODIFIEDBY = "modifiedBy";

    public static final String MOVE = "move";

    public static final String MS = "Ms";

    public static final String MR = "Mr";

    public static final String MS_SAMPLE = "MS Sample";

    public static final String MULTIPLEX_ID = "multiplexId";

    public static final String MULTIPLEX_ID_2 = "multiplexId2";

    public static final String MULTIPLEX_ID_ACTG_REGEXP = "^[ACTG]+$";

    public static final String MULTIPLEX_ID_CHECK_ADVANCED = "multiplexIdCheckTypeAdvanced";

    public static final String MULTIPLEX_ID_CHECK_BASIC = "multiplexIdCheckTypeBasic";

    public static final String MULTIPLEX_ID_INCOMPLETE_KEY = "multiplexIdsIncompleteKey";

    public static final String MULTIPLEX_ID_MISMATCH_KEY = "multiplexIdsMismatchKey";

    public static final String MULTIPLEX_ID_MIXED_KEY = "multiplexIdsMixedKey";

    public static final String MULTIPLEX_ID_UNIQUENESS_CHECK_DELIMITER = "__::||::__";

    public static final String NAME = "name";

    public static final String NAME_INPUT = "NameInput";

    public static final String NUMBER_OF_CELLS_NUCLEI = "numberOfCellsNuclei";

    public static final String NUMBER_OF_CHIPS = "numberOfChips";

    public static final String NUMBER_OF_RUNS_SEQUENCING = "numberOfRunsSequencing";

    public static final String NUMBER_OF_RUNS_TAPE_STATION = "numberOfRunsTapeStation";

    public static final String NUMBER_OF_SAMPLES = "numberOfSamples";

    public static final String NOTES = "notes";

    public static final String NULL = "null";

    public static final String OPERATOR_EQ = " = ";

    public static final String OPERATOR_GT = " > ";

    public static final String OPERATOR_GTE = " >= ";

    public static final String OPERATOR_IS = " IS ";

    public static final String OPERATOR_LIKE = " LIKE ";

    public static final String OPERATOR_LT = " < ";

    public static final String OPERATOR_LTE = " <= ";

    public static final String OPERATOR_NE = " <> ";

    public static final String ORDER = "order";

    public static final String ORDERS = "orders";

    public static final String ORDER_ITEM_INSERT_SIZE = "insertSize";

    public static final String ORDER_ITEM_LIBRARY_TYPE = "orderItemLibraryType";

    public static final String ORDER_ITEM_MULTIPLEXING = "multiplexing";

    public static final String ORDER_ITEM_READ_TYPE = "readType";

    public static final String ORDER_ITEM_REGION = "region";

    public static final String SAMPLE_NAME_CHARACTERS = "_a-zA-Z0-9\\-";

    public static final String SAMPLE_IN_EDIT_LIST_NAME_CHARACTERS_REGEXP = "[" + SAMPLE_NAME_CHARACTERS + "]{0,256}";

    public static final String SAMPLE_IN_MULTIPLEX_NAME_CHARACTERS_REGEXP = "[" + SAMPLE_NAME_CHARACTERS + "]{0,128}";

    public static final String SAMPLE_NAME_CHARACTERS_REGEXP = "[" + SAMPLE_NAME_CHARACTERS + "]+";

    public static final String SEQUENCE_CHARACTERS = "[acgtACGT]";

    public static final String SEQUENCE_CHARACTERS_REGEXP = SEQUENCE_CHARACTERS + "*";

    public static final String ORDER_ITEM_TUBEID_REGEXP = "((p(([0-9]+_)|))|o|)[0-9]+/[0-9]+";

    public static final String ORDER_ITEM_TUBE_ID = "orderItemTubeId";

    public static final String ORGANIZATION = "organization";

    public static final String ORGANIZATION_TYPE = "organizationtype";

    public static final String ORGANIZATIONTYPE_CH_UNI = "Swiss University other than ETHZ and UZH";

    public static final String ORGANIZATIONTYPE_UNI_ZH = "University in Zurich (ETHZ or UZH)";

    public static final String PLATE = "plate";

    public static final String QC_PASSED = "qcPassed";

    public static final String STATUS = "status";

    public static final String PLATE_LAYOUT = "plateLayout";

    public static final String PLATE_NAME = "plateName";

    public static final String PLATE_NAME_GIVEN = "plateNameGiven";

    public static final String PLATE_TYPE_USER_SUBMITTED_NAME = "User Submitted";

    public static final String PLURAL_S = "s";

    public static final String POSITION = "position";

    public static final String PROJECT = "project";

    public static final String PROTOCOL_SEPARATOR = "://";

    public static final String QC_PLATE_SAMPLE_TABLE_COLUMN_ORDER = "qcPlateSampleTableColumnOrder";

    public static final String QUALITY_CONTROL_TYPE = "qualityControlType";

    public static final String QUANTITY = "quantity";

    public static final String REMARKS = "remarks";

    public static final String REMOVE = "remove";

    public static final String REMOVED = "removed";

    public static final String REMOVE_ALL = "all";

    public static final String REMOVE_FOLLOWING = "following";

    public static final String REMOVE_THIS = "this";

    public static final String REPORT_TEMPLATE_FOLDER = "/report-templates/";

    public static final String RESOURCE = "resource";

    public static final String REQUIRED = "required";

    public static final String REQUIRED_FIELD = "requiredField";

    public static final String REVIEW_APPROVED = "approved";

    public static final String REVIEW_REJECTED = "rejected";

    public static final String ROLE_BUDGETOFFICER = "budgetOfficer";

    public static final String ROLE_CONTACT = "contact";

    public static final String ROLE_LEADER = "leader";

    public static final String ROLE_MEMBER = "member";

    public static final String ROLE_REQUESTER = "requester";

    public static final String ROW = "Row";

    public static final String RUN = "run";

    public static final String SAMPLE = "sample";

    public static final String SAMPLE_FORM = "sampleForm";

    public static final String SAMPLE_NAME = "sampleName";

    public static final String SAMPLE_PLATE_ASSIGNMENT = "samplePlateAssignment";

    public static final String SAMPLE_PLATE_ASSIGNMENT_ORDER = "samplePlateAssignmentOrder";

    public static final String SAMPLE_TUBE_ID = "sampleTubeId";

    public static final String SAVED = "saved";

    public static final String SELECT = "select";

    public static final String SELECTED = "selected";

    public static final String SELECTION = "Selection";

    public static final String SERVICE = "service";

    public static final String SERVICE_AREA = "servicearea";

    public static final String SEQUENCING_APPLICATION = "sequencingApplication";

    public static final String SEQUENCING_APPLICATION_NAME_CUSTOM_OTHER = "Custom / Other";

    public static final String SERVICE_AREA_MICROARRAYS = "Microarrays";

    public static final String SERVICE_AREA_NEXTGENSEQUENCING = "Next Generation Sequencing";

    public static final String SERVICE_CODE = "serviceCode";

    public static final String SERVICE_TYPE = "servicetype";

    public static final String SERVICE_SELECTION = "serviceSelection";

    public static final String SERVICE_TYPE_NAME_HIGH_THROUGHPUT_SEQUENCING = "High Throughput Sequencing (NGS)";

    public static final String SERVICE_TYPE_NAME_LONG_READ_SEQUENCING = "Long Read Sequencing";

    public static final String SERVICE_TYPE_NAME_READY_MADE_LIBRARIES_SEQUENCING = "Ready-made Libraries Sequencing";

    public static final String SHIBBOLETH_ACCOUNT_AUTO_MAPPED = "account-auto-mapped";

    public static final String SHIBBOLETH_LOGGED_IN = "logged-in";

    public static final String SHOW = "show";

    public static final int SIZE_B = 0;

    public static final int SIZE_GB = 3;

    public static final int SIZE_KB = 1;

    public static final int SIZE_MB = 2;

    public static final int SIZE_TB = 4;

    public static final String SOURCE = "source";

    public static final String SOURCE_TABLE_CLIENT_ID = "sourceTableClientId";

    public static final String STARREDBY = "starredBy";

    public static final String STORAGE = "storage";

    public static final String STORAGE_MODEL = "storageModel";

    public static final String STRING = "String";

    public static final String STRING_TYPE = "stringType";

    public static final String SWITCH_BUTTON_HIDE = "switchButtonHide";

    public static final String SWITCH_BUTTON_HIDE_TITLE = "switchButtonHideTitle";

    public static final String SWITCH_BUTTON_SHOW = "switchButtonShow";

    public static final String SWITCH_BUTTON_SHOW_TITLE = "switchButtonShowTitle";

    public static final String SWITCH_INCLUDE = "switchInclude";

    public static final String SWITCH_INCLUDED = "switchIncluded";

    public static final String SYSTEM = "system";

    public static final String TABLE = "table";

    public static final String TABLE_CLIENT_ID = "tableClientId";

    public static final String TABLE_IS_SELECT = "tableIsSelect";

    public static final String TAB_DETAILS = "&tab=details";

    public static final String TARGET = "target";

    public static final String TAX_TYPE = "taxType";

    public static final String TIMELINE_INTERVAL = "timeline_interval";

    public static final String TOTAL_NUMBER_OF_INSTRUMENT_DATA_PACKAGES = "totalNumberOfInstrumentDataPackages";

    public static final String TOTAL = "total";

    public static final String TOTAL_PRICE = "totalPrice";

    public static final String TOTAL_PRICE_DEFAULT_CURRENCY = "totalPriceDefaultCurrency";

    public static final String TRI_STATE_CHECKBOX_GROUP = "triStateCheckboxGroup";

    public static final String TRUE = "true";

    public static final String UI_INCLUDE_COLUMN_PATH_BOOLEAN_TYPE = "/fragments/batch/sample-attribute-boolean-column.xhtml";

    public static final String UI_INCLUDE_COLUMN_PATH_DATE_TYPE = "/fragments/batch/date-column.xhtml";

    public static final String UI_INCLUDE_COLUMN_PATH_MULTI_VALUED_TYPE = "/fragments/batch/sample-attribute-annotation-multi-column.xhtml";

    public static final String UI_INCLUDE_COLUMN_PATH_NUMBER_TYPE = "/fragments/batch/number-column.xhtml";

    public static final String UI_INCLUDE_COLUMN_PATH_SINGLE_SELECTION_TYPE = "/fragments/batch/select-one-menu-column.xhtml";

    public static final String UI_INCLUDE_COLUMN_PATH_STRING_TYPE = "/fragments/batch/string-column.xhtml";

    public static final String UPDATED = "updated";

    public static final String USER = "user";

    public static final String USER_BENCH_USAGE = "userBenchUsage";

    public static final String UZH = "University of Zurich";

    public static final String VIEW_BASIC = "basic";

    public static final String VIEW_FULL = "full";

    public static final String VIEWEDBY = "viewedBy";

    public static final String WARNING = "warning";

    public static final String WORKUNIT = "workunit";

    public static final String X = "X";

    public static final String EDIT_TARGET_SAMPLE_TABLE = EDIT + ":" + TARGET + SAMPLE + TABLE;

    public static final String EDIT_SOURCE_SAMPLE_TABLE = EDIT + ":" + SOURCE + SAMPLE + TABLE;

    public static final String POSITION_COLUMN = POSITION + COLUMN;

    public static final String POSITION_COLUMN_ALL = POSITION_COLUMN + ALL;

    public static final String CHECK_BOX_POSITION_COLUMN = CHECK_BOX + POSITION_COLUMN;

    public static final String CHECK_BOX_POSITION_COLUMN_ALL = CHECK_BOX_POSITION_COLUMN + ALL;

    public static final String SELECT_CHECK_BOX = SELECT + CHECK_BOX;

    public static final String SELECT_CHECK_BOX_COLUMN = SELECT_CHECK_BOX + COLUMN;

    public static final String APPLICATION_TYPE_ANALYSIS = "analysis";

    public static final String APPLICATION_TYPE_IMPORT = "import";

    public static final String APPLICATION_TYPE_WEBAPP = "webApp";
}
