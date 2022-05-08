
 import groovy.lang.Closure
 
 import java.time.LocalDate;
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import groovy.json.JsonSlurper;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;



public class WriteCHX extends ExtendM3Transaction {
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
  private String sunm; 
  
  private String puno;
  private String appr;
  private String asts;
   private int XXCONO;
 
  public WriteCHX(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
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
  	
  	sudo = mi.inData.get("SUDO") == null ? '' : mi.inData.get("SUDO").trim();
  		if (sudo == "?") {
  	  sudo = "";
  	}
  	
  	 	car1 = mi.inData.get("CAR1") == null ? '' : mi.inData.get("CAR1").trim();
  		if (car1 == "?") {
  	  car1 = "";
  	}
  	
  		suno = mi.inData.get("SUNO") == null ? '' : mi.inData.get("SUNO").trim();
  		if (suno == "?") {
  	  suno = "";
  	}
  	
  	lots = mi.inData.get("LOTS") == null ? '' : mi.inData.get("LOTS").trim();
  		if (lots == "?") {
  	  lots = "";
  	}
  	
  		chtp = mi.inData.get("CHTP") == null ? '' : mi.inData.get("CHTP").trim();
  		if (chtp == "?") {
  	  chtp = "";
  	}
  	
  		wght = mi.inData.get("WGHT") == null ? '' : mi.inData.get("WGHT").trim();
  		if (wght == "?") {
  	  wght = "";
  	}
  	
  		rate = mi.inData.get("RATE") == null ? '' : mi.inData.get("RATE").trim();
  		if (rate == "?") {
  	  rate = "";
  	}
  
		  lnam = mi.inData.get("LNAM") == null ? '' : mi.inData.get("LNAM").trim();
  		if (lnam == "?") {
  	  lnam = "";
  	}
  	
  	  trdt = mi.inData.get("TRDT") == null ? '' : mi.inData.get("TRDT").trim();
  		if (trdt == "?") {
  	  trdt = "";
  	}
  	
  	  sunm = mi.inData.get("SUNM") == null ? '' : mi.inData.get("SUNM").trim();
  		if (sunm == "?") {
  	  sunm = "";
  	}
  	
  	
  	
  	  if (inbn.isEmpty()) { inbn = "0";  }
  	  if (trno.isEmpty()) { trno = "0";  }
  	  if (lots.isEmpty()) { lots = "0";  }
  	  if (wght.isEmpty()) { wght = "0";  }
  	  if (rate.isEmpty()) { rate = "0";  }
  	  if (lnam.isEmpty()) { lnam = "0";  }
  	  if (trdt.isEmpty()) { trdt = "0";  }
  	  
  	
  	    writeEXTCHX(cono, divi, inbn, trno, sudo, car1, suno, lots, chtp, wght, rate, lnam, trdt, sunm);
    
  }
  
  
  def writeEXTCHX(String cono, String divi, String inbn, String trno, String sudo, String car1, String suno, String lots, String chtp, String wght, String rate, String lnam, String trdt, String sunm) {
	  //Current date and time
  	int currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	int currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
  	
  	  int currentCompany = (Integer)program.getLDAZD().CONO
  	
	  DBAction ActionEXTCHX = database.table("EXTCHX").build();
  	DBContainer EXTCHX = ActionEXTCHX.getContainer();
  	// QI Test information
  	EXTCHX.set("EXCONO", currentCompany);
  	EXTCHX.set("EXDIVI", divi);
  	EXTCHX.set("EXINBN", inbn.toInteger());
  	EXTCHX.set("EXTRNO", trno.toInteger());
  	EXTCHX.set("EXSUDO", sudo);
  	EXTCHX.set("EXCAR1", car1);
  	EXTCHX.set("EXSUNO", suno);
  	EXTCHX.set("EXLOTS", lots.toInteger());
  	EXTCHX.set("EXCHTP", chtp);
  	EXTCHX.set("EXWGHT", wght.toDouble());
  	EXTCHX.set("EXRATE", rate.toDouble());
  	EXTCHX.set("EXLNAM", lnam.toDouble());
  	EXTCHX.set("EXTRDT", trdt.toInteger());
  	EXTCHX.set("EXSUNM", sunm);
  	EXTCHX.set("EXRGDT", currentDate);
  	EXTCHX.set("EXRGTM", currentTime);
  	EXTCHX.set("EXLMDT", currentDate);
  	EXTCHX.set("EXCHID", program.getUser());
  	
  	ActionEXTCHX.insert(EXTCHX, recordExists);
	}
  
  
  Closure recordExists = {
	
  }
  
  
  
  
  
  
  
  
  
  
}
