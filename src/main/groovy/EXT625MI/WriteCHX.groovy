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

public class WriteCHX extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  
  private String cono;
  private String divi;
  private String inbn;
  private String trno;
  private String sudo;
  private String car1;
  private String suno;
  private String lots;
  private String chtp;
  private String wght;
  private String lnam;
  private String rate;
  private String trdt;
  private String sunm; 
  private String puno;
  private String appr;
  private String asts;
  private int XXCONO;
  private boolean found;
 
  public WriteCHX(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
  	
  	sudo = mi.inData.get("SUDO") == null ? '' : mi.inData.get("SUDO").trim();
  	if (sudo == "?") {
  	  sudo = "";
  	}
  	
  	car1 = mi.inData.get("CAR1") == null ? '' : mi.inData.get("CAR1").trim();
  	if (car1 == "?") {
  	  car1 = "";
  	}
  	
  	suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
  	if (suno == "?") {
  	  suno = "";
  	}
  	
  	lots = mi.inData.get("LOTS") == null ? '' : mi.inData.get("LOTS").trim();
  	if (lots == "?") {
  	  lots = "";
  	}
  	
  	chtp = mi.inData.get("CHTP") == null ? '' : mi.inData.get("CHTP").trim();
  	if (chtp == "?") {
  	  chtp = "";
  	}
  	
  	wght = mi.inData.get("WGHT") == null ? '' : mi.inData.get("WGHT").trim();
  	if (wght == "?") {
  	  wght = "";
  	}
  	
  	rate = mi.inData.get("RATE") == null ? '' : mi.inData.get("RATE").trim();
  	if (rate == "?") {
  	  rate = "";
  	}
  
		lnam = mi.inData.get("LNAM") == null ? '' : mi.inData.get("LNAM").trim();
  	if (lnam == "?") {
  	  lnam = "";
  	}
  	
  	trdt = mi.inData.get("TRDT") == null ? '' : mi.inData.get("TRDT").trim();
  	if (trdt == "?") {
  	  trdt = "";
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
		
		if (divi.isEmpty()) {
		  program.LDAZD.DIVI;
		} else {
		  DBAction queryCMNDIV = database.table("CMNDIV").index("00").selection("CCDIVI").build();
      DBContainer CMNDIV = queryCMNDIV.getContainer();
      CMNDIV.set("CCCONO", XXCONO);
      CMNDIV.set("CCDIVI", divi);
      if(!queryCMNDIV.read(CMNDIV)) {
        mi.error("Division does not exist.");
        return;
      } 
		}
		
		 // - validate AP invoice header
    DBAction queryFAPIBH = database.table("FAPIBH").index("00").selection("E5INBN").build();
    DBContainer FAPIBH = queryFAPIBH.getContainer();
    FAPIBH.set("E5CONO", XXCONO);
    FAPIBH.set("E5DIVI", divi);
    FAPIBH.set("E5INBN", inbn.toInteger());
    if (!queryFAPIBH.read(FAPIBH)) {
      mi.error("Invoice header is invalid.");
      return;
    }
    
		
		 // - AP invoice line
    DBAction queryFAPIBL = database.table("FAPIBL").index("00").selection("E6INBN").build();
    DBContainer FAPIBL = queryFAPIBL.getContainer();
    FAPIBL.set("E6CONO", XXCONO);
    FAPIBL.set("E6DIVI", divi);
    FAPIBL.set("E6INBN", inbn.toInteger());
    FAPIBL.set("E6TRNO", trno.toInteger());
    if (!queryFAPIBL.read(FAPIBL)) {
      mi.error("Invoice Line is invalid.");
      return;
    }
    
     // - Supplier No
    DBAction queryCIDMAS = database.table("CIDMAS").index("00").selection("IDSUNO").build();
    DBContainer CIDMAS = queryCIDMAS.getContainer();
    CIDMAS.set("IDCONO", XXCONO);
    CIDMAS.set("IDSUNO", suno);
    if (!queryCIDMAS.read(CIDMAS)) {
      mi.error("Supplierno is invalid.");
      return;
    }
    
    
    
     // - validate sudo
  	found = false;
    DBAction queryFGRECL = database.table("FGRECL").index("30").selection("F2DIVI", "F2SUDO").build();
    DBContainer FGRECL = queryFGRECL.getContainer();
    FGRECL.set("F2CONO", XXCONO);
    FGRECL.set("F2DIVI", divi);
    FGRECL.set("F2SUDO", sudo);
    
    queryFGRECL.readAll(FGRECL, 3, 1, lstFGRECL);
    if (!found) {
      mi.error("Delivery No is invalid.");
      return;
    }
    
    if (!chtp.equals("Carrier Fees") && !chtp.equals("Loader Fees") && !chtp.equals("DFA Fees")) {
      mi.error("Invalid fee description");
      return;
    }
    
    
    if (trdt.toInteger() != 0 && !isDateValid(trdt)) {
      mi.error("Transaction date is invalid.");
      return;
    }
    
    
    def paramsy = ["SUNO":suno.toString()];
    
    def callbacky = {
      Map<String, String> response ->
      if(response.SUNO != null){
        sunm = response.SUNM;  
      }
    }
    
    miCaller.call("CRS620MI","GetBasicData", paramsy, callbacky);	
    
  	if (inbn.isEmpty()) { inbn = "0";  }
  	if (trno.isEmpty()) { trno = "0";  }
  	if (lots.isEmpty()) { lots = "0";  }
  	if (wght.isEmpty()) { wght = "0";  }
  	if (rate.isEmpty()) { rate = "0";  }
  	if (lnam.isEmpty()) { lnam = "0";  }
  	if (trdt.isEmpty()) { trdt = "0";  }
  	  
  	
  	writeEXTCHX(cono, divi, inbn, trno, sudo, car1, suno, lots, chtp, wght, rate, lnam, trdt, sunm);
    
  }
  
  /*
   * writeEXTCHX - Write APS Invoice extension record
   *
   */
  
  def writeEXTCHX(String cono, String divi, String inbn, String trno, String sudo, String car1, String suno, String lots, String chtp, String wght, String rate, String lnam, String trdt, String sunm) {
	  //Current date and time
    int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
	  DBAction actionEXTCHX = database.table("EXTCHX").build();
  	DBContainer EXTCHX = actionEXTCHX.getContainer();
  	EXTCHX.set("EXCONO", XXCONO);
  	EXTCHX.set("EXDIVI", divi);
  	EXTCHX.set("EXINBN", inbn.toInteger());
  	EXTCHX.set("EXTRNO", trno.toInteger());
  	EXTCHX.set("EXSUDO", sudo);
  	EXTCHX.set("EXCAR1", car1);
  	EXTCHX.set("EXSUNO", suno);
  	EXTCHX.set("EXLOTS", lots.toInteger());
  	EXTCHX.set("EXCHTP", chtp);
  	EXTCHX.set("EXWGHT", wght.toDouble());
  	EXTCHX.set("EXRATE", rate.toDouble());
  	EXTCHX.set("EXLNAM", lnam.toDouble());
  	EXTCHX.set("EXTRDT", trdt.toInteger());
  	EXTCHX.set("EXSUNM", sunm);
  	EXTCHX.set("EXRGDT", currentDate);
  	EXTCHX.set("EXRGTM", currentTime);
  	EXTCHX.set("EXLMDT", currentDate);
  	EXTCHX.set("EXCHNO", 0);
  	EXTCHX.set("EXCHID", program.getUser());
  	actionEXTCHX.insert(EXTCHX, recordExists);
	}
  /*
   * recordExists - return record already exists error message to the MI
   *
   */
  Closure recordExists = {
     mi.error("Record already exists");
  }
  
  /*
   * lstFGRECL - Callback function to return FGRECL records
   *
  */
  Closure<?> lstFGRECL = { DBContainer FGRECL ->
    found = true;
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
        + "|^(((19|2[0-9])[0-9]{2})(0[469]|11)(0[1-9]|[12][0-9]|30))\$").matcher(dateStr)
    dateIsValid = matcher.matches()
    if (dateIsValid) {
      dateIsValid = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")) != null
    } 
    return dateIsValid;
  }
}
