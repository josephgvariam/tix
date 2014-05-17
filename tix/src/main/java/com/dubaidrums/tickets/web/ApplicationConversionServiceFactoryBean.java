package com.dubaidrums.tickets.web;

import com.dubaidrums.tickets.domain.Item;
import com.dubaidrums.tickets.domain.PaypalTransaction;
import com.dubaidrums.tickets.domain.TransactionLog;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

@Configurable
/**
 * A central place to register application converters and formatters. 
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
	}

	public Converter<Item, String> getItemToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.dubaidrums.tickets.domain.Item, java.lang.String>() {
            public String convert(Item item) {
                return new StringBuilder().append(item.getName()).append(' ').append(item.getItemNumber()).append(' ').append(item.getQuantity()).append(' ').append(item.getPrice()).toString();
            }
        };
    }

	public Converter<Long, Item> getIdToItemConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.dubaidrums.tickets.domain.Item>() {
            public com.dubaidrums.tickets.domain.Item convert(java.lang.Long id) {
                return Item.findItem(id);
            }
        };
    }

	public Converter<String, Item> getStringToItemConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.dubaidrums.tickets.domain.Item>() {
            public com.dubaidrums.tickets.domain.Item convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Item.class);
            }
        };
    }

	public Converter<PaypalTransaction, String> getPaypalTransactionToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.dubaidrums.tickets.domain.PaypalTransaction, java.lang.String>() {
            public String convert(PaypalTransaction paypalTransaction) {
                return new StringBuilder().append(paypalTransaction.getTxnId()).append(' ').append(paypalTransaction.getCurrency()).append(' ').append(paypalTransaction.getFirstName()).append(' ').append(paypalTransaction.getLastName()).toString();
            }
        };
    }

	public Converter<Long, PaypalTransaction> getIdToPaypalTransactionConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.dubaidrums.tickets.domain.PaypalTransaction>() {
            public com.dubaidrums.tickets.domain.PaypalTransaction convert(java.lang.Long id) {
                return PaypalTransaction.findPaypalTransaction(id);
            }
        };
    }

	public Converter<String, PaypalTransaction> getStringToPaypalTransactionConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.dubaidrums.tickets.domain.PaypalTransaction>() {
            public com.dubaidrums.tickets.domain.PaypalTransaction convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), PaypalTransaction.class);
            }
        };
    }

	public Converter<TransactionLog, String> getTransactionLogToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.dubaidrums.tickets.domain.TransactionLog, java.lang.String>() {
            public String convert(TransactionLog transactionLog) {
                return new StringBuilder().append(transactionLog.getTstamp()).append(' ').append(transactionLog.getMessage()).append(' ').append(transactionLog.getCode()).toString();
            }
        };
    }

	public Converter<Long, TransactionLog> getIdToTransactionLogConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.dubaidrums.tickets.domain.TransactionLog>() {
            public com.dubaidrums.tickets.domain.TransactionLog convert(java.lang.Long id) {
                return TransactionLog.findTransactionLog(id);
            }
        };
    }

	public Converter<String, TransactionLog> getStringToTransactionLogConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.dubaidrums.tickets.domain.TransactionLog>() {
            public com.dubaidrums.tickets.domain.TransactionLog convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), TransactionLog.class);
            }
        };
    }

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getItemToStringConverter());
        registry.addConverter(getIdToItemConverter());
        registry.addConverter(getStringToItemConverter());
        registry.addConverter(getPaypalTransactionToStringConverter());
        registry.addConverter(getIdToPaypalTransactionConverter());
        registry.addConverter(getStringToPaypalTransactionConverter());
        registry.addConverter(getTransactionLogToStringConverter());
        registry.addConverter(getIdToTransactionLogConverter());
        registry.addConverter(getStringToTransactionLogConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
