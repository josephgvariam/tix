package com.dubaidrums.tickets.service;

import com.dubaidrums.tickets.domain.PaypalTransaction;

public interface TicketService {
	public String getEventName();
	public String getStatus(PaypalTransaction p);
	public boolean giveIsEventTransaction(PaypalTransaction p);
	public String getItums(PaypalTransaction p);
}
