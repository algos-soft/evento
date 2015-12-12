package it.algos.evento.ui.company;

import com.vaadin.data.util.converter.StringToBigDecimalConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import it.algos.evento.EventoApp;
import it.algos.evento.entities.stagione.Stagione;
import it.algos.evento.multiazienda.EQuery;
import it.algos.webbase.web.lib.LibSession;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Dashboard della Company
 */
public class CompanyDashboard extends VerticalLayout {

    private StringToIntegerConverter intConverter = new StringToIntegerConverter();
    private StringToBigDecimalConverter bdConverter = new StringToBigDecimalConverter();
    private CompanyHome home;

    private HorizontalLayout titlePlaceholder = new HorizontalLayout();
    private HorizontalLayout pscadPlaceholder = new HorizontalLayout();
    private HorizontalLayout confPagScadPlaceholder = new HorizontalLayout();
    private HorizontalLayout eventiInProgrammaPlaceholder = new HorizontalLayout();
    private HorizontalLayout rappresentazioniPlaceholder = new HorizontalLayout();
    private HorizontalLayout prenotazioniRicevutePlaceholder = new HorizontalLayout();
    private HorizontalLayout postiPrenotatiPlaceholder = new HorizontalLayout();

    private InfoBar barNumeri;
    private InfoBar barPosti;
    private InfoBar barImporti;


    // classi per il CSS iniettato
    private static final String CSS_MIDDLE = "middle";
    private static final String CSS_BIG = "big";

    public CompanyDashboard(CompanyHome home) {
        this.home = home;

        //addStyleName("lightGrayBg");

        setWidthUndefined();
        setHeight("100%");
        setMargin(false);

        injectCSS();

        barNumeri = new InfoBar("Prenotazioni", home, false);
        barPosti = new InfoBar("Posti", home, false);
        barImporti = new InfoBar("Importi", home, true);

        init();

        //update();

    }

    /**
     * Crea e aggiunge i componenti
     */
    private void init() {

        addComponent(titlePlaceholder);
        addComponent(new Hr());

        // riga scaduti
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthUndefined();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.addComponent(pscadPlaceholder);
        layout.addComponent(confPagScadPlaceholder);
        addComponent(layout);

        // barre grafiche
        addComponent(barNumeri);
        addComponent(new VSpacer());
        addComponent(barPosti);
        addComponent(new VSpacer());
        addComponent(barImporti);
        addComponent(new VSpacer());
        addComponent(new InfoBarLegend());


        addComponent(new Hr());
        addComponent(eventiInProgrammaPlaceholder);
        addComponent(rappresentazioniPlaceholder);
        addComponent(prenotazioniRicevutePlaceholder);
        addComponent(postiPrenotatiPlaceholder);

//        String title="titolo";
//
//        DefaultPieDataset dataset = new DefaultPieDataset( );
//        dataset.setValue( "IPhone 5s" , new Double( 20 ) );
//        dataset.setValue( "SamSung Grand" , new Double( 20 ) );
//        dataset.setValue( "MotoG" , new Double( 40 ) );
//        dataset.setValue( "Nokia Lumia" , new Double( 10 ) );
//
//        JFreeChart chart = ChartFactory.createPieChart(
//                "Mobile Sales",  // chart title
//                dataset,        // data
//                true,           // include legend
//                true,
//                false);
//
//        JFreeChartWrapper jFreeChartWrapper = new JFreeChartWrapper(chart) {
//
//            @Override
//            public void attach() {
//                super.attach();
//                markAsDirty();
//            }
//
//        };
//
//        addComponent(jFreeChartWrapper);


//        addComponent(createBasicDemo());


//
//
//        DataSeries dataSeries = new DataSeries()
//                .add(2, 6, 7, 10);
//
//        SeriesDefaults seriesDefaults = new SeriesDefaults()
//                .setRenderer(SeriesRenderers.BAR);
//
//        Axes axes = new Axes()
//                .addAxis(
//                        new XYaxis()
//                                .setRenderer(AxisRenderers.CATEGORY)
//                                .setTicks(
//                                        new Ticks()
//                                                .add("a", "b", "c", "d")));
//
//        Highlighter highlighter = new Highlighter()
//                .setShow(false);
//
//        Options options = new Options()
//                .setSeriesDefaults(seriesDefaults)
//                .setAxes(axes)
//                .setHighlighter(highlighter);
//
//        DCharts chart = new DCharts()
//                .setDataSeries(dataSeries)
//                .setOptions(options)
//                .show();
//
//        addComponent(chart);

    }


    /**
     * Aggiorna tutti i valori visualizzati
     */
    public void update() {

        creaTitoloDashboard();
        createPrenScadute();
        createConfPagaScadute();

        // barra n. prenotazioni
        barNumeri.update(EQuery.countPrenotazioniNonConfermate(),
                EQuery.countPrenotazioniPagamentoNonConfermato(),
                EQuery.countPrenotazioniPagamentoConfermato(),
                EQuery.countPrenotazioniPagamentoRicevuto());

        // barra n. posti
        barPosti.update(EQuery.sumPostiPrenotazioniNonConfermate(),
                EQuery.sumPostiPrenotazioniPagamentoNonConfermato(),
                EQuery.sumPostiPrenotazioniPagamentoConfermato(),
                EQuery.sumPostiPrenotazioniPagamentoRicevuto());

        // barra importi
        barImporti.update(EQuery.sumImportoPrenotazioniNonConfermate().intValue(),
                EQuery.sumImportoPrenotazioniPagamentoNonConfermato().intValue(),
                EQuery.sumImportoPrenotazioniPagamentoConfermato().intValue(),
                EQuery.sumImportoPrenotazioniPagamentoRicevuto().intValue());

        createEventiInProgramma();
        createRappresentazioni();
        createPrenotazioniRicevute();
        createPostiPrenotati();

        //updateBarNumeri();

    }

    /**
     * Crea il componente UI che rappresenta il titolo della dashboard
     */
    private void creaTitoloDashboard() {
        String s = "Andamento stagione " + Stagione.getStagioneCorrente().toString();
        HTMLLabel label = new HTMLLabel();
        label.setValue(spanBig(s));

        titlePlaceholder.removeAllComponents();
        titlePlaceholder.addComponent(label);
    }





    /**
     * Crea il componente UI che rappresenta le conferme prenotazione scadute
     */
    private void createPrenScadute() {

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setSpacing(true);
        HTMLLabel label = new HTMLLabel();
        int quante = EQuery.countPrenRitardoConferma(getStagione());
        String s = spanSmall("conferme prenotazione scadute:");
        label.setValue(s);
        Button button = new NumButton(""+quante,s, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                // regola l'attributo che farà sì che il modulo esegua la query quando diventa visibile
                LibSession.setAttribute(EventoApp.KEY_MOSTRA_PREN_RITARDO_CONFERMA, true);

                // clicca sul menu Prenotazioni
                clickMenuPren();

            }
        });

        hLayout.addComponent(label);
        hLayout.addComponent(button);
        hLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        hLayout.setComponentAlignment(button, Alignment.MIDDLE_LEFT);

        pscadPlaceholder.removeAllComponents();
        pscadPlaceholder.addComponent(hLayout);

    }






    /**
     * Crea il componente UI che rappresenta le conferme di pagamento scadute
     */
    private void createConfPagaScadute() {

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setSpacing(true);
        HTMLLabel label = new HTMLLabel();
        int quante = EQuery.countPrenRitardoPagamento1(getStagione());
        String s = spanSmall("conferme di pagamento scadute:");
        label.setValue(s);
        Button button = new NumButton(""+quante,s, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {

                // regola l'attributo che farà sì che il modulo esegua la query quando diventa visibile
                LibSession.setAttribute(EventoApp.KEY_MOSTRA_PREN_RITARDO_PAGAMENTO_1, true);

                // clicca sul menu Prenotazioni
                clickMenuPren();

            }
        });

        hLayout.addComponent(label);
        hLayout.addComponent(button);
        hLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        hLayout.setComponentAlignment(button, Alignment.MIDDLE_LEFT);

        confPagScadPlaceholder.removeAllComponents();
        confPagScadPlaceholder.addComponent(hLayout);

    }



    /**
     * Crea il componente UI che rappresenta gli eventi in programma
     */
    private void createEventiInProgramma() {
        HTMLLabel label;
        label = new HTMLLabel();
        label.setValue(spanSmall("eventi in programma:\u2003") + spanBig(getString(EQuery.countEventi(getStagione()))));

        eventiInProgrammaPlaceholder.removeAllComponents();
        eventiInProgrammaPlaceholder.addComponent(label);

    }


    /**
     * Crea il componente UI che rappresenta le rappresentazioni
     */
    private void createRappresentazioni() {
        HTMLLabel label = new HTMLLabel();
        int totRapp = EQuery.countRappresentazioni(getStagione());
        int rappPassate = EQuery.countRappresentazioni(getStagione(), new Date());
        int percent = Math.round(rappPassate * 100 / totRapp);
        label.setValue(spanSmall("rappresentazioni effettuate:\u2003") + spanBig(getString(rappPassate)) + spanSmall("\u2003su\u2003") + spanBig(getString(totRapp) + " (" + percent + "%)"));

        rappresentazioniPlaceholder.removeAllComponents();
        rappresentazioniPlaceholder.addComponent(label);
    }


    /**
     * Crea il componente UI che rappresenta le prenotazioni ricevute
     */
    private void createPrenotazioniRicevute() {
        HorizontalLayout hLayout;
        HTMLLabel label;
        Button button;

        hLayout = new HorizontalLayout();
        label = new HTMLLabel();
        int prenRicevute = EQuery.countPrenotazioni(getStagione());
        int prenCongelate = EQuery.countPrenotazioniCongelate(getStagione());
        String s = spanSmall("prenotazioni ricevute:\u2003") + spanBig(getString(prenRicevute));
        button = null;
        if (prenCongelate > 0) {
            s += spanSmall("\u2003(di cui congelate:\u2003");
            s += spanBig(getString(prenCongelate));
            s += spanSmall(")");

            button = new Button("Vedi", new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent clickEvent) {

                    // regola l'attributo che farà sì che il modulo esegua la query quando diventa visibile
                    LibSession.setAttribute(EventoApp.KEY_MOSTRA_PREN_CONGELATE, true);

                    // clicca sul menu Prenotazioni
                    clickMenuPren();

                }
            });

        }
        s += "\u2003";
        label.setValue(s);
        hLayout.addComponent(label);
        if (prenCongelate > 0) {
            hLayout.addComponent(button);
            hLayout.setComponentAlignment(button, Alignment.MIDDLE_LEFT);
        }

        prenotazioniRicevutePlaceholder.removeAllComponents();
        prenotazioniRicevutePlaceholder.addComponent(label);

    }

    /**
     * Crea il componente UI che rappresenta i posti prenotati
     */
    private void createPostiPrenotati() {
        HTMLLabel label;
        label = new HTMLLabel();
        int prenotati = EQuery.countPostiPrenotati(getStagione());
        int disponibili = EQuery.countCapienza(getStagione());
        int percent = Math.round(prenotati * 100 / disponibili);
        label.setValue(spanSmall("posti prenotati:\u2003") + spanBig(getString(prenotati)) + spanSmall("\u2003su\u2003") + spanBig(getString(disponibili) + " (" + percent + "%)"));

        postiPrenotatiPlaceholder.removeAllComponents();
        postiPrenotatiPlaceholder.addComponent(label);

    }


    private void injectCSS() {

        Page.Styles styles = Page.getCurrent().getStyles();

        styles.add("." + CSS_MIDDLE + " {   display: inline-block;" +
                "  vertical-align: middle;" +
                "  font-size:1.2em;" +
                "  line-height: normal;}");

        styles.add("." + CSS_BIG + " { font-weight:bold; font-size:2em;}");

    }

    private String spanSmall(String text) {
        return "<span class='" + CSS_MIDDLE + "'>" + text + "</span>";
    }

    private String spanBig(String text) {
        return "<span class='" + CSS_MIDDLE + " " + CSS_BIG + "'>" + text + "</span>";
    }

    /**
     * Ritorna la stagione corrente
     */
    private Stagione getStagione() {
        return Stagione.getStagioneCorrente();
    }


    private String getString(int num) {
        return intConverter.convertToPresentation(num, String.class, null);
    }

    private String getString(BigDecimal num) {
        return bdConverter.convertToPresentation(num, String.class, null);
    }

    /**
     * Clicca sul menu Prenotazioni
     */
    private void clickMenuPren() {
        MenuBar.MenuItem mi = home.getItemPrenotazioni();
        mi.getCommand().menuSelected(mi);
        if (mi.isCheckable()) {
            mi.setChecked(!mi.isChecked());
        }
    }


    /**
     * HTML Label
     */
    private class HTMLLabel extends Label {

        public HTMLLabel(String content) {
            super(content, ContentMode.HTML);
            //addStyleName("greenBg");
        }

        public HTMLLabel() {
            this("");
        }
    }

    /**
     * Horizontal divider
     */
    private class Hr extends Label {
        Hr() {
            super("<hr>", ContentMode.HTML);
            addStyleName("hrule");
        }
    }


    /**
     * Vertical Spacer
     */
    private class VSpacer extends Label {

        VSpacer(double ems) {
            String h = ems + "em";
            setHeight(h);
        }

        VSpacer() {
            this(1);
        }

    }


    private class NumButton extends Button{

        public NumButton(String caption, String tooltip, ClickListener listener) {
            super(caption, listener);
            addStyleName("infoBarSegment");
            addStyleName("infoBarSegment1");
            setHeight("2em");
            setHtmlContentAllowed(true);
            setDescription(tooltip+" "+caption);
        }
    }


}
