package it.algos.evento.entities.lettera.allegati;

import it.algos.webbase.web.form.AForm;
import it.algos.webbase.web.module.ModulePop;

import com.vaadin.data.Item;

@SuppressWarnings("serial")
public class AllegatoForm extends AForm {

	public AllegatoForm(ModulePop modulo) {
		super(modulo);
	}// end of constructor

	public AllegatoForm(ModulePop modulo, Item item) {
		super(modulo, item);
	}// end of constructor
	
}
