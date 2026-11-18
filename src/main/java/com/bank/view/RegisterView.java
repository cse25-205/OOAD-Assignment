package com.bank.view;

import com.bank.controller.RegistrationController;
import com.bank.util.BankException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Boundary class: registration form for individuals and organisations. No business logic here. */
public class RegisterView extends ScrollPane {
    private final GridPane form = new GridPane();
    private final List<Node> individualOnly = new ArrayList<>();
    private final List<Node> organisationOnly = new ArrayList<>();
    private int row;

    public RegisterView(Navigator navigator, RegistrationController controller) {
        form.setHgap(10);
        form.setVgap(10);

        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("Individual", "Organisation"));
        type.setValue("Individual");
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        password.setPromptText("At least 8 characters");
        TextField address = new TextField();
        TextField contact = new TextField();
        TextField firstName = new TextField();
        TextField surname = new TextField();
        TextField idNumber = new TextField();
        TextField employerName = new TextField();
        employerName.setPromptText("Optional - needed for a Cheque account");
        TextField employerAddress = new TextField();
        employerAddress.setPromptText("Optional - needed for a Cheque account");
        TextField orgName = new TextField();
        TextField regNumber = new TextField();
        DatePicker regDate = new DatePicker();
        TextField contactPerson = new TextField();

        addRow("Customer type", type, null);
        addRow("Username", username, null);
        addRow("Password", password, null);
        addRow("Address", address, null);
        addRow("Contact details", contact, null);
        addRow("First name", firstName, individualOnly);
        addRow("Surname", surname, individualOnly);
        addRow("ID number", idNumber, individualOnly);
        addRow("Employer name", employerName, individualOnly);
        addRow("Employer address", employerAddress, individualOnly);
        addRow("Organisation name", orgName, organisationOnly);
        addRow("Registration number", regNumber, organisationOnly);
        addRow("Date of registration", regDate, organisationOnly);
        addRow("Contact person", contactPerson, organisationOnly);

        type.setOnAction(e -> showFieldsFor(type.getValue()));
        showFieldsFor(type.getValue());

        Label feedback = Ui.errorLabel();
        Button submit = Ui.primary("Register");
        submit.setOnAction(e -> {
            try {
                if ("Individual".equals(type.getValue())) {
                    controller.registerIndividual(username.getText(), password.getText(), firstName.getText(),
                            surname.getText(), idNumber.getText(), address.getText(), contact.getText(),
                            employerName.getText(), employerAddress.getText());
                } else {
                    controller.registerOrganisation(username.getText(), password.getText(), orgName.getText(),
                            regNumber.getText(), regDate.getValue(), address.getText(), contact.getText(),
                            contactPerson.getText());
                }
                Ui.info("Registration successful. You can now log in.");
                navigator.showLogin(null);
            } catch (BankException ex) {
                feedback.setText(ex.getMessage());
            }
        });
        Button back = Ui.link("Back to login");
        back.setOnAction(e -> navigator.showLogin(null));

        VBox box = new VBox(14, Ui.title("Register as a customer"), form, feedback, new HBox(12, submit, back));
        box.setPadding(new Insets(24));
        setContent(box);
        setFitToWidth(true);
    }

    private void addRow(String labelText, Node field, List<Node> group) {
        Label label = new Label(labelText);
        form.add(label, 0, row);
        form.add(field, 1, row);
        row++;
        if (group != null) {
            group.add(label);
            group.add(field);
        }
    }

    private void showFieldsFor(String customerType) {
        boolean individual = "Individual".equals(customerType);
        for (Node n : individualOnly) { n.setVisible(individual); n.setManaged(individual); }
        for (Node n : organisationOnly) { n.setVisible(!individual); n.setManaged(!individual); }
    }
}
