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

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *ABF_R_0625        20220405  RDRIESSEN   Mods BF0625- Generate APS450 invoice printout extension workfile
 *
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

public class UpdateCHG extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  
  private String cono;
  private String car1;
  private String whlo;
  private String sino;
  private String vfdt;
  private String lfdt;
  
  private int XXCONO;
 
  public UpdateCHG(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
  
  	car1 = mi.inData.get("CAR1") == null ? '' : mi.inData.get("CAR1").trim();
  	if (car1 == "?") {
  	  car1 = "";
  	} 
  	
  	vfdt = mi.inData.get("VFDT") == null ? '' : mi.inData.get("VFDT").trim();
  	if (vfdt == "?") {
  	  vfdt = "";
  	}
  	
  	lfdt = mi.inData.get("LFDT") == null ? '' : mi.inData.get("LFDT").trim();
  	if (lfdt == "?") {
  	  lfdt = "";
  	}
  	
  	sino = mi.inData.get("SINO") == null ? '' : mi.inData.get("SINO").trim();
  	if (sino == "?") {
  	  sino = "";
  	}
  	
  	
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
  	if (whlo.isEmpty()) {
  	  mi.error("Warehouse must be entered.");
  	  return;
  	}
  	DBAction queryMITWHL = database.table("MITWHL").index("00").selection("MWDIVI","MWFACI").build();
    DBContainer MITWHL = queryMITWHL.createContainer();
    MITWHL.set("MWCONO", XXCONO);
    MITWHL.set("MWWHLO", whlo);
    if (!queryMITWHL.read(MITWHL)){
      mi.error("Warehouse dose not exist in MITWHL.");
      return;
    }
  	if (vfdt.isEmpty()) { 
  	  vfdt = "0";  
  	} else {
  	  if (!isDateValid(vfdt)) {
  	    mi.error("From date is not a valid date.");
  	    return;
  	  }
  	}
  	if (lfdt.isEmpty()) { 
  	  lfdt = "0";  
  	} else {
  	  if (!isDateValid(lfdt)) {
  	    mi.error("To date is not a valid date.");
  	    return;
  	  }
  	}
  	
  	ExpressionFactory expression = database.getExpressionFactory("EXTCHG");
    expression = expression.ge("EXTRDT", vfdt.toString());
    expression = expression.and(expression.le("EXTRDT", lfdt.toString()));
    expression = expression.and(expression.eq("EXPROC", "0"));
    
  
    DBAction query = database.table("EXTCHG").index("10").matching(expression).selectAllFields().build();
    DBContainer container = query.getContainer();
    container.set("EXCONO", XXCONO);
    container.set("EXWHLO", whlo.trim());
    container.set("EXCAR1", car1.trim());  
    query.readAll(container, 3, lstEXTCHG);
    
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
   * lstEXTCHG - callback to list records from EXTCHG
   *
  */
  Closure<?> lstEXTCHG = { DBContainer EXTCHG ->
    String itno = EXTCHG.get("EXITNO").toString().trim();
    String rgdt = EXTCHG.get("EXRGDT").toString().trim();
    String rgtm = EXTCHG.get("EXRGTM").toString().trim();
    String tmsx = EXTCHG.get("EXTMSX").toString().trim();
    String chtp = EXTCHG.get("EXCHTP").toString().trim();
    
    DBAction actionEXTCHG1 = database.table("EXTCHG").index("00").build();
    DBContainer EXTCHG1 = actionEXTCHG1.getContainer();
    EXTCHG1.set("EXCONO", XXCONO);
    EXTCHG1.set("EXWHLO", whlo);
    EXTCHG1.set("EXITNO", itno);
    EXTCHG1.set("EXRGDT", rgdt.toInteger());
    EXTCHG1.set("EXRGTM", rgtm.toInteger());
    EXTCHG1.set("EXTMSX", tmsx.toInteger());
    EXTCHG1.set("EXCHTP", chtp);
    
    actionEXTCHG1.readLock(EXTCHG1, updateEXTCHG1);
  }
  /*
   * updateEXTCHG1 - Callback function to update EXTCHG table
   *
   */
 Closure updateEXTCHG1 = { LockedResult EXTCHG ->
  
    int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    
    EXTCHG.set("EXPROC", "1");
    EXTCHG.set("EXSINO", sino.trim());
    EXTCHG.set("EXLMDT", currentDate);
    EXTCHG.set("EXCHNO",  EXTCHG.get("EXCHNO").toString().toInteger() +1);
    EXTCHG.set("EXCHID", program.getUser());
    
    EXTCHG.update();
   
  }
}
