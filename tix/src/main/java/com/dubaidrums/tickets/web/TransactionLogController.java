package com.dubaidrums.tickets.web;

import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.domain.TransactionLog;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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

@RequestMapping("/transactionlogs")
@Controller
@RooWebScaffold(path = "transactionlogs", formBackingObject = TransactionLog.class)
@RooWebJson(jsonObject = TransactionLog.class)
public class TransactionLogController {

//	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
//    public String create(@Valid TransactionLog transactionLog, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
//        if (bindingResult.hasErrors()) {
//            populateEditForm(uiModel, transactionLog);
//            return "transactionlogs/create";
//        }
//        uiModel.asMap().clear();
//        transactionLog.persist();
//        return "redirect:/transactionlogs/" + encodeUrlPathSegment(transactionLog.getId().toString(), httpServletRequest);
//    }
//
//	@RequestMapping(params = "form", produces = "text/html")
//    public String createForm(Model uiModel) {
//        populateEditForm(uiModel, new TransactionLog());
//        return "transactionlogs/create";
//    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("transactionlog", TransactionLog.findTransactionLog(id));
        uiModel.addAttribute("itemId", id);
        return "transactionlogs/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("transactionlogs", TransactionLog.findTransactionLogEntries(firstResult, sizeNo));
            float nrOfPages = (float) TransactionLog.countTransactionLogs() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("transactionlogs", TransactionLog.findAllTransactionLogs());
        }
        addDateTimeFormatPatterns(uiModel);
        return "transactionlogs/list";
    }

//	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
//    public String update(@Valid TransactionLog transactionLog, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
//        if (bindingResult.hasErrors()) {
//            populateEditForm(uiModel, transactionLog);
//            return "transactionlogs/update";
//        }
//        uiModel.asMap().clear();
//        transactionLog.merge();
//        return "redirect:/transactionlogs/" + encodeUrlPathSegment(transactionLog.getId().toString(), httpServletRequest);
//    }
//
//	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
//    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
//        populateEditForm(uiModel, TransactionLog.findTransactionLog(id));
//        return "transactionlogs/update";
//    }
//
//	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
//    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
//        TransactionLog transactionLog = TransactionLog.findTransactionLog(id);
//        transactionLog.remove();
//        uiModel.asMap().clear();
//        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
//        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
//        return "redirect:/transactionlogs";
//    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("transactionLog_tstamp_date_format", "yyyy-MM-dd HH:mm:ss");
    }

	void populateEditForm(Model uiModel, TransactionLog transactionLog) {
        uiModel.addAttribute("transactionLog", transactionLog);
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("paypaltransactions", PaypalTransaction.findAllPaypalTransactions());
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

	@RequestMapping(value = "/{id}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> showJson(@PathVariable("id") Long id) {
        TransactionLog transactionLog = TransactionLog.findTransactionLog(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        if (transactionLog == null) {
            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(transactionLog.toJson(), headers, HttpStatus.OK);
    }

	@RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        List<TransactionLog> result = TransactionLog.findAllTransactionLogs();
        return new ResponseEntity<String>(TransactionLog.toJsonArray(result), headers, HttpStatus.OK);
    }

//	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
//    public ResponseEntity<String> createFromJson(@RequestBody String json) {
//        TransactionLog transactionLog = TransactionLog.fromJsonToTransactionLog(json);
//        transactionLog.persist();
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
//    }
//
//	@RequestMapping(value = "/jsonArray", method = RequestMethod.POST, headers = "Accept=application/json")
//    public ResponseEntity<String> createFromJsonArray(@RequestBody String json) {
//        for (TransactionLog transactionLog: TransactionLog.fromJsonArrayToTransactionLogs(json)) {
//            transactionLog.persist();
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
//        TransactionLog transactionLog = TransactionLog.fromJsonToTransactionLog(json);
//        if (transactionLog.merge() == null) {
//            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<String>(headers, HttpStatus.OK);
//    }
//
//	@RequestMapping(value = "/jsonArray", method = RequestMethod.PUT, headers = "Accept=application/json")
//    public ResponseEntity<String> updateFromJsonArray(@RequestBody String json) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        for (TransactionLog transactionLog: TransactionLog.fromJsonArrayToTransactionLogs(json)) {
//            if (transactionLog.merge() == null) {
//                return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
//            }
//        }
//        return new ResponseEntity<String>(headers, HttpStatus.OK);
//    }
//
//	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
//    public ResponseEntity<String> deleteFromJson(@PathVariable("id") Long id) {
//        TransactionLog transactionLog = TransactionLog.findTransactionLog(id);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        if (transactionLog == null) {
//            return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
//        }
//        transactionLog.remove();
//        return new ResponseEntity<String>(headers, HttpStatus.OK);
//    }
}
