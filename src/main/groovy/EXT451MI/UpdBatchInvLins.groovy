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
 import java.time.ZoneId;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 
 /*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *BF0499            20220519   WZHAO       Update batch invoice lines
 *
 */
 
public class UpdBatchInvLins extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  private String cono;
  private String divi;
  private String inbn;
  private String trno;
  
  private int XXCONO;
  private int currentDate;
  private int currentTime;
  
  public UpdBatchInvLins(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.miCaller = miCaller;
    this.logger = logger;
    this.program = program;
  }
  
  public void main() {
    //Fetch input fields from MI
    cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
  	if (cono == "?") {
  	  cono = "";
  	} 
    divi = mi.inData.get("DIVI") == null ? '' : mi.inData.get("DIVI").trim();
  	if (divi == "?") {
  	  divi = "";
  	} 
  	inbn = mi.inData.get("INBN") == null ? '' : mi.inData.get("INBN").trim();
  	if (inbn == "?") {
  	  inbn = "";
  	} 
  	trno = mi.inData.get("TRNO") == null ? '' : mi.inData.get("TRNO").trim();
  	if (trno == "?") {
  	  trno = "";
  	} 
  	//Validate input fields
    if (!cono.isEmpty()) {
			if (cono.isInteger()){
				XXCONO= cono.toInteger();
			} else {
				mi.error("Company " + cono + " is invalid");
				return;
			}
		} else {
			XXCONO= program.LDAZD.CONO;
		}
    if (divi.isEmpty()) {
      mi.error("Division must be entered");
      return;
    }
    if (inbn.isEmpty()) {
      mi.error("Invoice batch number must be entered");
      return;
    }
    if (trno.isEmpty()) {
      mi.error("Transaction number must be entered");
      return;
    }
    // Get record from FAPIBH
    DBAction queryFAPIBH = database.table("FAPIBH").index("00").selection("E5SUPA").build()
    DBContainer FAPIBH = queryFAPIBH.getContainer();
    FAPIBH.set("E5CONO", XXCONO);
    FAPIBH.set("E5DIVI", divi);
    FAPIBH.set("E5INBN", inbn.toInteger());
    if (!queryFAPIBH.read(FAPIBH)) {
      mi.error("Invoice batch number does not exist.");
      return;
    } else {
      String supa = FAPIBH.get("E5SUPA").toString().trim();
      if (supa.toInteger() == 90) {
        return;
      }
    }
    currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    
    DBAction actionFAPIBL = database.table("FAPIBL").index("00").build();
    DBContainer FAPIBL = actionFAPIBL.getContainer();
		FAPIBL.set("E6CONO", XXCONO);
		FAPIBL.set("E6DIVI", divi);
		FAPIBL.set("E6INBN", inbn.toInteger());
		FAPIBL.set("E6TRNO", trno.toInteger());
		actionFAPIBL.readLock(FAPIBL, updateFAPIBL);
  }
  
  /*
  * updateFAPIBL - Callback function
  *
  */
  Closure<?> updateFAPIBL = { LockedResult FAPIBL ->
    String rdtp = FAPIBL.get("E6RDTP").toString().trim();
    String fav5 = FAPIBL.get("E6FAV5").toString().trim();
    String puno = FAPIBL.get("E6PUNO").toString().trim();
    String pnli = FAPIBL.get("E6PNLI").toString().trim();
    String pnls = FAPIBL.get("E6PNLS").toString().trim();
    String repn = FAPIBL.get("E6REPN").toString().trim();
    String relp = FAPIBL.get("E6RELP").toString().trim();
    // - already updated, don't do second time
    if (fav5.toDouble() == 1) {
      FAPIBL.update();
      return;
    }
    // - if line type is 1, update FGRPCL
    if (rdtp.toInteger() == 1) {
      FAPIBL.set("E6FAV5", 1); // set flag ON to indicates it has been calculated.
      FAPIBL.set("E6LMDT", currentDate);
      FAPIBL.set("E6CHNO",  FAPIBL.get("E6CHNO").toString().toInteger() +1);
      FAPIBL.set("E6CHID", program.getUser()); 
      FAPIBL.update();
      updateGoodsReceiptLineCharges(puno, pnli, pnls, repn, relp);
    } else if (rdtp.toInteger() == 5) {
      String puun = FAPIBL.get("E6PUUN").toString().trim();
      String ivqa = FAPIBL.get("E6IVQA").toString().trim();
      double dIVQA = 0.0;
      if (puun.equals("TNE")) {
        dIVQA = ivqa.toDouble() * 1000;
        FAPIBL.set("E6IVQA", dIVQA);
      }
      String ppun = FAPIBL.get("E6PPUN").toString().trim();
      String grpr = FAPIBL.get("E6GRPR").toString().trim();
      double dGRPR = 0.0;
      if (ppun.equals("TNE")) {
        dGRPR = grpr.toDouble() / 1000;
        FAPIBL.set("E6GRPR", dGRPR);
      }
      FAPIBL.set("E6FAV5", 1); // set flag ON to indicates it has been calculated.
      FAPIBL.set("E6LMDT", currentDate);
      FAPIBL.set("E6CHNO",  FAPIBL.get("E6CHNO").toString().toInteger() +1);
      FAPIBL.set("E6CHID", program.getUser()); 
      FAPIBL.update();
    } else {
      FAPIBL.update();
      return;
    }
  }
   /*
   * updateGoodsReceiptLineCharges - update goods receipt line charges
   *
  */
  def updateGoodsReceiptLineCharges(String puno, String pnli, String pnls, String repn, String relp) {
    DBAction actionFGRPCL = database.table("FGRPCL").index("00").build();
    DBContainer FGRPCL = actionFGRPCL.getContainer();
		FGRPCL.set("F3CONO", XXCONO);
		FGRPCL.set("F3DIVI", divi);
		FGRPCL.set("F3PUNO", puno);
		FGRPCL.set("F3PNLI", pnli.toInteger());
		FGRPCL.set("F3PNLS", pnls.toInteger());
		FGRPCL.set("F3REPN", repn.toInteger());
		FGRPCL.set("F3RELP", relp.toInteger());
		actionFGRPCL.readAllLock(FGRPCL,7, updateFGRPCL);
  }
   /*
  * updateFGRPCL - Callback function
  *
  */
   Closure<?> updateFGRPCL = { LockedResult FGRPCL ->
    String scop = FGRPCL.get("F3SCOP").toString().trim();
    double dSCOP = scop.toDouble() / 1000;
    FGRPCL.set("F3SCOP", dSCOP);
    FGRPCL.set("F3LMDT", currentDate);
    FGRPCL.set("F3CHNO",  FGRPCL.get("F3CHNO").toString().toInteger() +1);
    FGRPCL.set("F3CHID", program.getUser()); 
    FGRPCL.update();
   }
}
