package com.dubaidrums.tickets.web;

import com.dubaidrums.tickets.domain.Item;
import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.domain.TransactionLog;
import com.dubaidrums.tickets.service.TicketService;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

@RequestMapping("/paypaltransactions")
@Controller
@RooWebScaffold(path = "paypaltransactions", formBackingObject = PaypalTransaction.class)
public class PaypalTransactionController {

	@Autowired
    TicketService ticketService;

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid PaypalTransaction paypalTransaction, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, paypalTransaction);
            return "paypaltransactions/create";
        }
        uiModel.asMap().clear();
        ticketService.savePaypalTransaction(paypalTransaction);
        return "redirect:/paypaltransactions/" + encodeUrlPathSegment(paypalTransaction.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new PaypalTransaction());
        return "paypaltransactions/create";
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("paypaltransaction", ticketService.findPaypalTransaction(id));
        uiModel.addAttribute("itemId", id);
        return "paypaltransactions/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("paypaltransactions", ticketService.findPaypalTransactionEntries(firstResult, sizeNo));
            float nrOfPages = (float) ticketService.countAllPaypalTransactions() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("paypaltransactions", ticketService.findAllPaypalTransactions());
        }
        return "paypaltransactions/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid PaypalTransaction paypalTransaction, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, paypalTransaction);
            return "paypaltransactions/update";
        }
        uiModel.asMap().clear();
        ticketService.updatePaypalTransaction(paypalTransaction);
        return "redirect:/paypaltransactions/" + encodeUrlPathSegment(paypalTransaction.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, ticketService.findPaypalTransaction(id));
        return "paypaltransactions/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        PaypalTransaction paypalTransaction = ticketService.findPaypalTransaction(id);
        ticketService.deletePaypalTransaction(paypalTransaction);
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/paypaltransactions";
    }

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
