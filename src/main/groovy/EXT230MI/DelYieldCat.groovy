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
*Nbr            Date      User id     Description
*FD38740        20220808  XWZHAO      Yield category
*/
/*
 * Delete records from table EXTCAT
*/
public class DelYieldCat extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  
  //Input fields
  private String cono;
  private String cate;
  private String cuky;
  
  private int XXCONO;
  
  public DelYieldCat(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program) {
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
  	cuky = mi.inData.get("CUKY") == null ? '' : mi.inData.get("CUKY").trim();
  	if (cuky == "?") {
  	  cuky = "";
  	}
  	// validate inputs
  	if (!cono.isEmpty() ){
        if (cono.isInteger()){
            XXCONO= cono.toInteger();
        } else {
            mi.error("Company " + cono + " is invalid");
            return;
        }
    } else {
        XXCONO= program.LDAZD.CONO;
    }
    if (cate.isEmpty()){
        mi.error("Category must be entered");
        return;
    }
    if (cuky.isEmpty()) {
      mi.error("CutSpec Key cannot be blank.");
      return;
    }
    DBAction actionEXTCAT = database.table("EXTCAT").index("00").build();
    DBContainer EXTCAT = actionEXTCAT.getContainer();
    EXTCAT.set("EXCONO", XXCONO);
    EXTCAT.set("EXCATE", cate.toInteger());
    EXTCAT.set("EXCUKY", cuky);
    
    if (!actionEXTCAT.readLock(EXTCAT, deleteEXTCAT)){
      mi.error("Record does not exists");
    } 
  }
  /*
   * deleteEXTCAT - delete one record from EXTCAT
   */
  Closure deleteEXTCAT = { LockedResult EXTCAT ->

    EXTCAT.delete()

  }
   
}
