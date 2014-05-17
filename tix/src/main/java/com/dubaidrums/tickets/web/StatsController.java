package com.dubaidrums.tickets.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dubaidrums.tickets.domain.Item;
import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.domain.TransactionLog;
import com.dubaidrums.tickets.service.TicketService;

import flexjson.JSONSerializer;

@RequestMapping("/stats/**")
@Controller
public class StatsController {
	
	@Autowired
    TicketService ticketService;
	
	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> reports() {
		HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        
        String json = new JSONSerializer().exclude("*.class", "description").serialize(getCounts());        
        return new ResponseEntity<String>(json, headers, HttpStatus.OK);
	}
	
    public String getCounts(){
    	int totalTickets = 0;
    	int scannedTickets = 0;
    	int totalAdultCount = 0;
    	int totalKidCount = 0;
    	int arrivedAdultCount = 0;
    	int arrivedKidCount = 0;
    	String event = giveEvent();
    	
    	List<PaypalTransaction> txns = PaypalTransaction.findAllPaypalTransactions();
    	for (PaypalTransaction txn : txns) {
    		boolean isEventTransaction = ticketService.giveIsEventTransaction(txn);//txn.giveIsEventTransaction();
    		boolean isTransactionScanned = txn.getScanned();
    		
    		if(isEventTransaction){
    			totalTickets++;
    			if(isTransactionScanned) scannedTickets++;
    			
    			for (Item item : txn.getItems()) {
    				if(item.getItemNumber().startsWith(event)){
    					if(item.getItemNumber().endsWith("ADULT")){
    						int q = Integer.parseInt(item.getQuantity());
    						totalAdultCount += q;
    						if(isTransactionScanned)
    							arrivedAdultCount += q;
    					}
    					else if(item.getItemNumber().endsWith("CHILD")){
    						int q = Integer.parseInt(item.getQuantity());
    						totalKidCount += q;
    						if(isTransactionScanned) 
    							arrivedKidCount += q;
    					}
    				}
    			}
    		}
    		
			
		}
    	
    	return scannedTickets+"#"+totalTickets+"#"+arrivedAdultCount+"#"+totalAdultCount+"#"+arrivedKidCount+"#"+totalKidCount;
    }

    
    private String giveEvent(){
    	//return "FMDD26042013";
    	return ticketService.getEventName();
    }


}
