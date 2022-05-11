
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
 *ABF_R_0085        20220405  RDRIESSEN   Mods BF085- Write weight entries to extension file EXTWGT
 *
 */
 
/*
* - Write to EXTWGT file when weights have been recorded 
*/

 import groovy.lang.Closure
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;

public class WeightAVE extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String cono;
  private String faci;
  private String prno;
  private String mfno;
  private String opno;
  private String acts;
  private String spos;
  private String wg01;
  private String wg02;
  private String wg03;
  private String wg04;
  private String wg05;
  private String wg06;
  private String wg07;
  private String wg08;
  private String wg09;
  private String wg10;
  private String wg11;
  private String wg12;
  
   private int XXCONO;
 
  public WeightAVE(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	  
  }
  
  public void main() {
    
  	faci = mi.inData.get("FACI") == null ? '' : mi.inData.get("FACI").trim();
	  if (faci == "?") {
	    faci = "";
	  } 
  	
  	prno = mi.inData.get("PRNO") == null ? '' : mi.inData.get("PRNO").trim();
		if (prno == "?") {
	    prno = "";
	  } 
  
  	mfno = mi.inData.get("MFNO") == null ? '' : mi.inData.get("MFNO").trim();
		if (mfno == "?") {
	    mfno = "";
  	} 
  	
  	opno = mi.inData.get("OPNO") == null ? '' : mi.inData.get("OPNO").trim();
  	if (opno == "?") {
	    opno = "0";
	  } 
  	
  	acts = mi.inData.get("ACTS") == null ? '' : mi.inData.get("ACTS").trim();
  	if (acts == "?") {
  	  acts = "0";
  	} 
  	
  	spos = mi.inData.get("SPOS") == null ? '' : mi.inData.get("SPOS").trim();
  	if (spos == "?") {
  	  spos = "0";
  	} 
  	
  	wg01 = mi.inData.get("WG01") == null ? '' : mi.inData.get("WG01").trim();
  	if (wg01 == "?") {
  	  wg01 = "0";
  	} 
  	
  	wg02 = mi.inData.get("WG02") == null ? '' : mi.inData.get("WG02").trim();
  	if (wg02 == "?") {
  	  wg02 = "0";
  	} 
  	
  	wg03 = mi.inData.get("WG03") == null ? '' : mi.inData.get("WG03").trim();
  	if (wg03 == "?") {
  	  wg03 = "0";
  	} 
  	
  	wg04 = mi.inData.get("WG04") == null ? '' : mi.inData.get("WG04").trim();
  	if (wg04 == "?") {
  	  wg04 = "0";
  	} 
  	
  	wg05 = mi.inData.get("WG05") == null ? '' : mi.inData.get("WG05").trim();
  	if (wg05 == "?") {
  	  wg05 = "0";
  	} 
  	
  	wg06 = mi.inData.get("WG06") == null ? '' : mi.inData.get("WG06").trim();
  	if (wg06 == "?") {
  	  wg06 = "0";
  	} 
  	
  	wg07 = mi.inData.get("WG07") == null ? '' : mi.inData.get("WG07").trim();
  	if (wg07 == "?") {
  	  wg07 = "0";
  	} 
  	
  	wg08 = mi.inData.get("WG08") == null ? '' : mi.inData.get("WG08").trim();
  	if (wg08 == "?") {
  	  wg08 = "0";
  	} 
  	
  	wg09 = mi.inData.get("WG09") == null ? '' : mi.inData.get("WG09").trim();
  	if (wg09 == "?") {
  	  wg09 = "0";
  	} 
  	
  	wg10 = mi.inData.get("WG10") == null ? '' : mi.inData.get("WG10").trim();
  	if (wg10 == "?") {
  	  wg10 = "0";
  	} 
  	
  	wg11 = mi.inData.get("WG11") == null ? '' : mi.inData.get("WG11").trim();
  		if (wg11 == "?") {
  	  wg11 = "0";
  	} 
  	
  	wg12 = mi.inData.get("WG12") == null ? '' : mi.inData.get("WG12").trim();
  	if (wg12 == "?") {
  	  wg12 = "0";
  	} 
  	
	  if (opno.isEmpty()) { opno = "0";  }
	  if (acts.isEmpty()) { acts = "0";  }
	  if (spos.isEmpty()) { spos = "0";  }
	  if (wg01.isEmpty()) { wg01 = "0";  }
	  if (wg02.isEmpty()) { wg02 = "0";  }
	  if (wg03.isEmpty()) { wg03 = "0";  }
	  if (wg04.isEmpty()) { wg04 = "0";  }
	  if (wg05.isEmpty()) { wg05 = "0";  }
	  if (wg06.isEmpty()) { wg06 = "0";  }
	  if (wg07.isEmpty()) { wg07 = "0";  }
	  if (wg08.isEmpty()) { wg08 = "0";  }
	  if (wg09.isEmpty()) { wg09 = "0";  }
	  if (wg10.isEmpty()) { wg10 = "0";  }
	  if (wg11.isEmpty()) { wg11 = "0";  }
	  if (wg12.isEmpty()) { wg12 = "0";  }
  	
  	writeEXTWGT(faci, prno, mfno, opno, acts, spos, wg01, wg02, wg03, wg04, wg05, wg06, wg07, wg08, wg09, wg10, wg11, wg12);
  }
  
  
  /*
   * write record to EXTWGT
   *
  */
  
  def writeEXTWGT(String faci, String prno, String mfno, String opno, String acts, String spos, String wg01, String wg02, String wg03, String wg04, String wg05, String wg06, String wg07, String wg08, String wg09, String wg10, String wg11, String wg12) {
	  
	  int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	int currentCompany = (Integer)program.getLDAZD().CONO

    //check-validate if MWOHED record exists
    DBAction query = database.table("MWOHED").index("00").selection("VHCONO", "VHFACI", "VHPRNO", "VHMFNO").build();
    DBContainer container = query.getContainer();
    container.set("VHCONO", currentCompany);
    container.set("VHFACI", faci);
    container.set("VHPRNO", prno);
    container.set("VHMFNO", mfno);
    
    query.readAll(container, 4, releasedItemProcessor);
  
	}
  
  
  Closure<?> releasedItemProcessor = { DBContainer container ->
    cono = container.get("VHCONO");
    faci = container.get("VHFACI");
    prno = container.get("VHPRNO");
    mfno = container.get("VHMFNO");
  
    DBAction actionEXTWGT = database.table("EXTWGT").build();
  	DBContainer EXTWGT = actionEXTWGT.getContainer();
  	 
  	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));

  	//Company
  	int currentCompany = (Integer)program.getLDAZD().CONO;
  	 
  	EXTWGT.set("EXCONO", currentCompany);
  	EXTWGT.set("EXFACI", faci);
  	EXTWGT.set("EXPRNO", prno);
  	EXTWGT.set("EXMFNO", mfno);
  	EXTWGT.set("EXOPNO", opno.toInteger());
  	EXTWGT.set("EXACTS", acts.toInteger());
  	EXTWGT.set("EXSPOS", spos.toInteger());
  	EXTWGT.set("EXWG01", wg01.toDouble());
  	EXTWGT.set("EXWG02", wg02.toDouble());
  	EXTWGT.set("EXWG03", wg03.toDouble());
  	EXTWGT.set("EXWG04", wg04.toDouble());
  	EXTWGT.set("EXWG05", wg05.toDouble());
  	EXTWGT.set("EXWG06", wg06.toDouble());
  	EXTWGT.set("EXWG07", wg07.toDouble());
  	EXTWGT.set("EXWG08", wg08.toDouble());
  	EXTWGT.set("EXWG09", wg09.toDouble());
  	EXTWGT.set("EXWG10", wg10.toDouble());
  	EXTWGT.set("EXWG11", wg11.toDouble());
  	EXTWGT.set("EXWG12", wg12.toDouble());
  	EXTWGT.set("EXRGDT", currentDate);
  	EXTWGT.set("EXRGTM", currentTime);
  	EXTWGT.set("EXLMDT", currentDate);
  	EXTWGT.set("EXCHNO", 0);
  	EXTWGT.set("EXCHID", program.getUser());
  	
  	actionEXTWGT.insert(EXTWGT, recordExists);
  
  
  }
  
  
   Closure recordExists = {
	  mi.error("Record already exists");
  }
  
  
}
