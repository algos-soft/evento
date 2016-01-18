package it.algos.evento.entities.prenotazione;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import it.algos.evento.EventoApp;
import it.algos.evento.EventoBootStrap;
import it.algos.evento.entities.comune.Comune;
import it.algos.evento.entities.evento.Evento;
import it.algos.evento.entities.insegnante.Insegnante;
import it.algos.evento.entities.modopagamento.ModoPagamento;
import it.algos.evento.entities.prenotazione.eventi.TipoEventoPren;
import it.algos.evento.entities.rappresentazione.Rappresentazione;
import it.algos.evento.entities.scuola.Scuola;
import it.algos.evento.entities.spedizione.Spedizione;
import it.algos.evento.entities.stagione.Stagione;
import it.algos.evento.entities.tiporicevuta.TipoRicevuta;
import it.algos.evento.multiazienda.EQuery;
import it.algos.evento.multiazienda.ETable;
import it.algos.evento.multiazienda.EventoEntity;
import it.algos.evento.pref.CompanyPrefs;
import it.algos.webbase.web.converter.StringToBigDecimalConverter;
import it.algos.webbase.web.dialog.ConfirmDialog;
import it.algos.webbase.web.entity.BaseEntity;
import it.algos.webbase.web.lib.Lib;
import it.algos.webbase.web.lib.LibDate;
import it.algos.webbase.web.lib.LibResource;
import it.algos.webbase.web.module.Module;
import it.algos.webbase.web.module.ModulePop;
import it.algos.webbase.web.table.ATable;
import org.vaadin.addons.lazyquerycontainer.LazyEntityContainer;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("serial")
public abstract class PrenotazioneBaseTable extends ATable {

    private static final StringToBigDecimalConverter bdConv = new StringToBigDecimalConverter(2);
    private static final StringToIntegerConverter intConv = new StringToIntegerConverter();
    private BigDecimal bd;
    private static final BigDecimal bdZero = new BigDecimal(0);

    // id della colonna generata "stato"
    protected static final String COL_STATUS = "stato";

    // id della colonna generata "pagamento"
    protected static final String COL_PAGAM = "pagato";

    // id della colonna generata "referente"
    protected static final String COL_PRIVATO = "tipo";


    /**
     * Classe base tabella prenotazioni
     */
    public PrenotazioneBaseTable(EntityManager em) {
        super(Prenotazione.class, em);
    }// end of constructor


    @Override
    protected void init() {
        super.init();

        setColumnHeader(Prenotazione_.numPrenotazione, "N.");
        setColumnHeader(Prenotazione_.dataPrenotazione, "data");
        setColumnHeader(Prenotazione_.insegnante, "referente");
        setColumnHeader(Prenotazione_.numTotali, "posti");
        setColumnHeader(Prenotazione_.importoDaPagare, "importo");
        setColumnHeader(Prenotazione_.importoPagato, "pagato");
        setColumnHeader(Prenotazione_.scadenzaConferma, "conf. entro");
        setColumnHeader(Prenotazione_.scadenzaPagamento, "pagam. entro");
        setColumnHeader(Prenotazione_.modoPagamento, "modo pag");
        setColumnHeader(Prenotazione_.tipoRicevuta, "tipo ricev");

        setColumnWidth(Prenotazione_.dataPrenotazione, 80);
        setColumnWidth(Prenotazione_.numTotali, 46);
        setColumnWidth(Prenotazione_.importoDaPagare, 65);
        setColumnWidth(Prenotazione_.importoPagato, 65);
        setColumnWidth(Prenotazione_.scadenzaConferma, 80);
        setColumnWidth(Prenotazione_.scadenzaPagamento, 80);
        setColumnWidth(Prenotazione_.modoPagamento, 70);
        setColumnWidth(Prenotazione_.tipoRicevuta, 70);

        setColumnAlignment(Prenotazione_.dataPrenotazione, Align.CENTER);
        setColumnAlignment(Prenotazione_.scadenzaConferma, Align.CENTER);
        setColumnAlignment(Prenotazione_.scadenzaPagamento, Align.CENTER);
        setColumnAlignment(COL_STATUS, Align.CENTER);
        setColumnAlignment(COL_PAGAM, Align.CENTER);
        setColumnAlignment(COL_PRIVATO, Align.CENTER);

        setColumnUseTotals(Prenotazione_.numTotali, true);
        setColumnUseTotals(Prenotazione_.importoDaPagare, true);
        setColumnUseTotals(Prenotazione_.importoPagato, true);




        // colonne collapsed di default
        setColumnCollapsingAllowed(true);
        setColumnCollapsed(Prenotazione_.scadenzaConferma.getName(), true);
        setColumnCollapsed(Prenotazione_.scadenzaPagamento.getName(), true);
        setColumnCollapsed(Prenotazione_.modoPagamento.getName(), true);
        setColumnCollapsed(Prenotazione_.tipoRicevuta.getName(), true);
        setColumnCollapsed(Prenotazione_.importoPagato.getName(), true);
        setColumnCollapsed(Prenotazione_.numInteri.getName(), true);
        setColumnCollapsed(Prenotazione_.numRidotti.getName(), true);
        setColumnCollapsed(Prenotazione_.numDisabili.getName(), true);
        setColumnCollapsed(Prenotazione_.numAccomp.getName(), true);


        // generatore di stili per le singole celle
        setCellStyleGenerator(new CellStyleGenerator() {

            @Override
            public String getStyle(Table table, Object itemId, Object propertyId) {
                // posti e importo in grigio se pren congelata
                if (propertyId == Prenotazione_.numTotali.getName() | propertyId == Prenotazione_.importoDaPagare.getName()) {
                    Prenotazione pren = (Prenotazione) getEntity(itemId);
                    if (pren.isCongelata()) {
                        return "cell-cong";
                    }
                }
                return null;
            }
        });


    }

    /**
     * Ritorna il numero di record di competenza della azienda corrente
     * presenti nella domain class di questa tabella
     */
    @SuppressWarnings("unchecked")
    public long getTotalRows() {
        Class<EventoEntity> entityClass = (Class<EventoEntity>)getEntityClass();
        return EQuery.getCount(entityClass);
    }// end of method


    /**
     * Updates the totals in the footer
     * <p>
     * Called when the container data changes
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void updateTotals() {

        // cycle the totalizable columns
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        for (TotalizableColumn column : totalizableColumns) {
            Object propertyId = column.getPropertyId();
            BigDecimal bd = getTotalForColumn(propertyId);
            int places = column.getDecimalPlaces();
            converter.setDecimalPlaces(places);
            String sTotal = converter.convertToPresentation(bd);
            setColumnFooter(propertyId, sTotal);
        }


    }


    @Override
    protected BigDecimal getTotalForColumn(Object propertyId) {
        BigDecimal tot = new BigDecimal(0);
        boolean cont = true;

        if (propertyId.equals(Prenotazione_.numTotali.getName())) {

            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
            Root<Prenotazione> root = cq.from(Prenotazione.class);
            Predicate pred = getFiltersPredicate(cb, cq, root);
            if (pred != null) {
                cq.where(pred);
            }

            Expression<Integer> e1 = cb.sum(root.get(Prenotazione_.numInteri), root.get(Prenotazione_.numRidotti));
            Expression<Integer> e2 = cb.sum(root.get(Prenotazione_.numDisabili), root.get(Prenotazione_.numAccomp));
            Expression<Integer> exp = cb.sum(e1, e2);

            cq.select(cb.sum(exp));

            TypedQuery<Integer> q = getEntityManager().createQuery(cq);
            Integer num = q.getSingleResult();
            if (num == null) {
                num = 0;
            }
            tot = new BigDecimal(num);
            cont = false;
        }

        if (propertyId.equals(Prenotazione_.importoDaPagare.getName())) {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Number> cq = cb.createQuery(Number.class);
            Root<Prenotazione> root = cq.from(Prenotazione.class);
            Predicate pred = getFiltersPredicate(cb, cq, root);
            if (pred != null) {
                cq.where(pred);
            }

            CriteriaBuilder.Coalesce zero = cb.coalesce().value(0); // creo una Expression che rappresenta lo zero, che uso nei coalesce() successivi

            Expression<Number> e1 = cb.prod(cb.coalesce(root.get(Prenotazione_.numInteri), zero), cb.coalesce(root.get(Prenotazione_.importoIntero), zero));
            Expression<Number> e2 = cb.prod(cb.coalesce(root.get(Prenotazione_.numRidotti), zero), cb.coalesce(root.get(Prenotazione_.importoRidotto), zero));
            Expression<Number> e3 = cb.prod(cb.coalesce(root.get(Prenotazione_.numDisabili), zero), cb.coalesce(root.get(Prenotazione_.importoDisabili), zero));
            Expression<Number> e4 = cb.prod(cb.coalesce(root.get(Prenotazione_.numAccomp), zero), cb.coalesce(root.get(Prenotazione_.importoAccomp), zero));
            Expression<Number> e1e2 = cb.sum(e1, e2);
            Expression<Number> e3e4 = cb.sum(e3, e4);
            Expression<Number> expr = cb.sum(e1e2, e3e4);

            cq.select(cb.sum(expr));

            Number num = getEntityManager().createQuery(cq).getSingleResult();
            if (num != null) {
                if (num instanceof BigDecimal) {
                    tot = (BigDecimal) num;
                }
            }

            cont = false;
        }

        if (cont) {
            tot = super.getTotalForColumn(propertyId);
        }


        return tot;
    }


    /**
     * Lancia la procedura di registrazione pagamento per la prenotazione
     * correntemente selezionata nella lista
     */
    public void registraPagamento() {

        boolean cont = true;

        // controllo una e una sola selezionata
        Prenotazione pren = (Prenotazione) getSelectedEntity();
        if (pren == null) {
            cont = false;
            Notification.show("Seleziona prima una prenotazione.");
        }

        // controllo che sia confermata
        if (cont) {
            if (!pren.isConfermata()) {
                cont = false;
                Notification.show("Questa prenotazione non è confermata.");
            }
        }

        // controlla che non sia già pagamento confermato e ricevuto
        if (cont) {
            if (pren.isPagamentoConfermato() && pren.isPagamentoRicevuto()) {
                cont = false;
                Notification.show("Il pagamento è già stato ricevuto.");
            }
        }

        // delega al dialogo
        if (cont) {
            DialogoRegistraPagamento dialogo = new DialogoRegistraPagamento(pren);
            dialogo.setPagamentoRegistratoListener(new DialogoRegistraPagamento.PagamentoRegistratoListener() {
                @Override
                public void pagamentoRegistrato(boolean confermato, boolean ricevuto, boolean mailInviata, boolean emailFailed) {
                    refreshRowCache();

                    String msg = "";
                    if (confermato) {
                        msg = "Pagamento confermato";
                    }
                    if (ricevuto) {
                        msg = "Pagamento ricevuto";
                    }

                    String strEmail = "";
                    if (mailInviata) {
                        strEmail = "e-mail inviata";
                    }
                    if (emailFailed) {
                        strEmail = "Invio e-mail fallito";
                    }

                    Notification notification = new Notification(msg, strEmail, Notification.Type.HUMANIZED_MESSAGE);
                    notification.setDelayMsec(-1);
                    notification.show(Page.getCurrent());

                }
            });
            dialogo.show();
        }

    }


    /**
     * Invio email riepilogo opzione (primo invio delle istruzioni)
     * <p>
     * Invocato dai menu
     */
    public void inviaRiepilogoPrenotazione() {

        boolean cont = true;

        // controllo una e una sola selezionata
        Prenotazione pren = (Prenotazione) getSelectedEntity();
        if (pren == null) {
            cont = false;
            Notification.show("Seleziona prima una prenotazione.");
        }


        if (cont) {
            DialogoConfermaInvioManuale dialog = new DialogoConfermaInvioManuale(pren, "Invio riepilogo opzione con istruzioni", "Vuoi inviare il riepilogo opzione?");
            dialog.setConfirmListener(new ConfirmDialog.ConfirmListener() {
                @Override
                public void confirmed(ConfirmDialog d) {

                    String dests = dialog.getDestinatari();

                    // esegue l'operazione in un thread separato
                    // al termine dell'operazione viene visualizzata una notifica
                    new Thread(
                            () -> {

                                try {

                                    Spedizione sped = PrenotazioneModulo.sendEmailEvento(pren, TipoEventoPren.invioIstruzioni, EventoBootStrap.getUsername(), dests);

                                    refreshRowCache();

                                    Notification notification = new Notification("Riepilogo prenotazione n. " + pren.getNumPrenotazione() + " inviato a " + sped.getDestinatario(), "", Notification.Type.HUMANIZED_MESSAGE);
                                    notification.setDelayMsec(-1);
                                    notification.show(Page.getCurrent());

                                } catch (EmailFailedException e) {
                                    PrenotazioneModulo.notifyEmailFailed(e);
                                }

                            }

                    ).start();

                }
            });

            dialog.show();

        }

    }


    /**
     * Invia l'attestato di partecipazione
     * <p>
     * Invocato dai menu
     */
    public void inviaAttestatoPartecipazione() {
        boolean cont = true;

        // controllo una e una sola selezionata
        Prenotazione pren = (Prenotazione) getSelectedEntity();
        if (pren == null) {
            cont = false;
            Notification.show("Seleziona prima una prenotazione.");
        }

        // controllo che la prenotazione sia confermata
        if (cont) {
            if (!pren.isConfermata()) {
                cont = false;
                Notification notification = new Notification("Questa prenotazione non è confermata", Notification.Type.HUMANIZED_MESSAGE);
                notification.setDelayMsec(-1);
                notification.show(Page.getCurrent());
            }
        }

        // controllo che il pagamento sia stato effettivamente ricevuto
        if (cont) {
            if (!pren.isPagamentoRicevuto()) {
                cont = false;
                Notification notification = new Notification("Il pagamento non è ancora stato ricevuto", Notification.Type.HUMANIZED_MESSAGE);
                notification.setDelayMsec(-1);
                notification.show(Page.getCurrent());
            }
        }

        // controllo che la prenotazione non sia congelata
        if (cont) {
            if (pren.isCongelata()) {
                cont = false;
                Notification notification = new Notification("Questa prenotazione è già congelata", "\nSe vuoi puoi reinviare la email dagli Eventi Prenotazione.", Notification.Type.HUMANIZED_MESSAGE);
                notification.setDelayMsec(-1);
                notification.show(Page.getCurrent());
            }
        }

        // esegue
        if (cont) {
            DialogoConfermaInvioManuale dialog = new DialogoConfermaInvioManuale(pren, "Invio attestato di partecipazione", "Vuoi inviare l'attestato di partecipazione?");
            dialog.setConfirmListener(new ConfirmDialog.ConfirmListener() {
                @Override
                public void confirmed(ConfirmDialog d) {

                    String dests = dialog.getDestinatari();

                    // esegue l'operazione in un thread separato
                    // al termine dell'operazione viene visualizzata una notifica
                    new Thread(
                            () -> {

                                try {

                                    Spedizione sped = PrenotazioneModulo.sendEmailEvento(pren, TipoEventoPren.attestatoPartecipazione, EventoBootStrap.getUsername(), dests);

                                    refreshRowCache();

                                    Notification notification = new Notification("Attestato partecipazione prenotazione n. " + pren.getNumPrenotazione() + " inviato a " + sped.getDestinatario(), "", Notification.Type.HUMANIZED_MESSAGE);
                                    notification.setDelayMsec(-1);
                                    notification.show(Page.getCurrent());

                                } catch (EmailFailedException e) {
                                    PrenotazioneModulo.notifyEmailFailed(e);
                                }

                            }

                    ).start();

                }
            });

            dialog.show();

        }

    }


    /**
     * Invio email promemoria invio conferma prenotazione
     * <p>
     * Invocato dai menu
     */
    public void inviaMemoConfermaPren() {
        boolean cont = true;

        // controllo una e una sola selezionata
        Prenotazione pren = (Prenotazione) getSelectedEntity();
        if (pren == null) {
            cont = false;
            Notification.show("Seleziona prima una prenotazione.");
        }

        // controllo che la prenotazione non sia già confermata
        if (cont) {
            if (pren.isConfermata()) {
                cont = false;
                Notification.show("Questa prenotazione è già confermata.");
            }
        }

        // esegue
        if (cont) {
            String testo = "Invia una e-mail di sollecito e proroga la scadenza a " + CompanyPrefs.ggProlungamentoConfDopoSollecito.getInt() + " giorni da oggi.";
            DialogoConfermaInvioManuale dialog = new DialogoConfermaInvioManuale(pren, "Sollecito conferma prenotazione", testo);
            dialog.setConfirmListener(new ConfirmDialog.ConfirmListener() {
                @Override
                public void confirmed(ConfirmDialog d) {

                    String dests = dialog.getDestinatari();

                    // esegue l'operazione in un thread separato
                    // al termine dell'operazione viene visualizzata una notifica
                    new Thread(
                            () -> {

                                try {

                                    Spedizione sped = PrenotazioneModulo.doPromemoriaInvioSchedaPrenotazione(pren, EventoBootStrap.getUsername(), dests);

                                    refreshRowCache();

                                    Notification notification = new Notification("Sollecito conferma prenotazione n. " + pren.getNumPrenotazione() + " inviato a " + sped.getDestinatario(), "", Notification.Type.HUMANIZED_MESSAGE);
                                    notification.setDelayMsec(-1);
                                    notification.show(Page.getCurrent());

                                } catch (EmailFailedException e) {
                                    PrenotazioneModulo.notifyEmailFailed(e);
                                }

                            }

                    ).start();

                }
            });

            dialog.show();

        }


    }


    /**
     * Invio email promemoria scadenza pagamento
     * <p>
     * Invocato dai menu
     */
    public void inviaPromemoriaScadenzaPagamento() {
        boolean cont = true;

        // controllo una e una sola selezionata
        Prenotazione pren = (Prenotazione) getSelectedEntity();
        if (pren == null) {
            cont = false;
            Notification.show("Seleziona prima una prenotazione.");
        }

        // controllo che il pagamento non sia già confermato
        if (cont) {
            if (pren.isPagamentoConfermato()) {
                cont = false;
                Notification.show("Questo pagamento è già confermato.");
            }
        }

        // controllo che la prenotazione sia confermata
        if (cont) {
            if (!pren.isConfermata()) {
                cont = false;
                Notification.show("Questa prenotazione non è confermata.");
            }
        }

        // esegue
        if (cont) {
            String testo = "Invia una e-mail di sollecito e proroga la scadenza pagamento a " + CompanyPrefs.ggProlungamentoPagamDopoSollecito.getInt() + " giorni da oggi.";
            DialogoConfermaInvioManuale dialog = new DialogoConfermaInvioManuale(pren, "Sollecito conferma pagamento", testo);
            dialog.setConfirmListener(new ConfirmDialog.ConfirmListener() {
                @Override
                public void confirmed(ConfirmDialog d) {

                    String dests = dialog.getDestinatari();

                    // esegue l'operazione in un thread separato
                    // al termine dell'operazione viene visualizzata una notifica
                    new Thread(
                            () -> {

                                try {

                                    Spedizione sped = PrenotazioneModulo.doPromemoriaScadenzaPagamento(pren, EventoBootStrap.getUsername(), dests);

                                    refreshRowCache();

                                    Notification notification = new Notification("Sollecito conferma pagamento prenotazione n. " + pren.getNumPrenotazione() + " inviato a " + sped.getDestinatario(), "", Notification.Type.HUMANIZED_MESSAGE);
                                    notification.setDelayMsec(-1);
                                    notification.show(Page.getCurrent());

                                } catch (EmailFailedException e) {
                                    PrenotazioneModulo.notifyEmailFailed(e);
                                }

                            }

                    ).start();

                }
            });

            dialog.show();

        }


    }


    /**
     * Congelamento prenotazione
     * <p>
     * Invocato dalla UI
     */
    public void congelaPrenotazione() {
        boolean cont = true;

        // controllo una e una sola selezionata
        Prenotazione pren = (Prenotazione) getSelectedEntity();
        if (pren == null) {
            cont = false;
            Notification.show("Seleziona prima una prenotazione.");
        }

        // controllo che la prenotazione non sia già confermata
        if (cont) {
            if (pren.isConfermata()) {
                cont = false;
                Notification.show("Questa prenotazione è confermata, non la puoi congelare.");
            }
        }

        // controllo che la prenotazione non sia congelata
        if (cont) {
            if (pren.isCongelata()) {
                cont = false;
                Notification notification = new Notification("Questa prenotazione è già congelata", "\nSe vuoi puoi reinviare la email dagli Eventi Prenotazione.", Notification.Type.HUMANIZED_MESSAGE);
                notification.setDelayMsec(-1);
                notification.show(Page.getCurrent());
            }
        }

        // esegue
        if (cont) {
            String title = "Il congelamento di una prenotazione libera i posti impegnati e blocca l'invio di ulteriori solleciti";
            DialogoCongelaPrenotazione dialog = new DialogoCongelaPrenotazione(pren, "Congelamento prenotazione", title);
            dialog.setConfirmListener(new ConfirmDialog.ConfirmListener() {
                @Override
                public void confirmed(ConfirmDialog d) {

                    String dests = dialog.getDestinatari();

                    // esegue l'operazione in un thread separato
                    // al termine dell'operazione, se ha effettuato una spedizione, viene visualizzata una notifica
                    new Thread(
                            () -> {

                                try {

                                    Spedizione sped = PrenotazioneModulo.doCongelamentoOpzione(pren, EventoBootStrap.getUsername(), dests);

                                    refreshRowCache();

                                    if (sped != null) {
                                        Notification notification = new Notification("Avviso congelamento prenotazione n. " + pren.getNumPrenotazione() + " inviato a " + sped.getDestinatario(), "", Notification.Type.HUMANIZED_MESSAGE);
                                        notification.setDelayMsec(-1);
                                        notification.show(Page.getCurrent());
                                    }

                                } catch (EmailFailedException e) {
                                    PrenotazioneModulo.notifyEmailFailed(e);
                                }

                            }

                    ).start();

                }
            });

            dialog.show();

        }

    }


    /**
     * Sposta delle prenotazioni ad altra rappresentazione
     * <p>
     * Invocato dai menu
     */
    public void spostaAdAltraData() {

        BaseEntity[] entities = getTable().getSelectedEntities();
        Prenotazione[] aPren = new Prenotazione[entities.length];
        for (int i = 0; i < aPren.length; i++) {
            aPren[i] = (Prenotazione) entities[i];
        }

        if (aPren.length > 0) {
            Evento e = aPren[0].getRappresentazione().getEvento();
            try {
                DialogoSpostaPrenotazioni dialogo = new DialogoSpostaPrenotazioni(e, aPren, new DialogoSpostaPrenotazioni.OnMoveDoneListener() {
                    @Override
                    public void moveDone(int quante, Rappresentazione dest) {
                        refreshRowCache();

                        Notification notification = new Notification(quante + " prenotazioni spostate.", Notification.Type.HUMANIZED_MESSAGE);
                        notification.setDelayMsec(-1);
                        notification.show(Page.getCurrent());

                    }
                });

                dialogo.show();

            } catch (DialogoSpostaPrenotazioni.EventiDiversiException e1) {
                Notification.show(null, e1.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
        }
    }


    @Override
    protected void createAdditionalColumns() {

        // disabilitato alex 04-01 per refactoring table container
        // da verificare se funziona ancora!!
//        // queste property aggiunte servono per consentire
//        // di effettuare ricerche su proprietà in relazione
//        getJPAContainer().addNestedContainerProperty(PrenotazioneModulo.PROP_PROGETTO);
//        getJPAContainer().addNestedContainerProperty(PrenotazioneModulo.PROP_EVENTO);
//        getJPAContainer().addNestedContainerProperty(PrenotazioneModulo.PROP_STAGIONE);
        // end disabilitato

        addGeneratedColumn(COL_STATUS, new StatusColumnGenerator());
        addGeneratedColumn(COL_PAGAM, new PagamColumnGenerator());
        addGeneratedColumn(COL_PRIVATO, new PrivatoColumnGenerator());

    }


    protected Object[] getDisplayColumns() {
        return new Object[]{Prenotazione_.numPrenotazione,
                Prenotazione_.dataPrenotazione,
                Prenotazione_.rappresentazione,
                Prenotazione_.scuola,
                COL_PRIVATO,
                Prenotazione_.insegnante,
                Prenotazione_.numTotali,
                Prenotazione_.importoDaPagare,
                Prenotazione_.importoPagato,
                COL_STATUS,
                Prenotazione_.scadenzaConferma,
                COL_PAGAM,
                Prenotazione_.scadenzaPagamento,
                Prenotazione_.modoPagamento,
                Prenotazione_.tipoRicevuta,
                Prenotazione_.numInteri,
                Prenotazione_.numRidotti,
                Prenotazione_.numDisabili,
                Prenotazione_.numAccomp
        };


    }// end of method

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {
        String string = null;

        if (colId.equals(Prenotazione_.scuola.getName())) {
            Object value = property.getValue();
            if (value != null && value instanceof Scuola) {
                Scuola scuola = (Scuola) value;
                string = scuola.getSigla();
                Comune comune = scuola.getComune();
                if (comune != null) {
                    string += " - " + comune.getNome();
                }
            } else {
                string = "";
            }
            return string;
        }

        if (colId.equals(Prenotazione_.insegnante.getName())) {
            Object value = property.getValue();
            if (value != null && value instanceof Insegnante) {
                Insegnante ins = (Insegnante) value;
                string = ins.getCognome() + " " + ins.getNome();
            } else {
                string = "";
            }
            return string;
        }


        if (colId.equals(Prenotazione_.importoDaPagare.getName())) {
            string = "";
            bd = Lib.getBigDecimal(property.getValue());
            if (!bd.equals(bdZero)) {
                string = bdConv.convertToPresentation(bd);
            }
            return string;
        }

        if (colId.equals(Prenotazione_.importoPagato.getName())) {
            string = "";
            bd = Lib.getBigDecimal(property.getValue());
            if (!bd.equals(bdZero)) {
                string = bdConv.convertToPresentation(bd);
            }
            return string;
        }


        if (colId.equals(Prenotazione_.livelloSollecitoConferma.getName())) {
            string = "";
            int integer = Lib.getInt(property.getValue());
            if (integer != 0) {
                string = intConv.convertToPresentation(integer, String.class, Locale.getDefault());
            }
            return string;
        }

        if (colId.equals(Prenotazione_.livelloSollecitoPagamento.getName())) {
            string = "";
            int integer = Lib.getInt(property.getValue());
            if (integer != 0) {
                string = intConv.convertToPresentation(integer, String.class, Locale.getDefault());
            }
            return string;
        }

        if (colId.equals(Prenotazione_.modoPagamento.getName())) {
            Object value = property.getValue();
            if (value != null && value instanceof ModoPagamento) {
                ModoPagamento tipo = (ModoPagamento) value;
                string = tipo.getSigla();
            } else {
                string = "";
            }
            return string;
        }

        if (colId.equals(Prenotazione_.tipoRicevuta.getName())) {
            Object value = property.getValue();
            if (value != null && value instanceof TipoRicevuta) {
                TipoRicevuta tipo = (TipoRicevuta) value;
                string = tipo.getSigla();
            } else {
                string = "";
            }
            return string;
        }


        return super.formatPropertyValue(rowId, colId, property);
    }// end of method

    /**
     * Genera la colonna di stato.
     */
    class StatusColumnGenerator implements ColumnGenerator {

        @SuppressWarnings("unchecked")
        public Component generateCell(Table source, Object itemId, Object columnId) {
            boolean conf = Lib.getBool(getContainerProperty(itemId, Prenotazione_.confermata.getName()).getValue());
            boolean cong = Lib.getBool(getContainerProperty(itemId, Prenotazione_.congelata.getName()).getValue());
            int levelSC = Lib.getInt(getContainerProperty(itemId, Prenotazione_.livelloSollecitoConferma.getName()).getValue());
            Object obj = (getContainerProperty(itemId, Prenotazione_.scadenzaConferma.getName()).getValue());

            // check se scaduta
            boolean scaduta = false;
            if (obj != null && obj instanceof Date) {
                Date dataScadPren = (Date) obj;
                if (!conf && (LibDate.isPrecedente(dataScadPren, LibDate.today()))) {
                    scaduta = true;
                }
            }

            String imgName;
            String description = "";
            if (conf) {
                imgName = "confirmed_20px.png";
                description = "La prenotazione è confermata";
            } else {
                if (cong) {
                    imgName = "frozen_20px.png";
                    description = "La prenotazione è stata congelata";
                } else {    // non congelata

                    switch (levelSC) {
                        case 0: // non sollecitata
                            if (scaduta) {
                                imgName = "exclam_red_20px.png";
                                description = "Prenotazione scaduta";
                            } else {
                                imgName = null;
                            }
                            break;
                        case 1: // sollecitata livello 1
                            imgName = "bell_num_1_20px.png";
                            description = "E' stato inviato un sollecito di conferma prenotazione";
                            break;
                        case 2: // sollecitata livello 2
                            imgName = "bell_num_2_20px.png";
                            description = "Sono stati inviati due solleciti di conferma prenotazione";
                            break;
                        default: // sollecitata livello ulteriore
                            imgName = "bell_num_other_20px.png";
                            description = "Sono stati inviati " + levelSC + " solleciti di conferma prenotazione";
                            break;
                    }


                }
            }

            Image img = null;
            if (imgName != null) {
                img = new Image(null, LibResource.getImgResource(EventoApp.IMG_FOLDER_NAME, imgName));
                img.setDescription(description);
            }

            return img;
        }
    }

    /**
     * Genera la colonna del pagamento.
     */
    class PagamColumnGenerator implements ColumnGenerator {

        @SuppressWarnings("unchecked")
        public Component generateCell(Table source, Object itemId, Object columnId) {
            boolean prenConf = Lib.getBool(getContainerProperty(itemId, Prenotazione_.confermata.getName()).getValue());

            String imgName = null;
            String description = "";

            if (prenConf) { // se la prenotazione non è confermata è inutile ragionare sui pagamenti

                boolean pagConf = Lib.getBool(getContainerProperty(itemId, Prenotazione_.pagamentoConfermato.getName()).getValue());
                boolean pagRegis = Lib.getBool(getContainerProperty(itemId, Prenotazione_.pagamentoRicevuto.getName()).getValue());
                int levelSC = Lib.getInt(getContainerProperty(itemId, Prenotazione_.livelloSollecitoPagamento.getName()).getValue());
                Object obj = (getContainerProperty(itemId, Prenotazione_.scadenzaPagamento.getName()).getValue());

                // check se conferma pagamento scaduta
                boolean scaduta = false;
                if (obj != null && obj instanceof Date) {
                    Date dataScadPaga = (Date) obj;
                    if (!pagConf && (LibDate.isPrecedente(dataScadPaga, LibDate.today()))) {
                        scaduta = true;
                    }
                }


                if (pagRegis) {
                    imgName = "confirmed_20px.png";
                    description = "Pagamento registrato";
                } else {  // pagamento non registrato
                    if (pagConf) {
                        imgName = "confirmed_half_20px.png";
                        description = "Pagamento confermato";
                    } else {

                        switch (levelSC) {
                            case 0: // mai sollecitata
                                if (scaduta) {
                                    imgName = "exclam_red_20px.png";
                                    description = "Conferma pagamento scaduta";
                                } else {    // non scaduto
                                    imgName = null;
                                }
                                break;
                            case 1: // sollecitata livello 1
                                imgName = "bell_num_1_20px.png";
                                description = "E' stato inviato un sollecito di conferma pagamento";
                                break;
                            case 2: // sollecitata livello 2
                                imgName = "bell_num_2_20px.png";
                                description = "Sono stati inviati due solleciti di conferma pagamento";
                                break;
                            default: // sollecitata livello ulteriore
                                imgName = "bell_num_other_20px.png";
                                description = "Sono stati inviati " + levelSC + " solleciti di conferma pagamento";
                                break;
                        }

                    }

                }

            }

            Image img = null;
            if (imgName != null) {
                img = new Image(null, LibResource.getImgResource(EventoApp.IMG_FOLDER_NAME, imgName));
                img.setDescription(description);
            }

            return img;
        }
    }


    /**
     * Genera la colonna del Referente.
     */
    private class PrivatoColumnGenerator implements ColumnGenerator {
        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            boolean priv = Lib.getBool(getContainerProperty(itemId, Prenotazione_.privato.getName()).getValue());
            String loc_img_name = "";
            String description = "";
            if (priv) {
                loc_img_name = "person_20px.png";
                description = "Il referente è un privato";
            } else {
                loc_img_name = "teacher_20px.png";
                description = "Il referente è un docente";
            }
            Image img = new Image(null, LibResource.getImgResource(EventoApp.IMG_FOLDER_NAME, loc_img_name));
            img.setDescription(description);
            return img;
        }
    }


}