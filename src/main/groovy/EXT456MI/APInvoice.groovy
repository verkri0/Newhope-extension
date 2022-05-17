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

 import groovy.lang.Closure;
 
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
  private String adj2;  
  private String neta;  
  private String netx;  
  private Double lo1x;
  private Double me1x;
  private Double netax1;
  private String ad1r;
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
  private double r511;
  private double ra51;
  private double rat51x;
  private double ad2x;  
  private double ca1x;  
  private double sunx;
  private String newx;
  private String agtd;
  private String itds;
  private String atnr; 
  private String grwe;
  private String newe; 
  private String rat5;  
  private String rat6;
  private String car1;
  private String mem1;
  private String sun1;
  private String agno;
  private double agnx;   
  private String grad;  
  private String gra1;  
  private boolean found;
  
 
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
		  program.LDAZD.DIVI;
		  } else {
		  DBAction queryCMNDIV = database.table("CMNDIV").index("00").selection("CCDIVI").build();
      DBContainer CMNDIV = queryCMNDIV.getContainer();
      CMNDIV.set("CCCONO", XXCONO);
      CMNDIV.set("CCDIVI", divi);
      if(!queryCMNDIV.read(CMNDIV)) {
        mi.error("Division does not exist.");
        return;
      } 
		}
		
		 // - validate AP invoice header
    DBAction queryFAPIBH = database.table("FAPIBH").index("00").selection("E5INBN").build();
    DBContainer FAPIBH = queryFAPIBH.getContainer();
    FAPIBH.set("E5CONO", XXCONO);
    FAPIBH.set("E5DIVI", divi);
    FAPIBH.set("E5INBN", inbn.toInteger());
    if (!queryFAPIBH.read(FAPIBH)) {
      mi.error("Invoice header is invalid.");
      return;
    }
    
		
		 // - AP invoice line
    DBAction queryFAPIBL = database.table("FAPIBL").index("00").selection("E6INBN").build();
    DBContainer FAPIBL = queryFAPIBL.getContainer();
    FAPIBL.set("E6CONO", XXCONO);
    FAPIBL.set("E6DIVI", divi);
    FAPIBL.set("E6INBN", inbn.toInteger());
    FAPIBL.set("E6TRNO", trno.toInteger());
    if (!queryFAPIBL.read(FAPIBL)) {
      mi.error("Invoice Line is invalid.");
      return;
    }
    
    
    // - Validate itno
    if (!itno.isEmpty()) {
      DBAction queryMITMAS = database.table("MITMAS").index("00").selection("MMITNO").build();
      DBContainer MITMAS = queryMITMAS.getContainer();
      MITMAS.set("MMCONO", XXCONO);
      MITMAS.set("MMITNO", itno);
      if (!queryMITMAS.read(MITMAS)) {
        mi.error("Itemno is invalid.");
        return;
      }
    }
    
    // - validate puno
    if (!puno.isEmpty()) { 
      DBAction queryMPHEAD = database.table("MPHEAD").index("00").selection("IAPUNO").build();
      DBContainer MPHEAD = queryMPHEAD.getContainer();
      MPHEAD.set("IACONO", XXCONO);
      MPHEAD.set("IAPUNO", puno);
      if (!queryMPHEAD.read(MPHEAD)) {
        mi.error("Purchase order number is invalid.");
        return;
      }
    }
    
    // - validate pnli
    if (!pnli.isEmpty()) { 
      DBAction queryMPLINE = database.table("MPLINE").index("00").selection("IBPUNO").build();
      DBContainer MPLINE = queryMPLINE.getContainer();
      MPLINE.set("IBCONO", XXCONO);
      MPLINE.set("IBPUNO", puno);
      MPLINE.set("IBPNLI", pnli.toInteger());
      MPLINE.set("IBPNLS", 0);
      if (!queryMPLINE.read(MPLINE)) {
        mi.error("Purchase order line is invalid.");
        return;
      }
    }
    
    // - validate sudo
    if (!sudo.isEmpty()) { 
  	  found = false;
      DBAction queryFGRECL = database.table("FGRECL").index("30").selection("F2DIVI", "F2SUDO").build();
      DBContainer FGRECL = queryFGRECL.getContainer();
      FGRECL.set("F2CONO", XXCONO);
      FGRECL.set("F2DIVI", divi);
      FGRECL.set("F2SUDO", sudo);
      queryFGRECL.readAll(FGRECL, 3, 1, lstFGRECL);
      if (!found) {
        mi.error("Delivery No is invalid.");
        return;
      }
    }
    
  	if (inbn.isEmpty()) { inbn = "0";  }
  	if (trno.isEmpty()) { trno = "0";  }
  	if (pnli.isEmpty()) { pnli = "0";  }
  	  
  	grwe =  "0";
  	newx =  "0";
  	rat5 =  "0";
  	rat6 =  "0";
  	loa1 =  "0";
  	car1 =  "0";
    adj2 =  "0";
    sun1 =  "0";
    agno =  "0";
    neta =  "0";
    mem1 =  "0";

  	writeEXTIBL(inbn, trno, itno,puno, pnli);
  
  }
  
  /*
   * write record EXTIBL APS450 extension record 
   *
  */
  
  def writeEXTIBL(String inbn, String trno, String itno, String puno, String pnli) {
	  //Current date and time
  	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	int currentCompany = (Integer)program.getLDAZD().CONO
  	
    def params = ["RIDN":puno.toString(), "RIDL": pnli.toString() ]; // toString is needed to convert from gstring to string
    String bano = null;
    def callback = {
      Map<String, String> response ->
      if(response.BANO != null){
        bano = response.BANO;  
      }
    }
    
    miCaller.call("MWS070MI","ListTransByRef", params, callback);	
  
    def paramsx1 = ["ITNO":itno.toString()]; // toString is needed to convert from gstring to string
  
    def callbackx1 = {
      Map<String, String> response ->
      if(response.ITNO != null){
        itno = response.ITNO;  
        itds = response.ITDS;
      }
    }
    
    miCaller.call("MMS200MI","Get", paramsx1, callbackx1);	
  
    getAttributeNo(bano);
    getLineDetails(inbn, puno, pnli);
    
    DBAction ActionEXTIBL = database.table("EXTIBL").build();
    DBContainer EXTIBL = ActionEXTIBL.getContainer();
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
    EXTIBL.set("EXGRAD", agtd);
    EXTIBL.set("EXGRWE", grwe.toDouble());
    EXTIBL.set("EXNEWE", newx.toDouble());
    EXTIBL.set("EXRAT5", rat5.toDouble());
    EXTIBL.set("EXRAT6", rat6.toDouble());
    EXTIBL.set("EXLP02", loa1.toDouble());
    EXTIBL.set("EXCAR2", car1.toDouble());
    EXTIBL.set("EXADJ2", adj2.toDouble());
    EXTIBL.set("EXKEYS", sun1.toDouble());
    EXTIBL.set("EXAGT1", agno.toDouble());
    EXTIBL.set("EXG004", neta.toDouble());
    EXTIBL.set("EXTOTS", netx);
    EXTIBL.set("EXMB02", mem1.toDouble());
    EXTIBL.set("EXITDS", itds);
    EXTIBL.set("EXADEX", ad1r);
    EXTIBL.set("EXGROS", rat6);
    EXTIBL.set("EXRGDT", currentDate);
    EXTIBL.set("EXLMDT", currentDate);
    EXTIBL.set("EXRGTM", currentTime);
    EXTIBL.set("EXCHNO", 0);
    EXTIBL.set("EXCHID", program.getUser());
	  ActionEXTIBL.insert(EXTIBL, recordExists);
	}
	
	/*
   * get attribute number from the bano record  
   *
  */
	
	def getAttributeNo(String bano) {
	  
	  def params01 = ["ITNO":itno.toString(), "BANO": bano.toString() ]; // toString is needed to convert from gstring to string
    String atnr = null;
    def callback01 = {
      Map<String, String> response ->
      if(response.ATNR != null){
        atnr = response.ATNR;  
      }
    }
    
	  miCaller.call("MMS235MI","GetItmLot", params01, callback01);	
	 
	  getAttributes(atnr);
	 
	}
	
	/*
   * get attributes from lotnumber/attribute nr  
   *
  */
	
	def getAttributes(String atnr) {
	  
	  newx = 0;
	  newe = "0";
	  ad1r = "";
	 
	  def params02 = ["ATNR":atnr.toString()]; // toString is needed to convert from gstring to string
    
    def callback02 = {
      Map<String, String> response ->
      if(response.ATID.trim().equals("DRF01")){
        agtd = response.ATVA;  
      }
      
      if(response.ATID.trim().equals("REC01")){
        grwe = response.ATVN;  
      }
      
      if(response.ATID.trim().equals("REC02")){
        newe = response.ATVN;  
        newx = grwe.toDouble() - newe.toDouble();
      }
      
      //adjustments  
      if(response.ATID.trim() >= "ADJ01" && response.ATID.trim() <= "ADJ99" && response.ATVA != null && response.ATVA.trim() !='N.'){
        ad1r += response.OPDS + ', ';  
      }
    }
    
	  miCaller.call("ATS101MI","GetAttributes", params02, callback02);	
	
	}
	
	def getLineDetails(String inbn, String puno, String pnli) {
	  
	  loa1 = "0";
	  car1 = "0";
	  adj2 = "0";
	  ad2x =  0;
	  me1x =  0;
	  lo1x =  0;
	  ca1x =  0;
	  grad = "0";
	  gra1 = "0";
	  ra51 =  0;
    r511 =  0;
    rat6 = "0";
	  sunx =  0;
	  agnx =  0;
	  
	  def params03 = ["DIVI":divi.toString(), "INBN": inbn.toString() ]; // toString is needed to convert from gstring to string
   
    def callback03 = {
      Map<String, String> response ->
     
      // rate per tonnes 
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('BAS01')){
        base = response.GRPR; 
        ra51 = base.toDouble() + grad.toDouble();
        rat5 = ra51.toString();
      }
      
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('GRADE')){
        grad = response.GRPR; 
        ra51 = base.toDouble() + grad.toDouble();
        rat51x = ra51 / 1000;
        rat5 = rat51x.toString();
      }
      
      // gross value
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('BAS01')){
        base1 = response.NLAM; 
        r511 = base1.toDouble() + gra1.toDouble();
        rat6 = r511.toString();
      }
       
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('GRADE')){
        gra1 = response.NLAM; 
        r511 = base1.toDouble() + gra1.toDouble();
        rat6 = r511.toString();
      }
      
       // adjustments
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim() >= 'ADJ01' && response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim() <= 'ADJ99'){
        ad2x = ad2x +  (response.NLAM.toDouble()); 
        adj2 = ad2x.toString();
      }
      
      // carrier
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('CAR01')){
        car1 = response.NLAM.toString(); 
        ca1x = response.NLAM.toDouble();
      }
      
      // keysupplier
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('GSU01')){
        sun1 = response.NLAM.toString(); 
        sunx = response.NLAM.toDouble();
      }
      
      // agreement type
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('AGT01')){
        agno = response.NLAM.toString(); 
        agnx = response.NLAM.toDouble();
      }
      
      // loader
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('LOA01')){
        loa1 = response.NLAM.toString(); 
        lo1x = response.NLAM.toDouble();
      }
      
      // member
      if(response.PUNO.trim().equals(puno) && response.PNLI.trim().equals(pnli) && response.CEID.trim().equals('MBR01')){
        mem1 = response.NLAM.toString(); 
        me1x = response.NLAM.toDouble();
      }
    }
  
	  miCaller.call("APS450MI","LstLines", params03, callback03);	
	  
	  netax1 = r511.toDouble() + ad2x.toDouble() + me1x.toDouble() + ca1x.toDouble() + lo1x.toDouble() + agnx.toDouble() + sunx.toDouble();
    neta = netax1.toString();
    netx = netax1.toString();
 
	}
	
	/*
   * recordEXists - Callback if record already exists
   *
  */
  
  Closure recordExists = {
    mi.error("Record already exists");
  }
  
  /*
   * lstFGRECL - Callback function to return FGRECL records
   *
  */
  
  Closure<?> lstFGRECL = { DBContainer FGRECL ->
    found = true;
  }
}
