package com.dubaidrums.tickets.web;

import com.dubaidrums.tickets.domain.Item;
import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.domain.TransactionLog;
import com.dubaidrums.tickets.service.TicketService;

import flexjson.JSONSerializer;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/paypaltransactions")
@Controller
@RooWebScaffold(path = "paypaltransactions", formBackingObject = PaypalTransaction.class)
@RooWebJson(jsonObject = PaypalTransaction.class)
public class PaypalTransactionController {
	
	@Autowired
    TicketService ticketService;
	
	Logger log = LogManager.getLogger(PaypalTransactionController.class);

	@RequestMapping(value = "/{id}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") String txnId) {
		log.info("txnId: "+txnId+", msg: inputted");
		PaypalTransaction pt;
		try{
			pt = PaypalTransaction.findPaypalTransactionsByTxnIdEquals(txnId.toUpperCase()).getSingleResult();
			log.info("txnId: "+txnId+", msg: transaction found, status: "+ticketService.getStatus(pt)+", id: "+pt.getId());
		}catch(Exception e){
			pt = null;
			log.info("txnId: "+txnId+", msg: no transaction found, status: 3#Transaction Not Found");
		}
		
        //PaypalTransaction paypalTransaction = PaypalTransaction.findPaypalTransaction(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        if (pt == null) {
//        	String counts = new PaypalTransaction().getCounts();
//            return new ResponseEntity<String>("{\"counts\":\""+counts+"\"}",headers, HttpStatus.OK);
        	return new ResponseEntity<String>("{}",headers, HttpStatus.OK);
        }

        pt.setStatus(ticketService.getStatus(pt));
        ResponseEntity<String> re = new ResponseEntity<String>(pt.toJson(), headers, HttpStatus.OK);
        
        TransactionLog t = new TransactionLog();
        t.setCode("300");
        t.setMessage("Barcode scanned.");
        t.setTstamp(new Date());
        t.setTxn(pt);
        t.persist();
        
        log.info("txnId: "+txnId+", msg: transaction updated, id: "+pt.getId());
        
        return re;
    }

//	@RequestMapping(headers = "Accept=application/json")
//    @ResponseBody
//    public ResponseEntity<String> listJson() {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json; charset=utf-8");
//        List<PaypalTransaction> result = PaypalTransaction.findAllPaypalTransactions();
//        return new ResponseEntity<String>(PaypalTransaction.toJsonArray(result), headers, HttpStatus.OK);
//    }
//
//	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
//    public ResponseEntity<String> createFromJson(@RequestBody String json) {
//        PaypalTransaction paypalTransaction = PaypalTransaction.fromJsonToPaypalTransaction(json);
//        paypalTransaction.persist();
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
//    }
//
//	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
//    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
//        for (PaypalTransaction paypalTransaction: PaypalTransaction.fromJsonArrayToPaypalTransactions(json)) {
//            paypalTransaction.persist();
//        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
//    }
//
//	@RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
//    public ResponseEntity<String> updateFromJson(@RequestBody String json) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        PaypalTransaction paypalTransaction = PaypalTransaction.fromJsonToPaypalTransaction(json);
//        if (paypalTransaction.merge() == null) {
//            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<String>(headers, HttpStatus.OK);
//    }
//
//	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
//    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        for (PaypalTransaction paypalTransaction: PaypalTransaction.fromJsonArrayToPaypalTransactions(json)) {
//            if (paypalTransaction.merge() == null) {
//                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
//            }
//        }
//        return new ResponseEntity<String>(headers, HttpStatus.OK);
//    }
//
//	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
//    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
//        PaypalTransaction paypalTransaction = PaypalTransaction.findPaypalTransaction(id);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        if (paypalTransaction == null) {
//            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
//        }
//        paypalTransaction.remove();
//        return new ResponseEntity<String>(headers, HttpStatus.OK);
//    }
//
//	@RequestMapping(params = "find=ByTxnIdEquals", headers = "Accept=application/json")
//    @ResponseBody
//    public ResponseEntity<String> jsonFindPaypalTransactionsByTxnIdEquals(@RequestParam("txnId") String txnId) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json; charset=utf-8");
//        return new ResponseEntity<String>(PaypalTransaction.toJsonArray(PaypalTransaction.findPaypalTransactionsByTxnIdEquals(txnId).getResultList()), headers, HttpStatus.OK);
//    }
//
//	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
//    public String create(@Valid PaypalTransaction paypalTransaction, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
//        if (bindingResult.hasErrors()) {
//            populateEditForm(uiModel, paypalTransaction);
//            return "paypaltransactions/create";
//        }
//        uiModel.asMap().clear();
//        paypalTransaction.persist();
//        return "redirect:/paypaltransactions/" + encodeUrlPathSegment(paypalTransaction.getId().toString(), httpServletRequest);
//    }
//
//	@RequestMapping(params = "form", produces = "text/html")
//    public String createForm(Model uiModel) {
//        populateEditForm(uiModel, new PaypalTransaction());
//        return "paypaltransactions/create";
//    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("paypaltransaction", PaypalTransaction.findPaypalTransaction(id));
        uiModel.addAttribute("itemId", id);
        return "paypaltransactions/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("paypaltransactions", PaypalTransaction.findPaypalTransactionEntries(firstResult, sizeNo));
            float nrOfPages = (float) PaypalTransaction.countPaypalTransactions() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("paypaltransactions", PaypalTransaction.findAllPaypalTransactions());
        }
        return "paypaltransactions/list";
    }

//	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
//    public String update(@Valid PaypalTransaction paypalTransaction, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
//        if (bindingResult.hasErrors()) {
//            populateEditForm(uiModel, paypalTransaction);
//            return "paypaltransactions/update";
//        }
//        uiModel.asMap().clear();
//        paypalTransaction.merge();
//        return "redirect:/paypaltransactions/" + encodeUrlPathSegment(paypalTransaction.getId().toString(), httpServletRequest);
//    }
//
//	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
//    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
//        populateEditForm(uiModel, PaypalTransaction.findPaypalTransaction(id));
//        return "paypaltransactions/update";
//    }
//
//	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
//    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
//        PaypalTransaction paypalTransaction = PaypalTransaction.findPaypalTransaction(id);
//        paypalTransaction.remove();
//        uiModel.asMap().clear();
//        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
//        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
//        return "redirect:/paypaltransactions";
//    }

	void populateEditForm(Model uiModel, PaypalTransaction paypalTransaction) {
        uiModel.addAttribute("paypalTransaction", paypalTransaction);
        uiModel.addAttribute("items", Item.findAllItems());
        uiModel.addAttribute("transactionlogs", TransactionLog.findAllTransactionLogs());
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
}
