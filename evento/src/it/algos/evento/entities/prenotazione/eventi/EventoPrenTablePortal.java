package it.algos.evento.entities.prenotazione.eventi;

import it.algos.web.module.ModulePop;
import it.algos.web.table.TablePortal;
import it.algos.web.toolbar.TableToolbar;

public class EventoPrenTablePortal extends TablePortal {

	public EventoPrenTablePortal(ModulePop modulo) {
		super(modulo);
	}

	public TableToolbar createToolbar() {
		TableToolbar toolbar = new EventoPrenTableToolbar();
		return toolbar;
	}// end of method

}
