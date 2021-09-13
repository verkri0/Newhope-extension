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
 
 
 /*
 *Modification area - M3
 *Nbr            Date       User id     Description
 *QMS601         20210809   WZHAO       QMS601 IDM changes
 */
 
 /*
  * Add records to table EXTTXL
  */
public class AddQIReqTexts extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String cono;
  private String bano;
  private String usid;
  private String orno;
  private String ponr;
  private String posx;
  private String orst;
  private long dlix;
  private String itno;
  private String faci;
  private String qtst1;
  private String qtst2;
  private String qtst3;
  private String qrid1;
  private String qrid2;
  private String qrid3;
  private String qlcd1;
  private String qlcd2;
  private String qlcd3;
  private String txid1;
  private String txid2;
  private String txid3;
  private String texts1;
  private String texts2;
  private String texts3;
  private int number;
  
  private int XXCONO;
 
  
  public AddQIReqTexts(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
	  this.mi = mi;
	  this.database = database;
	  this.miCaller = miCaller;
	  this.logger = logger;
	  this.program = program;
	  this.ion = ion;
  }
  
  public void main() {
	  //Fetch input fields from MI
	  cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
	  if (cono == "?") {
		cono = "";
	  }
	  bano = mi.inData.get("BANO") == null ? '' : mi.inData.get("BANO").trim();
	  if (bano == "?") {
		bano = "";
	  }
	  usid = mi.inData.get("USID") == null ? '' : mi.inData.get("USID").trim();
	  if (usid == "?") {
		usid = "";
	  }
	  orno = mi.inData.get("ORNO") == null ? '' : mi.inData.get("ORNO").trim();
	  if (orno == "?") {
		orno = "";
	  }
	  ponr = mi.inData.get("PONR") == null ? '' : mi.inData.get("PONR").trim();
	  if (ponr == "?") {
		ponr = "";
	  }
	  posx = mi.inData.get("POSX") == null ? '' : mi.inData.get("POSX").trim();
	  if (posx == "?") {
		posx = "";
	  }
	  if (posx == "") {
		posx = "0";
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
	if (usid.isEmpty()) {
	  mi.error("User ID must be entered");
	  return;
	}
	dlix = 0;
		orst = "";
	if (bano == "" && orno != "" && ponr != "") {
		  DBAction queryOOLINE = database.table("OOLINE").index("00").selection("OBORST").build();
	  DBContainer OOLINE = queryOOLINE.getContainer();
		  OOLINE.set("OBCONO", XXCONO);
		  OOLINE.set("OBORNO", orno);
		  OOLINE.set("OBPONR", ponr.toInteger());
		  OOLINE.set("OBPOSX", posx.toInteger());
		  if (queryOOLINE.read(OOLINE)) {
			orst = OOLINE.get("OBORST").toString();
			//logger.debug(("ORST=" + orst);
		  }
		  
		  DBAction queryMHDISL = database.table("MHDISL").index("10").selection("URDLIX").build();
	  DBContainer MHDISL = queryMHDISL.getContainer();
		  MHDISL.set("URCONO", XXCONO);
		  MHDISL.set("URRORC", 3);
		  MHDISL.set("URRIDN", orno);
		  MHDISL.set("URRIDL", ponr.toInteger());
		  queryMHDISL.readAll(MHDISL, 4, 1, listMHDISL);
		  
		  if (orst.toInteger() >= 23 && orst.toInteger() <= 64) {
			ExpressionFactory expression1 = database.getExpressionFactory("MITALO");
			expression1 = expression1.eq("MQRIDL", ponr);
			
			DBAction queryMITALO = database.table("MITALO").index("30").matching(expression1).selection("MQBANO","MQWHSL","MQCAMU").build();
		DBContainer MITALO = queryMITALO.getContainer();
			MITALO.set("MQCONO", XXCONO);
			MITALO.set("MQRIDI", dlix);
			queryMITALO.readAll(MITALO, 2, 1, listMITALO);
			logger.debug("MITALO.BANO=" + bano);
		  } else if (orst.toInteger() == 66 || orst.toInteger() == 77) {
			ExpressionFactory expression2 = database.getExpressionFactory("MITTRA");
			expression2 = expression2.eq("MTRIDI", dlix.toString());
			DBAction queryMITTRA = database.table("MITTRA").index("30").matching(expression2).selection("MTBANO","MTWHSL","MTCAMU").build();
		DBContainer MITTRA = queryMITTRA.getContainer();
			MITTRA.set("MTCONO", XXCONO);
			MITTRA.set("MTTTYP", 31);
			MITTRA.set("MTRIDN", orno);
			MITTRA.set("MTRIDL", ponr.toInteger());
			queryMITTRA.readAll(MITTRA, 4, 1, listMITTRA);
			logger.debug("MITTRA.BANO=" + bano);
		  }
		}
	// - Validate bano
	itno = "";
	faci = "";
	DBAction queryMILOMA = database.table("MILOMA").index("10").selection("LMITNO","LMFACI","LMEXPI","LMMFDT").build();
	DBContainer MILOMA = queryMILOMA.getContainer();
		MILOMA.set("LMCONO", XXCONO);
		MILOMA.set("LMBANO", bano);
		queryMILOMA.readAll(MILOMA, 2, 1, listMILOMA);
		if (itno == "") {
		  mi.error("Lot number does not exist in MILOMA.");
	  return;
		}
		// - Get QI Tests - Range, Target and Qualitative from QMSRQT
		qtst1 = "";
		qtst2 = "";
		qtst3 = "";
		qrid1 = "";
		qrid2 = "";
		qrid3 = "";
		number = 0;
	ExpressionFactory expression = database.getExpressionFactory("QMSRQT");
	  expression = expression.eq("RTLABO", "STATEMENT");
	  //expression = expression.eq("RTQLCD", "PASS");
	  DBAction queryQMSRQT = database.table("QMSRQT").index("20").matching(expression).selection("RTQRID","RTQTST").build();

		DBContainer QMSRQT = queryQMSRQT.createContainer();
		QMSRQT.set("RTCONO", XXCONO);
		QMSRQT.set("RTFACI", faci);
		QMSRQT.set("RTITNO", itno);
		QMSRQT.set("RTBANO", bano);
		queryQMSRQT.readAll(QMSRQT, 4, 3, listQMSRQT);
		
		qlcd1 = "";
		DBAction queryQMSTRS = database.table("QMSTRS").index("00").selection("RRQLCD").build();
	DBContainer QMSTRS = queryQMSTRS.getContainer();
		QMSTRS.set("RRCONO", XXCONO);
		QMSTRS.set("RRFACI", faci);
		QMSTRS.set("RRQRID", qrid1);
		QMSTRS.set("RRQTST", qtst1);
		queryQMSTRS.readAll(QMSTRS, 4, 1, listQMSTRS1);
		
	if (!qlcd1.equals("PASS")) {
	  qtst1 = "";
	}
	
		qlcd2 = "";
		QMSTRS.set("RRQRID", qrid2);
		QMSTRS.set("RRQTST", qtst2);
		queryQMSTRS.readAll(QMSTRS, 4, 1, listQMSTRS2);
		
		if (!qlcd2.equals("PASS")) {
	  qtst2 = "";
	}
		qlcd3 = "";
		QMSTRS.set("RRQRID", qrid3);
		QMSTRS.set("RRQTST", qtst3);
		queryQMSTRS.readAll(QMSTRS, 4, 1, listQMSTRS3);
		
		if (!qlcd3.equals("PASS")) {
	  qtst3 = "";
	}
		// Get TXID from test template table
	  txid1 = "";
	  txid2 = "";
	  txid3 = "";
		DBAction queryQMSTTP = database.table("QMSTTP").index("00").selection("QTTXID").build();
	DBContainer QMSTTP = queryQMSTTP.getContainer();
		QMSTTP.set("QTCONO", XXCONO);
		if (qtst1 != "") {
		  QMSTTP.set("QTQTST", qtst1);
		  queryQMSTTP.readAll(QMSTTP, 2, 1, listQMSTTP1);
		}
		if (qtst2 != "") {
		  QMSTTP.set("QTQTST", qtst2);
		  queryQMSTTP.readAll(QMSTTP, 2, 1, listQMSTTP2);
		}
		if (qtst3 != "") {
		  QMSTTP.set("QTQTST", qtst3);
		  queryQMSTTP.readAll(QMSTTP, 2, 1, listQMSTTP3);
		}
	//if ((txid1 != "" && txid1.toDouble() != 0) || (txid2 != "" && txid2.toDouble() != 0) || (txid3 != "" && txid3.toDouble() != 0)) {
	  DBAction ActionEXTTXL = database.table("EXTTXL").index("00").build();
	  DBContainer EXTTXL = ActionEXTTXL.getContainer();
		  EXTTXL.set("EXCONO", XXCONO);
		  EXTTXL.set("EXBANO", bano);
		  EXTTXL.set("EXUSID", usid);
		  if (ActionEXTTXL.read(EXTTXL)) {
			ActionEXTTXL.readLock(EXTTXL, deleteEXTTXL);
		  }
	//}
	texts1 = "";
	DBAction queryMSYTXL = database.table("MSYTXL").index("00").selection("TLTXID", "TLLINO", "TLTX60").build();
	DBContainer MSYTXL = queryMSYTXL.getContainer();
		MSYTXL.set("TLCONO", XXCONO);
		MSYTXL.set("TLDIVI", "");
	  if (txid1 != "" && txid1.toDouble() != 0) {
		  MSYTXL.set("TLTXID", txid1.toLong());
		  queryMSYTXL.readAll(MSYTXL, 3, listMSYTXL1);
	}
	texts2 = "";
	  if (txid2 != "" && txid2.toDouble() != 0) {
		  MSYTXL.set("TLTXID", txid2.toLong());
		  queryMSYTXL.readAll(MSYTXL, 3, listMSYTXL2);
	}
	texts3 = "";
	  if (txid3 != "" && txid3.toDouble() != 0) {
		  MSYTXL.set("TLTXID", txid3.toLong());
		  queryMSYTXL.readAll(MSYTXL, 3, listMSYTXL3);
	}
		writeEXTTXL(texts1, texts2, texts3);
  }
  /*
  * listMHDISL - Callback function to return MHDISL
  *
  */
  Closure<?> listMHDISL = { DBContainer MHDISL ->
	dlix = MHDISL.get("URDLIX");
  }
  /*
  * listMITALO - Callback function to return MITALO
  *
  */
  Closure<?> listMITALO = { DBContainer MITALO ->
	bano = MITALO.get("MQBANO").toString().trim();
  }
  /* listMITTRA - Callback function to return MITTRA
  *
  */
  Closure<?> listMITTRA = { DBContainer MITTRA ->
	bano = MITTRA.get("MTBANO").toString().trim();
  }
  /*
  * listMILOMA - Callback function to return MILOMA
  *
  */
  Closure<?> listMILOMA = { DBContainer MILOMA ->
	itno = MILOMA.get("LMITNO").toString().trim();
	faci = MILOMA.get("LMFACI").toString().trim();
	//logger.debug("ITNO=" + itno);
  }
  /*
  * listQMSRQT - Callback function to return QMSRQT
  *
  */
  Closure<?> listQMSRQT = { DBContainer QMSRQT ->
	if (number == 0) {
	  number++;
	  qrid1 = QMSRQT.get("RTQRID").toString().trim();
	  qtst1 = QMSRQT.get("RTQTST").toString().trim();
	  logger.debug("QTST1=" + qtst1);
	} else if (number == 1) {
	  number++;
	  qrid2 = QMSRQT.get("RTQRID").toString().trim();
	  qtst2 = QMSRQT.get("RTQTST").toString().trim();
	  logger.debug("QTST2=" + qtst2);
	} else if (number == 2) {
	  number++;
	  qrid3 = QMSRQT.get("RTQRID").toString().trim();
	  qtst3 = QMSRQT.get("RTQTST").toString().trim();
	  logger.debug("QTST3=" + qtst3);
	} else {
	  return;
	}
  }
  /*
  * listQMSTRS1 - Callback function to return QMSTRS
  *
  */
  Closure<?> listQMSTRS1 = { DBContainer QMSTRS ->
	qlcd1 = QMSTRS.get("RRQLCD").toString().trim();
	logger.debug("QLCD1=" + qlcd1);
  }
  /*
  * listQMSTRS2 - Callback function to return QMSTRS
  *
  */
  Closure<?> listQMSTRS2 = { DBContainer QMSTRS ->
	qlcd2 = QMSTRS.get("RRQLCD").toString().trim();
	logger.debug("QLCD2=" + qlcd2);
  }
  /*
  * listQMSTRS3 - Callback function to return QMSTRS
  *
  */
  Closure<?> listQMSTRS3 = { DBContainer QMSTRS ->
	qlcd3 = QMSTRS.get("RRQLCD").toString().trim();
	logger.debug("QLCD3=" + qlcd3);
  }
  /*
  * listQMSTTP1 - Callback function to return QMSTTP
  *
  */
  Closure<?> listQMSTTP1 = { DBContainer QMSTTP ->
	txid1 = QMSTTP.get("QTTXID").toString().trim();
	logger.debug("TXID1=" + txid1);
  }
  /*
  * listQMSTTP2 - Callback function to return QMSTTP
  *
  */
  Closure<?> listQMSTTP2 = { DBContainer QMSTTP ->
	txid2 = QMSTTP.get("QTTXID").toString().trim();
	logger.debug("TXID2=" + txid2);
  }
  /*
  * listQMSTTP3 - Callback function to return QMSTTP
  *
  */
  Closure<?> listQMSTTP3 = { DBContainer QMSTTP ->
	txid3 = QMSTTP.get("QTTXID").toString().trim();
	logger.debug("TXID3=" + txid3);
  }
  /*
   * deleteEXTTXL - Callback function to delete EXTTXL table
   *
   */
  Closure deleteEXTTXL = { LockedResult EXTTXL ->
	   EXTTXL.delete();
	 
	}
  /*
  * listMSYTXL1 - Callback function to return MSYTXL
  *
  */
  Closure<?> listMSYTXL1 = { DBContainer MSYTXL ->
	String lino = MSYTXL.get("TLLINO").toString().trim();
	String tx60 = MSYTXL.get("TLTX60").toString().trim();
	logger.debug("LINO=" + lino + " TX60=" + tx60);
	texts1 += tx60 + " ";
  }
  Closure<?> listMSYTXL2 = { DBContainer MSYTXL ->
	String lino = MSYTXL.get("TLLINO").toString().trim();
	String tx60 = MSYTXL.get("TLTX60").toString().trim();
	logger.debug("LINO=" + lino + " TX60=" + tx60);
	texts2 += tx60 + " ";
  }
  Closure<?> listMSYTXL3 = { DBContainer MSYTXL ->
	String lino = MSYTXL.get("TLLINO").toString().trim();
	String tx60 = MSYTXL.get("TLTX60").toString().trim();
	logger.debug("LINO=" + lino + " TX60=" + tx60);
	texts3 += tx60 + " ";
  }
  /*
	 * writeEXTTXL
	 *
	 */
	def writeEXTTXL(String tx60, String tx61, String tx62) {
	  int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
	  int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
	  
	  DBAction ActionEXTTXL = database.table("EXTTXL").build();
	  DBContainer EXTTXL = ActionEXTTXL.getContainer();
	  // QI Test information
	  EXTTXL.set("EXCONO", XXCONO);
	  EXTTXL.set("EXBANO", bano);
	  EXTTXL.set("EXUSID", usid);
	  EXTTXL.set("EXQRID", qrid1);
	  EXTTXL.set("EXQTST", qtst1);
	  //EXTTXL.set("EXTXID", txid1.toLong());
	  //EXTTXL.set("EXLINO", 1);
	  EXTTXL.set("EXTX60", tx60);
	  EXTTXL.set("EXTX61", tx61);
	  EXTTXL.set("EXTX62", tx62);
	  EXTTXL.set("EXRGDT", currentDate);
	  EXTTXL.set("EXRGTM", currentTime);
	  EXTTXL.set("EXLMDT", currentDate);
	  EXTTXL.set("EXCHNO", 0);
	  EXTTXL.set("EXCHID", program.getUser());
	  
	  ActionEXTTXL.insert(EXTTXL, recordExists);
	}
	 /*
   * recordExists - return record already exists error message to the MI
   *
   */
  Closure recordExists = {
	  mi.error("Record already exists");
  }
}