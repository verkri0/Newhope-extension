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
 import java.util.regex.Matcher
 import java.util.regex.Pattern
 import java.time.ZoneId;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 
 /*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *XXXX              20220510  XWZHAO      Credit card payment transactions
 *XXXX              20220601  XWZHAO      Intruduce additional input for searching date on AUTD/CAPD/SETD 
 *
 */
 
/*
* - List payment transaction records from EXTCRD by settlement date range
*/
public class LstTransByDates extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  private String cono;
  private String divi;
  private String frdt;
  private String todt;
  private String dtty;
  
  private int XXCONO;
  
  public LstTransByDates(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
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
  	frdt = mi.inData.get("FRDT") == null ? '' : mi.inData.get("FRDT").trim();
  	if (frdt == "?") {
  	  frdt = "";
  	} 
  	todt = mi.inData.get("TODT") == null ? '' : mi.inData.get("TODT").trim();
  	if (todt == "?") {
  	  todt = "";
  	} 
  	dtty = mi.inData.get("DTTY") == null ? '' : mi.inData.get("DTTY").trim();
  	if (dtty == "?") {
  	  dtty = "";
  	} 
  	//Validate input fields
    if (!validateInput()) {
      return;
    }
    ExpressionFactory expression = database.getExpressionFactory("EXTCRD");
    if (dtty.toInteger() == 1) {
      expression = expression.ge("EXAUTD", frdt.toString());
      expression = expression.and(expression.le("EXAUTD", todt.toString()));
    } else if (dtty.toInteger() == 2) {
      expression = expression.ge("EXCAPD", frdt.toString());
      expression = expression.and(expression.le("EXCAPD", todt.toString()));
    } else {
      expression = expression.ge("EXSETD", frdt.toString());
      expression = expression.and(expression.le("EXSETD", todt.toString()));
    }
    DBAction queryEXTCRD = database.table("EXTCRD").index("00").matching(expression).selectAllFields().build();
    DBContainer EXTCRD = queryEXTCRD.getContainer();
    EXTCRD.set("EXCONO", XXCONO);
    EXTCRD.set("EXDIVI", divi);
    int recNum = queryEXTCRD.readAll(EXTCRD, 2, 999, lstEXTCRD);
    if (recNum == 0) {
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
    if (frdt.isEmpty()) {
      mi.error("From date must be entered");
      return false;
    }
    if (!isDateValid(frdt)) {
      mi.error("Input From date is not a valid date");
      return
    }
    if (todt.isEmpty()) {
      mi.error("To date must be entered");
      return false;
    }
    if (!isDateValid(todt)) {
      mi.error("Input To date is not a valid date");
      return
    }
    if (frdt.toInteger() > todt.toInteger()) {
      mi.error("From date cannot be later than To date.");
      return false;
    }
    if (dtty.isEmpty()) {
      mi.error("Search date type must be entered.");
      return false;
    }
    if (dtty.toInteger() < 1 && dtty.toInteger() > 3) {
      mi.error("Search date type must be 1, 2 or 3.");
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
   * lstEXTCRD - Callback function to return EXTCRD records
   *
   */
  Closure<?> lstEXTCRD = { DBContainer EXTCRD ->
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
  }
}
