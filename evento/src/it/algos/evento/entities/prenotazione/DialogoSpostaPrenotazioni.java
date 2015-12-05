package it.algos.evento.entities.prenotazione;

import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import it.algos.evento.entities.evento.Evento;
import it.algos.evento.entities.rappresentazione.Rappresentazione;
import it.algos.evento.entities.rappresentazione.RappresentazioneForm;
import it.algos.evento.entities.rappresentazione.RappresentazioneModulo;
import it.algos.evento.entities.rappresentazione.Rappresentazione_;
import it.algos.evento.entities.sala.Sala;
import it.algos.webbase.web.dialog.ConfirmDialog;
import it.algos.webbase.web.field.ComboNewItemHandler;
import it.algos.webbase.web.field.RelatedComboField;
import it.algos.webbase.web.form.AForm;
import it.algos.webbase.web.module.ModulePop;

import javax.persistence.metamodel.Attribute;
import java.util.Collection;

/**
 * Dialogo di spostamento di un gruppo di prenotazioni relative allo stesso evento.
 * Prepara ed esegue l'operazione.
 */
public class DialogoSpostaPrenotazioni extends ConfirmDialog {

    private Evento evento;  // evento di riferimento
    private Prenotazione[] aPren;   // prenotazioni da spostare
    private VerticalLayout contentLayout;
    private VerticalLayout msgPlaceholder; // il placeholder per la visualizzazione dei messaggi di warning o error
    private VerticalLayout previewPlaceholder; // il placeholder per la visualizzazione del preview operazione
    private RelatedComboField destPop;
    private PrenMover mover;

    /**
     * @param evento l'evento di riferimento
     * @param aPren  le prenotazioni da spostare (devono essere tutte relative
     *               allo stesso evento passato in evento, altrimenti lancia una eccezione.
     */
    public DialogoSpostaPrenotazioni(Evento evento, Prenotazione[] aPren) throws EventiDiversiException {
        super(null);
        this.evento = evento;
        this.aPren = aPren;

        // controlla che tutte le prenotazioni siano dell'evento
        for (Prenotazione pren : aPren) {
            if (!pren.getRappresentazione().getEvento().equals(evento)) {
                throw new EventiDiversiException();
            }
        }

        // titolo della finestra
        int posti=getTotPostiSpostati();
        setTitle("Spostamento di " + aPren.length + " prenotazioni (pari a "+posti+" posti)");

        // combo filtrato sulle rappresentazioni dello stesso evento
        this.destPop = new DestPopup();

        // selezione del popup modificata
        destPop.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                mover=null;
                if (getSelectedRapp() != null) {
                    mover = new PrenMover(DialogoSpostaPrenotazioni.this.aPren, getSelectedRapp());
                }
                syncConfirmButton();
                syncPreviewArea();
            }
        });

        // placeholder per i messaggi
        msgPlaceholder = new VerticalLayout();

        // placeholder per la preview dell'operazione
        previewPlaceholder = new VerticalLayout();

        //costruzione UI
        addComponent( new Label("Evento: "+evento.toString()));
        addComponent(destPop.getEditComponent(RelatedComboField.EDIT_TYPE_NEW));
        addComponent(msgPlaceholder);
        addComponent(previewPlaceholder);

        syncConfirmButton();


    }

    /**
     * Ritorna il numero di posti spostati
     * (le prenotazioni congelate non sono conteggiate)
     * @return il numero di posti spostati
     */
    private int getTotPostiSpostati(){
        int posti=0;
        for (Prenotazione pren : aPren){
            if (!pren.isCongelata()){
                posti+=pren.getNumTotali();
            }
        }
        return posti;
    }


    /**
     * Sincronizza l'area di preview in base alla rappresentazione selezionata.
     */
    private void syncPreviewArea() {
        Rappresentazione rapp = getSelectedRapp();
        msgPlaceholder.removeAllComponents();
        previewPlaceholder.removeAllComponents();
        if (rapp != null) {
            if (mover != null) {
                if (mover.hasErrors()) {    // errors
                    Label label = new Label();
                    label.setContentMode(ContentMode.HTML);
                    label.addStyleName("red");
                    label.setValue(mover.getHTMLErrors());
                    msgPlaceholder.addComponent(label);
                }else{
                    if (mover.hasWarnings()) { // warnings
                        Label label = new Label();
                        label.setContentMode(ContentMode.HTML);
                        label.addStyleName("darkblue");
                        label.setValue(mover.getHTMLWarnings());
                        msgPlaceholder.addComponent(label);
                    }

                    // qui il riepilogo operazione...
                    Label label = new Label();
                    label.setContentMode(ContentMode.HTML);
                    label.setValue("<strong>Premi conferma per eseguire lo spostamento</strong>");
                    previewPlaceholder.addComponent(label);

                }

            }
        }
    }


    /**
     * Abilita il bottone di conferma in base allo stato corrente del dialogo
     */
    private void syncConfirmButton() {
        boolean enabled = false;
        if (getSelectedRapp() != null) {
            if (mover != null) {
                if (mover.isEffettuabile()) {
                    enabled = true;
                }
            }
        }
        getConfirmButton().setEnabled(enabled);
    }


    /**
     * @return la rappresentazione selezionata, null se nessuna
     */
    private Rappresentazione getSelectedRapp() {
        Rappresentazione rapp = null;
        Object value = destPop.getSelectedBean();
        if ((value != null) && (value instanceof Rappresentazione)) {
            rapp = (Rappresentazione) value;
        }
        return rapp;
    }


    /**
     * The component shown in the detail area.
     */
    protected VerticalLayout createDetailComponent() {
        contentLayout = new VerticalLayout();
        contentLayout.setSpacing(true);
        contentLayout.setMargin(true);
        contentLayout.setStyleName("yellowBg");
        return contentLayout;
    }


    @Override
    protected void onConfirm() {
        super.onConfirm();
    }




    /**
     * Custom exception se si cerca di spostare prenotazioni relative ad eventi diversi
     */
    public class EventiDiversiException extends Exception {
        public EventiDiversiException() {
            super("Le prenotazioni da spostare devono essere tutte relative allo stesso evento");
        }
    }

    /**
     * Classe specifica per il popup di selezione della destinazione
     * */
    private class DestPopup extends RelatedComboField{
        public DestPopup() {
            super(Rappresentazione.class, "Scegli la data di destinazione:");

            //setNewItemHandler(RappresentazioneForm.class, null);
            setNewItemHandler(new PrenNewItemHandler(this));
            JPAContainer container=getJPAContainer();

            // container filtrato sulle sole rappresentazioni dell'evento selezionato
            Container.Filter filter = new Compare.Equal(Rappresentazione_.evento.getName(), evento);
            container.addContainerFilter(filter);

            // regola i testi visualizzati nel popup
            setItemCaptionMode(ItemCaptionMode.EXPLICIT);   // li devo assegnare io o restano vuoti
            setCaptionAsHtml(true);
            Collection ids = container.getItemIds();
            for (Object id : ids){
                EntityItem<Rappresentazione> ei = container.getItem(id);
                Rappresentazione rapp = ei.getEntity();
                String sData = rapp.getDateAsString();
                Sala sala = rapp.getSala();
                String sSala="";
                if (sala!=null){
                    sSala=sala.toString();
                }
                int disponibili=RappresentazioneModulo.getPostiDisponibili(rapp);
                String s = sData+" "+sSala+" - disp: "+disponibili;
                setItemCaption(id,s);
            }


        }

        @Override
        protected void newButtonClicked() {
            super.newButtonClicked();
        }
    }

    /**
     * Classe di gestione della creazione di una nuova prenotazione dal combo.
     */
    private class PrenNewItemHandler extends ComboNewItemHandler{

        public PrenNewItemHandler(RelatedComboField field) {
            super(field, RappresentazioneLockedForm.class, null);
        }

        @Override
        /**
         * Intercetto la creazione del bean per assegnare il valore fisso all'evento
         */
        protected Object createBean() {
            Object bean = super.createBean();
            if (bean instanceof Rappresentazione){
                Rappresentazione rapp = (Rappresentazione)bean;
                rapp.setEvento(evento);
            }
            return bean;
        }

    }

    /**
     * Form specifico per la creazione di nuova rappresentazione da popup.
     * Il campo evento è bloccato.
     * Nota: le classi interne devono deve essere dichiarate static per
     * poter essere instanziate per reflection (e questa lo è).
     */
    public static class RappresentazioneLockedForm extends RappresentazioneForm{
        public RappresentazioneLockedForm(Item item) {
            super(item);
            Field field = getField(Rappresentazione_.evento);
            field.setReadOnly(true);
        }
    }


}

