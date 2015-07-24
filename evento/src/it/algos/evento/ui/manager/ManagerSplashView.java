package it.algos.evento.ui.manager;

import com.vaadin.ui.Component;

import it.algos.evento.EventoApp;
import it.algos.evento.SplashScreen;
import it.algos.web.lib.LibResource;

public class ManagerSplashView extends AbsMenuBarView {

	public static final String NAME = "splash";

	public ManagerSplashView() {
		super();
		Component splash = new SplashScreen(LibResource.getImgResource(EventoApp.IMG_FOLDER_NAME,"splash_image.png"));
		addComponent(splash);
	}// end of method

}// end of class
