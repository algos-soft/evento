package it.algos.evento.multiazienda;

import com.vaadin.ui.*;
import it.algos.evento.entities.company.Company;
import it.algos.web.field.*;
import it.algos.web.field.TextField;
import it.algos.web.module.ModulePop;
import it.algos.web.search.SearchManager;

import javax.persistence.metamodel.Attribute;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by alex on 30-05-2015.
 */
public class ESearchManager extends SearchManager {


    public ESearchManager() {
        super();
    }

    public ESearchManager(ModulePop module) {
        super(module);
    }

    /**
     * Crea un campo del tipo corrispondente all'attributo dato.
     * I campi vengono creati del tipo grafico previsto nella Entity.
     * Questo metodo è overridden per evitare che venga aggiunto automaticamente
     * anche il campo Company tra i filtri.
     */
    @SuppressWarnings("rawtypes")
    protected Field creaField(Attribute attr) {
        Field field = null;

        // evita se si tratta del campo che punta a Company
        boolean skip=false;
        if(attr.isAssociation()){
            Class clazz = attr.getJavaType();
            if(clazz.equals(Company.class)){
                skip=true;
            }
        }
        if(!skip){
            field = super.creaField(attr);
        }

        return field;
    }// end of method


    /**
     * Creates a Combo field to search in associated fields.
     * The field is automatically filtered by Company
     * @param clazz the related class
     * @return the combo field
     */
    @Override
    protected RelatedComboField createCombo(Class clazz){
        RelatedComboField field = new ERelatedComboField(clazz);
        return field;
    }

}
