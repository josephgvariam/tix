package com.dubaidrums.tickets.domain;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceContext;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@Entity
@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class Item {

    @NotNull
    private String name;

    @NotNull
    private String itemNumber;

    @NotNull
    private String quantity;

    @NotNull
    private String price;

    @ManyToOne
    private PaypalTransaction txn;

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

	public String getName() {
        return this.name;
    }

	public void setName(String name) {
        this.name = name;
    }

	public String getItemNumber() {
        return this.itemNumber;
    }

	public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

	public String getQuantity() {
        return this.quantity;
    }

	public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

	public String getPrice() {
        return this.price;
    }

	public void setPrice(String price) {
        this.price = price;
    }

	public PaypalTransaction getTxn() {
        return this.txn;
    }

	public void setTxn(PaypalTransaction txn) {
        this.txn = txn;
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

	@PersistenceContext
    transient EntityManager entityManager;

	public static final EntityManager entityManager() {
        EntityManager em = new Item().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }

	public static long countItems() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Item o", Long.class).getSingleResult();
    }

	public static List<Item> findAllItems() {
        return entityManager().createQuery("SELECT o FROM Item o", Item.class).getResultList();
    }

	public static Item findItem(Long id) {
        if (id == null) return null;
        return entityManager().find(Item.class, id);
    }

	public static List<Item> findItemEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Item o", Item.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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
            Item attached = Item.findItem(this.id);
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
    public Item merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Item merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
}
