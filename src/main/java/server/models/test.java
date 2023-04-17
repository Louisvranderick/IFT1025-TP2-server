package server.models;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Création des deux scènes avec leur contenu respectif
        VBox ListeDesCours = new VBox(new Label("Liste des cours"));
        HBox FormulaireDInscription = new HBox(new Label("Formulaire d'inscription"));

        // Création d'un SplitPane horizontal
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        splitPane.getItems().addAll(ListeDesCours, FormulaireDInscription);
        ListeDesCours.setPrefWidth(200);
        FormulaireDInscription.setPrefWidth(200);

        Scene scene = new Scene(splitPane, 600, 300);



        // Création du tableau qui affiche les cours disponibles
        TableView<Course> table = new TableView<>();
        ListeDesCours.getChildren().add(table);

        TableColumn<Course, String> code = new TableColumn<>("Code");
        TableColumn<Course, String> cours = new TableColumn<>("Cours");

        code.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Course, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Course, String> p) {
                return new SimpleStringProperty(p.getValue().getCode());
            }
        });

        cours.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Course, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Course, String> p) {
                return new SimpleStringProperty(p.getValue().getName());
            }
        });

        table.getColumns().addAll(code, cours);


        // TODO pour ajouter des items dans le table on fait table.getItems().addAll( "row 1 data 1, row 1 data 2, etc.);
        // TODO utiliser ca avec le bouton charger pour load le information des cours dans le tableau
        // TODO dropdown list pour choisir la session avec options: Hiver, Automne, Ete




        HBox buttonGroup = new HBox();
        Button charger = new Button("Charger");

        ComboBox<String> Session = new ComboBox<>();
        Session.getItems().addAll("Hiver", "Automne", "Ete");
        Session.setPromptText("Saison");

        buttonGroup.getChildren().addAll(Session, charger);
        buttonGroup.setAlignment(Pos.CENTER);
        ListeDesCours.getChildren().add(buttonGroup);
        ListeDesCours.setAlignment(Pos.CENTER);

        GridPane formulaire = new GridPane();
        FormulaireDInscription.getChildren().add(formulaire);

        Label formTitle = new Label("Formulaire d'inscription");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");


        formulaire.add(new Label("Prénom"), 0, 0);
        formulaire.add(new TextField(), 0, 1);
        formulaire.add(new Label("Nom"), 0, 2);
        formulaire.add(new TextField(), 0, 3);
        formulaire.add(new Label("Email"), 0, 4);
        formulaire.add(new TextField(), 0, 5);
        formulaire.add(new Label("Matricule"), 0, 6);
        formulaire.add(new TextField(), 0, 7);
        formulaire.setAlignment(Pos.CENTER);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Inscription UdeM");
        primaryStage.show();


        Button registerButton = new Button("Register");
        GridPane.setConstraints(registerButton, 1, 4);
        formulaire.getChildren().add(registerButton);


        VBox formWrapper = new VBox(10, formTitle, formulaire, registerButton);
        formWrapper.setAlignment(Pos.CENTER);
        FormulaireDInscription.getChildren().add(formWrapper);

        Label errorMessageLabel = new Label();
        errorMessageLabel.setStyle("-fx-text-fill: red;"); // Set error message text color to red
        GridPane.setConstraints(errorMessageLabel, 1, 5);
        formulaire.getChildren().add(errorMessageLabel);

        // TODO bouton envoyer





        charger.setOnAction(event -> {
            String session = Session.getValue();
            if (session != null) {
                try {
                    loadCourses(session, table);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Mauvaise donnée rentrée");
            }
        });

        registerButton.setOnAction(event -> {
            TextField firstNameField = (TextField) formulaire.getChildren().get(1);
            TextField lastNameField = (TextField) formulaire.getChildren().get(3);
            TextField emailField = (TextField) formulaire.getChildren().get(5);
            TextField matriculeField = (TextField) formulaire.getChildren().get(7);

            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String matricule = matriculeField.getText();
            Course selectedCourse = table.getSelectionModel().getSelectedItem();

            List<String> errors = validateInput(firstName, lastName, email, matricule);

            if (errors.isEmpty()) {
                try {
                    register(firstName, lastName, email, matricule, selectedCourse);
                    errorMessageLabel.setText(""); // Clear error message on successful registration
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                String errorMessage = String.join("\n", errors);
                errorMessageLabel.setText(errorMessage);
            }

        });


    }

    private void loadCourses(String session, TableView<Course> table) throws IOException, ClassNotFoundException {
        Socket cS;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;

        cS = new Socket("127.0.0.1", 1337);

        objectOutputStream = new ObjectOutputStream(cS.getOutputStream());
        objectInputStream = new ObjectInputStream(cS.getInputStream());

        objectOutputStream.writeObject("CHARGER " + session);
        objectOutputStream.flush();
        ArrayList<Course> listeDeCours = (ArrayList<Course>) objectInputStream.readObject();

        table.getItems().clear();
        for (Course element : listeDeCours) {
            table.getItems().addAll(element);
        }


        objectOutputStream.close();
        objectInputStream.close();
        cS.close();
    }

    private void register(String prenom, String nom, String courriel, String matricule, Course cours) throws IOException, ClassNotFoundException {
        Socket cS;
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;

        cS = new Socket("127.0.0.1", 1337);

        objectOutputStream = new ObjectOutputStream(cS.getOutputStream());
        objectInputStream = new ObjectInputStream(cS.getInputStream());

        RegistrationForm registrationform = new RegistrationForm(prenom, nom, courriel, matricule, cours);
        objectOutputStream.writeObject("INSCRIRE");
        objectOutputStream.flush();

        objectOutputStream.writeObject(registrationform);
        objectOutputStream.flush();


        objectOutputStream.close();
        objectInputStream.close();
        cS.close();
    }

    private List<String> validateInput(String firstName, String lastName, String email, String matricule) {
        ArrayList<String> errors = new ArrayList<>();

        if (firstName.isEmpty()) {
            errors.add("First name is required.");
        }
        if (lastName.isEmpty()) {
            errors.add("Last name is required.");
        }
        if (email.isEmpty()) {
            errors.add("Email is required.");
        } else if (!email.matches("\\S+@\\S+\\.\\S+")) {
            errors.add("Invalid email format.");
        }
        if (matricule.isEmpty()) {
            errors.add("Matricule is required.");
        } else if (!matricule.matches("\\d{8}")) {
            errors.add("la matricule doit avoir 8 chiffre");
        }

        return errors;
    }

}
