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
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *FD38740           20220808  XWZHAO      Yield category
 *FD38740           20221208  XWZHAO      32734 - Adding additional fields into the yield category extension 
 *
 */
 
/*
* - Write yield category record to EXTCAT
*/
public class AddYieldCat extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  private String cono;
  private String cate;
  private String pcat;
  private String cads;
  private String camk;
  private String sort;
  private String cuky;
  private String spds;
  private String tare;
  private String mefa;
  private String yied;
  private String cost;
  private String refa;
  private String unms;
  private String plco;
  private String gndr;
  private String cuex;
  private String excl;
  private String prim;
  private String ladj;
  private String madj;
  private String sadj;
  private String frdt;
  private String todt;
  private String conm;
  private String pote;
  private String targ;
  
  private int XXCONO;
  private int currentDate;
  private int currentTime;
  
  public AddYieldCat(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
    this.mi = mi;
    this.database = database;
    this.miCaller = miCaller;
    this.logger = logger;
    this.program = program;
  }
  
  public void main() {
    //Fetch input fields from MI
    cono = mi.inData.get("CONO") == null ? '' : mi.inData.get("CONO").trim();
  	if (cono == "?") {
  	  cono = "";
  	} 
    cate = mi.inData.get("CATE") == null ? '' : mi.inData.get("CATE").trim();
  	if (cate == "?") {
  	  cate = "";
  	} 
  	pcat = mi.inData.get("PCAT") == null ? '' : mi.inData.get("PCAT").trim();
  	if (pcat == "?") {
  	  pcat = "";
  	} 
    cads = mi.inData.get("CADS") == null ? '' : mi.inData.get("CADS").trim();
  	if (cads == "?") {
  	  cads = "";
  	}
  	camk = mi.inData.get("CAMK") == null ? '' : mi.inData.get("CAMK").trim();
  	if (camk == "?") {
  	  camk = "";
  	}
  	sort = mi.inData.get("SORT") == null ? '' : mi.inData.get("SORT").trim();
  	if (sort == "?") {
  	  sort = "";
  	}
  	cuky = mi.inData.get("CUKY") == null ? '' : mi.inData.get("CUKY").trim();
  	if (cuky == "?") {
  	  cuky = "";
  	}
  	spds = mi.inData.get("SPDS") == null ? '' : mi.inData.get("SPDS").trim();
  	if (spds == "?") {
  	  spds = "";
  	}
  	tare = mi.inData.get("TARE") == null ? '' : mi.inData.get("TARE").trim();
  	if (tare == "?") {
  	  tare = "";
  	}
  	mefa = mi.inData.get("MEFA") == null ? '' : mi.inData.get("MEFA").trim();
  	if (mefa == "?") {
  	  mefa = "";
  	}
  	yied = mi.inData.get("YIED") == null ? '' : mi.inData.get("YIED").trim();
  	if (yied == "?") {
  	  yied = "";
  	}
  	cost = mi.inData.get("COST") == null ? '' : mi.inData.get("COST").trim();
  	if (cost == "?") {
  	  cost = "";
  	}
  	refa = mi.inData.get("REFA") == null ? '' : mi.inData.get("REFA").trim();
  	if (refa == "?") {
  	  refa = "";
  	}
  	unms = mi.inData.get("UNMS") == null ? '' : mi.inData.get("UNMS").trim();
  	if (unms == "?") {
  	  unms = "";
  	}
  	plco = mi.inData.get("PLCO") == null ? '' : mi.inData.get("PLCO").trim();
  	if (plco == "?") {
  	  plco = "";
  	}
  	gndr = mi.inData.get("GNDR") == null ? '' : mi.inData.get("GNDR").trim();
  	if (gndr == "?") {
  	  gndr = "";
  	}
  	cuex = mi.inData.get("CUEX") == null ? '' : mi.inData.get("CUEX").trim();
  	if (cuex == "?") {
  	  cuex = "";
  	}
  	excl = mi.inData.get("EXCL") == null ? '' : mi.inData.get("EXCL").trim();
  	if (excl == "?") {
  	  excl = "";
  	}
  	prim = mi.inData.get("PRIM") == null ? '' : mi.inData.get("PRIM").trim();
  	if (prim == "?") {
  	  prim = "";
  	}
  	ladj = mi.inData.get("LADJ") == null ? '' : mi.inData.get("LADJ").trim();
  	if (ladj == "?") {
  	  ladj = "";
  	}
  	madj = mi.inData.get("MADJ") == null ? '' : mi.inData.get("MADJ").trim();
  	if (madj == "?") {
  	  madj = "";
  	}
  	sadj = mi.inData.get("SADJ") == null ? '' : mi.inData.get("SADJ").trim();
  	if (sadj == "?") {
  	  sadj = "";
  	}
  	frdt = mi.inData.get("FRDT") == null ? '' : mi.inData.get("FRDT").trim();
  	if (frdt == "?") {
  	  frdt = "";
  	}
  	todt = mi.inData.get("TODT") == null ? '' : mi.inData.get("TODT").trim();
  	if (todt == "?") {
  	  todt = "";
  	}
  	conm = mi.inData.get("CONM") == null ? '' : mi.inData.get("CONM").trim();
  	if (conm == "?") {
  	  conm = "";
  	}
  	pote = mi.inData.get("POTE") == null ? '' : mi.inData.get("POTE").trim();
  	if (pote == "?") {
  	  pote = "";
  	}
  	targ = mi.inData.get("TARG") == null ? '' : mi.inData.get("TARG").trim();
  	if (targ == "?") {
  	  targ = "";
  	}
  	//Validate input fields
    if (!validateInput()) {
      return;
    }
    
    currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInteger();
  	currentTime = Integer.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
    writeEXTCAT();
  }
  /*
  * validateInput - Validate all the input fields - replicated from PECHK() of CRS575
  * @return false if there is any error
  *         true if pass the validation
  */
  boolean validateInput() { 
    
    if (!cono.isEmpty() ){
      if (cono.isInteger()){
        XXCONO= cono.toInteger();
      } else {
        mi.error("Company " + cono + " is invalid");
        return false;
      }
    } else {
      XXCONO= program.LDAZD.CONO;
    }
    if (cate.isEmpty()) {
      mi.error("Category cannot be blank.");
      return false;
    }
    if (cuky.isEmpty()) {
      mi.error("CutSpec Key cannot be blank.");
      return false;
    }
    if (!tare.isEmpty() && !tare.equals("Y") && !tare.equals("N")) {
      mi.error("Reports To Target is invalid, can only be Y or N.");
      return false;
    }
    if (!frdt.isEmpty() && !isDateValid(frdt)) {
      mi.error("From date is not a valid date.");
      return false;
    }
    if (!todt.isEmpty() && !isDateValid(todt)) {
      mi.error("To date is not a valid date.");
      return false;
    }
    
    
    return true;
  }
  /**
   * isDateValid - check if input string is a valid date
   *  - date format: yyyyMMdd
   * return boolean
   */
  def isDateValid(String dateStr) {
    boolean dateIsValid = true;
    Matcher matcher=
      Pattern.compile("^((2000|2400|2800|(19|2[0-9](0[48]|[2468][048]|[13579][26])))0229)\$" 
        + "|^(((19|2[0-9])[0-9]{2})02(0[1-9]|1[0-9]|2[0-8]))\$"
        + "|^(((19|2[0-9])[0-9]{2})(0[13578]|10|12)(0[1-9]|[12][0-9]|3[01]))\$" 
        + "|^(((19|2[0-9])[0-9]{2})(0[469]|11)(0[1-9]|[12][0-9]|30))\$").matcher(dateStr);
    dateIsValid = matcher.matches();
    if (dateIsValid) {
      dateIsValid = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd")) != null;
    } 
    return dateIsValid;
  }
  /*
	 * writeEXTCAT - write record to EXTCAT
	 *
	*/
	def writeEXTCAT() {
  	
	  DBAction actionEXTCAT = database.table("EXTCAT").build();
  	DBContainer EXTCAT = actionEXTCAT.getContainer();
  	EXTCAT.set("EXCONO", XXCONO);
  	EXTCAT.set("EXCATE", cate.toInteger());
  	EXTCAT.set("EXCUKY", cuky);
  	
  	if (!pcat.isEmpty()) {
  	  EXTCAT.set("EXPCAT", pcat);
  	}
  	if (!cads.isEmpty()) {
  	  EXTCAT.set("EXCADS", cads);
  	}
  	if (!camk.isEmpty()) {
  	  EXTCAT.set("EXCAMK", camk);
  	}
  	if (!sort.isEmpty()) {
  	  EXTCAT.set("EXSORT", sort.toInteger());
  	}
  	if (!spds.isEmpty()) {
  	  EXTCAT.set("EXSPDS", spds);
  	}
  	if (!tare.isEmpty()) {
  	  EXTCAT.set("EXTARE", tare);
  	}
  	if (!mefa.isEmpty()) {
  	  EXTCAT.set("EXMEFA", mefa.toDouble());
  	}
  	if (!yied.isEmpty()) {
  	  EXTCAT.set("EXYIED", yied.toDouble());
  	}
  	if (!cost.isEmpty()) {
  	  EXTCAT.set("EXCOST", cost.toDouble());
  	}
  	if (!refa.isEmpty()) {
  	  EXTCAT.set("EXREFA", refa.toDouble());
  	}
  	if (!unms.isEmpty()) {
  	  EXTCAT.set("EXUNMS", unms);
  	}
  	if (!plco.isEmpty()) {
  	  EXTCAT.set("EXPLCO", plco.toDouble());
  	}
  	if (!gndr.isEmpty()) {
  	  EXTCAT.set("EXGNDR", gndr);
  	}
  	if (!cuex.isEmpty()) {
  	  EXTCAT.set("EXCUEX", cuex);
  	}
  	if (!excl.isEmpty()) {
  	  EXTCAT.set("EXEXCL", excl);
  	}
  	if (!prim.isEmpty()) {
  	  EXTCAT.set("EXPRIM", prim);
  	}
  	if (!ladj.isEmpty()) {
  	  EXTCAT.set("EXLADJ", ladj.toDouble());
  	}
  	if (!madj.isEmpty()) {
  	  EXTCAT.set("EXMADJ", madj.toDouble());
  	}
  	if (!sadj.isEmpty()) {
  	  EXTCAT.set("EXSADJ", sadj.toDouble());
  	}
  	if (!frdt.isEmpty()) {
  	  EXTCAT.set("EXFRDT", frdt.toInteger());
  	}
  	if (!todt.isEmpty()) {
  	  EXTCAT.set("EXTODT", todt.toInteger());
  	}
  	if (!conm.isEmpty()) {
  	  EXTCAT.set("EXCONM", conm);
  	}
  	if (!pote.isEmpty()) {
  	  EXTCAT.set("EXPOTE", pote.toDouble());
  	}
  	if (!targ.isEmpty()) {
  	  EXTCAT.set("EXTARG", targ.toDouble());
  	}
  	EXTCAT.set("EXRGDT", currentDate);
  	EXTCAT.set("EXRGTM", currentTime);
  	EXTCAT.set("EXLMDT", currentDate);
  	EXTCAT.set("EXCHNO", 0);
  	EXTCAT.set("EXCHID", program.getUser());
  	actionEXTCAT.insert(EXTCAT, recordExists);
	}
	/*
   * recordExists - return record already exists error message to the MI
   *
  */
	Closure recordExists = {
	  mi.error("Record already exists");
  }
}
