package it.algos.evento.entities.company;

import com.vaadin.ui.Notification;
import it.algos.evento.demo.DemoDataGenerator;
import it.algos.webbase.domain.utente.Utente;
import it.algos.webbase.web.lib.LibCrypto;

/**
 * Created by Alex on 03/10/15.
 */
public class CompanyService {

    /**
     * Activate a Company.
     * - create a corrisponding user with the given password
     * - create demo data for the new company
     *
     * @param company    the company
     * @param password   the password for the user (in clear text)
     * @param createData true to create demo data
     */
    public static boolean activateCompany(Company company, String password, boolean createData) {

        // create a user
        Utente user = new Utente();
        user.setNickname(company.getCompanyCode());
        user.setPassword(LibCrypto.encrypt(password));
        user.setEnabled(true);
        user.save();

        // create demo data in background
        if (createData) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DemoDataGenerator.createDemoData(company);
                    Notification.show("Creazione dati demo completata per " + company + ".");
                }
            }).start();
        }

        return true;
    }


}
