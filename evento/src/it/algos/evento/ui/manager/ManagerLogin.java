package it.algos.evento.ui.manager;

import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import it.algos.evento.EventoSession;
import it.algos.evento.entities.company.Company;
import it.algos.evento.entities.company.Company_;
import it.algos.evento.ui.company.CompanyHome;
import it.algos.webbase.domain.utente.Utente;
import it.algos.webbase.web.lib.LibImage;
import it.algos.webbase.web.lib.LibResource;
import it.algos.webbase.web.login.Login;
import it.algos.webbase.web.login.LoginListener;

/**
 * Login page for the Manager
 */
public class ManagerLogin extends VerticalLayout {

	public ManagerLogin() {

		super();

		createUI();

		Login.getLogin().setLoginListener(new LoginListener() {

			@Override
			public void onUserLogin(Utente utente, boolean b) {
				// Avvia la UI del manager
				Component comp = new ManagerHome();
				UI.getCurrent().setContent(comp);

			}
		});

	}

	private void createUI(){

		setWidth("100%");
		setHeight("100%");

		// horizontal: spacer left + image + spacer right
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setWidth("100%");
		addSpacer(logoLayout);
		Resource res=(LibResource.getImgResource("splash_image.png"));
		if (res!=null) {
			Image img = LibImage.getImage(res);
			logoLayout.addComponent(img);
		}
		addSpacer(logoLayout);

		// vertical: spacer top + image layout + button panel + spacer bottom
		addSpacer(this);
		addComponent(logoLayout);
		addSpacer(this, 0.2f);
		addComponent(createButtonLayout());
		addSpacer(this, 0.2f);
		addSpacer(this);
	}

	private Component createButtonLayout(){
		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("100%");
		addSpacer(layout);
		layout.addComponent(createLoginButton());
		addSpacer(layout);
		return layout;
	}


	private Button createLoginButton(){
		Button button=new Button("Manager Login");
		button.setStyleName("loginbutton");
		button.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Login.getLogin().showLoginForm(UI.getCurrent());
			}
		});
		return button;
	}

	/**
	 * Aggiunge a un layout una label con larghezza espandibile al 100%
	 */
	private void addSpacer(AbstractOrderedLayout layout){
		addSpacer(layout, 1.0f);
	}

	/**
	 * Aggiunge a un layout una label con larghezza espandibile
	 */
	private void addSpacer(AbstractOrderedLayout layout, float ratio){
		Label label = new Label();
		layout.addComponent(label);
		layout.setExpandRatio(label, ratio);
	}


}
