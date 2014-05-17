package com.dubaidrums.tickets.service;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dubaidrums.tickets.domain.Item;
import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.domain.TransactionLog;


@Service
@PropertySource("classpath:/app.properties")
@Transactional
public class TicketServiceImpl implements TicketService {
	
	@Autowired
    Environment env;
	
	public String getEventName() {
		return env.getProperty("eventname");
	}
	
    public String getItums(PaypalTransaction p){
    	StringBuilder sb = new StringBuilder();
    	String event = getEventName();
    	for (Item item : p.getItems()) {
    		//if(item.getItemNumber().startsWith(event)){
    			sb.append(item.getItemNumber()).append(" x").append(item.getQuantity()).append(" ");
    		//}
		}
    	
    	return sb.toString();
    }
	
    public boolean giveIsEventTransaction(PaypalTransaction p){
    	String event = getEventName();
    	for (Item item : p.getItems()) {
			if(item.getItemNumber().startsWith(event)){
				return true;
			}
		}
    	return false;
    }	
	
    public String getStatus(PaypalTransaction p) {
    	boolean flag = false;
    	String event = getEventName();
    	for (Iterator iterator = p.getItems().iterator(); iterator.hasNext();) {
			Item i = (Item) iterator.next();
			if(i.getItemNumber().startsWith(event)){ //|| i.getItemNumber().startsWith("FMDD22022013")){
				flag = true;
				break;
			}
		}
    	
    	if(!flag)
    		return "1#Invalid Ticket";
    	
		for (Iterator iterator = p.getLogs().iterator(); iterator.hasNext();) {
			TransactionLog t = (TransactionLog) iterator.next();
			if(t.getCode().equals("300")) return "1#Duplicate Ticket / Previously Scanned";
		}
		
		for (Iterator iterator = p.getLogs().iterator(); iterator.hasNext();) {
			TransactionLog t = (TransactionLog) iterator.next();
			if(t.getCode().equals("200")) return "2#Refund Issued";
		}
		
		return "0#OK";
	}	
	
}
