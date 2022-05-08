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
 *ABF_R_0625        20220405  RDRIESSEN   Mods BF0625- Generate APS450 invoice printout extension workfile
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



public class UpdateCHG extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  
  private String cono;
  private String divi;
  private String inbn;
  private String trno;
  private String sudo;
  private String car1;
  private String suno;
  private String lots;
  private String chtp;
  private String wght;
  private String lnam;
  private String rate;
  private String trdt;
  private String whlo;
  private String sino;
  private String vfdt;
  private String lfdt;
  private String puno;
  private String appr;
  private String asts;
   private int XXCONO;
 
  public UpdateCHG(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
	  
  }
  
  public void main() {
    
     cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
  	if (cono == "?") {
  	  cono = "";
  	} 
    
    
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
  	
  	whlo = mi.inData.get("WHLO") == null ? '' : mi.inData.get("WHLO").trim();
  		if (whlo == "?") {
  	  whlo = "";
  	} 
  
  	car1 = mi.inData.get("CAR1") == null ? '' : mi.inData.get("CAR1").trim();
  		if (car1 == "?") {
  	  car1 = "";
  	} 
  	
  	vfdt = mi.inData.get("VFDT") == null ? '' : mi.inData.get("VFDT").trim();
  		if (vfdt == "?") {
  	  vfdt = "";
  	}
  	
  	lfdt = mi.inData.get("LFDT") == null ? '' : mi.inData.get("LFDT").trim();
  		if (lfdt == "?") {
  	  lfdt = "";
  	}
  	
  		sino = mi.inData.get("SINO") == null ? '' : mi.inData.get("SINO").trim();
  		if (sino == "?") {
  	  sino = "";
  	}
  	
  	
  	
  	
  	  if (vfdt.isEmpty()) { vfdt = "0";  }
  	  if (lfdt.isEmpty()) { lfdt = "0";  }
  
  	    updateEXTCHG(cono, whlo, car1, vfdt, lfdt, sino);
    
  }
  
  
  def updateEXTCHG(String cono, String whlo, String car1, String vfdt, String lfdt, String sino) {
	  //Current date and time
  	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	 	int currentCompany = (Integer)program.getLDAZD().CONO
  	
  ExpressionFactory expression = database.getExpressionFactory("EXTCHG")
  
   expression = expression.ge("EXRGDT", vfdt);
   expression = expression.le("EXRGDT", lfdt);
   expression = expression.le("EXPROC", "0");
  
  DBAction query = database.table("EXTCHG").index("10").matching(expression).selection("EXCONO", "EXWHLO", "EXCAR1", "EXPROC", "EXSINO").build()
  DBContainer container = query.getContainer()
  container.set("EXCONO", currentCompany)
  container.set("EXWHLO", whlo.trim())
  container.set("EXCAR1", car1.trim())  
  query.readAllLock(container, 3, updateCallBack)
  
	}
  
  Closure<?> updateCallBack = { LockedResult lockedResult ->
  lockedResult.set("EXPROC", "1")
  lockedResult.set("EXSINO", sino.trim())
  
  lockedResult.update()
}
  
  
  
}
