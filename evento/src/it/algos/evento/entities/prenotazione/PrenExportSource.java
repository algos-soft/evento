package it.algos.evento.entities.prenotazione;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import it.algos.webbase.web.importexport.ExportConfiguration;
import it.algos.webbase.web.importexport.ExportProvider;
import it.algos.webbase.web.updown.ExportStreamSource;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import com.vaadin.data.Container;


import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;

/**
 * Custom ExportStreamSource needed to append totals row to the worksheet
 */
public class PrenExportSource extends ExportStreamSource {

    // values to be injected
    private int totPosti=998;
    private float totImporto=12525.34f;

    public PrenExportSource(ExportConfiguration config) {
        super(config);
    }

    @Override
    protected void populateWorkbook() {
        super.populateWorkbook();

        Cell cell;
        Row row;

        // add totals

        row = addRow();

        cell = row.createCell(0);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue("Totale posti");

        cell = row.createCell(1);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(totPosti);

        row = addRow();

        cell = row.createCell(0);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue("Totale importo");

        cell = row.createCell(1);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        BigDecimal bd = new BigDecimal(totImporto);
        BigDecimal scaled = bd.setScale(2, RoundingMode.HALF_UP);
        cell.setCellValue(scaled.doubleValue());

    }
}
