#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

#parse("Header.java")

import it.algos.web.module.ModulePop;

import javax.persistence.metamodel.Attribute;

@SuppressWarnings("serial")
public class ${Nome_classe}Modulo extends ModulePop {

    public ${Nome_classe}Modulo() {
        super(${Nome_classe}.class);
    }// end of constructor

    /**
     * Titolo (caption) dei dialogo nuovo record. <br>
     * Come default usa il titolo standard <br>
     * Può essere sovrascritto (facoltativo) nelle sottoclassi specifiche <br>
     */
    @Override
    protected String getCaptionNew() {
        return "${NuovoNuova} ${Singolare_minuscolo}";
    }// end of method

    /**
     * Titolo (caption) dei dialogo di modifica. <br>
     * Come default usa il titolo standard <br>
     * Può essere sovrascritto (facoltativo) nelle sottoclassi specifiche <br>
     */
    @Override
    protected String getCaptionEdit() {
        return "Modifica ${Singolare_minuscolo}";
    }// end of method

    /**
     * Titolo (caption) dei dialogo di ricerca. <br>
     * Come default usa il titolo standard <br>
     * Può essere sovrascritto (facoltativo) nelle sottoclassi specifiche <br>
     */
    @Override
    protected String getCaptionSearch() {
        return "Ricerca ${Plurale_minuscolo}";
    }// end of method

    /**
     * Crea i campi visibili
     * <p>
     * Come default spazzola tutti i campi della Entity <br>
     * Può essere sovrascritto (facoltativo) nelle sottoclassi specifiche <br>
     * Non garantiscel'ordine con cui vengono presentati i campi nella scheda <br>
     * Può mostrare anche il campo ID, oppure no <br>
     * Se si vuole differenziare tra Table, Form e Search, <br>
     * sovrascrivere creaFieldsList, creaFieldsForm e creaFieldsSearch <br>
     */
    protected Attribute<?, ?>[] creaFieldsAll() {
        return new Attribute[]{${Nome_classe}_.nome};
    }// end of method/*

}// end of class
