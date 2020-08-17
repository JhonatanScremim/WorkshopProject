package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import application.Main;
import gui.util.Alerts;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import model.services.DepartmentService;
import model.services.SellerService;

public class MainController implements Initializable {

	@FXML
	private MenuItem menuItemSeller;

	@FXML
	private MenuItem menuItemDepartment;

	@FXML
	private MenuItem menuItemAbout;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

	}

	@FXML
	public void onMenuItemSeller() {
		loadView("/gui/FXMLSellerList.fxml", (SellerListController controller) -> {
			controller.setSellerService(new SellerService());
			controller.updateTableView();//Mostrar os vendedores já registrados 
		});
	}

	@FXML
	public void onMenuItemDepartment() {
		loadView("/gui/FXMLDepartmentList.fxml", (DepartmentListController controller) -> {
			controller.setDepartmentService(new DepartmentService());
			controller.updateTableView();//Mostrar os departamentos já registrados 
		});
	}

	@FXML
	public void onMenuItemAbout() {
		loadView("/gui/FXMLAbout.fxml", x -> {});//x -> {} comando para não levar em nada, pois About não carrega nada
	}

	//como synchronized você garante que durante o processo ele não vai ser interrompido durante o multi-Thread
	private synchronized <T> void loadView(String absoluteName, Consumer<T> initializingAction) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			VBox newVbox = loader.load();
			
			Scene mainScene = Main.getMainScene();
			
			VBox mainVbox = (VBox) ((ScrollPane)mainScene.getRoot()).getContent();
			Node mainMenu = mainVbox.getChildren().get(0);
			mainVbox.getChildren().clear();
			mainVbox.getChildren().add(mainMenu);
			mainVbox.getChildren().addAll(newVbox.getChildren());
			
			//Esses comandos irá executar a função que você passar como argumento no metodo (initializingAction)
			T controller = loader.getController();
			initializingAction.accept(controller);
			
		} catch (IOException e) {
			Alerts.showAlert("Erro !!", "Erro ao carregar a página", e.getMessage(), AlertType.ERROR);
		}
	}
	
	

}
