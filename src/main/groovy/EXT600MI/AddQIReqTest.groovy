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
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 
 /*
 *Modification area - M3
 *Nbr            Date       User id     Description
 *QMS601         20210809   WZHAO       QMS601 IDM changes
 */
 
 /*
  * Add records to table EXTREQ
  */
public class AddQIReqTest extends ExtendM3Transaction {
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
  private String itno;
  private String faci;
  private String expi;
  private String mfdt;
  private String qrid;
  private String rorn;
  private int qsta;
  private String orty;
  private long dlix;
  private String whlo;
  private String camu;
  private String whsl;
  
  private List lstQITests_Range;
  private List lstQITests_Target;
  private List lstQITests_Quality;
  
  private List lstTestResults_Range;
  private List lstSortedTestResults_Range;
  private List lstTestResults_Target;
  private List lstSortedTestResults_Target;
  private List lstTestResults_Quality;
  private List lstSortedTestResults_Quality;
  
  private String COMMA_SEPERATED = "###,###.##";
  
  private int XXCONO;
 
  
  public AddQIReqTest(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
	if (orno.isEmpty() && bano.isEmpty()) {
	  mi.error("Order number or Lot number must be entered");
	  return;
	}
	whlo = "";
	whsl = "";
		camu = "";
		dlix = 0;
		orst = "";
		// get bano if it is not entered
		if (orno != "" && ponr != "") {
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
			if (bano != "") {
			  expression1 = expression1.and(expression1.eq("MQBANO", bano));
			}
			DBAction queryMITALO = database.table("MITALO").index("30").matching(expression1).selection("MQBANO","MQWHSL","MQCAMU").build();
		DBContainer MITALO = queryMITALO.getContainer();
			MITALO.set("MQCONO", XXCONO);
			MITALO.set("MQRIDI", dlix);
			queryMITALO.readAll(MITALO, 2, 1, listMITALO);
			logger.debug("MITALO.BANO=" + bano + " MITALO.CAMU=" + camu);
		  } else if (orst.toInteger() == 66 || orst.toInteger() == 77) {
			ExpressionFactory expression2 = database.getExpressionFactory("MITTRA");
			expression2 = expression2.eq("MTRIDI", dlix.toString());
			if (bano != "") {
			  expression2 = expression2.and(expression2.eq("MTBANO", bano));
			}
			DBAction queryMITTRA = database.table("MITTRA").index("30").matching(expression2).selection("MTBANO","MTWHSL","MTCAMU").build();
		DBContainer MITTRA = queryMITTRA.getContainer();
			MITTRA.set("MTCONO", XXCONO);
			MITTRA.set("MTTTYP", 31);
			MITTRA.set("MTRIDN", orno);
			MITTRA.set("MTRIDL", ponr.toInteger());
			queryMITTRA.readAll(MITTRA, 4, 1, listMITTRA);
			logger.debug("MITTRA.BANO=" + bano + " MITTRA.CAMU=" + camu);
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
		// - Get QRID from QMSRQH
		qrid = "";
		rorn = "";
		qsta = 0;
		DBAction queryQMSRQH = database.table("QMSRQH").index("20").selection("RHQRID", "RHRORN", "RHQSTA").build();
	DBContainer QMSRQH = queryQMSRQH.getContainer();
		QMSRQH.set("RHCONO", XXCONO);
		QMSRQH.set("RHFACI", faci);
		QMSRQH.set("RHITNO", itno);
		QMSRQH.set("RHBANO", bano);
		queryQMSRQH.readAll(QMSRQH, 4, 1, listQMSRQH);
		if (qrid == "") {
		  mi.error("QI Request does not exist in QMSRQH");
	  return;
		}
		if (qsta == 4) {
		  mi.error("QI Request status is 4, replaced QI requests are not considered.");
	  return;
		}
		// - Get ORTY from MWOHED
		orty = "";
		if (rorn != "") {
		  DBAction queryMWOHED = database.table("MWOHED").index("00").selection("VHORTY").build();
	  DBContainer MWOHED = queryMWOHED.getContainer();
		  MWOHED.set("VHCONO", XXCONO);
		  MWOHED.set("VHFACI", faci);
		  MWOHED.set("VHPRNO", itno);
		  MWOHED.set("VHMFNO", rorn);
		  if (queryMWOHED.read(MWOHED)) {
			orty = MWOHED.get("VHORTY").toString();
			//logger.debug("ORTY=" + orty);
		  }
		}
		// - Get QI Tests - Range, Target and Qualitative from QMSRQT
		lstQITests_Range = new ArrayList();
	lstQITests_Target = new ArrayList();
	lstQITests_Quality = new ArrayList();
	ExpressionFactory expression = database.getExpressionFactory("QMSRQT");
	  expression = expression.eq("RTPTCA", "1");
	  DBAction queryQMSRQT = database.table("QMSRQT").index("10").matching(expression)
	.selection("RTQTST", "RTTSTY", "RTTX40", "RTDCCD","RTFRTI","RTEVTG","RTSMSZ","RTTEUM","RTVLTP","RTQOP1","RTEVMN","RTEVMX").build();

		DBContainer QMSRQT = queryQMSRQT.createContainer();
		QMSRQT.set("RTCONO", XXCONO);
		QMSRQT.set("RTFACI", faci);
		QMSRQT.set("RTQRID", qrid);
		queryQMSRQT.readAll(QMSRQT, 3, listQMSRQT);
	logger.debug("Range.size=" + lstQITests_Range.size() + " Target.size=" + lstQITests_Target.size() + " Qualitative.size=" + lstQITests_Quality.size());
	if (lstQITests_Range.size() > 0 || lstQITests_Target.size() > 0 || lstQITests_Quality.size() > 0) {
	  deleteFromEXTREQ();
	  
	}
	if (lstQITests_Range.size() > 0) {
	  writeRangeTestResults();
	}
	if (lstQITests_Target.size() > 0) {
	  writeTargetTestResults();
	}
	if (lstQITests_Quality.size() > 0) {
	  writeQulityTestResults();
	}
  }
  /*
  * listMILOMA - Callback function to return MILOMA
  *
  */
  Closure<?> listMILOMA = { DBContainer MILOMA ->
	itno = MILOMA.get("LMITNO").toString().trim();
	faci = MILOMA.get("LMFACI").toString().trim();
	expi = MILOMA.get("LMEXPI").toString().trim();
	mfdt = MILOMA.get("LMMFDT").toString().trim();
	logger.debug("ITNO=" + itno + " FACI=" + faci);
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
	camu = MITALO.get("MQCAMU").toString().trim();
	whsl = MITALO.get("MQWHSL").toString().trim();
  }
  /* listMITTRA - Callback function to return MITTRA
  *
  */
  Closure<?> listMITTRA = { DBContainer MITTRA ->
	bano = MITTRA.get("MTBANO").toString().trim();
	camu = MITTRA.get("MTCAMU").toString().trim();
	whsl = MITTRA.get("MTWHSL").toString().trim();
  }
  /*
  * listQMSRQH - Callback function to return QMSRQH
  *
  */
  Closure<?> listQMSRQH = { DBContainer QMSRQH ->
	qrid = QMSRQH.get("RHQRID").toString().trim();
	rorn = QMSRQH.get("RHRORN").toString().trim();
	qsta = QMSRQH.get("RHQSTA");
	//logger.debug("QRID=" + qrid + " RORN=" + rorn);
  }
  /*
  * listQMSRQT - Callback function to return QMSRQT
  *
  */
   Closure<?> listQMSRQT = { DBContainer QMSRQT ->
	//"RTQTST", "RTTSTY", "RTTX40", "RTDCCD","RTFRTI","RTEVTG","RTSMSZ","RTTEUM","RTVLTP","","RTEVMN","RTEVMX"
	String qtst = QMSRQT.get("RTQTST").toString().trim();
	String tsty = QMSRQT.get("RTTSTY").toString().trim();
	String tx40 = QMSRQT.get("RTTX40").toString().trim();
	String dccd = QMSRQT.get("RTDCCD").toString().trim();
	String frti = QMSRQT.get("RTFRTI").toString().trim();
	String evtg = QMSRQT.get("RTEVTG").toString().trim();
	String smsz = QMSRQT.get("RTSMSZ").toString().trim();
	String teum = QMSRQT.get("RTTEUM").toString().trim();
	String vltp = QMSRQT.get("RTVLTP").toString().trim();
	String qop1 = QMSRQT.get("RTQOP1").toString().trim();
	String evmn = QMSRQT.get("RTEVMN").toString().trim();
	String evmx = QMSRQT.get("RTEVMX").toString().trim();
	def map = [QTST: qtst, TSTY: tsty, TX40: tx40, DCCD: dccd, FRTI: frti, EVTG: evtg, SMSZ: smsz, TEUM: teum, VLTP: vltp, QOP1: qop1, EVMN: evmn, EVMX: evmx];
	if (tsty.toInteger() == 2) {
	  lstQITests_Quality.add(map);
	} else if (vltp.toInteger() == 1) {
	  lstQITests_Range.add(map);
	} else if (vltp.toInteger() == 2) {
	  lstQITests_Target.add(map);
	}
  }
  /*
   * deleteFromEXTREQ - delete EXTREQ table
   *
   */
   def deleteFromEXTREQ() {
	DBAction queryEXTREQ = database.table("EXTREQ").index("00").selection("EXQRID", "EXQRID", "EXTSEQ").build();
	DBContainer EXTREQ = queryEXTREQ.getContainer();
		EXTREQ.set("EXCONO", XXCONO);
		EXTREQ.set("EXBANO", bano);
		EXTREQ.set("EXUSID", usid);
		queryEXTREQ.readAll(EXTREQ, 3, listEXTREQ);
   }
	/*
  * listEXTREQ - Callback function to return QMSRQT
  *
  */
   Closure<?> listEXTREQ = { DBContainer EXTREQ ->
	// - Delet exist record from EXTREQ
	DBAction ActionEXTREQ = database.table("EXTREQ").build();
	if (!ActionEXTREQ.readLock(EXTREQ, deleteEXTREQ)){
	  //mi.error("Record does not exists");
	}
   }
  /*
   * deleteEXTREQ - Callback function to delete EXTREQ table
   *
   */
  Closure deleteEXTREQ = { LockedResult EXTREQ ->
	EXTREQ.delete()
  }
  
  /*
	 * writeRangeTestResults
	 *
	 */
	def writeRangeTestResults() {
	  for (int i=0;i<lstQITests_Range.size();i++) {
		Map<String, String> record = (Map<String, String>) lstQITests_Range[i];
	  String qtst = record.QTST;
	  String tsty = record.TSTY;
	  String tx40 = record.TX40;
	  String dccd = record.DCCD;
	  String frti = record.FRTI;
	  String evtg = record.EVTG;
	  String smsz = record.SMSZ;
	  String teum = record.TEUM;
	  String vltp = record.VLTP;
	  String rtqop1 = record.QOP1;
	  String evmn = record.EVMN;
	  String evmx = record.EVMX;
	  
	  // - Get UOM from CSYTAB
	  String uomt = "";
	  if (teum != "") {
		  DBAction queryCSYTAB = database.table("CSYTAB").index("00").selection("CTTX15").build();
			  DBContainer CSYTAB = database.createContainer("CSYTAB");
		  CSYTAB.setInt("CTCONO", XXCONO);
		  CSYTAB.set("CTSTCO", "UNIT");
		  CSYTAB.set("CTSTKY", teum);
		  if ( queryCSYTAB.read(CSYTAB)){
			  uomt = CSYTAB.get("CTTX15").toString().trim();
		  }
	  }
	  String tcon = "";
	  String qlcd = "";
	  String qlc2 = "";
	  String maxrecs = "";
	  String spec = "";
	  BigDecimal bEVTG = new BigDecimal(evtg).setScale(2, RoundingMode.HALF_UP);
	  DecimalFormat decimalFormat = new DecimalFormat(COMMA_SEPERATED);
	  String strEVTG = decimalFormat.format(bEVTG.doubleValue());
	  if (!orty.equals("CHN")) {
		frti = "0";
		smsz = "0";
		maxrecs = 1;
		BigDecimal bEVMN = new BigDecimal(evmn).setScale(2, RoundingMode.HALF_UP);
		String strEVMN = decimalFormat.format(bEVMN.doubleValue());
		BigDecimal bEVMX = new BigDecimal(evmx).setScale(2, RoundingMode.HALF_UP);
		String strEVMX = decimalFormat.format(bEVMX.doubleValue());
		spec = strEVMN + " To " + strEVMX;
	  } else {
		maxrecs = 5;
		//spec = "n=" +  frti + "; c=0;m =" + evtg + ";M =" + evtg;
		spec = "n=" +  frti + "; c=0;m =" + strEVTG + ";M =" + strEVTG;
	  }
	  
	  lstTestResults_Range = new ArrayList();
		  
		  Map<String,String> headers = ["Accept": "application/json"]
	  Map<String,String> params = [ "FACI":faci, "QRID":qrid, "QTST":qtst, "TSTY":tsty, "maxrecs": maxrecs];
	  String url = "/M3/m3api-rest/v2/execute/MDBREADMI/LstQMSTRSU2";
	  
	  IonResponse response = ion.get(url, headers, params);
	  if (response.getStatusCode() == 200) {
		JsonSlurper jsonSlurper = new JsonSlurper();
		Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(response.getContent())
		ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
	   
		ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
		//logger.debug("LstQMSTRSU2: recordList:"+recordList.size());
		recordList.eachWithIndex { it, index ->
		  Map<String, String> recordQMSTRS = (Map<String, String>) it
		  lstTestResults_Range.add(recordQMSTRS);
		}
	  }
		  //logger.debug("lstTestResults_Range.size=" + lstTestResults_Range.size());
		  String result = "";
		  String firstQTRS = "0";
		  String firstCOND = "";
		  for (int j=0; j<lstTestResults_Range.size(); j++) {
			Map<String, String> record1 = (Map<String, String>) lstTestResults_Range[j];
			String tseq = record1.TSEQ;
		  String rrqop1 = record1.QOP1;
		  String qtrs = record1.QTRS;
		  //logger.debug("QTST=" + record1.RRQTST + " QTRS=" + qtrs + " RRQOP1=" + rrqop1);
		  String cond = "";
		  if (rrqop1.toInteger() == 1) {
			cond = ">";
		  } else if (rrqop1.toInteger() == 3) {
			cond = "<";
		  } else if (rrqop1.toInteger() == 5) {
			cond = " ";
		  } else {
			cond = "N/A";
		  }
		  boolean pass = false;
		  if (qtrs.toDouble() >= evmn.toDouble() &&  qtrs.toDouble() <= evmx.toDouble()) {
			pass = true;
		  }
		  if (pass) {
			if (j == 0) {
			  firstQTRS = qtrs;
			  firstCOND = cond;
			}
			  //writeEXTREQ(qtst, tseq, cond, qtrs, tx40, uomt, dccd, frti, evtg, smsz, tcon, evmn, evmx, qlcd, qlc2, result, "Range Tests");
			  BigDecimal bQTRS = new BigDecimal(qtrs).setScale(2, RoundingMode.HALF_UP);
			  String strQTRS = decimalFormat.format(bQTRS.doubleValue());
			  if (cond != "N/A") {
				result += cond + strQTRS;
			  } else {
				result += strQTRS;
			  }
			  if (j < (lstTestResults_Range.size()-1)) {
				result += "/";
			  }
		  }
		  }
		  writeEXTREQ(qtst, firstCOND, firstQTRS, tx40, uomt, dccd, frti, evtg, smsz, tcon, evmn, evmx, qlcd, qlc2, result, spec, "Range Tests");
	  }
	}
	/*
	 * writeTargetTestResults
	 *
	 */
	def writeTargetTestResults() {
	  
	  for (int i=0;i<lstQITests_Target.size();i++) {
		Map<String, String> record = (Map<String, String>) lstQITests_Target[i];
	  String qtst = record.QTST;
	  String tsty = record.TSTY;
	  String tx40 = record.TX40;
	  String dccd = record.DCCD;
	  String frti = record.FRTI;
	  String evtg = record.EVTG;
	  String smsz = record.SMSZ;
	  String teum = record.TEUM;
	  String vltp = record.VLTP;
	  String rtqop1 = record.QOP1;
	  String evmn = record.EVMN;
	  String evmx = record.EVMX;
	  
	  // - Get UOM from CSYTAB
	  String uomt = "";
	  if (teum != "") {
		  DBAction queryCSYTAB = database.table("CSYTAB").index("00").selection("CTTX15").build();
			  DBContainer CSYTAB = database.createContainer("CSYTAB");
		  CSYTAB.setInt("CTCONO", XXCONO);
		  CSYTAB.set("CTSTCO", "UNIT");
		  CSYTAB.set("CTSTKY", teum);
		  if ( queryCSYTAB.read(CSYTAB)){
			  uomt = CSYTAB.get("CTTX15").toString().trim();
		  }
	  }
	  String tcon = "";
	  String qlcd = "";
	  String qlc2 = "";
	  String maxrecs = "";
	  String spec = "";
	  BigDecimal bEVTG = new BigDecimal(evtg).setScale(2, RoundingMode.HALF_UP);
	  DecimalFormat decimalFormat = new DecimalFormat(COMMA_SEPERATED);
	  String strEVTG = decimalFormat.format(bEVTG.doubleValue());
	  if (!orty.equals("CHN")) {
		if (rtqop1.toInteger() == 1) {
		  tcon = ">";
		} else if (rtqop1.toInteger() == 2) {
		  tcon = ">=";
		} else if (rtqop1.toInteger() == 3) {
		  tcon = "<";
		} else if (rtqop1.toInteger() == 4) {
		  tcon = "<=";
		} else if (rtqop1.toInteger() == 5) {
		  tcon = " ";
		} else {
		  tcon = "N/A";
		}
		frti = "0";
		smsz = "0";
		maxrecs = 1;
		//spec = tcon + bEVTG;
		spec = tcon + strEVTG;
	  } else {
		maxrecs = 5;
		//spec = "n=" +  frti + "; c=0;m =" + evtg + ";M =" + evtg;
		spec = "n=" +  frti + "; c=0;m =" + strEVTG + ";M =" + strEVTG;
	  }
	  
	  lstTestResults_Target = new ArrayList();
		  
		  Map<String,String> headers = ["Accept": "application/json"]
	  Map<String,String> params = [ "FACI":faci, "QRID":qrid, "QTST":qtst, "TSTY":tsty, "maxrecs": maxrecs];
	  String url = "/M3/m3api-rest/v2/execute/MDBREADMI/LstQMSTRSU2";
	  
	  IonResponse response = ion.get(url, headers, params);
	  if (response.getStatusCode() == 200) {
		JsonSlurper jsonSlurper = new JsonSlurper();
		Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(response.getContent())
		ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
	   
		ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
		//logger.debug("LstQMSTRSU2: recordList:"+recordList.size());
		recordList.eachWithIndex { it, index ->
		  Map<String, String> recordQMSTRS = (Map<String, String>) it
		  lstTestResults_Target.add(recordQMSTRS);
		}
	  }
		  //logger.debug("lstTestResults_Target.size=" + lstTestResults_Target.size());
		  String result = "";
		  String firstQTRS = "0";
		  String firstCOND = "";
		  for (int j=0; j<lstTestResults_Target.size(); j++) {
			Map<String, String> record1 = (Map<String, String>) lstTestResults_Target[j];
			String tseq = record1.TSEQ;
		  String rrqop1 = record1.QOP1;
		  String qtrs = record1.QTRS;
		  //logger.debug("QTST=" + record1.RRQTST + " QTRS=" + qtrs + " RRQOP1=" + rrqop1);
		  String cond = "";
		  if (rrqop1.toInteger() == 1) {
			cond = ">";
		  } else if (rrqop1.toInteger() == 3) {
			cond = "<";
		  } else if (rrqop1.toInteger() == 5) {
			cond = " ";
		  } else {
			cond = "N/A";
		  }
		  boolean pass = testingResult(rtqop1, qtrs, evtg, rrqop1);
		  if (pass) {
			if (j == 0) {
			  firstQTRS = qtrs;
			  firstCOND = cond;
			}
			  //writeEXTREQ(qtst, tseq, cond, qtrs, tx40, uomt, dccd, frti, evtg, smsz, tcon, evmn, evmx, qlcd, qlc2, "Target Tests");
			  BigDecimal bQTRS = new BigDecimal(qtrs).setScale(2, RoundingMode.HALF_UP);
			  String strQTRS = decimalFormat.format(bQTRS.doubleValue());
			  if (cond != "N/A") {
				result += cond + strQTRS;
			  } else {
				result += strQTRS;
			  }
			  if (j < (lstTestResults_Target.size()-1)) {
				result += "/";
			  }
		  }
		  }
		  writeEXTREQ(qtst, firstCOND, firstQTRS, tx40, uomt, dccd, frti, evtg, smsz, tcon, evmn, evmx, qlcd, qlc2, result, spec, "Target Tests");
	  }
	 
	}
	/*
	 * writeQulityTestResults
	 *
	 */
	def writeQulityTestResults() {
	  for (int i=0;i<lstQITests_Quality.size();i++) {
		Map<String, String> record = (Map<String, String>) lstQITests_Quality[i];
	  String qtst = record.QTST;
	  String tsty = record.TSTY;
	  String tx40 = record.TX40;
	  String dccd = record.DCCD;
	  String frti = record.FRTI;
	  String evtg = record.EVTG;
	  String smsz = record.SMSZ;
	  String teum = record.TEUM;
	  String vltp = record.VLTP;
	  String rtqop1 = record.QOP1;
	  String evmn = record.EVMN;
	  String evmx = record.EVMX;
	  
	  // - Get UOM from CSYTAB
	  String uomt = "";
	  if (teum != "") {
		  DBAction queryCSYTAB = database.table("CSYTAB").index("00").selection("CTTX15").build();
			  DBContainer CSYTAB = database.createContainer("CSYTAB");
		  CSYTAB.setInt("CTCONO", XXCONO);
		  CSYTAB.set("CTSTCO", "UNIT");
		  CSYTAB.set("CTSTKY", teum);
		  if ( queryCSYTAB.read(CSYTAB)){
			  uomt = CSYTAB.get("CTTX15").toString().trim();
		  }
	  }
	  String tcon = "";
	  frti = "0";
	  smsz = "0";
	  evmn = "0";
	  evmx = "0";
	  String qtrs = "0";
	  String maxrecs = "";
	  String transaction = "";
	  if (orty.equals("CHN")) {
		maxrecs = 5;
		transaction = "LstQMSTRSU3";
	  } else {
		maxrecs = 1;
		transaction = "LstQMSTRSU4";
	  }
	  
	  lstTestResults_Quality = new ArrayList();
		  
		  Map<String,String> headers = ["Accept": "application/json"]
	  Map<String,String> params = [ "FACI":faci, "QRID":qrid, "QTST":qtst, "TSTY":tsty, "maxrecs": maxrecs];
	  String url = "/M3/m3api-rest/v2/execute/MDBREADMI/" + transaction;
	  
	  IonResponse response = ion.get(url, headers, params);
	  if (response.getStatusCode() == 200) {
		JsonSlurper jsonSlurper = new JsonSlurper();
		Map<String, Object> miResponse = (Map<String, Object>) jsonSlurper.parseText(response.getContent())
		ArrayList<Map<String, Object>> results = (ArrayList<Map<String, Object>>) miResponse.get("results");
	   
		ArrayList<Map<String, String>> recordList = (ArrayList<Map<String, String>>) results[0]["records"];
		//logger.debug("LstQMSTRSU2: recordList:"+recordList.size());
		recordList.eachWithIndex { it, index ->
		  Map<String, String> recordQMSTRS = (Map<String, String>) it
		  lstTestResults_Quality.add(recordQMSTRS);
		}
	  }
		  //logger.debug("lstTestResults_Quality.size=" + lstTestResults_Quality.size());
		  String result = "";
		  String firstCOND = "";
		  String firstQLCD = "";
		  String qlc2 = "Allowable value";
		  for (int j=0; j<lstTestResults_Quality.size(); j++) {
			Map<String, String> record1 = (Map<String, String>) lstTestResults_Quality[j];
			String tseq = record1.TSEQ;
		  String rrqop1 = record1.QOP1;
		  String qlcd = record1.QLCD;
		  logger.debug("QTST=" + record1.QTST + " QLCD=" + qlcd + " RRQOP1=" + rrqop1);
		  String cond = "";
		  if (rrqop1.toInteger() == 1) {
			cond = ">";
		  } else if (rrqop1.toInteger() == 3) {
			cond = "<";
		  } else if (rrqop1.toInteger() == 5) {
			cond = " ";
		  } else {
			cond = "N/A";
		  }
		  if (j == 0) {
			  firstQLCD = qlcd;
		   }
			//writeEXTREQ(qtst, tseq, cond, qtrs, tx40, uomt, dccd, frti, evtg, smsz, tcon, evmn, evmx, qlcd, qlc2, result, "Quality Tests");
		  }
		  result = firstQLCD;
		  String spec = result;
		  writeEXTREQ(qtst, firstCOND, "0", tx40, uomt, dccd, frti, evtg, smsz, tcon, evmn, evmx, firstQLCD, qlc2, result, spec, "Quality Tests");
	  }
	}
	 
   /*
	 * testingResult
	 *
	 */
	def boolean testingResult(String rtqop1, String qtrs, String evtg, String rrqop1) {
	   if (rtqop1.toInteger() == 3) {
		 if (qtrs.toDouble() < evtg.toDouble()) {
		   return true;
		 }
		 if (qtrs.toDouble() == evtg.toDouble() && rrqop1.toInteger() == 3) {
		   return true;
		 }
	   }
	   if (rtqop1.toInteger() == 5) {
		 if (qtrs.toDouble() == evtg.toDouble() && rrqop1.toInteger() == 5) {
		   return true;
		 }
	   }
	   if (rtqop1.toInteger() == 4) {
		 if (qtrs.toDouble() < evtg.toDouble()) {
		   return true;
		 }
		 if (qtrs.toDouble() == evtg.toDouble() && (rrqop1.toInteger() == 3 || rrqop1.toInteger() == 4 || rrqop1.toInteger() == 5)) {
		   return true;
		 }
	   }
	   if (rtqop1.toInteger() == 2) {
		 if (qtrs.toDouble() > evtg.toDouble()) {
		   return true;
		 }
		 if (qtrs.toDouble() == evtg.toDouble() && (rrqop1.toInteger() == 1 || rrqop1.toInteger() == 2 || rrqop1.toInteger() == 5)) {
		   return true;
		 }
	   }
	   if (rtqop1.toInteger() == 1) {
		 if (qtrs.toDouble() > evtg.toDouble()) {
		   return true;
		 }
		 if (qtrs.toDouble() == evtg.toDouble() && (rrqop1.toInteger() == 1 || rrqop1.toInteger() == 2)) {
		   return true;
		 }
	   }
	   return false;
	 }
   /*
	 * writeEXTREQ
	 *
	 */
	def writeEXTREQ(String qtst, String cond, String qtrs, String tx40, String uomt, String dccd, String frti, String evtg, String smsz, String tcon, String evmn, String evmx, String qlcd, String qlc2, String result, String spec, String testType) {
	  //Current date and time
	  int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
	  int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
	  
	  DBAction ActionEXTREQ = database.table("EXTREQ").build();
	  DBContainer EXTREQ = ActionEXTREQ.getContainer();
	  // QI Test information
	  EXTREQ.set("EXCONO", XXCONO);
	  EXTREQ.set("EXBANO", bano);
	  EXTREQ.set("EXUSID", usid);
	  EXTREQ.set("EXQRID", qrid);
	  EXTREQ.set("EXQTST", qtst);
	  
	  EXTREQ.set("EXITNO", itno);
	  EXTREQ.set("EXORTY", orty);
	  EXTREQ.set("EXEXPI", expi.toInteger());
	  EXTREQ.set("EXMFDT", mfdt.toInteger());
	  
	  // Test results
	  EXTREQ.set("EXCOND", cond);
	  EXTREQ.set("EXQTRS", qtrs.toDouble());
	  
	  EXTREQ.set("EXTSTX", tx40);
	  EXTREQ.set("EXUOMT", uomt);
	  EXTREQ.set("EXDCCD", dccd.toInteger());
	  EXTREQ.set("EXFRTI", frti.toInteger());
	  EXTREQ.set("EXEVTG", evtg.toDouble());
	  EXTREQ.set("EXSMSZ", smsz.toDouble());
	  EXTREQ.set("EXTCON", tcon);
	  
	  if (testType == "Range Tests") {
		EXTREQ.set("EXEVMN", evmn.toDouble());
		EXTREQ.set("EXEVMX", evmx.toDouble());
	  }
	  if (testType == "Quality Tests") {
		EXTREQ.set("EXQLCD", qlcd);
		EXTREQ.set("EXQLC2", qlc2);
	  }
	  EXTREQ.set("EXREST", result);
	  EXTREQ.set("EXSPEC", spec);
	  EXTREQ.set("EXTSTY", testType);
	  EXTREQ.set("EXCAMU", camu);
	  EXTREQ.set("EXWHSL", whsl);
	  EXTREQ.set("EXRGDT", currentDate);
	  EXTREQ.set("EXRGTM", currentTime);
	  EXTREQ.set("EXLMDT", currentDate);
	  EXTREQ.set("EXCHNO", 0);
	  EXTREQ.set("EXCHID", program.getUser());
	  
	  ActionEXTREQ.insert(EXTREQ, recordExists);
	}
	 /*
  /*
   * recordExists - return record already exists error message to the MI
   *
   */
  Closure recordExists = {
	  mi.error("Record already exists");
  }
	
}