package com.dubaidrums.tickets.service;

import com.dubaidrums.tickets.domain.PaypalTransaction;
import java.util.List;
import java.util.Map;

import org.springframework.roo.addon.layers.service.RooService;

@RooService(domainTypes = { com.dubaidrums.tickets.domain.PaypalTransaction.class })
public interface TicketService {
	public void handlePayment(Map params, boolean sendEmail);
	public void handleRefund(Map params);
	public void sendErrorMail(String msg) throws Exception;

	public abstract long countAllPaypalTransactions();


	public abstract void deletePaypalTransaction(PaypalTransaction paypalTransaction);


	public abstract PaypalTransaction findPaypalTransaction(Long id);


	public abstract List<PaypalTransaction> findAllPaypalTransactions();


	public abstract List<PaypalTransaction> findPaypalTransactionEntries(int firstResult, int maxResults);


	public abstract void savePaypalTransaction(PaypalTransaction paypalTransaction);


	public abstract PaypalTransaction updatePaypalTransaction(PaypalTransaction paypalTransaction);

}
