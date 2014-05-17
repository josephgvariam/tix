package com.dubaidrums.tickets.domain;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Configurable
@RooJavaBean
@RooToString
@RooJson
@RooJpaActiveRecord(finders = { "findPaypalTransactionsByTxnIdEquals" })
public class PaypalTransaction {

    @NotNull
    @Column(unique = true)
    private String txnId;

    @NotNull
    private String currency;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String email;

    @NotNull
    private String total;

    @NotNull
    private String contactNumber;
    
    @Transient
    private String status;
    
    public String getStatus(){
    	return status;    	
    }
    public void setStatus(String s){
    	this.status=s;
    }
    

    

    


    
    public boolean getScanned(){
		for (TransactionLog log : getLogs()) {
			if(log.getCode().equals("300")){
				return true;
			}
		}
		return false;
    }
    
//    private String giveEvent(){
//    	return "FMDD26042013";
//    }
    
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "txn")
    private Set<Item> items = new HashSet<Item>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "txn")
    private Set<TransactionLog> logs = new HashSet<TransactionLog>();

	public static TypedQuery<PaypalTransaction> findPaypalTransactionsByTxnIdEquals(String txnId) {
        if (txnId == null || txnId.length() == 0) throw new IllegalArgumentException("The txnId argument is required");
        EntityManager em = PaypalTransaction.entityManager();
        TypedQuery<PaypalTransaction> q = em.createQuery("SELECT o FROM PaypalTransaction AS o WHERE o.txnId = :txnId", PaypalTransaction.class);
        q.setParameter("txnId", txnId);
        return q;
    }

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

	@Version
    @Column(name = "version")
    private Integer version;

	public Long getId() {
        return this.id;
    }

	public void setId(Long id) {
        this.id = id;
    }

	public Integer getVersion() {
        return this.version;
    }

	public void setVersion(Integer version) {
        this.version = version;
    }

	public String getTxnId() {
        return this.txnId;
    }

	public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

	public String getCurrency() {
        return this.currency;
    }

	public void setCurrency(String currency) {
        this.currency = currency;
    }

	public String getFirstName() {
        return this.firstName;
    }

	public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

	public String getLastName() {
        return this.lastName;
    }

	public void setLastName(String lastName) {
        this.lastName = lastName;
    }

	public String getEmail() {
        return this.email;
    }

	public void setEmail(String email) {
        this.email = email;
    }

	public String getTotal() {
        return this.total;
    }

	public void setTotal(String total) {
        this.total = total;
    }

	public String getContactNumber() {
        return this.contactNumber;
    }

	public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

	public Set<Item> getItems() {
        return this.items;
    }

	public void setItems(Set<Item> items) {
        this.items = items;
    }

	public Set<TransactionLog> getLogs() {
        return this.logs;
    }

	public void setLogs(Set<TransactionLog> logs) {
        this.logs = logs;
    }

	public String toJson() {
        return new JSONSerializer().exclude("*.class").include("items").include("logs").exclude("counts").exclude("itums").exclude("scanned").serialize(this);
    }
	
	public String toJson2() {
        return new JSONSerializer().exclude("*.class").include("itums").serialize(this);
    }

	public static PaypalTransaction fromJsonToPaypalTransaction(String json) {
        return new JSONDeserializer<PaypalTransaction>().use(null, PaypalTransaction.class).deserialize(json);
    }

	public static String toJsonArray(Collection<PaypalTransaction> collection) {
        return new JSONSerializer().exclude("*.class").serialize(collection);
    }

	public static Collection<PaypalTransaction> fromJsonArrayToPaypalTransactions(String json) {
        return new JSONDeserializer<List<PaypalTransaction>>().use(null, ArrayList.class).use("values", PaypalTransaction.class).deserialize(json);
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new PaypalTransaction().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countPaypalTransactions() {
        return entityManager().createQuery("SELECT COUNT(o) FROM PaypalTransaction o", Long.class).getSingleResult();
    }

	public static List<PaypalTransaction> findAllPaypalTransactions() {
        return entityManager().createQuery("SELECT o FROM PaypalTransaction o", PaypalTransaction.class).getResultList();
    }

	public static PaypalTransaction findPaypalTransaction(Long id) {
        if (id == null) return null;
        return entityManager().find(PaypalTransaction.class, id);
    }

	public static List<PaypalTransaction> findPaypalTransactionEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM PaypalTransaction o", PaypalTransaction.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

	@Transactional
    public void persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }

	@Transactional
    public void remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            PaypalTransaction attached = PaypalTransaction.findPaypalTransaction(this.id);
            this.entityManager.remove(attached);
        }
    }

	@Transactional
    public void flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }

	@Transactional
    public void clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }

	@Transactional
    public PaypalTransaction merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        PaypalTransaction merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
