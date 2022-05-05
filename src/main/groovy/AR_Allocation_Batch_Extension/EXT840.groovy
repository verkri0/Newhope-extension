/*
 ***************************************************************
 *                                                             *
 *                           NOTICE                            *
 *                                                             *
 *   THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS             *
 *   CONFIDENTIAL INFORMATION OF INFOR AND/OR ITS AFFILIATES   *
 *   OR SUBSIDIARIES AND SHALL NOT BE DISCLOSED WITHOUT PRIOR  *
 *   WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND       *
 *   ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH  *
 *   THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.            *
 *   ALL OTHER RIGHTS RESERVED.                                *
 *                                                             *
 *   (c) COPYRIGHT 2020 INFOR.  ALL RIGHTS RESERVED.           *
 *   THE WORD AND DESIGN MARKS SET FORTH HEREIN ARE            *
 *   TRADEMARKS AND/OR REGISTERED TRADEMARKS OF INFOR          *
 *   AND/OR ITS AFFILIATES AND SUBSIDIARIES. ALL RIGHTS        *
 *   RESERVED.  ALL OTHER TRADEMARKS LISTED HEREIN ARE         *
 *   THE PROPERTY OF THEIR RESPECTIVE OWNERS.                  *
 *                                                             *
 ***************************************************************
 */
 
 import groovy.lang.Closure
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.time.ZoneId;
 
 /*
 *Modification area - M3
 *Nbr            Date       User id     Description
 *BF_R_1070      20220225   WZHAO       AR allocation night run
 */
 
 /*
  * Add records to GLS840
  */
public class EXT840 extends ExtendM3Batch {
  private final LoggerAPI logger;
  private final DatabaseAPI database;
  private final BatchAPI batch;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  
  private String divi;
  private String inPYNO;
  
  private int XXCONO;
  private String noseries01; 
  private boolean scenario1_matched;
  private boolean scenario2_matched;
  private boolean scenario3_matched;
  
  private String CINO2;
  private String oFEID;
  private String oFNCN;
  private String oVTXT;
  private String oIVNO;
  private String oORNO;
  private String oEXIN;
  private String oINPX;
  private String oYREF;
  
  private String savedCUNO;
  private String savedPYNO;
  private String savedCINO;
  private String savedCUAM;
  private String savedINYR;
  
  private List lstMatchedRecords;
  private int lineNo;
  
  public EXT840(LoggerAPI logger, DatabaseAPI database, BatchAPI batch, MICallerAPI miCaller, ProgramAPI program) {
    this.logger = logger
    this.database = database
    this.batch = batch
  	this.miCaller = miCaller;
  	this.program = program;
  }
  
  public void main() {
    
    XXCONO= program.LDAZD.CONO;
    divi = "";
    inPYNO = "";
    
    if (!batch.getReferenceId().isPresent()) {
      logger.error("Job data for job ${batch.getJobId()} is missing");
      return;
    }
    
    // Get parameters from EXTJOB
    logger.debug("ReferenceId=" + batch.getReferenceId());
    Optional<String> data = getJobData(batch.getReferenceId().get())
    
    if (!data.isPresent()) {
      logger.error("Job reference Id ${batch.getReferenceId().get()} is passed, but data was not found")
      return
    }
    
    String rawData = data.get()
    String[] str;
    str = rawData.split(',');
    
    if (str.length > 1) {
      divi = str[0];
      inPYNO = str[1];
    } else if (str.length == 1) {
      divi = str[0];
    }
    
    noseries01 = "";
    initRun();
    
    lstMatchedRecords = new ArrayList();
    
    ExpressionFactory expression = database.getExpressionFactory("FSLEDG")
    expression = expression.eq("ESRECO", "0");
    expression = expression.and(expression.lt("ESCUAM", "0.00"));
    DBAction query = database.table("FSLEDG").index("10").matching(expression).selection("ESCONO", "ESDIVI", "ESYEA4", "ESJRNO", "ESJSNO", "ESPYNO", "ESCUNO", "ESCINO", "ESCUAM", "ESRECO", "ESINYR", "ESTRCD").build()      
    DBContainer container = query.getContainer()
    container.set("ESCONO", XXCONO);
    container.set("ESDIVI", divi);
    container.set("ESPYNO", inPYNO);
    int numberOfKeys = 0;
    if (inPYNO.isEmpty()) {
      numberOfKeys = 2;
    } else {
      numberOfKeys = 3;
    }
    query.readAll(container, numberOfKeys, releasedItemProcessor);
    
    if (lstMatchedRecords.size() > 0) {
      create_GLS840MI_header();
    }
    lineNo = 0;
    for (int i=0;i<lstMatchedRecords.size();i++) {
	    Map<String, String> record = (Map<String, String>) lstMatchedRecords[i];
      String pyno = record.PYNO;
      String cuno = record.CUNO;
      String cino = record.CINO;
      String cuam = record.CUAM;
      String inyr = record.INYR;
      String pyno2 = record.PYNO2;
      String cuno2 = record.CUNO2;
      String cino2 = record.CINO2;
      String cuam2 = record.CUAM2;
      String inyr2 = record.INYR2;
      lineNo++;
      create_GLS840MI_line(cuno, pyno, cino, inyr, cuam, lineNo);
      lineNo++;
      create_GLS840MI_line(cuno2, pyno2, cino2, inyr2, cuam2, lineNo);
    }
    
    if (lstMatchedRecords.size() > 0) {
      update_GLS840MI_batch();
    }  
    
  }
  
  /*
	 * getJobData 
	 *
	*/
  private Optional<String> getJobData(String referenceId) {
    def queryEXTJOB = database.table("EXTJOB").index("00").selection("EXRFID", "EXJOID", "EXDATA").build();
    def EXTJOB = queryEXTJOB.createContainer();
    EXTJOB.set("EXCONO", XXCONO);
    EXTJOB.set("EXRFID", referenceId)
    if (queryEXTJOB.read(EXTJOB)) {
      return Optional.of(EXTJOB.getString("EXDATA"))
    }
    return Optional.empty()
  } 
  
  /*
	 * initRun 
	 *
	*/
  def initRun() {
    
    DBAction ActionCUGEX1 = database.table("CUGEX1").index("00").selection("F1A030").build();

    DBContainer CUGEX1 = ActionCUGEX1.getContainer();
    CUGEX1.set("F1CONO", XXCONO);
    CUGEX1.set("F1FILE", "ARRUN");
    CUGEX1.set("F1PK01", "01");
    ActionCUGEX1.readAll(CUGEX1, 3, 1, lstCUGEX1);
    logger.debug("noseries01=" + noseries01);
    
    String trans = "";
    if (noseries01.isBlank()) {
      noseries01 = "100000001";
      trans = "AddFieldValue";
    } else {
      trans = "ChgFieldValue";
    }
    
    int noser = noseries01.toInteger() + 1;
    def params01 = [ "FILE":"ARRUN".toString(), "PK01": "01".toString(),"A030": noser.toString()] 
       
    def callback01 = {
       Map<String, String> response ->
       
    }
  
    miCaller.call("CUSEXTMI", trans, params01, callback01)
    
  }
  
  /*
   * lstCUGEX1 - Callback function to return CUGEX1 records
   *
  */
  Closure<?> lstCUGEX1 = { DBContainer CUGEX1 ->
      noseries01 = CUGEX1.get("F1A030").toString().trim();
  }
  
  
  /*
   * releasedItemProcessor - Callback function to return FSLEDG records
   *
  */
  Closure<?> releasedItemProcessor = { DBContainer FSLEDG ->
  
    String oDIVI = FSLEDG.get("ESDIVI").toString().trim();
    String oYEA4 = FSLEDG.get("ESYEA4").toString().trim();
    String oJRNO = FSLEDG.get("ESJRNO").toString().trim();
    String oJSNO = FSLEDG.get("ESJSNO").toString().trim();
    String oPYNO = FSLEDG.get("ESPYNO").toString().trim();
    savedPYNO = oPYNO;
    String oCUNO = FSLEDG.get("ESCUNO").toString().trim();
    savedCUNO = oCUNO;
    String oCINO = FSLEDG.get("ESCINO").toString().trim();
    savedCINO = oCINO;
    String oCUAM = FSLEDG.get("ESCUAM").toString().trim();
    savedCUAM = oCUAM;
    String oRECO = FSLEDG.get("ESRECO").toString().trim();
    String oTRCD = FSLEDG.get("ESTRCD").toString().trim();
    String oINYR = FSLEDG.get("ESINYR").toString().trim();
    savedINYR = oINYR;
 
    oFEID = "";
    oFNCN = "";
    oVTXT = "";
    
    DBAction queryFGLEDG = database.table("FGLEDG").index("00").selection("EGCONO", "EGDIVI", "EGJRNO", "EGJSNO", "EGYEA4", "EGFEID", "EGFNCN","EGVTXT").build()
    DBContainer FGLEDG = queryFGLEDG.getContainer();
    FGLEDG.set("EGCONO", XXCONO);
    FGLEDG.set("EGDIVI", divi);
    FGLEDG.set("EGYEA4", oYEA4.toInteger());
    FGLEDG.set("EGJRNO", oJRNO.toInteger());
    FGLEDG.set("EGJSNO", oJSNO.toInteger());
    
    if (queryFGLEDG.read(FGLEDG)) {
      oFEID = FGLEDG.get("EGFEID").toString().trim();
      oVTXT = FGLEDG.get("EGVTXT").toString().trim();
      oFNCN = FGLEDG.get("EGFNCN").toString().trim();
    }
  
    scenario1_matched = false;
    scenario2_matched = false;
    scenario3_matched = false;
    
    // scenario 1  Price Adjustments and Stock Returns created by CO's against outstanding invoices
    match_FSLEDG_01(oPYNO, oCUNO, oCINO, oFEID, oYEA4);
    
    // scenario 2  Manually entered Claims and Credits created via Customer Invoice 
    if (!scenario1_matched) {  
      match_FSLEDG_02(oPYNO, oCUNO, oCINO, oFEID,  oVTXT);
    }
    
    // scenario 3   On-Account credit invoices created during AR Remittance Upload
    if (!scenario1_matched && !scenario2_matched) {
      match_FSLEDG_03(oPYNO, oCUNO, oCINO, oFEID, oFNCN);
    }
    
  }
  
  /*
   * match_FSLEDG_01 - Read FSLEDG - scenario 1 
   *
  */
  def match_FSLEDG_01(String oPYNO, String oCUNO, String oCINO, String oFEID, String oYEA4) {
   
    String sFEID = "";
    if(!oFEID.isEmpty()) {  
      sFEID = oFEID.substring(0,2); 
    }
    
    if (!sFEID.equals("OI")) {
       return;
    } 
    oIVNO = "";
    oEXIN = oCINO;
    oINPX = "";
    // Read OINVOH to get the IVNO
    DBAction queryOINVOH = database.table("OINVOH").index("42").selection("UHEXIN","UHIVNO").build()
    DBContainer OINVOH = queryOINVOH.getContainer();
    OINVOH.set("UHCONO", XXCONO);
    OINVOH.set("UHDIVI", divi);
    OINVOH.set("UHYEA4", oYEA4.toInteger());
    OINVOH.set("UHPYNO", oPYNO);
    OINVOH.set("UHEXIN", oEXIN);
    
    queryOINVOH.readAll(OINVOH, 5, 1, lstOINVOH);
    if (oIVNO.isEmpty()) {
      return;
    }
    oORNO = "";
    // Read OINVOL to get the ORNO
    DBAction queryOINVOL = database.table("OINVOL").index("00").selection("ONORNO").build()
    DBContainer OINVOL = queryOINVOL.getContainer();
    OINVOL.set("ONCONO", XXCONO);
    OINVOL.set("ONDIVI", divi);
    OINVOL.set("ONYEA4", oYEA4.toInteger());
    OINVOL.set("ONINPX", oINPX);
    OINVOL.set("ONIVNO", oIVNO.toInteger());
    OINVOL.set("ONIVTP", "30");
    
    queryOINVOL.readAll(OINVOL, 6, 1, lstOINVOL);
    if (oORNO.isEmpty()) {
      return;
    }
    oYREF = "";
    // Read OOHEAD to get YREF
    DBAction queryOOHEAD = database.table("OOHEAD").index("00").selection("OAYREF").build()
    DBContainer OOHEAD = queryOOHEAD.getContainer();
    OOHEAD.set("OACONO", XXCONO);
    OOHEAD.set("OAORNO", oORNO);
    
    if (queryOOHEAD.read(OOHEAD)) {
      oYREF = OOHEAD.get("OAYREF").toString().trim();
    }
    if (oYREF.isEmpty()) {
      return;
    }
    //logger.debug("YREF=" + oYREF);
    ExpressionFactory expression = database.getExpressionFactory("FSLEDG");
    expression = expression.eq("ESRECO", "0");
    expression = expression.and(expression.gt("ESCUAM", "0.00"));
    DBAction queryFSLEDG = database.table("FSLEDG").index("13").matching(expression).selection("ESDIVI", "ESPYNO", "ESCINO", "ESCUNO", "ESCUAM", "ESINYR").build()
    DBContainer FSLEDG = queryFSLEDG.getContainer();
    FSLEDG.set("ESCONO", XXCONO);
    FSLEDG.set("ESDIVI", divi);
    FSLEDG.set("ESPYNO", oPYNO);
    FSLEDG.set("ESCINO", oYREF);
   
    queryFSLEDG.readAll(FSLEDG, 4, 1, releasedItemProcessor1);
    
  }
  /*
   * lstOINVOH - Callback function to return OINVOH records
   *
  */
  Closure<?> lstOINVOH = { DBContainer OINVOH ->
    oIVNO = OINVOH.get("UHIVNO").toString().trim();
    oINPX = OINVOH.get("UHINPX").toString().trim();
    
  }
  /*
   * lstOINVOL - Callback function to return OINVOL records
   *
  */
  Closure<?> lstOINVOL = { DBContainer OINVOL ->
    oORNO = OINVOL.get("ONORNO").toString().trim();
  }
   /*
   * releasedItemProcessor1 - Callback function to return FSLEDG records 
   *
  */
  Closure<?> releasedItemProcessor1 = { DBContainer FSLEDG ->
    
    scenario1_matched = true;
    
    String pyno2 = FSLEDG.get("ESPYNO").toString().trim();
    String cuno2 = FSLEDG.get("ESCUNO").toString().trim();
    String cino2 = FSLEDG.get("ESCINO").toString().trim();
    String cuam2 = FSLEDG.get("ESCUAM").toString().trim();
    String inyr2 = FSLEDG.get("ESINYR").toString().trim();
    
    logger.debug("scenario1_matched found CINO=" + cino2);
    
    // - if the cuam is not match cuam2, use the smaller one for GLS840
    double amt = 0;
    if (Math.abs(savedCUAM.toDouble()) > cuam2.toDouble()) {
      amt = cuam2.toDouble();
    } else if (Math.abs(savedCUAM.toDouble()) < cuam2.toDouble()) {
      amt = Math.abs(savedCUAM.toDouble());
    } else {
      amt = cuam2.toDouble()
    }
    
    def map = [PYNO: savedPYNO, CUNO: savedCUNO, CINO: savedCINO, CUAM: (-amt).toString(), INYR: savedINYR, PYNO2: pyno2, CUNO2: cuno2, CINO2: cino2, CUAM2: amt.toString(), INYR2: inyr2];
    lstMatchedRecords.add(map);
  }
  /*
   * match_FSLEDG_02 - Read FSLEDG - scenario 2 
   *
  */  
  def match_FSLEDG_02(String oPYNO, String oCUNO, String oCINO, String oFEID, String oVTXT) {

    String sFEID = "";
    if(!oFEID.isEmpty()) {  
      sFEID = oFEID.substring(0,2); 
    }
    
    if (sFEID.equals("AR") && !oFEID.equals("AR30")) {
      ExpressionFactory expression = database.getExpressionFactory("FSLEDG");
      expression = expression.eq("ESRECO", "0");
      expression = expression.and(expression.gt("ESCUAM", "0.00"));
      DBAction queryFSLEDG = database.table("FSLEDG").index("13").matching(expression).selection("ESDIVI", "ESPYNO", "ESCINO", "ESCUNO", "ESCUAM", "ESINYR").build()
      DBContainer FSLEDG = queryFSLEDG.getContainer();
      FSLEDG.set("ESCONO", XXCONO);
      FSLEDG.set("ESDIVI", divi);
      FSLEDG.set("ESPYNO", oPYNO);
      FSLEDG.set("ESCINO", oVTXT);
     
      queryFSLEDG.readAll(FSLEDG, 4, 1, releasedItemProcessor2);
    }
  }
  
   /*
   * releasedItemProcessor2 - Callback function to return FSLEDG records 
   *
  */
  Closure<?> releasedItemProcessor2 = { DBContainer FSLEDG ->
      
    scenario2_matched = true;
    
    String pyno2 = FSLEDG.get("ESPYNO").toString().trim();
    String cuno2 = FSLEDG.get("ESCUNO").toString().trim();
    String cino2 = FSLEDG.get("ESCINO").toString().trim();
    String cuam2 = FSLEDG.get("ESCUAM").toString().trim();
    String inyr2 = FSLEDG.get("ESINYR").toString().trim();
    
    logger.debug("scenario2_matched found CINO=" + cino2);
    
    // - if the cuam is not match cuam2, use the smaller one for GLS840
    double amt = 0;
    if (Math.abs(savedCUAM.toDouble()) > cuam2.toDouble()) {
      amt = cuam2.toDouble();
    } else if (Math.abs(savedCUAM.toDouble()) < cuam2.toDouble()) {
      amt = Math.abs(savedCUAM.toDouble());
    } else {
      amt = cuam2.toDouble()
    }
    
    def map = [PYNO: savedPYNO, CUNO: savedCUNO, CINO: savedCINO, CUAM: (-amt).toString(), INYR: savedINYR, PYNO2: pyno2, CUNO2: cuno2, CINO2: cino2, CUAM2: amt.toString(), INYR2: inyr2];
    lstMatchedRecords.add(map);
  }
  
  /*
   * match_FSLEDG_03 - Read FSLEDG - scenario 3 
   *
  */    
 def match_FSLEDG_03(String oPYNO, String oCUNO, String oCINO, String oFEID, String oFNCN) {
    String lastDigit = oCINO.substring(oCINO.length() - 1);
    if (!lastDigit.equals("1")) {
      return;
    }
    if (!oFEID.equals("AR30") || !oFNCN.equals("850")) {
      return;
    }  
    CINO2 = oCINO.substring(0, oCINO.length() - 1);
    DBAction queryFSLEDG = database.table("FSLEDG").index("10").selection("ESCONO", "ESDIVI", "ESPYNO", "ESCUNO", "ESCINO", "ESINYR", "ESCUAM").build();
    DBContainer FSLEDG = queryFSLEDG.getContainer();
    FSLEDG.set("ESCONO", XXCONO);
    FSLEDG.set("ESDIVI", divi);
    FSLEDG.set("ESPYNO", oPYNO);
    FSLEDG.set("ESCINO", CINO2);
      
    queryFSLEDG.readAll(FSLEDG, 4, 1, releasedItemProcessor3);
  }
  
  /*
   * releasedItemProcessor3 - Callback function to return FSLEDG records 
   *
  */
  Closure<?> releasedItemProcessor3 = { DBContainer FSLEDG ->
    
    scenario3_matched = true;
    
    String pyno2 = FSLEDG.get("ESPYNO").toString().trim();
    String cuno2 = FSLEDG.get("ESCUNO").toString().trim();
    String cino2 = FSLEDG.get("ESCINO").toString().trim();
    String cuam2 = FSLEDG.get("ESCUAM").toString().trim();
    String inyr2 = FSLEDG.get("ESINYR").toString().trim();
    
    logger.debug("scenario3_matched found CINO=" + cino2);
    
    // - if the cuam is not match cuam2, use the smaller one for GLS840
    double amt = 0;
    if (Math.abs(savedCUAM.toDouble()) > cuam2.toDouble()) {
      amt = cuam2.toDouble();
    } else if (Math.abs(savedCUAM.toDouble()) < cuam2.toDouble()) {
      amt = Math.abs(savedCUAM.toDouble());
    } else {
      amt = cuam2.toDouble()
    }
    
    def map = [PYNO: savedPYNO, CUNO: savedCUNO, CINO: savedCINO, CUAM: (-1).toString(), INYR: savedINYR, PYNO2: pyno2, CUNO2: cuno2, CINO2: cino2, CUAM2: amt.toString(), INYR2: inyr2];
    lstMatchedRecords.add(map);
    
  }
  
  /*
   * create_GLS840MI_header - executing GLS840MI.AddBatchHead
   *
  */
  def create_GLS840MI_header() {
    logger.debug("Call GLS840MI.AddBatchHead...");
    ZoneId zid = ZoneId.of("Australia/Sydney"); 
    int currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    
    String desc =  "AR Allocation - " + currentDate.toString();
    def params = [ "CONO": XXCONO.toString(), "DIVI": divi, "KEY1":noseries01, "INTN": "AR_PAYMALLOC", "DESC": desc]; 
    
    def callback = {
      Map<String, String> response ->
       
    }
    
    miCaller.call("GLS840MI","AddBatchHead", params, callback)
  }
  
   /*
   * create_GLS840MI_line - executing GLS840MI.AddBatchLine
   *
  */
  def create_GLS840MI_line(String cuno, String pyno, String cino, String inyr, String cuam,  int line) {
    logger.debug("Call GLS840MI.AddBatchLine...");
    ZoneId zid = ZoneId.of("Australia/Sydney"); 
    int currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    
    String parm = "I1" + noseries01 + "00000001" + divi + formatFixedLen(cuno, 10) + formatFixedLen(pyno, 10) + formatFixedLen(cino, 15); 
    parm += inyr + formatFixedLen(cuam, 17) + currentDate.toString() + "ARNightRun";
    logger.debug("parm=" + parm);
    //def params1 = [ "CONO": XXCONO.toString(), "DIVI": divi, "KEY1":noseries01, "LINE": "1".toString(), "PARM": "I11000000010000000130051802C    500284    015418         2021464.05           20220214ARNightRun".toString() ] 
    def params = [ "CONO": XXCONO.toString(), "DIVI": divi, "KEY1": noseries01, "LINE": line.toString(), "PARM": parm] 
    def callback = {
    Map<String, String> response ->
       
    }
    
    miCaller.call("GLS840MI","AddBatchLine", params, callback)
  }
  /*
   * formatFixedLength
   *
  */  
  def String formatFixedLen(String str, int len) {
    String strTemp = str;
    while (strTemp.length() < len) {
      strTemp += " ";
    }
    return strTemp;
  }
  
   /*
   * update_GLS840MI_batch - executing GLS840MI.UpdBatch
   *
  */
  def update_GLS840MI_batch() {
    logger.debug("Call GLS840MI.UpdBatch...");
    def params = [ "CONO": XXCONO.toString(), "DIVI": divi, "KEY1":noseries01] 
    def callback = {
    Map<String, String> response ->
      logger.debug("Response = ${response}")
    
    }
    
    miCaller.call("GLS840MI","UpdBatch", params, callback)
  
  }
}
