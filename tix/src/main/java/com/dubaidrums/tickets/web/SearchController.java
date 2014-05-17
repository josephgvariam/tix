package com.dubaidrums.tickets.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dubaidrums.tickets.domain.PaypalTransaction;

import flexjson.JSONSerializer;

@RequestMapping("/search/**")
@Controller
public class SearchController {
	
	Logger log = LogManager.getLogger(SearchController.class);
	
	@RequestMapping(value = "/{id}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> search(@PathVariable("id") String q) {
		log.info("Searching Transactions. query: "+q);
		HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        
        List<PaypalTransaction> results = doSearch(q);
        
        log.info("Search results found: "+results.size());
        
        String json = new JSONSerializer().exclude("*.class", "description").exclude("counts").exclude("status").exclude("currency").exclude("total").include("itums").serialize(results);        
        return new ResponseEntity<String>(json, headers, HttpStatus.OK);
	}

	private List<PaypalTransaction> doSearch(String q) {
		List<PaypalTransaction> txns = PaypalTransaction.findAllPaypalTransactions();
		List<PaypalTransaction> results = new ArrayList<PaypalTransaction>(); 
		
		q = q.toLowerCase().replaceAll(" ", "");
		
		for (PaypalTransaction txn : txns) {
			StringBuilder sb = new StringBuilder();
			sb.append(txn.getContactNumber()).append(txn.getEmail()).append(txn.getFirstName()).append(txn.getLastName()).append(txn.getTxnId());
			if(sb.toString().toLowerCase().indexOf(q)>-1)
				results.add(txn);
		} 
		
		return results;
	}


}
