package com.dubaidrums.tickets.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.service.TicketService;

import flexjson.JSONSerializer;

@RequestMapping("/emails/**")
@Controller
public class EmailController {
	@Autowired
    TicketService ticketService;
	
	
	Logger log = LogManager.getLogger(EmailController.class);
	
	@RequestMapping(produces = "text/html")
    public @ResponseBody String getEmails() {
		log.info("Generating emails...");

		StringBuilder results = new StringBuilder();
        for (PaypalTransaction txn : PaypalTransaction.findAllPaypalTransactions()) {
        	if(ticketService.giveIsEventTransaction(txn)){
        		results.append(txn.getFirstName()+" "+txn.getLastName()+" "+txn.getEmail()+" "+txn.getContactNumber()).append("<br/>");
        	}
		}
        
        return results.toString();
	}



}
