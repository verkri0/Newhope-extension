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
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;

/*
 *Modification area - M3
 *Nbr               Date      User id     Description
 *NHG_R_007         20220718  RDRIESSEN   custom API required for an OIS300 script extending multiple columns 
  *
*/

public class GetCharge extends ExtendM3Transaction {
  private final MIAPI mi;
  private final DatabaseAPI database;
  private final MICallerAPI miCaller;
  private final LoggerAPI logger;
  private final ProgramAPI program;
  private final IonAPI ion;
  
  //Input fields
  private String puno;
  private String orno;
  private String oPBAM;
  private int XXCONO;
  private double pbamT;
  private double brlaT;
  private double brlaY;
   
 /*
  * Get Purchase Authorisation extension table row
 */
  public GetCharge(MIAPI mi, DatabaseAPI database, MICallerAPI miCaller, LoggerAPI logger, ProgramAPI program, IonAPI ion) {
    this.mi = mi;
    this.database = database;
  	this.miCaller = miCaller;
  	this.logger = logger;
  	this.program = program;
	  this.ion = ion;
    
  }
  
  public void main() {
  	orno = mi.inData.get("ORNO") == null ? '' : mi.inData.get("ORNO").trim();
  	if (orno == "?") {
  	  orno = "";
  	}
  	
    if (orno.isEmpty()) {
      mi.error("CO number must be entered");
      return;
    }
    XXCONO = (Integer)program.LDAZD.CONO;
    
    pbamT = 0;
    
    def params = ["ORNO": orno]; 
    def callback = {Map<String, String> response ->
      if(response.PBAM != null){  oPBAM = response.PBAM;
        pbamT = pbamT + oPBAM.toDouble();
      }
    }
    miCaller.call("OIS100MI","LstOrderCharge", params, callback);   
    
    def df = new DecimalFormat("#0.00");
    def pbamX = df.format(pbamT);
    
    DBAction query = database.table("OOHEAD").index("00").selection("OAORNO", "OABRLA", "OAUCA3", "OANTAM").build();
    DBContainer container = query.getContainer();
    container.set("OACONO", XXCONO);
    container.set("OAORNO", orno);
    if (query.read(container)) {
      brlaT = container.get("OABRLA").toString().toDouble();
      brlaY = pbamT + brlaT;
      
      def brlaX = df.format(brlaY);
      mi.outData.put("CONO", XXCONO.toString());
      mi.outData.put("ORNO", container.get("OAORNO").toString());
      mi.outData.put("PBAM", pbamX.toString());
      mi.outData.put("BRLA", brlaX.toString());
      mi.outData.put("UCA3", container.get("OAUCA3").toString());
      mi.write();
      } else {
        mi.error("Record does not exist in OOHEAD.");
      return;
    }
  }
}
