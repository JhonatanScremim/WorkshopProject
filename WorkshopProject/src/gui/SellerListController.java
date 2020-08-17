package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Seller;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener {
	
	private SellerService service;

	@FXML
	private TableView<Seller> tableViewSeller;

	@FXML 
	private TableColumn<Seller, Integer> tableColumnEmail;
	
	@FXML 
	private TableColumn<Seller, Date> tableColumnBirthDate;

	@FXML 
	private TableColumn<Seller, Double> tableColumnBaseSalary;
	
	@FXML 
	private TableColumn<Seller, Integer> tableColumnId;

	@FXML 
	private TableColumn<Seller, String> tableColumnName;
	
	@FXML
	private TableColumn<Seller, Seller> tableColumnEdit;
	
	@FXML
	private TableColumn<Seller, Seller> tableColumnRemove;

	@FXML 
	private Button btNew;
	
	private ObservableList<Seller> obsList;
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializaNodes();
	}
	
	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Seller obj = new Seller();
		createDialogForm(obj, "/gui/FXMLSellerForm.fxml", parentStage);
	}

	private void initializaNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		//Formtar data
		Utils.formatTableColumnDate(tableColumnBirthDate, "dd/MM/yyyy");
		
		tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		//Formatar Double
		Utils.formatTableColumnDouble(tableColumnBaseSalary, 2);
		
		
		//fazer o tableView acompanhar a altura da janela
		Stage stage = (Stage)Main.getMainScene().getWindow();
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty());
		
	}
	
	public void setSellerService(SellerService service) {
		this.service = service;
	}
	
	public void updateTableView() {
		if(service == null) {
			throw new IllegalStateException("O serviço está nulo");
		}
		List<Seller> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewSeller.setItems(obsList);
		initEditButtons();
		initRemoveButton();
	}
	
	private void createDialogForm(Seller obj, String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();
			
			SellerFormController controller = loader.getController();
			controller.setSeller(obj);
			controller.setServices(new SellerService(), new DepartmentService());
			
			//Carregar os departamentos no comboBox
			controller.loadAssociatedObjects();
			
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();
			
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Registrar vendedor:");
			dialogStage.setScene(new Scene(pane));
			dialogStage.setResizable(false);//Diz se a janela pode ou não ser redimencionada
			dialogStage.initOwner(parentStage);
			dialogStage.initModality(Modality.WINDOW_MODAL);//Trava a tela, enquanto você não fechar ela vc não usa outra
			dialogStage.showAndWait();
			
		}
		catch(IOException e) {
			e.printStackTrace();
			Alerts.showAlert("Erro !!", "Erro ao carregar",e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
		
	}
	
	//Botão de editar ////////////////////////
	private void initEditButtons() {
		tableColumnEdit.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEdit.setCellFactory(param -> new TableCell<Seller, Seller>(){
			private final Button button = new Button("Editar");
			
			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				
				if(obj == null) {
					setGraphic(null);
					return;
				}
				
				setGraphic(button);
				button.setOnAction(
						event -> createDialogForm(
								obj, "/gui/FXMLSellerForm.fxml",Utils.currentStage(event)));
			}
		});
	}
	
	
	private void initRemoveButton() {
		tableColumnRemove.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnRemove.setCellFactory(param -> new TableCell<Seller, Seller>(){
			private final Button button = new Button("Remover");
			
			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				
				if(obj == null) {
					setGraphic(null);
					return;
				}
				
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
							
			}

		});
	}

	private void removeEntity(Seller obj) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confimação", "Tem certeza que você quer deletar");
		
		if(result.get() == ButtonType.OK) {
			if(service == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				service.remove(obj);
				updateTableView();
			}
			catch(DbIntegrityException e) {
				Alerts.showAlert("Erro ao remover", null, e.getMessage(), AlertType.ERROR);
			}
		}
		
		
	}
	
}
		













