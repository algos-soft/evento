package it.algos.evento.entities.tiporicevuta;

import it.algos.evento.multiazienda.EventoEntity;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.validation.constraints.Size;

/**
 * Created by alex on 30-05-2015.
 */
@Entity
public class TipoRicevuta extends EventoEntity {

    @NotEmpty
    @Size(min = 2, max = 30)
    private String sigla = "";

    @NotEmpty
    private String descrizione = "";


    public TipoRicevuta() {
        this("","");
    }

    public TipoRicevuta(String sigla, String descrizione) {
        this.sigla = sigla;
        this.descrizione = descrizione;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    @Override
    public String toString() {
        return sigla+" "+descrizione;
    }// end of method

}
