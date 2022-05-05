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
 *ABF_R_1072        20220325  XWZHAO      Mods Sunbeam payment schedule update
 *
 */
 
/*
* - Write payment split records to EXTSPL
*/
public class AddPmtSplitTrns extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  private String cono;
  private String divi;
  private String inbn;
  
  
  private List lstSplitPercentLines;

  
  private int XXCONO;
  private int currentDate;
  private int currentTime;
  
  public AddPmtSplitTrns(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
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
    
    String spyn = "";
    String suno = "";
    String sino = "";
    String supa = "";
    String bist = "";
    String ibhe = "";
    String ible = "";
    String ivdt = "";
    String dudt = "";
    String acdt = "";
    String cuam = "";
    String cucd = "";
    String tepy = "";
    String sucl = "";
    String pdat = "";
    
    ZoneId zid = ZoneId.of("Australia/Sydney"); 
    currentDate = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
    currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  
    // Get record from FAPIBH
    DBAction queryFAPIBH = database.table("FAPIBH").index("00").selection("E5SPYN", "E5SUNO", "E5SINO", "E5SUPA","E5BIST","E5IBHE","E5IBLE", "E5IVDT", "E5DUDT", "E5ACDT","E5CUAM","E5CUCD","E5TEPY","E5CRTP","E5ARAT").build()
    DBContainer FAPIBH = queryFAPIBH.getContainer();
    FAPIBH.set("E5CONO", XXCONO);
    FAPIBH.set("E5DIVI", divi);
    FAPIBH.set("E5INBN", inbn.toInteger());
    
    if (queryFAPIBH.read(FAPIBH)) {
      spyn = FAPIBH.get("E5SPYN").toString().trim();
      suno = FAPIBH.get("E5SUNO").toString().trim();
      sino = FAPIBH.get("E5SINO").toString().trim();
      supa = FAPIBH.get("E5SUPA").toString().trim();
      bist = FAPIBH.get("E5BIST").toString().trim();
      ibhe = FAPIBH.get("E5IBHE").toString().trim();
      ible = FAPIBH.get("E5IBLE").toString().trim();
      if (supa.toInteger() != 10 || bist.toInteger() != 0 || ibhe.toInteger() != 1 || ible.toInteger() != 1) {
        mi.error("Incorrect status, Batch invoice " + inbn + " payment cannot be splited.");
        return;
      }
      ivdt = FAPIBH.get("E5IVDT").toString().trim();
      dudt = FAPIBH.get("E5DUDT").toString().trim();
      acdt = FAPIBH.get("E5ACDT").toString().trim();
      cuam = FAPIBH.get("E5CUAM").toString().trim();
      cucd = FAPIBH.get("E5CUCD").toString().trim();
      tepy = FAPIBH.get("E5TEPY").toString().trim();
    } else {
      mi.error("Batch invoice " + inbn + " does not exist in APS450.");
      return;
    }
    
    DBAction queryCIDVEN = database.table("CIDVEN").index("00").selection("IISUCL").build()
    DBContainer CIDVEN = queryCIDVEN.getContainer();
    CIDVEN.set("IICONO", XXCONO);
    CIDVEN.set("IISUNO", suno);
    if (queryCIDVEN.read(CIDVEN)) {
     sucl = CIDVEN.get("IISUCL").toString().trim();
    }
     
    lstSplitPercentLines = new ArrayList();
    
    DBAction queryCUGEX1 = database.table("CUGEX1").index("00").selection("F1PK05","F1A030","F1A130").build();

    DBContainer CUGEX1 = queryCUGEX1.getContainer();
    CUGEX1.set("F1CONO", XXCONO);
    CUGEX1.set("F1FILE", "PAYSCHED");
    CUGEX1.set("F1PK01", "SCHEDULE");
    CUGEX1.set("F1PK02", sucl);
    CUGEX1.set("F1PK03", suno);
    CUGEX1.set("F1PK04", tepy);
    queryCUGEX1.readAll(CUGEX1, 6, lstCUGEX1);
    
    // - now checking suplier group level
    if (lstSplitPercentLines.size() == 0) {
      CUGEX1.set("F1PK03", "");
      queryCUGEX1.readAll(CUGEX1, 6, lstCUGEX1);
    }
    
    if (lstSplitPercentLines.size() == 0) {
      mi.error("No split schedule lines found. Please set them up in payment-schedule SDK.");
      return;
    } 
    if (cuam.toDouble() == 0) {
      mi.error("Batch invoice amount is zero.");
      return;
    }
    deleteFromEXTSPL();
    double totalLineAmt = 0;
    for (int i=0;i<lstSplitPercentLines.size();i++) {
      Map<String, String> record = (Map<String, String>) lstSplitPercentLines[i];
      String date = record.DATE;
      String percent = record.PERCENT;
      logger.debug("DATE=" + date + " PERCENT=" + percent);
      double amt = cuam.toDouble() * (percent.toDouble()/100);
      BigDecimal bAmt = new BigDecimal(amt).setScale(2, RoundingMode.HALF_UP);
      amt = bAmt.doubleValue();
      if (date.equals("00000000")) {
        pdat = dudt;
      } else {
        pdat = date;
      }
      totalLineAmt += amt;
      // - Last split line ajustment.
      if ((i + 1) == lstSplitPercentLines.size()) {
        if (totalLineAmt > cuam.toDouble()) {
          amt = amt - (totalLineAmt - cuam.toDouble());
        } else if (totalLineAmt < cuam.toDouble()) {
          amt = amt + (cuam.toDouble() - totalLineAmt);
        }
      }
      writeEXTSPL(i, spyn, suno, sino, pdat,  cucd, amt);
    }
  }
  
  /*
   * lstCUGEX1 - Callback function to return CUGEX1 records
   *
   */
 
  Closure<?> lstCUGEX1 = { DBContainer CUGEX1 ->
    String pk05 = CUGEX1.get("F1PK05").toString();
    if (pk05.toInteger() > 0) {
      String a030 = CUGEX1.get("F1A030").toString().trim();
      String a130 = CUGEX1.get("F1A130").toString().trim();
      def map = [DATE: a030, PERCENT: a130];
      lstSplitPercentLines.add(map);
    }
  }
  /*
   * deleteFromEXTSPL - delete EXTSPLtable
   *
   */
   def deleteFromEXTSPL() {
    DBAction queryEXTSPL = database.table("EXTSPL").index("00").selection("EXINBN").build();
    DBContainer EXTSPL = queryEXTSPL.getContainer();
		EXTSPL.set("EXCONO", XXCONO);
		EXTSPL.set("EXDIVI", divi);
		EXTSPL.set("EXINBN", inbn.toInteger());
		queryEXTSPL.readAllLock(EXTSPL, 3, deleteEXTSPL);
   }
    /*
  * deleteEXTSPL - Callback function
  *
  */
   Closure<?> deleteEXTSPL = { LockedResult EXTSPL ->
    EXTSPL.delete();
   }
  /*
	 * writeEXTSPL
	 *
	 */
	def writeEXTSPL(int lineNo, String spyn, String suno, String sino, String pdat,  String cucd, double amt) {
	  
  	lineNo++;
  	
	  DBAction ActionEXTSPL = database.table("EXTSPL").build();
  	DBContainer EXTSPL = ActionEXTSPL.getContainer();
  	EXTSPL.set("EXCONO", XXCONO);
  	EXTSPL.set("EXDIVI", divi);
  	EXTSPL.set("EXINBN", inbn.toInteger());
  	EXTSPL.set("EXLINO", lineNo);
  	EXTSPL.set("EXSPYN", spyn);
  	EXTSPL.set("EXSUNO", suno);
  	EXTSPL.set("EXSINO", sino);
  	EXTSPL.set("EXPDAT", pdat.toInteger());
  	EXTSPL.set("EXCUCD", cucd);
  	EXTSPL.set("EXCUAM", amt);
  	EXTSPL.set("EXRGDT", currentDate);
  	EXTSPL.set("EXRGTM", currentTime);
  	EXTSPL.set("EXLMDT", currentDate);
  	EXTSPL.set("EXCHNO", 0);
  	EXTSPL.set("EXCHID", program.getUser());
  	ActionEXTSPL.insert(EXTSPL, recordExists);
	}
	/*
   * recordExists - return record already exists error message to the MI
   *
   */
  Closure recordExists = {
	  mi.error("Record already exists");
  }
}
