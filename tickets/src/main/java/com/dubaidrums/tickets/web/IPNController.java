package com.dubaidrums.tickets.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.config.TxNamespaceHandler;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dubaidrums.tickets.service.TicketService;

@RequestMapping("/ipn/**")
@Controller
public class IPNController{
	
	Logger log = LogManager.getLogger(IPNController.class);
	
	@Autowired
    TicketService ticketService;
	
	@Autowired
	ServletContext servletContext;
	
	@Resource(name = "settings")
	private Properties settings;

    @RequestMapping
    public String index() {
        return "ipn/index";
    }
    
//    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
//    public void handleIPN(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
//    	try {
//    		log.info("------------ Handling NEW IPN ------------");
//    		Map params = getDummyParams(); 
//    		params.put("path", servletContext.getRealPath("/"));
//    		ticketService.handlePayment(params,true);
//		} catch (Exception e) {
//			log.error("Exception occurred while handlin IPN!", e);
//		}
//	}

    
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public void handleIPN(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
    	log.info("------------ Handling NEW IPN ------------");
    	String txnId = request.getParameter("txn_id");
    	try {
    		logIPN(request);
    		String res = validateIPN(request);
			log.info(txnId+" Paypal Verification status: "+res);

			// check notification validation
			if (res.equals("VERIFIED")) {
				log.info(txnId+" PayPal IPN Validation Successful!");
//				// check that paymentStatus=Completed
//				// check that txnId has not been previously processed
//				// check that receiverEmail is your Primary PayPal email
//				// check that paymentAmount/paymentCurrency are correct
//				// process payment
				
				String paymentStatus = request.getParameter("payment_status");
				String receiverEmail = request.getParameter("receiver_email");
				String txnType = request.getParameter("txn_type");
				
				if(paymentStatus==null) paymentStatus="";
				if(receiverEmail==null) receiverEmail="";
				if(txnType==null) txnType="";
				
				log.info(txnId+" Checking: txn_type="+txnType+" payment_status="+paymentStatus+" receiver_email="+receiverEmail);
				if(txnType.equalsIgnoreCase("cart") && paymentStatus.equalsIgnoreCase("completed") && receiverEmail.equalsIgnoreCase("guy@dubaidrums.com")){
					log.info(txnId+" Handling Payment.");								    	
			    	ticketService.handlePayment(getParams(request), true);	
				}
				else if(paymentStatus.equalsIgnoreCase("Refunded")){
					log.info(txnId+" Handling Refund.");
					ticketService.handleRefund(getParams(request));
				}
				else{
					log.info(txnId+" Aborting. Unable to handle IPN!");
					sendErrorMail(request, txnId+" Aborting. Unable to handle IPN!");
				}
			} else {
				log.error(txnId+" PayPal IPN Validation Failed!");
				sendErrorMail(request, txnId+" PayPal IPN Validation Failed!");
			}
		} catch (Exception e) {
			log.error(txnId+" Exception occurred while handlin IPN!", e);
			sendErrorMail(request, txnId+" Exception occurred while handlin IPN!" + e.getMessage());
		} 
    }
    
    private void sendErrorMail(HttpServletRequest request, String msg) {
    	StringBuilder sb = new StringBuilder(msg+"\n");
    	
    	if(request!=null && request.getParameterNames()!=null){
        	Enumeration en = request.getParameterNames();
	    	String paramName, paramValue;
	    	sb.append("\nPayPal IPN recieved! Dumping "+request.getParameterMap().keySet().size()+" request parameters: \n");
			while (en.hasMoreElements()) {
				paramName = (String) en.nextElement();
				paramValue = request.getParameter(paramName);
				sb.append(paramName+": "+paramValue+"\n");
			}
			
			
			String charset = request.getParameter("charset");
			if(charset==null) charset="utf-8";
			sb.append("\nValidating PayPal IPN. charset: "+charset+" URL: "+settings.getProperty("paypal.url")+"\n\nParams:\n");
			
			Enumeration en2 = request.getParameterNames();
			StringBuffer strBuffer = new StringBuffer("cmd=_notify-validate");				
			
			while (en2.hasMoreElements()) {
				paramName = (String) en2.nextElement();
				paramValue = request.getParameter(paramName);
				try {
					strBuffer.append("&").append(paramName).append("=").append(URLEncoder.encode(paramValue, charset));
				} catch (UnsupportedEncodingException e) {
					strBuffer.append("\nError occured while encoding. paramName: "+paramName+" paramValue: "+paramValue+"\n");
				}
			} 
					
			String message = sb.toString()+strBuffer.toString();		
    		try {
				ticketService.sendErrorMail(message);
			} catch (Exception e) {
				log.error("Unable to send ERROR mail. msg: "+message);
			}			
    	}else{
    		try {
				ticketService.sendErrorMail(sb.toString());
			} catch (Exception e) {
				log.error("Unable to send ERROR mail. msg: "+sb.toString());
			}
    		
    	}
    }

	private Map getParams(HttpServletRequest request){
    	Map params = new HashMap();
    	Enumeration en = request.getParameterNames();
    	
    	String paramName;
		String paramValue;
		while (en.hasMoreElements()) {
			paramName = (String) en.nextElement();
			paramValue = request.getParameter(paramName);
			params.put(paramName, paramValue);
		}
    	params.put("path", servletContext.getRealPath("/"));
    	
    	return params;
    }
    
    private String validateIPN(HttpServletRequest request) throws Exception{
    	log.info("Validating PayPal IPN...");	

		// read post from PayPal system and add 'cmd'
		Enumeration en = request.getParameterNames();
		StringBuffer strBuffer = new StringBuffer("cmd=_notify-validate");
		String paramName;
		String paramValue;
		String charset = request.getParameter("charset");
		if(charset==null) charset="utf-8";
		
		while (en.hasMoreElements()) {
			paramName = (String) en.nextElement();
			paramValue = request.getParameter(paramName);
			strBuffer.append("&").append(paramName).append("=").append(URLEncoder.encode(paramValue, charset));
		}

		// post back to PayPal system to validate
		// NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
		// using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
		// and configured for older versions.
		String url = settings.getProperty("paypal.url");
		log.info("Validating IPN against: "+url);
		URL u = new URL(url);
		HttpsURLConnection uc = (HttpsURLConnection) u.openConnection();
		uc.setDoOutput(true);
		uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		uc.setRequestProperty("Host", "www.paypal.com");
		PrintWriter pw = new PrintWriter(uc.getOutputStream());
		pw.println(strBuffer.toString());
		pw.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		String res = in.readLine();
		in.close();
		
		return res;
    }

	private void logIPN(HttpServletRequest request){
    	Enumeration en = request.getParameterNames();
    	if(en!=null && request!=null){
	    	log.info("PayPal IPN recieved! Dumping "+request.getParameterMap().keySet().size()+" request parameters: ");
	    	StringBuilder sb = new StringBuilder();
	    	String paramName, paramValue;
			while (en.hasMoreElements()) {
				paramName = (String) en.nextElement();
				paramValue = request.getParameter(paramName);
				sb.append(paramName+": "+paramValue+"\n");
			}    	
			log.info(sb.toString());
    	}
    }
    
    private void logPayPalError(HttpServletRequest request){
    	Enumeration en = request.getParameterNames();
    	if(en!=null && request!=null){
	    	log.error("PayPal IPN failed! Dumping "+request.getParameterMap().keySet().size()+" request parameters: ");
	    	StringBuilder sb = new StringBuilder();
	    	String paramName, paramValue;
			while (en.hasMoreElements()) {
				paramName = (String) en.nextElement();
				paramValue = request.getParameter(paramName);
				sb.append(paramName+": "+paramValue+"\n");
			}    	
			log.error(sb.toString());
    	}
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
		params.put("receiver_email","guy@dubaidrums.com");
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
