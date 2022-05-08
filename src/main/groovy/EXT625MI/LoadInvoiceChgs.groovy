
 import groovy.lang.Closure
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;

public class LoadInvoiceChgs extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String cono;
  private String divi;
  private String bref;
  private String supplier;
  private String suno;
  private String sudo;  
  private String sunm;
  private String rgtm;  
  private String tmsx;
  private String bins; 
  private String atnr;   
  private String atid; 
  private String atva;
  private String tx30; 
  private String tx15;
  private String tx31;
  private String dfa1;
  private String name;
  private String whlo;
  private String rgdt;
  private String grwe;
  private String ridn;
  private String ridl;
  private String ref3;
  private String ref4;
  private String ovhe;
  private String ovhem;
  private String mstx;
  private String ovhex; 
  private String ovhey;  
  private String bano;
  private String itno;
  private String puno;
  private String pnli;
  private String atnb; 
  private String grad;
  private String car1;
  private String loa1;
  private String itty;
  private String itgr;
  
  
  
  private List lstQITests_Range;
  private List lstQITests_Target;
  private List lstQITests_Quality;
  
  private List lstTestResults_Range;
  private List lstSortedTestResults_Range;
  private List lstTestResults_Target;
  private List lstSortedTestResults_Target;
  private List lstTestResults_Quality;
  private List lstSortedTestResults_Quality;
  
  private List lstTestResults_Range01;
  private List lstSortedTestResults_Range01;
  private List lstTestResults_Target01;
  private List lstSortedTestResults_Target01;
  private List lstTestResults_Quality01;
  private List lstSortedTestResults_Quality01;
  
  
   private int XXCONO;
 
  public LoadInvoiceChgs(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
  	
  	whlo = mi.inData.get("WHLO") == null ? '' : mi.inData.get("WHLO").trim();
  		if (whlo == "?") {
  	  whlo = "";
  	} 
  
  	itno = mi.inData.get("ITNO") == null ? '' : mi.inData.get("ITNO").trim();
  		if (itno == "?") {
  	  itno = "";
  	}
  
  	rgdt = mi.inData.get("RGDT") == null ? '' : mi.inData.get("RGDT").trim();
  		if (rgdt == "?") {
  	  rgdt = "";
  	}	
  	
  	rgtm = mi.inData.get("RGTM") == null ? '' : mi.inData.get("RGTM").trim();
  		if (rgtm == "?") {
  	  rgtm = "";
  	} 
  	
  		tmsx = mi.inData.get("TMSX") == null ? '' : mi.inData.get("TMSX").trim();
  		if (tmsx == "?") {
  	  tmsx = "";
  	} 
  	
  		bano = mi.inData.get("BANO") == null ? '' : mi.inData.get("BANO").trim();
  		if (bano == "?") {
  	  bano = "";
  	} 
  	
  		ridn = mi.inData.get("RIDN") == null ? '' : mi.inData.get("RIDN").trim();
  		if (ridn == "?") {
  	  ridn = "";
  	} 
  	
  		ridl = mi.inData.get("RIDL") == null ? '' : mi.inData.get("RIDL").trim();
  		if (ridl == "?") {
  	  ridl = "";
  	} 
  	
  		sudo = mi.inData.get("SUDO") == null ? '' : mi.inData.get("SUDO").trim();
  		if (sudo == "?") {
  	  sudo = "";
  	} 
  	
  		atnb = mi.inData.get("ATNB") == null ? '' : mi.inData.get("ATNB").trim();
  		if (atnb == "?") {
  	  atnb = "";
  	} 
  	
  	suno = "";
  	grwe = "0";
  	ovhe = "0";
  	ovhex = "0";
  	ovhem = "0";
  	ovhey = "0";
  	
  	car1 = "";
  	loa1 = "";
  	
  	  if (rgdt.isEmpty()) { rgdt = "0";  }
  	  if (rgtm.isEmpty()) { rgtm = "0";  }
  	  if (tmsx.isEmpty()) { tmsx = "0";  }
  	  if (ridl.isEmpty()) { ridl = "0";  }
  	  if (atnb.isEmpty()) { atnb = "0";  }
  	
  	
  	grwe = "0";
  	
  	
  	 deleteEXTCHG(cono, whlo, itno, rgdt, rgtm, mstx);
  	
  	// carrier fees
  	   writeEXTCHG0101(cono, whlo, itno, rgdt, rgtm, mstx, bano, ridn, ridl, sudo, atnb);

    // loader fees  	   
  	   writeEXTCHG0102(cono, whlo, itno, rgdt, rgtm, mstx, bano, ridn, ridl, sudo, atnb);
  	   
  	// DFA fees   
  	   writeEXTCHG0103(cono, whlo, itno, rgdt, rgtm, mstx, bano, ridn, ridl, sudo, atnb);
  	   
  	   
  }
  
  
   def deleteEXTCHG(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx) {
    
    
      int currentCompany = (Integer)program.getLDAZD().CONO
    
    DBAction queryEXTCHG = database.table("EXTCHG").index("00").selection("EXCONO", "EXWHLO", "EXITNO", "EXRGDT", "EXRGTM", "EXTMSX").build();
    DBContainer EXTCHG = queryEXTCHG.getContainer();
                EXTCHG.set("EXCONO", currentCompany);
                EXTCHG.set("EXWHLO", whlo);
                EXTCHG.set("EXITNO", itno);
                EXTCHG.set("EXRGDT", rgdt.toInteger());
                EXTCHG.set("EXRGTM", rgtm.toInteger());
                EXTCHG.set("EXTMSX", tmsx.toInteger());
                
        
queryEXTCHG.readAllLock(EXTCHG, 6, deleteEXTCHG);
}
/*
* deleteEXTCHG - Callback function
*
*/
Closure<?> deleteEXTCHG = { LockedResult EXTCHG ->
EXTCHG.delete();
}
  
  
  
  
  def writeEXTCHG0101(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx, String bano, String ridn, String ridl, String sudo, String atnb) {
  
     def params01 = ["ATNR":atnb.toString()] // toString is needed to convert from gstring to string
    
    def callback01 = {
    Map<String, String> response ->
      logger.info("Response = ${response}")
     
     
      if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC01") && response.ATVN != null){
          grwe =  response.ATVN   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("SUP01") && response.ATVA != null){
          suno =  response.ATVA   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC04") && response.ATVA != null){
          car1 =  response.ATVA   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC05") && response.ATVA != null){
          loa1 =  response.ATVA   
        }
        
          
      }
    

    }
   
   	 miCaller.call("ATS101MI","GetAttributes", params01, callback01)
    
    
    logger.debug('hoi ' + car1)
    logger.debug('hoi2 ' + suno)

  def params02 = ["CEID": "CAR01", "OVK1":"99999", "OVK2":suno.toString(),  ] // toString is needed to convert from gstring to string
    
    def callback02 = {
    Map<String, String> response ->
      logger.info("Response = ${response}")
     
     
      if(response.OVHE != null){
          ovhe =  response.OVHE   
          
            logger.debug('hoi3 ' + ovhe)
          
          
        }
        
    }
    
    
	 miCaller.call("PPS280MI","LstElementValue", params02, callback02)

    
    
     ovhex = ovhe.toDouble() * grwe.toDouble()

   
   	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	int currentCompany = (Integer)program.getLDAZD().CONO
   
   	DBAction ActionEXTCHG = database.table("EXTCHG").build();
  	DBContainer EXTCHG = ActionEXTCHG.getContainer();
  
  if(car1.trim() != '99999')  {
  
  		// write carrier fees
  	EXTCHG.set("EXCONO", currentCompany);
  	EXTCHG.set("EXWHLO", whlo);
  	EXTCHG.set("EXRGDT", rgdt.toInteger());
  	EXTCHG.set("EXRGTM", rgtm.toInteger());
   	EXTCHG.set("EXTMSX", tmsx.toInteger());
  	EXTCHG.set("EXSUDO", sudo);
  	EXTCHG.set("EXCHTP", 'Carrier Fees');
  	EXTCHG.set("EXSUNO", suno);
  	EXTCHG.set("EXITNO", itno);
  	EXTCHG.set("EXBANO", bano);
  	EXTCHG.set("EXLOTS", 1);
  	EXTCHG.set("EXPUNO", ridn);
  	EXTCHG.set("EXPNLI", ridl.toInteger());
  	EXTCHG.set("EXWGHT", grwe.toDouble());
  	EXTCHG.set("EXRATE", ovhe.toDouble());
  	EXTCHG.set("EXLNAM", ovhex.toDouble());
  	EXTCHG.set("EXCAR1", car1);
  	EXTCHG.set("EXLOA1", loa1);
  	EXTCHG.set("EXPROC", '0');
  	
  	EXTCHG.set("EXCHID", program.getUser());
  	
  	ActionEXTCHG.insert(EXTCHG, recordExists);
    
  }
    
    
  }
  
 
 def writeEXTCHG0102(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx, String bano, String ridn, String ridl, String sudo, String atnb) {
  
     def params01 = ["ATNR":atnb.toString()] // toString is needed to convert from gstring to string
    
    def callback01 = {
    Map<String, String> response ->
      logger.info("Response = ${response}")
     
     
      if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC01") && response.ATVN != null){
          grwe =  response.ATVN   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("SUP01") && response.ATVA != null){
          suno =  response.ATVA   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC04") && response.ATVA != null){
          car1 =  response.ATVA   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC05") && response.ATVA != null){
          loa1 =  response.ATVA   
        }
        
          
      }
    

    }
   
   	 miCaller.call("ATS101MI","GetAttributes", params01, callback01)
    

    if(car1.trim() == loa1.trim())  { ovhem = "4" }
    if(loa1.trim() == "99998") { ovhem = "2" }
    
     ovhey = ovhem.toDouble() * grwe.toDouble()

   
   	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	int currentCompany = (Integer)program.getLDAZD().CONO
   
   	DBAction ActionEXTCHG = database.table("EXTCHG").build();
  	DBContainer EXTCHG = ActionEXTCHG.getContainer();
  
  if(loa1.trim() != '99999')  {
  
  		// write carrier fees
  	EXTCHG.set("EXCONO", currentCompany);
  	EXTCHG.set("EXWHLO", whlo);
  	EXTCHG.set("EXRGDT", rgdt.toInteger());
  	EXTCHG.set("EXRGTM", rgtm.toInteger());
   	EXTCHG.set("EXTMSX", tmsx.toInteger());
  	EXTCHG.set("EXSUDO", sudo);
  	EXTCHG.set("EXCHTP", 'Loader Fees');
  	EXTCHG.set("EXSUNO", suno);
  	EXTCHG.set("EXITNO", itno);
  	EXTCHG.set("EXBANO", bano);
  	EXTCHG.set("EXLOTS", 1);
  	EXTCHG.set("EXPUNO", ridn);
  	EXTCHG.set("EXPNLI", ridl.toInteger());
  	EXTCHG.set("EXWGHT", grwe.toDouble());
  	EXTCHG.set("EXRATE", ovhem.toDouble());
  	EXTCHG.set("EXLNAM", ovhey.toDouble());
  	EXTCHG.set("EXCAR1", car1);
  	EXTCHG.set("EXLOA1", loa1);
  	EXTCHG.set("EXPROC", '0');
  	
  	EXTCHG.set("EXCHID", program.getUser());
  	
  	ActionEXTCHG.insert(EXTCHG, recordExists);
    
  }
    
    
  }
 
  
  def writeEXTCHG0103(String cono, String whlo, String itno, String rgdt, String rgtm, String mstx, String bano, String ridn, String ridl, String sudo, String atnb) {
  
     def params01 = ["ATNR":atnb.toString()] // toString is needed to convert from gstring to string
    
    def callback01 = {
    Map<String, String> response ->
      logger.info("Response = ${response}")
     
     
      if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC01") && response.ATVN != null){
          grwe =  response.ATVN   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("SUP01") && response.ATVA != null){
          suno =  response.ATVA   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC04") && response.ATVA != null){
          car1 =  response.ATVA   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC05") && response.ATVA != null){
          loa1 =  response.ATVA   
        }
        
          
      }
    
     if(response.ATID != null){
        
         if(response.ATID.trim().equals("REC11") && response.ATVA != null){
          dfa1 =  response.ATVA.trim()   
        }
        
          
      }
      
       if(response.ATID != null){
        
         if(response.ATID.trim().equals("ITM01") && response.ATVA != null){
          itno =  response.ATVA   
        }
        
          
      }
      

    }
   
   	 miCaller.call("ATS101MI","GetAttributes", params01, callback01)
    
    
    
    // get item parameters
    
     def params04 = ["ITNO": itno.toString(),  ] // toString is needed to convert from gstring to string
    
    def callback04 = {
    Map<String, String> response ->
      logger.info("Response = ${response}")
     
     
      if(response.OVHE != null){
          itty =  response.ITTY 
          itgr =  response.ITGR
        }
        
    }
    
    
	 miCaller.call("MMS200MI","Get", params04, callback04)
    
    
    
    def params03 = ["CEID": "LEVDFA", "OVK1":itty.toString(), "OVK2":itgr.toString(),  ] // toString is needed to convert from gstring to string
    
    def callback03 = {
    Map<String, String> response ->
      logger.info("Response = ${response}")
     
     
      if(response.OVHE != null){
          ovhe =  response.OVHE   
        }
        
    }
    
	 miCaller.call("PPS280MI","LstElementValue", params03, callback03)
    

     ovhex = ovhe.toDouble() * grwe.toDouble()

   
   	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	int currentCompany = (Integer)program.getLDAZD().CONO
   
   	DBAction ActionEXTCHG = database.table("EXTCHG").build();
  	DBContainer EXTCHG = ActionEXTCHG.getContainer();
  
  if(dfa1 != '99999')  {
  
  		// write carrier fees
  	EXTCHG.set("EXCONO", currentCompany);
  	EXTCHG.set("EXWHLO", whlo);
  	EXTCHG.set("EXRGDT", rgdt.toInteger());
  	EXTCHG.set("EXRGTM", rgtm.toInteger());
   	EXTCHG.set("EXTMSX", tmsx.toInteger());
  	EXTCHG.set("EXSUDO", sudo);
  	EXTCHG.set("EXCHTP", 'DFA Member Fees');
  	EXTCHG.set("EXSUNO", suno);
  	EXTCHG.set("EXITNO", itno);
  	EXTCHG.set("EXBANO", bano);
  	EXTCHG.set("EXLOTS", 1);
  	EXTCHG.set("EXPUNO", ridn);
  	EXTCHG.set("EXPNLI", ridl.toInteger());
  	EXTCHG.set("EXWGHT", grwe.toDouble());
  	EXTCHG.set("EXRATE", ovhe.toDouble());
  	EXTCHG.set("EXLNAM", ovhex.toDouble());
  	EXTCHG.set("EXCAR1", dfa1);
  	EXTCHG.set("EXLOA1", loa1);
  	EXTCHG.set("EXPROC", '0');
  	
  	EXTCHG.set("EXCHID", program.getUser());
  	
  	ActionEXTCHG.insert(EXTCHG, recordExists);
    
  }
    
  }
  
  
  Closure recordExists = {
	  
  }
  
  
  
  
}
