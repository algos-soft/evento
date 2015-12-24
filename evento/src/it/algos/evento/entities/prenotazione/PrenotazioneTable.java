package it.algos.evento.entities.prenotazione;

import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.event.Action;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
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
import it.algos.evento.multiazienda.ETable;
import it.algos.webbase.web.converter.StringToBigDecimalConverter;
import it.algos.webbase.web.dialog.ConfirmDialog;
import it.algos.webbase.web.lib.Lib;
import it.algos.webbase.web.lib.LibResource;
import it.algos.webbase.web.module.Module;
import it.algos.webbase.web.module.ModulePop;

import java.math.BigDecimal;
import java.util.Locale;

@SuppressWarnings("serial")
public class PrenotazioneTable extends ETable {

    private static final StringToBigDecimalConverter bdConv = new StringToBigDecimalConverter(2);
    private static final StringToIntegerConverter intConv = new StringToIntegerConverter();
    private BigDecimal bd;
    private static final BigDecimal bdZero = new BigDecimal(0);

    // id della colonna generata "stato"
    private static final String COL_STATUS = "stato";

    // id della colonna generata "pagamento"
    private static final String COL_PAGAM = "pagato";

    // id della colonna generata "referente"
    private static final String COL_PRIVATO = "tipo";


    /**
     * Creates the container
     * <p>
     *
     * @return un container RW filtrato sulla azienda corrente
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Container createContainer() {
        // aggiunge un filtro sulla stagione corrente
        Container cont = super.createContainer();
        JPAContainer JPAcont = (JPAContainer) cont;
        Filter filter = new Compare.Equal(PrenotazioneModulo.PROP_STAGIONE, Stagione.getStagioneCorrente());
        JPAcont.addContainerFilter(filter);
        return JPAcont;
    }// end of method


    public PrenotazioneTable(ModulePop modulo) {
        super(modulo);

        setColumnHeader(Prenotazione_.numPrenotazione, "N.");
        setColumnHeader(Prenotazione_.dataPrenotazione, "data");
//        setColumnHeader(Prenotazione_.privato, "priv");
        setColumnHeader(Prenotazione_.insegnante, "referente");
        setColumnHeader(Prenotazione_.numTotali, "pers");
        setColumnHeader(Prenotazione_.importoDaPagare, "importo");
        setColumnHeader(Prenotazione_.importoPagato, "pagato");
        setColumnHeader(Prenotazione_.scadenzaConferma, "conf. entro");
//        setColumnHeader(Prenotazione_.livelloSollecitoConferma, "SC");
//        setColumnHeader(Prenotazione_.congelata, "cong");
//        setColumnHeader(Prenotazione_.confermata, "conf");
        setColumnHeader(Prenotazione_.scadenzaPagamento, "pagam. entro");
//        setColumnHeader(Prenotazione_.livelloSollecitoPagamento, "SP");
//        setColumnHeader(Prenotazione_.pagamentoConfermato, "pag conf.");
//        setColumnHeader(Prenotazione_.pagamentoRicevuto, "pag ricev.");
        setColumnHeader(Prenotazione_.modoPagamento, "modo pag");
        setColumnHeader(Prenotazione_.tipoRicevuta, "tipo ricev");


        setColumnWidth(Prenotazione_.dataPrenotazione, 80);
//		setColumnWidth(Prenotazione_.rappresentazione, 220);
//		setColumnWidth(Prenotazione_.scuola, 110);
//		setColumnWidth(Prenotazione_.insegnante, 110);
//        setColumnWidth(Prenotazione_.privato, 40);
        setColumnWidth(Prenotazione_.numTotali, 46);
        setColumnWidth(Prenotazione_.importoDaPagare, 65);
        setColumnWidth(Prenotazione_.importoPagato, 65);
//        setColumnWidth(Prenotazione_.livelloSollecitoConferma, 30);
//        setColumnWidth(Prenotazione_.congelata, 50);
//        setColumnWidth(Prenotazione_.confermata, 50);
        setColumnWidth(Prenotazione_.scadenzaConferma, 80);
//        setColumnWidth(Prenotazione_.pagamentoConfermato, 50);
        setColumnWidth(Prenotazione_.scadenzaPagamento, 80);
//        setColumnWidth(Prenotazione_.livelloSollecitoPagamento, 30);
//        setColumnWidth(Prenotazione_.pagamentoRicevuto, 50);
        setColumnWidth(Prenotazione_.modoPagamento, 70);
        setColumnWidth(Prenotazione_.tipoRicevuta, 70);

        setColumnAlignment(Prenotazione_.dataPrenotazione, Align.CENTER);
        setColumnAlignment(Prenotazione_.scadenzaConferma, Align.CENTER);
//        setColumnAlignment(Prenotazione_.livelloSollecitoConferma, Align.CENTER);
//        setColumnAlignment(Prenotazione_.livelloSollecitoConferma, Align.CENTER);
        setColumnAlignment(Prenotazione_.scadenzaPagamento, Align.CENTER);
//        setColumnAlignment(Prenotazione_.livelloSollecitoPagamento, Align.CENTER);
        setColumnAlignment(COL_STATUS, Align.CENTER);
        setColumnAlignment(COL_PAGAM, Align.CENTER);
        setColumnAlignment(COL_PRIVATO, Align.CENTER);

        setColumnUseTotals(Prenotazione_.numTotali, true);
        setColumnUseTotals(Prenotazione_.importoDaPagare, true);
        setColumnUseTotals(Prenotazione_.importoPagato, true);

        // comandi contestuali aggiuntivi specifici
        addActionHandler(new Action.Handler() {

            private final Action actRegistraPagamento = new Action(PrenotazioneTablePortal.CMD_REGISTRA_PAGAMENTO,
                    PrenotazioneTablePortal.ICON_REGISTRA_PAGAMENTO);
            private final Action actIstruzioni = new Action(PrenotazioneTablePortal.CMD_RIEPILOGO_OPZIONE,
                    PrenotazioneTablePortal.ICON_RIEPILOGO_OPZIONE);
            private final Action actMemoInvioSchedaPren = new Action(
                    PrenotazioneTablePortal.CMD_MEMO_INVIO_SCHEDA_PREN,
                    PrenotazioneTablePortal.ICON_MEMO_INVIO_SCHEDA_PREN);
            private final Action actMemoScadPag = new Action(PrenotazioneTablePortal.CMD_MEMO_SCAD_PAGA,
                    PrenotazioneTablePortal.ICON_MEMO_SCAD_PAGA);
            private final Action actAttestatoPartecipazione = new Action(PrenotazioneTablePortal.CMD_ATTESTATO_PARTECIPAZIONE,
                    PrenotazioneTablePortal.ICON_ATTESTATO_PARTECIPAZIONE);
            private final Action actAvvisoCongOpz = new Action(PrenotazioneTablePortal.CMD_CONGELA_OPZIONE,
                    PrenotazioneTablePortal.ICON_CONGELA_OPZIONE);
            private final Action actSpostaAdAltraData = new Action(PrenotazioneTablePortal.CMD_SPOSTA_AD_ALTRA_DATA,
                    PrenotazioneTablePortal.ICON_SPOSTA_AD_ALTRA_DATA);


            public Action[] getActions(Object target, Object sender) {
                Action[] actions = null;
                actions = new Action[7];
                actions[0] = actRegistraPagamento;
                actions[1] = actIstruzioni;
                actions[2] = actMemoInvioSchedaPren;
                actions[3] = actMemoScadPag;
                actions[4] = actAttestatoPartecipazione;
                actions[5] = actAvvisoCongOpz;
                actions[6] = actSpostaAdAltraData;

                return actions;
            }

            public void handleAction(Action action, Object sender, Object target) {

                //Prenotazione pren=(Prenotazione)getSelectedBean();

                Item rowItem = PrenotazioneTable.this.getItem(target);
                if (rowItem != null) {
                    Object value = rowItem.getItemProperty("id").getValue();

                    long id = Lib.getLong(value);
                    if (id > 0) {

                        if (action.equals(actRegistraPagamento)) {
                            registraPagamento();
                        }

                        if (action.equals(actIstruzioni)) {
                            inviaRiepilogoPrenotazione();
                        }

                        if (action.equals(actMemoInvioSchedaPren)) {
                            inviaMemoConfermaPren();
                        }

                        // if (action.equals(actConfPren)) {
                        // PrenotazioneModulo.cmdSendEmailConfermaPrenotazione(id);
                        // }

                        // if (action.equals(actConfRegisPag)) {
                        // PrenotazioneModulo.cmdSendEmailConfermaRegistrazionePagamento(id);
                        // }

                        if (action.equals(actAvvisoCongOpz)) {
                            congelaPrenotazione();
                        }

                        if (action.equals(actSpostaAdAltraData)) {
                            spostaAdAltraData();
                        }

                        if (action.equals(actMemoScadPag)) {
                            inviaPromemoriaScadenzaPagamento();
                        }

                        if (action.equals(actAttestatoPartecipazione)) {
                            inviaAttestatoPartecipazione();
                        }


                    }
                }

            }
        });


//        getPrenotazioneModulo().addStatusChangeListener(new StatusChangeListener() {
//
//            @Override
//            public void statusChanged(TipoEventoPren tipoEvento) {
//                refreshRowCache();
//            }
//        });


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

    }// end of constructor


    private PrenotazioneModulo getPrenotazioneModulo() {
        PrenotazioneModulo pm = null;
        Module mod = getModule();
        if (mod != null && mod instanceof PrenotazioneModulo) {
            pm = (PrenotazioneModulo) mod;
        }
        return pm;
    }


    /**
     * Lancia la procedura di registrazione pagamento per la prenotazione
     * correntemente selezionata nella lista
     */
    public void registraPagamento() {

        boolean cont = true;

        // controllo una e una sola selezionata
        Prenotazione pren = (Prenotazione) getSelectedBean();
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
        Prenotazione pren = (Prenotazione) getSelectedBean();
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
        Prenotazione pren = (Prenotazione) getSelectedBean();
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
        Prenotazione pren = (Prenotazione) getSelectedBean();
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

        // controllo che il livello sollecito sia < 1
        if (cont) {
            if (pren.getLivelloSollecitoConferma() > 0) {
                cont = false;
                Notification notification = new Notification("Il promemoria è gia stato inviato",
                        "\nSe vuoi puoi reinviarlo dagli Eventi Prenotazione.", Notification.Type.HUMANIZED_MESSAGE);
                notification.setDelayMsec(-1);
                notification.show(Page.getCurrent());
            }
        }

        // esegue
        if (cont) {
            DialogoConfermaInvioManuale dialog = new DialogoConfermaInvioManuale(pren, "Invio promemoria conferma prenotazione", "Vuoi inviare il promemoria conferma prenotazione?");
            dialog.setConfirmListener(new ConfirmDialog.ConfirmListener() {
                @Override
                public void confirmed(ConfirmDialog d) {

                    String dests = dialog.getDestinatari();

                    // esegue l'operazione in un thread separato
                    // al termine dell'operazione viene visualizzata una notifica
                    new Thread(
                            () -> {

                                try {

                                    Spedizione sped = PrenotazioneModulo.sendEmailEvento(pren, TipoEventoPren.promemoriaInvioSchedaPrenotazione, EventoBootStrap.getUsername(), dests);

                                    refreshRowCache();

                                    Notification notification = new Notification("Promemoria conferma prenotazione n. " + pren.getNumPrenotazione() + " inviato a " + sped.getDestinatario(), "", Notification.Type.HUMANIZED_MESSAGE);
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
        Prenotazione pren = (Prenotazione) getSelectedBean();
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
                Notification.show("Questa prenotazione non è ancora confermata.");
            }
        }

        // controllo che il livello di sollecito sia < 1
        if (cont) {
            if (pren.getLivelloSollecitoPagamento() > 0) {
                cont = false;
                Notification notification = new Notification("Il promemoria è gia stato inviato",
                        "\nSe vuoi puoi reinviarlo dagli Eventi Prenotazione.", Notification.Type.HUMANIZED_MESSAGE);
                notification.setDelayMsec(-1);
                notification.show(Page.getCurrent());
            }
        }

        // esegue
        if (cont) {
            DialogoConfermaInvioManuale dialog = new DialogoConfermaInvioManuale(pren, "Invio promemoria scadenza pagamento", "Vuoi inviare un promemoria scadenza pagamento?");
            dialog.setConfirmListener(new ConfirmDialog.ConfirmListener() {
                @Override
                public void confirmed(ConfirmDialog d) {

                    String dests = dialog.getDestinatari();

                    // esegue l'operazione in un thread separato
                    // al termine dell'operazione viene visualizzata una notifica
                    new Thread(
                            () -> {

                                try {

                                    Spedizione sped = PrenotazioneModulo.sendEmailEvento(pren, TipoEventoPren.promemoriaScadenzaPagamento, EventoBootStrap.getUsername(), dests);

                                    refreshRowCache();

                                    Notification notification = new Notification("Promemoria scadenza pagamento prenotazione n. " + pren.getNumPrenotazione() + " inviato a " + sped.getDestinatario(), "", Notification.Type.HUMANIZED_MESSAGE);
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
        Prenotazione pren = (Prenotazione) getSelectedBean();
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
            String title="Il congelamento di una prenotazione libera i posti impegnati e blocca l'invio di ulteriori solleciti";
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

                                    if(sped!=null){
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

//
//        // mostra il dialogo
//        if (cont) {
//            ConfirmDialog dialog = new DialogoCongelamentoOpzione(pren);
//            dialog.show(UI.getCurrent());
//        }
    }


    /**
     * Sposta delle prenotazioni ad altra rappresentazione
     * <p>
     * Invocato dai menu
     */
    public void spostaAdAltraData() {

        BeanItem[] beans = PrenotazioneTable.this.getSelectedBeans();
        Prenotazione[] aPren = new Prenotazione[beans.length];
        for (int i = 0; i < aPren.length; i++) {
            aPren[i] = (Prenotazione) beans[i].getBean();
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
        // queste property aggiunte servono per consentire
        // di effettuare ricerche su proprietà in relazione
        getJPAContainer().addNestedContainerProperty(PrenotazioneModulo.PROP_PROGETTO);
        getJPAContainer().addNestedContainerProperty(PrenotazioneModulo.PROP_EVENTO);
        getJPAContainer().addNestedContainerProperty(PrenotazioneModulo.PROP_STAGIONE);

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
            String imgName;
            String description = "";
            if (conf) {
                imgName = "confirmed_20px.png";
                description = "La prenotazione è confermata";
            } else {
                if (cong) {
                    imgName = "frozen_20px.png";
                    description = "La prenotazione è stata congelata";
                } else {
                    switch (levelSC) {
                        case 0:
                            imgName = null;
                            break;
                        case 1:
                            imgName = "bell_num_1_20px.png";
                            description = "E' stato inviato un sollecito di conferma prenotazione";
                            break;
                        case 2:
                            imgName = "bell_num_2_20px.png";
                            description = "Sono stati inviati due solleciti di conferma prenotazione";
                            break;
                        default:
                            imgName = "bell_num_other_20px.png";
                            description = "Sono stati inviati "+levelSC+" solleciti di conferma prenotazione";
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
            boolean conf = Lib.getBool(getContainerProperty(itemId, Prenotazione_.pagamentoConfermato.getName()).getValue());
            boolean regis = Lib.getBool(getContainerProperty(itemId, Prenotazione_.pagamentoRicevuto.getName()).getValue());
            int levelSC = Lib.getInt(getContainerProperty(itemId, Prenotazione_.livelloSollecitoPagamento.getName()).getValue());
            String imgName;
            String description = "";
            if (regis) {
                imgName = "confirmed_20px.png";
                description = "Il pagamento è stato confermato e registrato";
            } else {
                if (conf) {
                    imgName = "confirmed_half_20px.png";
                    description = "Il pagamento è stato confermato dal referente ma non ancora registrato";
                } else {
                    switch (levelSC) {
                        case 0:
                            imgName = null;
                            break;
                        case 1:
                            imgName = "bell_num_1_20px.png";
                            description = "E' stato inviato un sollecito di conferma pagamento";
                            break;
                        case 2:
                            imgName = "bell_num_2_20px.png";
                            description = "Sono stati inviati due solleciti di conferma pagamento";
                            break;
                        default:
                            imgName = "bell_num_other_20px.png";
                            description = "Sono stati inviati "+levelSC+" solleciti di conferma pagamento";
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
