package com.dubaidrums.tickets.web;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dubaidrums.tickets.service.*;


@RequestMapping("/history/**")
@Controller
public class HistoryController{
	
	Logger log = LogManager.getLogger(HistoryController.class);
	
	@Autowired
    TicketService ticketService;
	
	@Autowired
	ServletContext servletContext;
	
    @RequestMapping
    public String index() {
        return "history/index";
    }
    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void handleHistory(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {

		log.info("Handling History...");
		
		String path = servletContext.getRealPath("/");
		String sCurrentLine="";
		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss MMM dd, yyyy z");
		SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		sdf1.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		sdf2.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
		
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(path+"history/download3.csv"));    		 
        	Map<String,Map> p = new HashMap<String,Map>();
        	
			while ((sCurrentLine = br.readLine()) != null) {
				String[] s = sCurrentLine.split(",",-1);
				String date = s[0];
				String time = s[1];
				String timezone = s[2];
				String name = s[3];
				String type = s[4];
				String status = s[5];
				String currency = s[6];
				String gross = s[7];
				String from = s[8];
				String to = s[9];
				String txnId = s[10];
				String itemName = s[11];
				String itemNumber = s[12];
				String parentTxnId = s[13];
				String quantity = s[14];
				String contactNumber = s[15];
				
//				if(!to.toLowerCase().equals("guy@dubaidrums.com") || !(status.equals("Completed") || status.equals("Cleared"))     ){
//					System.out.println("Not Processed: "+sCurrentLine);
//					continue;
//				}
				
				if(type.startsWith("Shopping Cart ") && to.toLowerCase().equals("guy@dubaidrums.com")){
					Map params;
					if(p.containsKey(txnId)){
						params = p.get(txnId);
					}else{
						params = new HashMap();
						params.put("path",path);
						params.put("txn_id", txnId);
						params.put("mc_currency",currency);
						params.put("contact_phone", contactNumber);
						
						String datetime = sdf1.format(sdf2.parse(s[0]+" "+s[1]));
						params.put("payment_date", datetime);
					}
					
					
					if(type.equals("Shopping Cart Payment Received")){
						String fName,lName;
						int i = name.lastIndexOf(" ");
						if(i==-1){
							fName = name;
							lName = "";
						}else{
							fName = name.substring(0,i);
							lName = name.substring(i+1,name.length());
						}
						params.put("first_name",fName);
						params.put("last_name",lName);
						params.put("payer_email",from);
						params.put("mc_gross",gross);
					}
					else if(type.equals("Shopping Cart Item")){
						int i = getItemIndex(params);
						params.put("item_name"+i,itemName);
						params.put("item_number"+i,itemNumber);
						params.put("quantity"+i,quantity);
						params.put("mc_gross_"+i,gross);
					}
					
					
					p.put(txnId, params);
				}
				
			}
			
			
			Set txns = p.keySet();
			for (Iterator it = txns.iterator(); it.hasNext();) {
				String k = (String) it.next();
				Map m = p.get(k);
				boolean sendMail = checkSendMail(m);
				ticketService.handlePayment(m, sendMail);
			}
			
			br.close();
			log.info("Finished Processing Payment History!");
			
			br = new BufferedReader(new FileReader(path+"history/download3.csv"));
			while ((sCurrentLine = br.readLine()) != null) {
				String[] s = sCurrentLine.split(",",-1);
				String date = s[0];
				String time = s[1];
				String timezone = s[2];
				String name = s[3];
				String type = s[4];
				String status = s[5];
				String currency = s[6];
				String gross = s[7];
				String from = s[8];
				String to = s[9];
				String txnId = s[10];
				String itemName = s[11];
				String itemNumber = s[12];
				String parentTxnId = s[13];
				String quantity = s[14];
				String contactNumber = s[15];
				
				if(type.equals("Refund")){
					Map params = new HashMap();
					params.put("txn_id", txnId);
					params.put("parent_txn_id", parentTxnId);
					params.put("mc_gross", gross);
					params.put("mc_currency", currency);
					
					String datetime = sdf1.format(sdf2.parse(s[0]+" "+s[1]));
					params.put("payment_date", datetime);
					
					ticketService.handleRefund(params);
				}
			}
			
			log.info("Finished Processing Refund History!");
			
			
		} catch (Exception e) {
			log.error("LINE: "+sCurrentLine);
			log.error("Exception occurred while handlin History!", e);
		}
	}


	
	private boolean checkSendMail(Map m) {
//		Set keys = m.keySet();
//		boolean sendEmail = false;
//		for (Iterator it = keys.iterator(); it.hasNext();) {
//			String key = (String) it.next();
//			if(key.startsWith("item_number")){
//				String itemNumber = (String) m.get(key);
//				if(itemNumber.startsWith("FMDD30112012")){
//					sendEmail = true;
//				}
//			}
//		}
//		return sendEmail;
		return true;
		//return false;
	}

	private int getItemIndex(Map params) {
		Set keys = params.keySet();
		int count=0;
		for (Iterator it = keys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			if(key.startsWith("item_name")){
				count++;
			}
		}
		return count+1;
	}

	private Map getDummyParams(){
		Map params = new HashMap();
		params.put("custom","xyz123");
		params.put("residence_country","US");
		params.put("test_ipn","1");
		params.put("charset","windows-1252");
		params.put("invoice","abc1234");
		params.put("mc_fee","0");
		params.put("business","seller@paypalsandbox.com");
		params.put("payment_type","instant");
		params.put("mc_shipping","3.02");
		params.put("receiver_id","TESTSELLERID1");
		params.put("mc_handling","2.06");
		params.put("txn_type","cart");
		params.put("verify_sign","Ax3GSVo.egVxKVw8lJvoe.JhMqzwA0p4CRHlZ40e5QeeyEs0mZxboFKB");
		params.put("payer_id","TESTBUYERID01");
		params.put("payer_status","verified");
		params.put("notify_version","2.4");
		params.put("tax","0");
		params.put("receiver_email","seller@paypalsandbox.com");
		params.put("payment_status","Completed");
		params.put("mc_handling1","1.67");
		params.put("payment_date","03:24:00 Nov 09, 2012 PST");
		params.put("mc_shipping1","1.02");

		params.put("txn_id","123");
		params.put("mc_currency","USD");
		params.put("first_name","Pattu");
		params.put("last_name","Fry");
		params.put("payer_email","codeshag@gmail.com");

		params.put("item_name1","Adult Ticket 200 AED for Nov 30 2012");
		params.put("item_number1","FMDD30112012_ADULT");
		params.put("quantity1","1");
		params.put("mc_gross_1","9163.35");

		params.put("item_name2","Child Ticket (6-13 years) 100 AED for Nov 30 2012");
		params.put("item_number2","FMDD30112012_CHILD");
		params.put("quantity2","11");
		params.put("mc_gross_2","9954.46");

		params.put("mc_gross","9217.81");
		
		return params;
	}
}
