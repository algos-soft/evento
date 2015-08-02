package it.algos.evento.multiazienda;

import javax.persistence.EntityManager;

import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import it.algos.webbase.web.entity.EM;
import it.algos.webbase.web.field.RelatedComboField;

import com.vaadin.data.Container;

@SuppressWarnings("serial")
public class ERelatedComboField extends RelatedComboField{

	@SuppressWarnings("rawtypes")
	public ERelatedComboField(Class entityClass) {
		super(entityClass);
	}

	@SuppressWarnings("rawtypes")
	public ERelatedComboField(Class entityClass, String caption) {
		super(entityClass, caption);
	}
	
	/**
	 * Creates the container usead as data source for the combo.
	 * <p>
	 * @return the container
	 */
	protected Container createContainer(){

		return new EROContainer(getEntityClass(), EM.createEntityManager());

		//return JPAContainerFactory.makeReadOnly(getEntityClass(), EM.createEntityManager());

	}

}
