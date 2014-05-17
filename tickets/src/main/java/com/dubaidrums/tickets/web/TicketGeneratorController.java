package com.dubaidrums.tickets.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.service.TicketService;


@RequestMapping("/generateticket/**")
@Controller
public class TicketGeneratorController {
	
    @Autowired
    TicketService ticketService;
	
	@Autowired
	ServletContext servletContext;
	
	@RequestMapping(produces = "text/html")
	public String create(){
		return "generateticket/form";
	}
	
	
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String generate(HttpServletRequest request) {
    	SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss MMM dd, yyyy z");
    	
    	Map params = new HashMap();
    	
    	String path = servletContext.getRealPath("/");
    	Date d = new Date();
    	params.put("path",path);
		params.put("txn_id", "DD"+d.getTime());
		params.put("mc_currency",request.getParameter("currency").toUpperCase());
		params.put("contact_phone", request.getParameter("contactNumber"));
		params.put("payment_date", sdf1.format(d));
		params.put("first_name",request.getParameter("firstName"));
		params.put("last_name",request.getParameter("lastName"));
		params.put("payer_email",request.getParameter("email"));
		params.put("mc_gross",request.getParameter("total"));
		
		params.put("comment",request.getParameter("comment"));
		
		String event = request.getParameter("event");
		String eventDate = request.getParameter("eventdate");
		int numAdult = getInt(request.getParameter("numAdults"));
		int numChild = getInt(request.getParameter("numChild"));
		int numBaby = getInt(request.getParameter("numBaby"));
		String amtAdult = request.getParameter("amtAdults");
		String amtChild = request.getParameter("amtChild");
		String amtBaby = request.getParameter("amtBaby");
		String adultRate = request.getParameter("adultRate");
		String childRate = request.getParameter("childRate");
		
		int i=1;
		
		if(numAdult!=0){
			params.put("item_name"+i,"Adult Ticket "+adultRate+" AED for "+eventDate);
			params.put("item_number"+i,event+"_ADULT");
			params.put("quantity"+i,numAdult+"");
			params.put("mc_gross_"+i,amtAdult+"");
			i++;
		}
		
		if(numChild!=0){
			params.put("item_name"+i,"Child Ticket (6-13 years) "+childRate+" AED for "+eventDate);
			params.put("item_number"+i,event+"_CHILD");
			params.put("quantity"+i,numChild+"");
			params.put("mc_gross_"+i,amtChild+"");
			i++;
		}
		
		if(numBaby!=0){
			params.put("item_name"+i,"Child Ticket (under 6 years) Free for "+eventDate);
			params.put("item_number"+i,event+"_BABY");
			params.put("quantity"+i,numBaby+"");
			params.put("mc_gross_"+i,amtBaby+"");
			i++;
		}
		

    	
		ticketService.handlePayment(params, true);
		
//    	Enumeration enParams = request.getParameterNames(); 
//    	while(enParams.hasMoreElements()){
//    	 String paramName = (String)enParams.nextElement();
//    	 System.out.println("Attribute Name - "+paramName+", Value - "+request.getParameter(paramName));
//    	}
    	
    	return "redirect:/paypaltransactions/";
//        ticketService.savePaypalTransaction(paypalTransaction);
//        return "redirect:/paypaltransactions/" + encodeUrlPathSegment(paypalTransaction.getId().toString(), httpServletRequest);
    }
    
    private int getInt(String s){
    	try{
    		return Integer.parseInt(s.trim());
    	}catch(Exception e){
    		return 0;
    	}
    }
    
    

}
