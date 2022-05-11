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
 *ABF_R_200         20220405  RDRIESSEN   Mods BF0200- Update EXTAPR records as a basis for PO authorization process
 *ABF_R_200         20220511  KVERCO      Update for XtendM3 review feedback
 *
 */

 import groovy.lang.Closure

 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import java.time.ZoneId;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;


 /*
  * Write/Update Purchase Authorisation extension table
 */
  public class Update extends ExtendM3Transaction {
    
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

  private String puno1;
  private String appr1;
  private String asts1;
  private String YYCONO;
  private String YYUSID;
  private int YYLMDT;
  private int chno;
   
  
  public Update(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
   
  }
  
  public void main() {
    
     
  	//cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();

  	puno = mi.inData.get("PUNO") == null ? '' : mi.inData.get("PUNO").trim();
  	if (puno == "?") {
  	  puno = "";
  	} 
  	
  	asts = mi.inData.get("ASTS") == null ? '' : mi.inData.get("ASTS").trim();
  	if (asts == "?") {
  	  asts = "";
  	} 
  		appr = mi.inData.get("APPR") == null ? '' : mi.inData.get("APPR").trim();
  	if (appr == "?") {
  	  appr = "";
  	} 

    
			XXCONO = (Integer)program.LDAZD.CONO;
	
  	
    if (!puno.isEmpty()) {
      DBAction queryMPHEAD = database.table("MPHEAD").index("00").selection("IAPUNO").build()
      DBContainer MPHEAD = queryMPHEAD.getContainer();
      MPHEAD.set("IACONO", XXCONO);
      MPHEAD.set("IAPUNO", puno);
      if (!queryMPHEAD.read(MPHEAD)) {
        mi.error("Purchase order number is invalid.");
        return;
      }
    }  
    
    if (!asts.isEmpty()) {
      if (!asts.equals("Authorised") && !asts.equals("Cancelled") && !asts.equals("Declined") && !asts.equals("Sent for approval") && !asts.equals("Cancelling workflow") && !asts.equals("Under authorisation")) {
        mi.error("Invalid authorisation status");
        return;
      }
    }

    if (!appr.isEmpty()) {
      DBAction queryCMNUSR = database.table("CMNUSR").index("00").selection("JUUSID").build()
      DBContainer CMNUSR = queryCMNUSR.getContainer();
      CMNUSR.set("JUCONO", 0);
      CMNUSR.set("JUDIVI", "");
      CMNUSR.set("JUUSID", appr);
      if (!queryCMNUSR.read(CMNUSR)) {
        mi.error("Approver is invalid.");
        return;
      }
    }
    
    update_EXTAPR(puno, asts, appr)
   
  }
  
  
 /*
  * Update Purchase Authorisation extension table EXTAPR
 */
  def update_EXTAPR(String puno, String asts, String appr) {
    
    int currentCompany = (Integer)program.getLDAZD().CONO
    YYCONO = currentCompany.toString()
    YYUSID = program.getLDAZD().USID
    ZoneId zid = ZoneId.of("Australia/Sydney"); 
    YYLMDT = LocalDate.now(zid).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();

    DBAction query = database.table("EXTAPR").index("00").selection("EXCONO", "EXPUNO", "EXAPPR", "EXASTS", "EXCHNO").build()
    DBContainer container = query.getContainer()
    container.set("EXCONO", currentCompany)
    container.set("EXPUNO", puno)
    query.readLock(container, updateCallBack)

  }
  
  Closure<?> updateCallBack = { LockedResult lockedResult ->
    chno = lockedResult.get("EXCHNO").toString().toInteger() +1;
    lockedResult.set("EXASTS", asts)
    lockedResult.set("EXAPPR", appr)
    lockedResult.set("EXCHNO", chno)
    lockedResult.set("EXCHID", YYUSID)
    lockedResult.set("EXLMDT", YYLMDT)
    lockedResult.update()
  
    mi.outData.put("CONO" , YYCONO)
    mi.outData.put("PUNO" , puno)
    mi.outData.put("APPR" , appr)
    mi.outData.put("ASTS" , asts)
    mi.write()
  
  }

  
}
