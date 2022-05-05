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
 
 
 /*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *BF_R_1072         20220325  XWZHAO      Sunbeam payment schedule update
 *
 */
 
 /*
  * AP paymnent split night run
 */
public class AddAPSpltNgtRun extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  private String divi;
  private String xnow;
  
  private int XXCONO;
  
  public AddAPSpltNgtRun(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.miCaller = miCaller;
    this.logger = logger;
    this.program = program;
  }
  
  public void main() {
    divi = mi.inData.get("DIVI") == null ? '' : mi.inData.get("DIVI").trim();
  	if (divi == "?") {
  	  divi = "";
  	} 
  	xnow = mi.inData.get("XNOW") == null ? '' : mi.inData.get("XNOW").trim();
  	if (xnow == "?") {
  	  xnow = "";
  	}
  	if (divi.isEmpty()) {
      mi.error("Division must be entered");
      return;
    }
    XXCONO= program.LDAZD.CONO;
  	String referenceId = UUID.randomUUID().toString();
    setupData(referenceId);
    if (xnow.equals("1")) {
      def params = ["JOB": "EXT841", "TX30": "AP Split NightRun", "XCAT": "010", "SCTY": "1", "XNOW": "1", "UUID": referenceId]; // ingle run - now
      miCaller.call("SHS010MI", "SchedXM3Job", params, { result -> })
    } else {
      def params = ["JOB": "EXT841", "TX30": "AP Split NightRun", "XCAT": "010", "SCTY": "2", "XNOW": "", "XEMO": "1", "XETU": "1", "XEWE": "1", "XETH": "1", "XEFR": "1", "XESA": "1", "XESU": "1","XJTM": "220000", "UUID": referenceId]; // run every night
      miCaller.call("SHS010MI", "SchedXM3Job", params, { result -> })
    }
  }
  /*
	 * setupData  - write to EXTJOB
	 *
	 */
  private void setupData(String referenceId) {
    String data = "";
    if (!divi.isEmpty()) {
      data = divi;
    }
    int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
    DBAction ActionEXTJOB = database.table("EXTJOB").build();
  	DBContainer EXTJOB = ActionEXTJOB.getContainer();
  	EXTJOB.set("EXCONO", XXCONO);
  	EXTJOB.set("EXRFID", referenceId);
  	EXTJOB.set("EXDATA", data);
    EXTJOB.set("EXRGDT", currentDate);
  	EXTJOB.set("EXRGTM", currentTime);
  	EXTJOB.set("EXLMDT", currentDate);
  	EXTJOB.set("EXCHNO", 0);
  	EXTJOB.set("EXCHID", program.getUser());
    ActionEXTJOB.insert(EXTJOB);
  }
}
