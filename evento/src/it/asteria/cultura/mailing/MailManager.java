package it.asteria.cultura.mailing;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import it.algos.evento.entities.lettera.Lettera;
import it.algos.evento.entities.lettera.LetteraService;
import it.algos.web.lib.LibDate;
import it.algos.web.lib.LibSession;
import it.asteria.cultura.destinatario.Destinatario;

import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class MailManager {

    //-- nome/titolo della spedizione
    private String titolo;

    //-- lettera di riferimento, da spedire
    private Lettera lettera;

    //-- lista (wrapper) destinatari
    private ArrayList<DestWrap> lista;


//    private Object[] ids;
//    private TextField titoloField;
//    private ArrayComboField letteraField;
//    private OptionGroup destinatariOptions;
//    private String itemReferente = "Referente della prenotazione";
//    private String itemScuola = "Mail della scuola";
//    private String itemEntrambi = "Entrambi gli indirizzi";
//    private String currentItem = "";


    public MailManager(MailWrap wrap) {

        if (wrap != null) {
            this.titolo = wrap.getTitolo();
            this.lettera = wrap.getLettera();
            this.lista = wrap.getLista();
        }// fine del blocco if

        this.gestione();
    }// end of constructor


    /**
     * Gestione <br>
     * <p>
     * Registra la mailing
     * Registra le spedizioni ai singoli destinatari
     * Spedisce effettivamente le mail
     * Conferma la spedizione nei singoli records
     */
    private void gestione() {
        Mailing mailing = null;

        mailing = this.registraMailing();
        if (mailing != null) {
            this.registraSpedisceConferma(mailing);
        }// fine del blocco if

    }// end of method

    /**
     * Registra la mailing
     * <p>
     * Creazione di 1 record di Mailing <br>
     * Viene aggiunta la data corrente
     */
    private Mailing registraMailing() {
        Mailing mailing = new Mailing();

        mailing.setTitolo(titolo);
        mailing.setLettera(lettera);
        mailing.save();

        return mailing;
    }// end of method


    /**
     * Registrazione
     * <p>
     * Creazione di n records di Destinatari <br>
     * Sono già stati eliminati gli indirizzi doppi
     * <p>
     * Spazzola la lista di destinatari e per ognuno:
     * Registra la spedizione
     * Spedisce effettivamente la mail
     * Conferma la spedizione nel flag del record
     */
    private void registraSpedisceConferma(Mailing mailing) {

        for (DestWrap wrap : lista) {
            regSpeConf(mailing, wrap);
        } // fine del ciclo for-each

    }// end of method


    /**
     * Registra la spedizione
     * Spedisce effettivamente la mail
     * Conferma la spedizione nel flag del record
     */
    private void regSpeConf(Mailing mailing, DestWrap wrap) {
        String indirizzo = "";
        Destinatario destinatario = null;
        boolean spedita = false;

        indirizzo = wrap.getIndirizzo();
        destinatario = registra(mailing, indirizzo);

        if (destinatario != null) {
            spedita = spedisce(destinatario, wrap);
        }// fine del blocco if

        if (spedita) {
            conferma(destinatario);
        }// fine del blocco if

    }// end of method

    /**
     * Registra la spedizione (una per ogni destinatario)
     */
    private Destinatario registra(Mailing mailing, String indirizzo) {
        Destinatario destinatario = null;

        if (mailing != null && !indirizzo.equals("")) {
            destinatario = new Destinatario();
            destinatario.setMailing(mailing);
            destinatario.setIndirizzo(indirizzo);
            destinatario.save();
        }// fine del blocco if

        return destinatario;
    }// end of method

    /**
     * Spedisce la singola mail
     */
    private boolean spedisce(Destinatario destinatario, DestWrap wrap) {
        boolean spedita = false;
        String oggetto = "";
        String titolo = "";
        String dest = "";
        String testo = "";
        HashMap<String, String> mappa = wrap.getMappa();

        if (destinatario != null && wrap != null) {
            dest = destinatario.getIndirizzo();
            oggetto = destinatario.getOggetto();
            titolo = destinatario.getTitolo();
            testo = destinatario.getTesto(mappa);

            if (LibSession.isDebug()) {
                String testoDebug;

//                String hostName = "smtp.algos.it";
//                int smtpPort = 25;
//                boolean useAuth = true;
//                String username = "gac@algos.it";
//                String password = "fulvia";
//                String from = "alex@algos.it";
//                boolean html = false;
//                String allegati = "";

                testoDebug = "Titolo della mail: ";
                testoDebug += titolo + "\n\n";
                testoDebug += "Spedita a: ";
                testoDebug += dest + "\n\n";
                testoDebug += "Testo definitivo: \n";

                dest = "gac@algos.it";
                testo = testoDebug + testo;

            } else {
//                new Notification("Occhio che non sei in debug!",
//                        "Cambia il parametro d'ingresso",
//                        Notification.TYPE_ERROR_MESSAGE, true)
//                        .show(Page.getCurrent());
            }// fine del blocco if-else

            try { // prova ad eseguire il codice
                spedita = LetteraService.sendMail(dest, oggetto, testo);
            } catch (Exception unErrore) { // intercetta l'errore
                String alfa = "";
            }// fine del blocco try-catch

        }// fine del blocco if

        return spedita;
    }// end of method

    /**
     * Spedizione <br>
     */
    private void spedisceOld(String titolo, Lettera lettera, ArrayList<String> destinatari) {
        String dest = "";
        String oggetto = "";
        String testo = "";
        boolean spedita = false;

        if (LibSession.isDebug()) {
            String hostName = "smtp.algos.it";
            int smtpPort = 25;
            boolean useAuth = true;
            String username = "gac@algos.it";
            String password = "fulvia";
            String from = "alex@algos.it";
            boolean html = false;
            String allegati = "";

            dest = "gac@algos.it";
            oggetto = "Test/Prova";
            testo = lettera.getTesto();

            try { // prova ad eseguire il codice
//                spedita = LetteraService.sendMail(hostName, smtpPort, useAuth, username, password, from, dest, oggetto, testo, html, allegati);
                spedita = LetteraService.sendMail(dest, oggetto, testo, false);
            } catch (Exception unErrore) { // intercetta l'errore
                String alfa = "";
            }// fine del blocco try-catch
        } else {
            new Notification("Occhio che non sei in debug!",
                    "Cambia il parametro d'ingresso",
                    Notification.TYPE_ERROR_MESSAGE, true)
                    .show(Page.getCurrent());
        }// fine del blocco if-else

    }// end of method

    /**
     * Conferma la singola spedizione (una per ogni destinatario)
     */
    private void conferma(Destinatario destinatario) {

        if (destinatario != null) {
            destinatario.setSpedita(true);
            destinatario.setDataSpedizione(LibDate.today());
            destinatario.save();
        }// fine del blocco if

    }// end of method

    private String getTitolo() {
        return titolo;
    }// end of method


    private Lettera getLettera() {
        return lettera;
    }// end of method


}// end of class
