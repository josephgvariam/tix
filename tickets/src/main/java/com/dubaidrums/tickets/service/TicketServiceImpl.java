package com.dubaidrums.tickets.service;

import com.dubaidrums.tickets.domain.Item;
import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.domain.TransactionLog;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.mail.internet.MimeMessage;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

	Logger log = LogManager.getLogger(TicketServiceImpl.class);
	
    @Autowired
    private transient JavaMailSender mailTemplate;

    public void sendEmail(String txnId, String to, String name, String reportPath) throws Exception{
		MimeMessage message = mailTemplate.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		//helper.setFrom("admin@dubaidrums.com");
		helper.setFrom("tickets@dubaidrums.com");
		helper.setTo(to);
		helper.setBcc("tickets@dubaidrums.com");
		helper.setSubject("Payment Voucher");
		//helper.setText("Ticket Generated! See Attached PDF.");
		helper.setText("<html>\n<body>\n<p>\nDear "+name+"<br/>\nThank you for purchasing your tickets to our Desert Drumming event at the Gulf Ventures camp in Al Awir.<br/>\nPlease find attached your payment voucher and display it at the event gates.<br/>\nYour voucher number is : <strong>"+txnId+"</strong></br>\nDetails of the location with map and coordinates are available for your GPS receiver.<br/>\n<br/>\nFor details please go to our website at:<br/>\n<a href='http://www.dubaidrums.com/full-moon-desert-drumming-event'>http://www.dubaidrums.com/full-moon-desert-drumming-event</a><br/>\n<br/>\nNearer the time we will send you further information on drivers and contact liaisons accordingly<br/>\n<br/>\nThank You and Regards<br/>\n<br/>\nCapt. Guy Odell<br/>\nCOO Jupiter Eclipse Group<br/>\nDubai Drums<br/>\n+971 50 6139180<br/>\n<body>\n</html>", true);
			
		String reportName = "ticket_"+txnId+".pdf";
		
		FileSystemResource file = new FileSystemResource(new File(reportPath));
		helper.addAttachment(reportName, file);
		mailTemplate.send(message);        
		log.info(txnId+" Email Sent!");
    }
    
    public void sendErrorMail(String msg) throws Exception{
		MimeMessage message = mailTemplate.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		//helper.setFrom("admin@dubaidrums.com");
		helper.setFrom("tickets@dubaidrums.com");
		helper.setTo("tickets@dubaidrums.com");
		helper.setSubject("Error occured while processing IPN");
		helper.setText(msg);
		mailTemplate.send(message);        
		log.info("ERROR Email Sent!");
    }    

	@Async
	public void handleRefund(Map params) {
		String txnId = (String) params.get("txn_id");
		try{
			String parentTxnId = (String) params.get("parent_txn_id");
			String amount = (String) params.get("mc_gross");
			
			String paymentDate = (String) params.get("payment_date");
			String currency = (String) params.get("mc_currency");
					
			log.info(txnId+" Processing Refund. Parent txn_id: "+parentTxnId+" amount: "+currency+" "+amount);
			if(parentTxnId!=null && parentTxnId.length()>0){
				PaypalTransaction pt = PaypalTransaction.findPaypalTransaction(parentTxnId);
				if(pt!=null){
					TransactionLog tl = new TransactionLog();
					tl.setTstamp(getGSTDate(paymentDate));
					//tl.setTstamp(new Date());
					tl.setCode("200");
					tl.setMessage("Refund Issued. "+currency+": "+amount);
					tl.setTxn(pt);
					tl.persist();
					
					log.info(txnId+" Transaction merged to DB. id: "+pt.getId());
				}else{
					log.info(txnId+" Refund aborted! Parent txn_id not found: "+parentTxnId);
				}
			}else{
				log.info(txnId+" Refund aborted! Invalid parent txn_id!");
			}
		}catch(Exception e){
			log.error(txnId+" Unable to process refund.", e);
		}
	}
    
	@Async
	public void handlePayment(Map params, boolean sendEmail) {
		String txnId = (String) params.get("txn_id");
		String path = (String) params.get("path");
		
		try {
			log.info(txnId+" Processing Payment.");
			
			//Check if Transaction already exist in DB
			if(PaypalTransaction.findPaypalTransaction(txnId)==null){
				PaypalTransaction t = new PaypalTransaction();
				Set items = getItems(params, t);
				String reportPath = path+"tickets/ticket_"+txnId+".pdf";
				log.info(txnId+" Send Email: "+sendEmail);
				if(sendEmail){
					//Generate Ticket
					log.info(txnId+" Generating Ticket.");
					JasperReport jr = getReport(path); 
					JasperPrint print = JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(items));	
					JasperExportManager.exportReportToPdfFile(print, reportPath);			
					log.info(txnId+" Ticket Generated");
				}
				//Save Transaction to DB
				log.info(txnId+" Persisting Transaction.");
				t.setCurrency((String) params.get("mc_currency"));
				t.setEmail((String) params.get("payer_email"));
				t.setFirstName((String) params.get("first_name"));
				t.setLastName((String) params.get("last_name"));
				t.setTotal((String) params.get("mc_gross"));
				String contactNumber = (String) params.get("contact_phone");
				if(contactNumber==null) contactNumber="";
				t.setContactNumber(contactNumber);
				t.setTxnId(txnId);
				t.setItems(items);
				
				TransactionLog tl = new TransactionLog();
				String paymentDate = (String) params.get("payment_date");
				tl.setTstamp(getGSTDate(paymentDate));
				tl.setCode("100");
				tl.setMessage("Payment Complete. "+t.getCurrency()+" "+t.getTotal());
				tl.setTxn(t);
				
				Set logs = new HashSet();
				logs.add(tl);
				
				String comment = (String) params.get("comment");
				if(comment!=null && comment.length()>0){
					TransactionLog t2 = new TransactionLog();
					t2.setTstamp(getGSTDate(paymentDate));
					t2.setCode("123");
					t2.setMessage("Comment: "+comment);
					t2.setTxn(t);
					logs.add(t2);
				}
				
				
				t.setLogs(logs);
			
				t.persist();
				log.info(txnId+" Transaction persisted to DB. id: "+t.getId());
				
				//Email Ticket
				if(sendEmail){
					log.info(txnId+" Sending email: "+t.getEmail());
					sendEmail(txnId, t.getEmail(), t.getFirstName()+" "+t.getLastName(), reportPath);
				}
			}else{
				log.error(txnId+" Payment processing aborted. Transaction exists!");
			}
		} catch (Exception e) {
			log.error(txnId+" Unable to process payment.", e);
		}
	}

	private Set getItems(Map params, PaypalTransaction t){
		Set tmp = new HashSet();
	
		for (Iterator it = params.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			if(key.toLowerCase().startsWith("item_name")){
				String itemName = (String) params.get(key);
				String n = key.replace("item_name", "");
				String itemNumber = (String) params.get("item_number"+n);
				String quantity = (String) params.get("quantity"+n);
				String price = (String) params.get("mc_gross_"+n);
				
				Item i1 = new Item();
				i1.setQuantity(quantity);
				i1.setName(itemName);
				i1.setItemNumber(itemNumber);
				i1.setPrice(price);
				i1.setTxn(t);
				
				tmp.add(i1);
			}
		}
		
		return tmp;
	}
	
	private Date getGSTDate(String s){
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss MMM dd, yyyy z");
			SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss MMM dd, yyyy");
			sdf2.setTimeZone(TimeZone.getTimeZone("Asia/Dubai"));
			
			Date d1 = sdf1.parse(s);
			String x = sdf2.format(d1);			
			Date d2 = sdf2.parse(x);
			
//			System.out.println("Before: "+s+" Date: "+d1);
//			System.out.println("After: "+x+" Date: "+d3);
			
			return d2;
		} catch (Exception e) {
			log.error("Error in converting date!",e);
			return new Date();
		}
	}	
	
//	private Set getDummyItems(Map params){
//		Set tmp = new HashSet();
//		
//		Item i1 = new Item();
//		i1.setQuantity("3");
//		i1.setName("Adult Ticket 200 AED for Nov 30 2012");
//		i1.setItemNumber("FMDD30112012_ADULT");
//		i1.setPrice("163.35");
//		i1.setUsed(false);
//		i1.setUsedTstamp(null);
//		
//		Item i2 = new Item();
//		i2.setQuantity("2");
//		i2.setName("Child Ticket (6-13 years) 100 AED for Nov 30 2012");
//		i2.setItemNumber("FMDD30112012_CHILD");
//		i2.setPrice("54.46");
//		i2.setUsed(false);
//		i2.setUsedTstamp(null);
//		
//		tmp.add(i1);
//		tmp.add(i2);
//		
//		return tmp;
//	}

	@Cacheable("reports")
	private JasperReport getReport(String path) throws Exception{	
		log.info("Loading JasperReport from: "+path);
		return (JasperReport) JRLoader.loadObjectFromFile(path+"WEB-INF/reports/fmdd-ticket.jasper");
	} 
    

	public long countAllPaypalTransactions() {
        return PaypalTransaction.countPaypalTransactions();
    }

	public void deletePaypalTransaction(PaypalTransaction paypalTransaction) {
        paypalTransaction.remove();
    }

	public PaypalTransaction findPaypalTransaction(Long id) {
        return PaypalTransaction.findPaypalTransaction(id);
    }

	public List<PaypalTransaction> findAllPaypalTransactions() {
        return PaypalTransaction.findAllPaypalTransactions();
    }

	public List<PaypalTransaction> findPaypalTransactionEntries(int firstResult, int maxResults) {
        return PaypalTransaction.findPaypalTransactionEntries(firstResult, maxResults);
    }

	public void savePaypalTransaction(PaypalTransaction paypalTransaction) {
        paypalTransaction.persist();
    }

	public PaypalTransaction updatePaypalTransaction(PaypalTransaction paypalTransaction) {
        return paypalTransaction.merge();
    }
}
