
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
 *ABF_R_200         20220405  RDRIESSEN   Mods BF0200- Write/Update EXTAPR records as a basis for PO authorization process
 *
 */

 import groovy.lang.Closure
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;



public class Add extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String cono;
  private String puno;
  private String appr;
  private String asts;
   private int XXCONO;
 
  public Add(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	  
  }
  
  public void main() {
    
  	puno = mi.inData.get("PUNO") == null ? '' : mi.inData.get("PUNO").trim();
  	if (puno == "?") {
  	  puno = "";
  	} 
  	appr = mi.inData.get("APPR") == null ? '' : mi.inData.get("APPR").trim();
  	if (appr == "?") {
  	  appr = "";
  	} 
  	asts = mi.inData.get("ASTS") == null ? '' : mi.inData.get("ASTS").trim();
  	if (asts == "?") {
  	  asts = "";
  	} 
    
    
    
		if (puno.isEmpty()) {
      mi.error("Purchase Order must be entered");
      return;
    }
    if (appr.isEmpty()) {
      mi.error("Approver must be entered");
      return;
    }
    
     writeEXTAPR(puno, appr, asts);
    
  }
  
  
  def writeEXTAPR(String puno, String appr, String asts) {
	  //Current date and time
  	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
  	  int currentCompany = (Integer)program.getLDAZD().CONO
  	
	  DBAction ActionEXTAPR = database.table("EXTAPR").build();
  	DBContainer EXTAPR = ActionEXTAPR.getContainer();
  	// QI Test information
  	EXTAPR.set("EXCONO", currentCompany);
  	EXTAPR.set("EXPUNO", puno);
  	EXTAPR.set("EXAPPR", appr);
  	EXTAPR.set("EXASTS", asts);
  	EXTAPR.set("EXRGDT", currentDate);
  	EXTAPR.set("EXRGTM", currentTime);
  	EXTAPR.set("EXLMDT", currentDate);
  	EXTAPR.set("EXCHNO", 0);
  	EXTAPR.set("EXCHID", program.getUser());
  	
  	ActionEXTAPR.insert(EXTAPR, recordExists);
	}
  
  
  Closure recordExists = {
	  mi.error("Record already exists");
  }
  
  
}
