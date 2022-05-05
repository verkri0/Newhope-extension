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
 *ABF_R_200         20220405  RDRIESSEN   Mods BF0456- Create extension file to connect to APS456PF AP Invoice print
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

public class APInvoice extends ExtendM3Transaction {
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
  private String itno;
  private String ceid;
  private String sino;
  private String nlam;
  private String grpr;
  private String puno;
  private String pnli;
  private String lp01;
  private String lp02;
  private String bano;
  private String sudo;  
  private String grx1;
  private String grx2;
  private String nlamx1;
  private String nlamx2;
  private String rate01;
  private String rate02;
  private int XXCONO;
  private String base;
  private String base1;
  private String loa1; 
  private String adj02;  
  private String neta;  
  private String netx;  
  private Double loa1x;
  private Double mem01x;
  private Double netax1;
  private String adj01r;
  private String adj02r;
  private String adj03r;
  private String adj04r;
  private String adj05r;
  private String adj06r;
  private String adj07r;
  private String adj08r;
  private String adj09r;
  private String adj10r;
  private String adj11r;
  private String adj12r;
  private String adj13r;
  private String adj14r;
  private String adj15r;
  private String adj16r;
  private String adj17r;
  private String adj18r;
  private String adj19r;
  private String adj20r;
  private String adj21r;
  private String adj22r;
  private String adj23r;
  private String adj24r;
  private double rat511;
  private double rat51;
  private double rat51x;
  private double adj02x;  
  private double car01x;  
  private double keysupplierx;
  private String newex;
  private String agatid;
  private String itds;
  private String atnr; 
  private String grwe;
  private String newe; 
  private String rat5;  
  private String rat6;
  private String car01;
  private String mem01;
  private String keysupplier;
  private String agreement;
  private double agreementx;   
  private String grad;  
  private String grad1;  
  
 
  public APInvoice(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
  	
  	divi = mi.inData.get("DIVI") == null ? '' : mi.inData.get("DIVI").trim();
  		if (divi == "?") {
  	  divi = "";
  	} 
  
  	inbn = mi.inData.get("INBN") == null ? '' : mi.inData.get("INBN").trim();
  		if (inbn == "?") {
  	  inbn = "";
  	} 
  	
  	trno = mi.inData.get("TRNO") == null ? '' : mi.inData.get("TRNO").trim();
  		if (trno == "?") {
  	  trno = "";
  	}
  	
  	ceid = mi.inData.get("CEID") == null ? '' : mi.inData.get("CEID").trim();
  		if (ceid == "?") {
  	  ceid = "";
  	}
  	
  	 	nlam = mi.inData.get("NLAM") == null ? '' : mi.inData.get("NLAM").trim();
  		if (nlam == "?") {
  	  nlam = "";
  	}
  	
  		puno = mi.inData.get("PUNO") == null ? '' : mi.inData.get("PUNO").trim();
  		if (puno == "?") {
  	  puno = "";
  	}
  	
  	pnli = mi.inData.get("PNLI") == null ? '' : mi.inData.get("PNLI").trim();
  		if (pnli == "?") {
  	  pnli = "";
  	}
  	
  		itno = mi.inData.get("ITNO") == null ? '' : mi.inData.get("ITNO").trim();
  		if (itno == "?") {
  	  itno = "";
  	}
  	
  		sudo = mi.inData.get("SUDO") == null ? '' : mi.inData.get("SUDO").trim();
  		if (sudo == "?") {
  	  sudo = "";
  	}
  	
  		sino = mi.inData.get("SINO") == null ? '' : mi.inData.get("SINO").trim();
  		if (sino == "?") {
  	  sino = "";
  	}
  
		itds = mi.inData.get("ITDS") == null ? '' : mi.inData.get("ITDS").trim();
  		if (itds == "?") {
  	  itds = "";
  	}
  
  	  if (inbn.isEmpty()) { inbn = "0";  }
  	  if (trno.isEmpty()) { trno = "0";  }
  	  if (nlam.isEmpty()) { nlam = "0";  }
  	  if (pnli.isEmpty()) { pnli = "0";  }
  	  
  	  grwe = "0"
  	  newex = "0"
  	  rat5 = "0"
  	  rat6 = "0"
  	  loa1 = "0"
  	  car01 = "0"
      adj02 = "0"
      keysupplier = "0"
      agreement = "0"
      neta = "0"
     mem01 =  "0"

  	  writeEXTIBL(inbn, trno, itno, ceid, nlam, puno, pnli, itds);
  
  }
  
  def writeEXTIBL(String inbn, String trno, String itno, String ceid, String nlam, String puno, String pnli, String itds) {
	  //Current date and time
  	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	int currentCompany = (Integer)program.getLDAZD().CONO
  	
  	
  def params = ["RIDN":puno.toString(), "RIDL": pnli.toString() ] // toString is needed to convert from gstring to string
    String bano = null
     def callback = {
    Map<String, String> response ->
      logger.info("Response = ${response}")
      if(response.BANO != null){
        bano = response.BANO  
      
      }
    }
    
     miCaller.call("MWS070MI","ListTransByRef", params, callback)	
       
        getAttributeNo(bano);
        getLineDetails(inbn, puno, pnli);
      
   
    DBAction ActionEXTIBL = database.table("EXTIBL").build();
  	DBContainer EXTIBL = ActionEXTIBL.getContainer();
  	// QI Test information
  	
  	EXTIBL.set("EXCONO", XXCONO);
  	EXTIBL.set("EXDIVI", divi);
  	EXTIBL.set("EXPUNO", puno);
  	EXTIBL.set("EXPNLI", pnli.toInteger());
  	EXTIBL.set("EXINBN", inbn.toInteger());
  	EXTIBL.set("EXTRNO", trno.toInteger());
  	EXTIBL.set("EXITNO", itno);
  	EXTIBL.set("EXSUDO", sudo);
  	EXTIBL.set("EXSINO", sino);
    EXTIBL.set("EXBANO", bano);
    EXTIBL.set("EXLOTS", 1);
    EXTIBL.set("EXGRAD", agatid);
   	EXTIBL.set("EXGRWE", grwe.toDouble());
    EXTIBL.set("EXNEWE", newex.toDouble());
    EXTIBL.set("EXRAT5", rat5.toDouble());
    EXTIBL.set("EXRAT6", rat6.toDouble());
    EXTIBL.set("EXLP02", loa1.toDouble());
    EXTIBL.set("EXCAR2", car01.toDouble());
    EXTIBL.set("EXADJ2", adj02.toDouble());
    EXTIBL.set("EXKEYS", keysupplier.toDouble());
    EXTIBL.set("EXAGT1", agreement.toDouble());
    EXTIBL.set("EXG004", neta.toDouble());
    EXTIBL.set("EXTOTS", netx);
    EXTIBL.set("EXMB02", mem01.toDouble());
    EXTIBL.set("EXITDS", itds);
    EXTIBL.set("EXADEX", adj01r);
    EXTIBL.set("EXRGDT", currentDate);
    EXTIBL.set("EXLMDT", currentDate);
    EXTIBL.set("EXRGTM", currentTime);
    EXTIBL.set("EXCHID", program.getUser());

  	ActionEXTIBL.insert(EXTIBL, recordExists);

	}
	
	def getAttributeNo(String bano) {
	  
	 def params01 = ["ITNO":itno.toString(), "BANO": bano.toString() ] // toString is needed to convert from gstring to string
    String atnr = null
    
    def callback01 = {
    Map<String, String> response ->
      if(response.ATNR != null){
        atnr = response.ATNR  
    }
    }
    
    
	 miCaller.call("MMS235MI","GetItmLot", params01, callback01)	
	 
	  getAttributes(atnr);
	 
	}
	
	
	def getAttributes(String atnr) {
	  
	  newex = 0
	  newe = "0"
	  adj01r = ""
	 
	 def params02 = ["ATNR":atnr.toString()] // toString is needed to convert from gstring to string
    
    def callback02 = {
    Map<String, String> response ->
        if(response.ATID.trim().equals("DRF01")){
        agatid = response.ATVA  
      
      }
      
      if(response.ATID.trim().equals("REC01")){
        grwe = response.ATVN  
       
      }
      
      if(response.ATID.trim().equals("REC02")){
        newe = response.ATVN  
        newex = grwe.toDouble() - newe.toDouble()
      
      }
      
    //adjustments  
      if(response.ATID.trim() >= "ADJ01" && response.ATID.trim() <= "ADJ99" && response.ATVA != null && response.ATVA.trim() !='N.'){
        adj01r += response.OPDS + ', '  
        
      
      }
      
    }
    
    
	 miCaller.call("ATS101MI","GetAttributes", params02, callback02)	
	
	}
	
	def getLineDetails(String inbn, String puno, String pnli) {
	  
	 
	 loa1 = "0"
	 car01 = "0"
	 adj02 = "0"
	 adj02x = 0
	 mem01x = 0
	 loa1x = 0
	 car01x = 0
	 grad = "0"
	 grad1 = "0"
	 rat51 = 0
   rat511 = 0
   rat6 = "0"
	 
	 keysupplierx = 0
	 agreementx = 0
	  
	 def params03 = ["DIVI":divi.toString(), "INBN": inbn.toString() ] // toString is needed to convert from gstring to string
   
    def callback03 = {
    Map<String, String> response ->
     
      // rate per tonnes 
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('BAS01')){
           base = response.GRPR 
                  rat51 = base.toDouble() + grad.toDouble()
                  rat5 = rat51.toString()
           
       
      }
       if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('GRADE')){
           grad = response.GRPR 
                  rat51 = base.toDouble() + grad.toDouble()
                  rat51x = rat51 / 1000
                  rat5 = rat51x.toString()
         
      }
      
      // gross value
             if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('BAS01')){
           base1 = response.NLAM 
           
            rat511 = base1.toDouble() + grad1.toDouble()
            rat6 = rat511.toString()
           
       
      }
       if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('GRADE')){
          grad1 = response.NLAM 
       
         rat511 = base1.toDouble() + grad1.toDouble()
         rat6 = rat511.toString()
         
          }
      
      
       // adjustments
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim() >= 'ADJ01' && response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim() <= 'ADJ99'){
           adj02x = adj02x +  (response.NLAM.toDouble()) 
           adj02 = adj02x.toString()
       
      }
      
        // carrier
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('CAR01')){
           car01 = response.NLAM.toString() 
           car01x = response.NLAM.toDouble()
        }
      
      
        // keysupplier
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('GSU01')){
           keysupplier = response.NLAM.toString() 
           keysupplierx = response.NLAM.toDouble()
         
        }
      
       // keysupplier
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('AGT01')){
           agreement = response.NLAM.toString() 
           agreementx = response.NLAM.toDouble()
           
      }
      
        // loader
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('LOA01')){
          loa1 = response.NLAM.toString() 
          loa1x = response.NLAM.toDouble()
        
      }
      
      
      // member
       if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('MBR01')){
           mem01 = response.NLAM.toString() 
           mem01x = response.NLAM.toDouble()
           
       netax1 = rat511.toDouble() + adj02x.toDouble() + mem01x.toDouble() + car01x.toDouble() + loa1x.toDouble() + agreementx.toDouble() + keysupplierx.toDouble()
      
          neta = netax1.toString()
           netx = netax1.toString()
       
      }
      
     
    }
    
    
	 miCaller.call("APS450MI","LstLines", params03, callback03)	
 
	
	}
	
  
  Closure recordExists = {

  }


}
