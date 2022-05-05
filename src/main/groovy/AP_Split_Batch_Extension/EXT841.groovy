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
 *ABF_R_1072     20220330   XWZHAO       Mods Sunbeam payment schedule update
 
 /*
  * Add records to GLS840
  */
public class EXT841 extends ExtendM3Batch {
  private final LoggerAPI logger;
  private final DatabaseAPI database;
  private final BatchAPI batch;
  private final MICallerAPI miCaller;
  private final ProgramAPI program;
  
  private String divi;
  private String currentINBN;
  private boolean bInvalidStatus;
  private List lstInvoices;
  
  private String spyn;
  private String suno;
  private String sino;
  private String ivdt;
  private String dudt;
  private String cuam;
  private String vtam;
  private String cucd;
  private String crtp;
  private String arat;
  private String acdt;
  private String apcd;
  private String cdp1;
  private String cdt1;
  private String cdp2;
  private String cdt2;
  private String cdp3;
  private String cdt3;
  private String vtcd;
  private String puno;
  
  private int XXCONO;
  private String noseries01;
  private int currentDate;
  private int currentTime
  
  public EXT841(LoggerAPI logger, DatabaseAPI database, BatchAPI batch, MICallerAPI miCaller, ProgramAPI program) {
    this.logger = logger
    this.database = database
    this.batch = batch
  	this.miCaller = miCaller;
  	this.program = program;
  }
  
  public void main() {
    
    XXCONO= program.LDAZD.CONO;
    divi = "";
    
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
    
    ZoneId zid = ZoneId.of("Australia/Sydney"); 
    currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    
    divi = data.get();
    
    noseries01 = "";
    initRun();
    
    currentINBN = "";
    lstInvoices = new ArrayList();
    
    DBAction queryEXTSPL = database.table("EXTSPL").index("10").selectAllFields().build();
    DBContainer EXTSPL = queryEXTSPL.getContainer();
  	EXTSPL.set("EXCONO", XXCONO);
  	EXTSPL.set("EXDIVI", divi);
  	EXTSPL.set("EXPROC", 0);
  	queryEXTSPL.readAll(EXTSPL, 3, listEXTSPL);
  	
  	if (lstInvoices.size() > 0) {
  	  create_GLS840MI_header();
  	}
  	for (int i=0;i<lstInvoices.size();i++) {
  	  Map<String, String> record = (Map<String, String>) lstInvoices[i];
  	  create_GLS840MI_line(i, record);
  	}
  	if (lstInvoices.size() > 0) {
  	  update_GLS840MI_batch();
  	}
  	for (int i=0;i<lstInvoices.size();i++) {
  	  Map<String, String> record = (Map<String, String>) lstInvoices[i];
  	  String inbn = record.INBN;
	    String line = record.LINE;
	    if (line.toInteger() == 0) {
  	    updateProcessFlag(inbn);
	    }
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
    CUGEX1.set("F1FILE", "APSPLIT");
    CUGEX1.set("F1PK01", "01");
    ActionCUGEX1.readAll(CUGEX1, 3, 1, lstCUGEX1);
    logger.debug("noseries01=" + noseries01);
    
    String trans = "";
    if (noseries01.isBlank()) {
      noseries01 = "200000001";
      trans = "AddFieldValue";
    } else {
      trans = "ChgFieldValue";
    }
    
    int noser = noseries01.toInteger() + 1;
    def params01 = [ "FILE":"APSPLIT".toString(), "PK01": "01".toString(),"A030": noser.toString()] 
       
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
  * listEXTSPL - Callback function to return EXTSPL
  *
  */
  Closure<?> listEXTSPL = { DBContainer EXTSPL ->
    
    String inbn = EXTSPL.get("EXINBN").toString().trim();
      
     if (!currentINBN.equals(inbn)) {
      currentINBN = inbn;
      bInvalidStatus = false;
      spyn = "";
      suno = "";
      sino = "";
      ivdt = "";
      dudt = "";
      cuam = "";
      vtam = "";
      cucd = "";
      crtp = "";
      arat = "";
      acdt = "";
      apcd = "";
      cdp1 = "";
      cdt1 = "";
      cdp2 = "";
      cdt2 = "";
      cdp3 = "";
      cdt3 = "";
      vtcd = "";
      
      DBAction queryFAPIBH = database.table("FAPIBH").index("00").selection("E5SPYN", "E5SUNO", "E5SINO", "E5SUPA","E5BIST","E5IBHE","E5IBLE", "E5IVDT", "E5DUDT", "E5ACDT","E5CUAM","VTAM","E5CUCD","E5TEPY","E5CRTP","E5ARAT", "E5APCD","E5CDP1","E5CDT1","E5CDP2","E5CDT2","E5CDP3","E5CDT3").build()
      DBContainer FAPIBH = queryFAPIBH.getContainer();
      FAPIBH.set("E5CONO", XXCONO);
      FAPIBH.set("E5DIVI", divi);
      FAPIBH.set("E5INBN", inbn.toInteger());
      
      if (queryFAPIBH.read(FAPIBH)) {
        spyn = FAPIBH.get("E5SPYN").toString().trim();
        suno = FAPIBH.get("E5SUNO").toString().trim();
        sino = FAPIBH.get("E5SINO").toString().trim();
        ivdt = FAPIBH.get("E5IVDT").toString().trim();
        dudt = FAPIBH.get("E5DUDT").toString().trim();
        cuam = FAPIBH.get("E5CUAM").toString().trim();
        vtam = FAPIBH.get("E5VTAM").toString().trim();
        cucd = FAPIBH.get("E5CUCD").toString().trim();
        crtp = FAPIBH.get("E5CRTP").toString().trim();
        arat = FAPIBH.get("E5ARAT").toString().trim();
        acdt = FAPIBH.get("E5ACDT").toString().trim();
        if (acdt.toInteger() == 0) {
          acdt = currentDate.toString();
        }
        logger.debug("ACDT=" + acdt);
        apcd = FAPIBH.get("E5APCD").toString().trim();
        cdp1 = FAPIBH.get("E5CDP1").toString().trim();
        cdt1 = FAPIBH.get("E5CDT1").toString().trim();
        cdp2 = FAPIBH.get("E5CDP2").toString().trim();
        cdt2 = FAPIBH.get("E5CDT2").toString().trim();
        cdp3 = FAPIBH.get("E5CDP3").toString().trim();
        cdt3 = FAPIBH.get("E5CDT3").toString().trim();
        String supa = FAPIBH.get("E5SUPA").toString().trim();
        String bist = FAPIBH.get("E5BIST").toString().trim();
        String ibhe = FAPIBH.get("E5IBHE").toString().trim();
        String ible = FAPIBH.get("E5IBLE").toString().trim();
        if (supa.toInteger() != 90 || bist.toInteger() != 0 || ibhe.toInteger() != 1 || ible.toInteger() != 1) {
          bInvalidStatus = true;
        }
      } else {
        bInvalidStatus = true;
      }
      if (!bInvalidStatus) {
        DBAction queryCIDVEN = database.table("CIDVEN").index("00").selection("IIVTCD").build();
        DBContainer CIDVEN = queryCIDVEN.getContainer();
        CIDVEN.set("IICONO", XXCONO);
        CIDVEN.set("IISUNO", suno);
        if (queryCIDVEN.read(CIDVEN)) {
          vtcd = CIDVEN.get("IIVTCD").toString().trim();
        }
        // - Get first PUNO from FAPIBL
        puno = "";
        DBAction queryFAPIBL = database.table("FAPIBL").index("00").selection("E6PUNO").build()
        DBContainer FAPIBL = queryFAPIBL.getContainer();
        FAPIBL.set("E6CONO", XXCONO);
        FAPIBL.set("E6DIVI", divi);
        FAPIBL.set("E6INBN", inbn.toInteger());
        queryFAPIBL.readAll(FAPIBL, 3, lstFAPIBL);
        
        if (arat.toDouble() == 0) {
          def params = ["FRDI":divi, "TODI":divi, "FCUR":cucd, "TCUR":cucd, "CRTP":crtp, "CUTD":currentDate.toString()];
      
          Closure<?> CRS055MIcallback = {
            Map<String, String> response ->
            
            if(response.ARAT != null){
              arat = response.ARAT.trim(); 
            }
          }
    
          miCaller.call("CRS055MI", "SelExchangeRate", params, CRS055MIcallback);
        }
        
        def map = [INBN: inbn, LINE: "0", ID: "I1", RNNO: noseries01, GRNR: "00000001", DIVI: divi, SUNO: suno, SPYN: spyn, SINO: sino, IVDT: ivdt, DUDT: dudt, IVAM: cuam, VTCD: vtcd, VTAM: vtam, CUCD: cucd, CRTP: crtp, ARAT: arat, ACDT: acdt, APCD: apcd, CDAM: "", CDP1: cdp1, CDT1: cdt1, CDP2: cdp2, CDT2: cdt2, CDP3: cdp3, CDT3: cdt3, PUNO: puno];
        lstInvoices.add(map);
      }
     }
     if (!bInvalidStatus) {
        String line = EXTSPL.get("EXLINO").toString().trim();
        String pdat = EXTSPL.get("EXPDAT").toString().trim();
        String cuam_line = EXTSPL.get("EXCUAM").toString().trim();
        def map = [INBN: inbn, LINE: line, ID: "I2", RNNO: noseries01, GRNR: "00000001", DIVI: divi, SUNO: suno, SPYN: spyn, SINO: sino, IVDT: ivdt, DUDT: pdat, IVAM: cuam_line, VTCD: vtcd, VTAM: vtam, CUCD: cucd, CRTP: crtp, ARAT: arat, ACDT: acdt, APCD: apcd, CDAM: "", CDP1: cdp1, CDT1: cdt1, CDP2: cdp2, CDT2: cdt2, CDP3: cdp3, CDT3: cdt3, PUNO: puno];
        lstInvoices.add(map);
     }
  }
  /*
   * lstFAPIBL - Callback function to return FAPIBL records
   *
   */
 
  Closure<?> lstFAPIBL = { DBContainer FAPIBL ->
    if (puno.isEmpty()) {
      puno  = FAPIBL.get("E6PUNO").toString();
    }
  }
  /*
   * create_GLS840MI_header - executing GLS840MI.AddBatchHead
   *
  */
  def create_GLS840MI_header() {
    logger.debug("Call GLS840MI.AddBatchHead...");
    
    String desc =  "APSPLIT - " + currentDate.toString();
    def params = [ "CONO": XXCONO.toString(), "DIVI": divi, "KEY1":noseries01, "INTN": "APSPLIT", "DESC": desc]; 
    
    def callback = {
      Map<String, String> response ->
      
    }
    
    miCaller.call("GLS840MI","AddBatchHead", params, callback)
  }
  /*
   * create_GLS840MI_line - executing GLS840MI.AddBatchLine
   *
  */
  def create_GLS840MI_line(int lineNo, Map<String, String> record) {
    logger.debug("Call GLS840MI.AddBatchLine...");
    
    lineNo++;
    
	  String inbn = record.INBN;
	  String line = record.LINE;
	  String id = record.ID;
	  String rnno = record.RNNO;
	  String grnr = record.GRNR;
	  String suno_line = record.SUNO;
	  String spyn_line = record.SPYN;
	  String sino_line = record.SINO;
	  if (line.toInteger() > 0) {
	    sino_line = sino_line + " 00" + line;
	  }
	  String ivdt_line = record.IVDT;
	  String dudt_line = record.DUDT;
	  String ivam_line = record.IVAM;
	  String vtcd_line = record.VTCD;
	  //logger.debug("inbn=" + inbn + " line=" + lineNo + " rnno=" + rnno + " suno=" + suno_line + " spyn=" + spyn_line + " sino=" + sino_line + " ivdt=" + ivdt_line + " dudt=" + dudt_line + " ivam=" + ivam_line + " vtcd=" + vtcd_line);
	  String vtam_line = record.VTAM;
	  String cucd_line = record.CUCD;
	  String crtp_line = record.CRTP;
	  String arat_line = record.ARAT;
	  String acdt_line = record.ACDT;
	  logger.debug("ACDT_LINE=" + acdt_line);
	  String apcd_line = record.APCD;
	  String cdp1_line = record.CDP1;
	  String cdt1_line = record.CDT1;
	  String cdp2_line = record.CDP2;
	  String cdt2_line = record.CDT2;
	  String cdp3_line = record.CDP3;
	  String cdt3_line = record.CDT3;
	  String puno_line = record.PUNO;
    
    String parm = "" + id + noseries01 + "00000001" + divi + formatFixedLen(suno_line, 10) + formatFixedLen(spyn_line, 10) + formatFixedLen(sino_line, 24); 
    parm += ivdt_line + dudt_line + formatFixedLen(ivam_line, 17) + vtcd_line + formatFixedLen(vtam_line, 17) + cucd + formatFixedLen(crtp_line, 2) + formatFixedLen(arat_line, 13);
    parm += acdt_line + formatFixedLen(apcd_line, 10) + formatFixedLen(" ", 17) + formatFixedLen(cdp1_line, 6) + formatFixedLen(cdt1_line, 8) + formatFixedLen(cdp2_line, 6) + formatFixedLen(cdt2_line, 8) + formatFixedLen(cdp3_line, 6) + formatFixedLen(cdt3_line, 8) + formatFixedLen(puno_line, 10);
    logger.debug("parm=" + parm);
    //def params1 = [ "CONO": XXCONO.toString(), "DIVI": divi, "KEY1":noseries01, "LINE": "1".toString(), "PARM": "I10000000120000001230010007     10007     TEST460                 2021111520211115600              01                 AUD011            20211116 ] 
    def params = [ "CONO": XXCONO.toString(), "DIVI": divi, "KEY1": noseries01, "LINE": lineNo.toString(), "PARM": parm] 
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
    
    }
    miCaller.call("GLS840MI","UpdBatch", params, callback)
  }
  /*
   * updateProcessFlag - update PROC in EXTSPL
   *
  */
  def updateProcessFlag(String inbn) {
    
  	  DBAction queryEXTSPL = database.table("EXTSPL").index("00").selection("EXINBN").build();
      DBContainer EXTSPL = queryEXTSPL.getContainer();
  		EXTSPL.set("EXCONO", XXCONO);
  		EXTSPL.set("EXDIVI", divi);
  		EXTSPL.set("EXINBN", inbn.toInteger());
  		queryEXTSPL.readAllLock(EXTSPL, 3, updateEXTSPL);
  }
  /*
  * updateEXTSPL - Callback function
  *
  */
   Closure<?> updateEXTSPL = { LockedResult EXTSPL ->
    EXTSPL.set("EXPROC", 1);
    EXTSPL.set("EXLMDT", currentDate);
  	EXTSPL.set("EXCHNO", EXTSPL.get("EXCHNO").toString().toInteger() +1);
  	EXTSPL.set("EXCHID", program.getUser());
    EXTSPL.update();
   }
}
