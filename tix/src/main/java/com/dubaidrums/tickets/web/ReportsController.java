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

import com.dubaidrums.tickets.domain.Item;
import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.service.TicketService;

import flexjson.JSONSerializer;

@RequestMapping("/reports/**")
@Controller
public class ReportsController {
	
	@Autowired
    TicketService ticketService;
	
	Logger log = LogManager.getLogger(ReportsController.class);
	
	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> reports() {
		log.info("Generating Reports...");
		HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        
        List<PaypalTransaction> results = new ArrayList<PaypalTransaction>();
        for (PaypalTransaction txn : PaypalTransaction.findAllPaypalTransactions()) {
        	if(ticketService.giveIsEventTransaction(txn)){
        		results.add(txn);
        	}
		}
        
        log.info("Reports Generated. #Transactions: "+results.size());
        
        String json = new JSONSerializer().exclude("*.class", "description").exclude("counts").exclude("status").exclude("currency").exclude("total").include("itums").serialize(results);        
        return new ResponseEntity<String>(json, headers, HttpStatus.OK);
	}



}
