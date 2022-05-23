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
 
 import groovy.lang.Closure;
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *ABF_R_xxx        20220519   WZHAO       Update auisition cost in MITTRA
 *
 */
public class UpdAcquisitCost extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String cono;
  private String whlo;
  private String itno;
  private String rgdt;
  private String rgtm;
  private String tmsx;
  
  private String puno;
  private String pnli;
  private String ttyp;
  
  private int XXCONO;
  private double sum;
  
  public UpdAcquisitCost(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
  }
  
  public void main() {
    cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
    if (cono == "?") {
      cono = "";
    } 
    whlo = mi.inData.get("WHLO") == null ? '' : mi.inData.get("WHLO").trim();
    if (whlo == "?") {
      whlo = "";
    } 
    itno = mi.inData.get("ITNO") == null ? '' : mi.inData.get("ITNO").trim();
    if (itno == "?") {
      itno = "";
    }
    rgdt = mi.inData.get("RGDT") == null ? '' : mi.inData.get("RGDT").trim();
    if (rgdt == "?") {
      rgdt = "";
    }	
  	
    rgtm = mi.inData.get("RGTM") == null ? '' : mi.inData.get("RGTM").trim();
    if (rgtm == "?") {
      rgtm = "";
    } 
    tmsx = mi.inData.get("TMSX") == null ? '' : mi.inData.get("TMSX").trim();
    if (tmsx == "?") {
      tmsx = "";
    } 
    //Validate input fields
    if (!validateInput()) {
      return;
    }
    puno = "";
    pnli = "";
    ttyp = "";
    // - mittra
    DBAction queryMITTRA = database.table("MITTRA").index("00").selection("MTRIDN", "MTRIDL", "MTTTYP").build();
    DBContainer MITTRA = queryMITTRA.getContainer();
    MITTRA.set("MTCONO", XXCONO);
		MITTRA.set("MTWHLO", whlo);
		MITTRA.set("MTITNO", itno);
		MITTRA.set("MTRGDT", rgdt.toInteger());
		MITTRA.set("MTRGTM", rgtm.toInteger());
		MITTRA.set("MTTMSX", tmsx.toInteger());
		if (!queryMITTRA.read(MITTRA)) {
		  mi.error("Record does not exist in MITTRA.");
      return;
    } else {
      ttyp = MITTRA.get("MTTTYP").toString().trim();
      puno = MITTRA.get("MTRIDN").toString().trim();
      pnli = MITTRA.get("MTRIDL").toString().trim();
    }
    // - if not a PO, stop
    if (!ttyp.toInteger() == 25 || puno.isEmpty() || pnli.isEmpty() || (!pnli.isEmpty() && pnli.toInteger() == 0)) {
      return;
    }
    sum = 0.0;
    DBAction queryMPOEXP = database.table("MPOEXP").index("00").selection("IVOVHE", "IVCEVA").build();
    DBContainer MPOEXP = queryMPOEXP.getContainer();
    MPOEXP.set("IVCONO", XXCONO);
		MPOEXP.set("IVPUNO", puno);
		MPOEXP.set("IVPNLI", pnli.toInteger());
		queryMPOEXP.readAll(MPOEXP, 3, lstMPOEXP);
		
		if (sum != 0.0) {
		  sum = sum / 1000;
		  logger.debug("Sum=" + sum);
  		queryMITTRA.readLock(MITTRA, updateMITTRA);
		}
  }
  
  /*
  * validateInput - Validate all the input fields - replicated from PECHK() of CRS575
  * @return false if there is any error
  *         true if pass the validation
  */
  boolean validateInput() { 
    if (!cono.isEmpty()) {
	    if (cono.isInteger()){
		    XXCONO= cono.toInteger();
			  } else {
				  mi.error("Company " + cono + " is invalid.");
				  return false;
			  }
		  } else {
			XXCONO= program.LDAZD.CONO;
	  }
    if (whlo.isEmpty()) {
      mi.error("Warehouse must be entered.");
      return false;
    }
    // - Warehouse
    DBAction queryMITWHL = database.table("MITWHL").index("00").selection("MWDIVI","MWFACI").build();
    DBContainer MITWHL = queryMITWHL.createContainer();
    MITWHL.set("MWCONO", XXCONO);
    MITWHL.set("MWWHLO", whlo);
    
    if (!queryMITWHL.read(MITWHL)){
      mi.error("Warehouse dose not exist in MITWHL.");
      return false;
    }
    if (itno.isEmpty()) {
      mi.error("Item number must be entered.");
      return false;
    }
    // - item master
    DBAction queryMITMAS = database.table("MITMAS").index("00").selection("MMITDS").build();
    DBContainer MITMAS = queryMITMAS.getContainer();
    MITMAS.set("MMCONO", XXCONO);
    MITMAS.set("MMITNO", itno);
    if (!queryMITMAS.read(MITMAS)){
      mi.error("Item number does not exist in MITMAS.");
      return false; 
    }
    if (rgdt.toInteger() == 0) {
      mi.error("Entry must be entered.");
      return false;
    }
    if (!isDateValid(rgdt)) {
      mi.error("Entry date is not a valid date");
      return
    }
    if (rgtm.toInteger() == 0) {
      mi.error("Entry time must be entered.");
      return false;
    }
    if (tmsx.toInteger() == 0) {
      mi.error("Time suffix must be entered.");
      return false;
    }
    return true;
  }
  /**
   * isDateValid - check if input string is a valid date
   *  - date format: yyyyMMdd
   * return boolean
   */
  def isDateValid(String dateStr) {
    boolean dateIsValid = true;
    Matcher matcher=
      Pattern.compile("^((2000|2400|2800|(19|2[0-9](0[48]|[2468][048]|[13579][26])))0229)\$" 
        + "|^(((19|2[0-9])[0-9]{2})02(0[1-9]|1[0-9]|2[0-8]))\$"
        + "|^(((19|2[0-9])[0-9]{2})(0[13578]|10|12)(0[1-9]|[12][0-9]|3[01]))\$" 
        + "|^(((19|2[0-9])[0-9]{2})(0[469]|11)(0[1-9]|[12][0-9]|30))\$").matcher(dateStr);
    dateIsValid = matcher.matches();
    if (dateIsValid) {
      dateIsValid = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")) != null;
    } 
    return dateIsValid;
  }
  /*
   * lstMPOEXP - Callback function to return MPOEXP records
   *
   */
 
  Closure<?> lstMPOEXP = { DBContainer MPOEXP ->
    
   String ovhe = MPOEXP.get("IVOVHE").toString().trim();
   String ceva = MPOEXP.get("IVCEVA").toString().trim();
   if (ceva.toDouble() != 0.0) {
    sum += ovhe.toDouble();
   }
  }
  /*
   * updateMITTRA - Callback function to update MITTRA table
   *
   */
 Closure updateMITTRA = { LockedResult MITTRA ->
  
    MITTRA.set("MTMFCO", sum);
    MITTRA.update();
  }
}
