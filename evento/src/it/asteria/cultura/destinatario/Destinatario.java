package it.asteria.cultura.destinatario;

import it.algos.evento.entities.lettera.Lettera;
import it.algos.evento.multiazienda.EventoEntity;
import it.algos.evento.multiazienda.EventoEntityQuery;
import it.asteria.cultura.mailing.Mailing;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;

@Entity
public class Destinatario extends EventoEntity {

    public static EventoEntityQuery<Destinatario> query = new EventoEntityQuery(Destinatario.class);
    @NotNull
    private Mailing mailing = null;
    @NotEmpty
    private String indirizzo = "";
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataSpedizione;
    private boolean spedita;

    public Destinatario() {
        this(null, "", null, false);
    }// end of constructor

    public Destinatario(Mailing mailing, String indirizzo, Date dataSpedizione, boolean spedita) {
        super();
        this.setMailing(mailing);
        this.setIndirizzo(indirizzo);
        this.setDataSpedizione(dataSpedizione);
        this.setSpedita(spedita);
    }// end of constructor

    @Override
    public String toString() {
        return getIndirizzo();
    }// end of method

    public Mailing getMailing() {
        return mailing;
    }

    public void setMailing(Mailing mailing) {
        this.mailing = mailing;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
    }

    public Date getDataSpedizione() {
        return dataSpedizione;
    }

    public void setDataSpedizione(Date dataSpedizione) {
        this.dataSpedizione = dataSpedizione;
    }

    public boolean isSpedita() {
        return spedita;
    }

    public void setSpedita(boolean spedita) {
        this.spedita = spedita;
    }

    public String getOggetto() {
        String oggetto = "";
        Mailing mailing = this.getMailing();

        if (mailing != null) {
            oggetto = mailing.getOggetto();
        }// fine del blocco if

        return oggetto;
    }// end of method

    public String getTitolo() {
        String titolo = "";
        Mailing mailing = this.getMailing();

        if (mailing != null) {
            titolo = mailing.getTitolo();
        }// fine del blocco if

        return titolo;
    }// end of method

    public String getTesto() {
        return getTesto(null);
    }// end of method

    public String getTesto(HashMap<String, String> mappaEscape) {
        String testo = "";
        Mailing mailing = this.getMailing();

        if (mailing != null) {
            testo = mailing.getTestOut(mappaEscape);
        }// fine del blocco if

        return testo;
    }// end of method

}// end of entity class
