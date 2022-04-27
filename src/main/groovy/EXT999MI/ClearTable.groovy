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
 
 
 /*
 *Modification area - M3
 *Nbr          Date         User id     Description
 *WZHAO		     20220317     WZHAO      New MI to delete records from the XtendM3 table by compnay and range of date
 */
 
 /*
  * Update reocrd in file
  */

public class ClearTable extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  //Input fields
  private String cono;
  private int zfrd;
  private int ztrd;
  private String zfrdS;
  private String ztrdS;
  private String file;
  private String fldi;//field
  private String zind;//index number
 
  
  private int XXCONO;
  
  private int numberOfRecordsDeleted=0;

 
  
  public ClearTable(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
	this.mi = mi;
	this.database = database;
	this.miCaller = miCaller;
	this.logger = logger;
	this.program = program;
  }
  
  public void main() {
	//Fetch input fields from MI
	//Not mandatory using getInputField method, mandatory fields using mi.in as it will validate if fields has value or not
	cono =getInputField("CONO");
	zfrdS = getInputField("ZFRD");
	ztrdS = getInputField("ZTRD");
	file = mi.in.get("FILE");
	fldi = getInputField("FLDI");
	zind = mi.in.get("ZIND");
	if (zfrdS =="") {
		zfrd=0;
	} else {
		zfrd=zfrdS.toInteger();
	}
	if (ztrdS =="") {
		ztrd=0;
	} else {
		ztrd=ztrdS.toInteger();
	}
	
	//Validate input fields
	if (!validateInput()) return;
	
	
	//Create expression for the input fields
	ExpressionFactory expression = database.getExpressionFactory(file);
	if (zfrd!=0)expression = expression.ge(fldi, Integer.toString(zfrd));
	if (ztrd!=0) {
		if (zfrd!=0) {
			expression = expression.and(expression.le(fldi, Integer.toString(ztrd)));
		} else {
			expression = expression.le(fldi, Integer.toString(ztrd));
		}
	}
	
	//Update to database file
	DBAction Actionfile = database.table(file).index(zind).matching(expression).build();

	DBContainer file = Actionfile.getContainer();
	if (XXCONO !=0){
	  file.set("EXCONO", XXCONO);
	}
	
	int numberOfKey=1;
	//If input CONO =0, then ingore CONO as the key field
	if (XXCONO ==0){
	  numberOfKey =0;
	}
	numberOfRecordsDeleted=0;
	if (!Actionfile.readAllLock(file, numberOfKey,deleteFile)){
	  mi.error("Record does not exists");
	  return;
	}

	mi.outData.put("ZNOR", Integer.toString(numberOfRecordsDeleted));
	mi.write();
	
  }
  
  
  /*
   *  getInputField - retrieve input field value from MI
   *  @parm fieldName input field name
   *  @return input field value from MI
   */
  String getInputField(String fieldName){
	String inputField    = mi.inData.get(fieldName);
	if (inputField ==null){
	  inputField=""
	} else {
	  inputField= inputField.trim();
	}
	return inputField;
  }
  
  /*
   * validateInput - Validate all the input fields
   * @return false if there is any error
   *         true if pass the validation
   */
  boolean validateInput(){
	
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
	
	if (zfrd!=0 && !validDate(Integer.toString(zfrd))){
	  mi.error("From date " + zfrd + " is invalid");
	  return false;
	}
	  
	if (ztrd!=0&& !validDate(Integer.toString(ztrd))){
		mi.error("To date " + ztrd + " is invalid");
		return false;
	}
	
	if (ztrd < zfrd) {
		mi.error("To date can't be earlier than from date");
		return false;
	}
	
	
	if (!validCFIHEA(file)){
		return false;
	}
	
	if (!validCFIHEA(file+zind)){
		return false;
	}
	
	if (!fldi.isEmpty() && !validCFIFFD(file,fldi)){
		return false;
	}
	
	return true;
  }
  
  /*
   * validCFIHEA - Validate CFIHEA
   * @parm  file file
   * @return false if there is any error
   *         true file exist in MNS120
   */
  def boolean validCFIHEA(String file){
	DBContainer CFIHEA = database.createContainer("CFIHEA");
	DBAction query = database.table("CFIHEA")
						  .index("00")
						  .selection("FIFILE")
						  .build();
						  
	CFIHEA.set("FIFILE", file);

	if (query.read(CFIHEA)){
	  return true;
	} else {
	  mi.error("File "+file +  " is invalid, setup the XtendM3 tables in MNS120 first");
	  return false;
	}
  
  }
  
  /*
   * validCFIFFD - Validate CFIFFD
   * @parm  file file
   *         fldi field name
   * @return false if there is any error
   *         true file exist in MNS121
   */
  def boolean validCFIFFD(String file, String fldi){
	DBContainer CFIFFD = database.createContainer("CFIFFD");
	DBAction query = database.table("CFIFFD")
						  .index("00")
						  .selection("WHFILE")
						  .build();
						  
	CFIFFD.set("WHFILE", file);
	CFIFFD.set("WHFLDI", fldi);
	
	if (query.read(CFIFFD)){
	  return true;
	} else {
	  mi.error("Field "+fldi +  " is invalid");
	  return false;
	}
  
  }
  /*
   * validDate - Validate transaction date
   * @parm  transaction date in the format of yyyymmdd
   * @return false if it is not a valid date
   *         true if it is a valid date
   */
  def boolean validDate(String date){
	boolean validRecord = false;
	def parameters = ["FRDT":date, "TODT":date];
	Closure<?> handler = { Map<String, String> response ->
	  logger.debug("User ${response}")
	  if (response.containsKey('errorMsid')){
		validRecord = false;
	  } else {
		validRecord = true;
	  }
	};
	miCaller.call("CRS900MI", "LstSysCalendar", parameters, handler);
	return validRecord;
  }
  
  
  /*
   * deleteFile - call back function to delete file
   */
  Closure deleteFile = { LockedResult file ->
	  file.delete()
	  numberOfRecordsDeleted++;
	}
	
}
