package it.algos.evento.entities.lettera.allegati;

import it.algos.evento.multiazienda.ETable;
import it.algos.web.lib.LibFile;
import it.algos.web.module.ModulePop;

import com.vaadin.data.Property;

@SuppressWarnings("serial")
public class AllegatoTable extends ETable {

	public AllegatoTable(ModulePop modulo) {
		super(modulo);

		setColumnAlignment(Allegato_.bytes, Align.CENTER);

	}// end of constructor

	protected Object[] getDisplayColumns() {
		return new Object[] { Allegato_.name, Allegato_.mimeType , Allegato_.bytes};
	}// end of method

	@SuppressWarnings("rawtypes")
	@Override
	protected String formatPropertyValue(Object rowId, Object colId, Property property) {
		String string = null;

		if (colId.equals(Allegato_.bytes.getName())) {
			Object value = property.getValue();
			if (value!=null && value instanceof Long) {
				string = LibFile.humanReadableByteCount((long)value);
			} else {
				string="";
			}
			return string;
		}

		return super.formatPropertyValue(rowId, colId, property);
	}// end of method

}
