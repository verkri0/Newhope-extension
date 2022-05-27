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
 *XXXX              20220510  XWZHAO      Credit card payment transactions
 *
 */
 
/*
* - Delete payment transaction record from EXTCRD
*/
public class DelCCPayTrans extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  private String cono;
  private String divi;
  private String exid;
  
  private int XXCONO;
  
  public DelCCPayTrans(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
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
  	exid = mi.inData.get("EXID") == null ? '' : mi.inData.get("EXID").trim();
  	if (exid == "?") {
  	  exid = "";
  	}
  	
  	//Validate input fields
    if (!validateInput()) {
      return;
    }
    DBAction actionEXTCRD = database.table("EXTCRD").index("00").build();
    DBContainer EXTCRD = actionEXTCRD.getContainer();
		EXTCRD.set("EXCONO", XXCONO);
		EXTCRD.set("EXDIVI", divi);
		EXTCRD.set("EXEXID", exid);
		
		if (!actionEXTCRD.readLock(EXTCRD, deleteEXTCRD)){
      mi.error("Record does not exists");
    }
		
  }
   /*
  * validateInput - Validate all the input fields - replicated from PECHK() of CRS575
  * @return false if there is any error
  *         true if pass the validation
  */
  boolean validateInput() { 
    
    if (!cono.isEmpty() ){
      if (cono.isInteger()){
        XXCONO= cono.toInteger();
      } else {
        mi.error("Company " + cono + " is invalid");
        return false;
      }
    } else {
      XXCONO= program.LDAZD.CONO;
    }
    if (divi.isEmpty()) {
      divi = program.LDAZD.DIVI;
    }
    if (exid.isEmpty()) {
      mi.error("External transaction ID must be entered.");
      return false;
    }
    return true;
  }
  
   /*
   * deleteEXTCRD - Callback function to delete EXTCRD table
   *
   */
  Closure deleteEXTCRD = { LockedResult EXTCRD ->
    EXTCRD.delete();
  }
}
