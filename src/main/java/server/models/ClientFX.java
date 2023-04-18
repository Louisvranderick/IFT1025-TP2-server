package server.models;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.geometry.Insets;
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


public class ClientFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Création des deux scènes avec leur contenu respectif
        VBox ListeDesCours = new VBox(new Label("Liste des cours"));
        HBox FormulaireDInscription = new HBox();

        // Création d'un SplitPane horizontal
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        splitPane.getItems().addAll(ListeDesCours, FormulaireDInscription);
        ListeDesCours.setPrefWidth(500);
        FormulaireDInscription.setPrefWidth(500);

        Scene scene = new Scene(splitPane, 600, 300);


        TableView<Course> table = new TableView<>();
        ListeDesCours.getChildren().add(table);

        ListeDesCours.setPadding(new Insets(20, 20, 20, 20));

        TableColumn<Course, String> code = new TableColumn<>("Code");
        TableColumn<Course, String> cours = new TableColumn<>("Cours");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        code.setPrefWidth(100);
        cours.setPrefWidth(150);

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

        HBox buttonGroup = new HBox();
        Button charger = new Button("Charger");

        ComboBox<String> Session = new ComboBox<>();
        Session.getItems().addAll("Hiver", "Automne", "Ete");
        Session.setPromptText("Session");

        buttonGroup.getChildren().addAll(Session, charger);
        buttonGroup.setAlignment(Pos.CENTER);
        ListeDesCours.getChildren().add(buttonGroup);
        ListeDesCours.setAlignment(Pos.CENTER);

        buttonGroup.setSpacing(10);
        buttonGroup.setPadding(new Insets(10, 10, 10, 10));

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
        FormulaireDInscription.setAlignment(Pos.CENTER);

        Label errorMessageLabel = new Label();
        errorMessageLabel.setStyle("-fx-text-fill: red;"); // Set error message text color to red
        GridPane.setConstraints(errorMessageLabel, 1, 5);
        formulaire.getChildren().add(errorMessageLabel);


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

            ArrayList<String> errors = validateInput(firstName, lastName, email, matricule);

            if (errors.isEmpty()) {
                try {
                    register(firstName, lastName, email, matricule, selectedCourse);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                showErrorPopup(errors);
            }

        });


    }

    private void showErrorPopup(ArrayList<String> messages) {
        StringBuilder messageBuilder = new StringBuilder();
        for (String message : messages) {
            messageBuilder.append(message).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(messageBuilder.toString());
        alert.showAndWait();
    }


    /**
     * cette fonction envoie une requête au serveur après avoir établi la connection puis une fois que la réponse du
     * serveur a été reçue, met le tableau à jour avec l'information correspondante
     * @param session la Session choisis par l'utilisateur
     * @param table la table de cours qu'on mets sur le interface
     * @throws IOException
     * @throws ClassNotFoundException
     */
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


    /**
     * fonction qui envoie l'information entrée par l'utilisateur dans le formulaire d'inscription et du cours choisi
     * en se connectant au serveur
     * @param prenom
     * @param nom
     * @param courriel
     * @param matricule
     * @param cours
     * @throws IOException
     * @throws ClassNotFoundException
     */
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

    /**
     * Fonction qui verifie si les input sont valide et pas vide. Verifie si le email est bien formatte et que la matricule
     * a bien 8 chiffre.
     * @param firstName
     * @param lastName
     * @param email
     * @param matricule
     * @return un arraylist avec toute les erreurs comme element
     */
    private ArrayList<String> validateInput(String firstName, String lastName, String email, String matricule) {
        ArrayList<String> errors = new ArrayList<>();

        if (firstName.isEmpty()) {
            errors.add("Pas de prenom inscrit");
        }
        if (lastName.isEmpty()) {
            errors.add("Pas de nom de famille inscrit");
        }

        String[] splitEmail = email.split("@");
        if (email.isEmpty()) {
            errors.add("Courriel est requis");
        } else if (splitEmail.length == 2) {
            String[] complete = email.split("");
            String[] splitAdress = splitEmail[1].split("\\.");
            if (complete[complete.length - 1] == "@") {
                errors.add("l'adresse fini par un @, veuillez saisir une adresse courriel valide");
            } else  if (splitAdress.length <= 1 ) {
                errors.add("manque au moins un point, veuillez saisir une adresse courriel valide");
            } else if (splitAdress.length > 2 ) {
                errors.add("trop de point, veuillez saisir une adresse courriel valide");
            }
        } else if (splitEmail.length <= 1 ){
            errors.add("Veuillez inscrire au moins un @, veuillez saisir une adresse courriel valide");
        } else {
            errors.add("trop de @!, veuillez saisir une adresse courriel valide");
        }

        if (matricule.isEmpty()) {
            errors.add("Pas de matricule inscrit");
        } else if (matricule.length() != 8) {
            errors.add("la matricule doit avoir 8 chiffre");
        }

        return errors;
    }

}
