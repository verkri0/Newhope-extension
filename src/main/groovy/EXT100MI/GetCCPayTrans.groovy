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
* - Get payment transaction record from EXTCRD
*/
public class GetCCPayTrans extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  private String cono;
  private String divi;
  private String yea4;
  private String ivno;
  private String exid;
  
  private int XXCONO;
  
  public GetCCPayTrans(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
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
    DBAction queryEXTCRD = database.table("EXTCRD").index("00").selectAllFields().build();
    DBContainer EXTCRD = queryEXTCRD.getContainer();
		EXTCRD.set("EXCONO", XXCONO);
		EXTCRD.set("EXDIVI", divi);
		EXTCRD.set("EXEXID", exid);
		
		if (queryEXTCRD.read(EXTCRD)) {
		  mi.outData.put("YEA4", EXTCRD.get("EXYEA4").toString());
		  mi.outData.put("IVNO", EXTCRD.get("EXIVNO").toString());
		  mi.outData.put("EXID", EXTCRD.get("EXEXID").toString());
		  mi.outData.put("IVAM", EXTCRD.get("EXIVAM").toString());
		  mi.outData.put("PRVP", EXTCRD.get("EX3RDP").toString());
		  mi.outData.put("PRVI", EXTCRD.get("EX3RDI").toString());
		  mi.outData.put("TSTA", EXTCRD.get("EXTSTA").toString());
		  mi.outData.put("AUTD", EXTCRD.get("EXAUTD").toString());
		  mi.outData.put("CAPD", EXTCRD.get("EXCAPD").toString());
		  mi.outData.put("SETD", EXTCRD.get("EXSETD").toString());
		  mi.outData.put("STAM", EXTCRD.get("EXSTAM").toString());
		  mi.outData.put("ATAM", EXTCRD.get("EXATAM").toString());
		  mi.outData.put("OGID", EXTCRD.get("EXOGID").toString());
		  mi.write();
		} else {
		  mi.error("Credit card payment transaction " + exid + " does not exist in EXTCRD.");
      return;
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
}
