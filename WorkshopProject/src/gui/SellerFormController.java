package gui;

import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exception.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;

	@FXML
	private TextField txtName;

	@FXML
	private TextField txtEmail;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField txtSalary;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	private ObservableList<Department> obsList;

	@FXML
	private Label lbErroName;

	@FXML
	private Label lbErroEmail;

	@FXML
	private Label lbErroBirthDate;

	@FXML
	private Label lbErroSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 70);
		Constraints.setTextFieldDouble(txtSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		
		initializeComboBoxDepartment();
	}

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setServices(SellerService service, DepartmentService departmentService) {
		this.service = service;
		this.departmentService = departmentService;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity is null");
		}
		if (service == null) {
			throw new IllegalStateException("Service is null");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();// Fechar janela após salvar
		} catch (DbException e) {
			Alerts.showAlert("Erro ao salvar o departamento", null, e.getMessage(), AlertType.ERROR);
		} catch (ValidationException e) {
			setErrorMessages(e.getErrors());
		}
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}

	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity is null");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(String.valueOf(entity.getName()));
		txtEmail.setText(String.valueOf(entity.getEmail()));

		if (entity.getBirthDate() != null) {
			LocalDate localDate = LocalDateTime.ofInstant(entity.getBirthDate().toInstant(), ZoneOffset.UTC)
					.toLocalDate();
			dpBirthDate.setValue(localDate);

		}

		Locale.setDefault(Locale.US);
		txtSalary.setText(String.format("%.2f", entity.getBaseSalary()));
		
		if(entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		}
		else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}
		
	}

	private Seller getFormData() {
		Seller dp = new Seller();

		ValidationException exception = new ValidationException("Erro de validação");

		dp.setId(Utils.tryParseToInt(txtId.getText()));

		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("nome", "  O campo não pode ser vazio !!");
		}
		dp.setName(txtName.getText());
		
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addError("email", "  O campo não pode ser vazio !!");
		}
		dp.setEmail(txtEmail.getText());
		
		if(dpBirthDate.getValue() == null) {
			exception.addError("aniversario", "  O campo não pode ser vazio !!");
		}
		
		else {

			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			dp.setBirthDate(Date.from(instant));
			
		}
		if (txtSalary.getText() == null || txtSalary.getText().trim().equals("")) {
			exception.addError("salario", "  O campo não pode ser vazio !!");
		}
		dp.setBaseSalary(Utils.tryParseToDouble(txtSalary.getText()));
		
		dp.setDepartment(comboBoxDepartment.getValue());
		
		if (exception.getErrors().size() > 0) {
			throw exception;
		}

		return dp;
	}

	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("DepartmentService was null");
		}
		List<Department> list = departmentService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

	private void setErrorMessages(Map<String, String> erro) {
		Set<String> fields = erro.keySet();

		
		lbErroName.setText((fields.contains("nome") ? erro.get("nome") : ""));

		lbErroEmail.setText((fields.contains("email") ? erro.get("email") : ""));
		
		lbErroBirthDate.setText((fields.contains("aniversario") ? erro.get("aniversario") : ""));
		
		lbErroSalary.setText((fields.contains("salario") ? erro.get("salario") : ""));
		
		
	}

}
