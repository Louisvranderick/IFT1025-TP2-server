package server;

import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.application.Application;
import javafx.geometry.Pos;


public class GUI extends Application{
        public static void main(String[] args) {
            launch(args);
        }
    @Override
    public void start (Stage primaryStage) {
        // Création des deux scènes avec leur contenu respectif
        VBox ListeDesCours = new VBox(new Label("Liste des cours"));
        HBox FormulaireDInscription = new HBox(new Label("Formulaire d'inscription"));
        ListeDesCours.setAlignment(Pos.CENTER);
        FormulaireDInscription.setAlignment(Pos.CENTER);

        // Création d'un SplitPane horizontal
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        splitPane.getItems().addAll(ListeDesCours, FormulaireDInscription);
        ListeDesCours.setPrefWidth(400);
        FormulaireDInscription.setPrefWidth(400);

        Scene scene = new Scene(splitPane, 400, 300);



        // Création du tableau qui affiche les cours disponibles
        TableView<String> table = new TableView<>();
        ListeDesCours.getChildren().add(table);

        TableColumn<String, String> code = new TableColumn<>("Code");
        TableColumn<String, String> cours = new TableColumn<>("Cours");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.getColumns().addAll(code, cours);

        // TODO pour ajouter des items dans le table on fait table.getItems().addAll( "row 1 data 1, row 1 data 2, etc.);
        // TODO utiliser ca avec le bouton charger pour load le information des cours dans le tableau


        // TODO un autre HBOX dans lequel seront les boutons de la gauche de l'application
        // Création du dropdown list button pour le choix de la session
        ComboBox<String> ChoixSession = new ComboBox<>();
        ChoixSession.setItems(FXCollections.observableArrayList(
                // options de choix ex. Hiver, Automne, Été, c'est une série de string séparés par des virgules
                ));

        // HBox vide pour laisser un espace entre le titre et le formulaire
        HBox espace = new HBox();
        FormulaireDInscription.getChildren().add(espace);
        // Création de la grille pour le formulaire d'inscription
        GridPane formulaire = new GridPane();
        FormulaireDInscription.getChildren().add(formulaire);


        formulaire.add(new Label("Prénom"), 0, 0);
        formulaire.add(new TextField(), 1, 0);
        formulaire.add(new Label("Nom"), 0, 1);
        formulaire.add(new TextField(), 1, 1);
        formulaire.add(new Label("Email"), 0, 2);
        formulaire.add(new TextField(),1, 2);
        formulaire.add(new Label("Matricule"), 0, 3);
        formulaire.add(new TextField(), 1, 3);

        // TODO bouton envoyer


        primaryStage.setScene(scene);
        primaryStage.setTitle("Inscription UdeM");
        primaryStage.show();
    }



}
