package server;

import javafx.geometry.Orientation;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.SplitPane;
import javafx.application.Application;

public class GUI {

    // TODO Manque du code ici pour le extends du server pour le class

    @Override
    public void start (Stage primaryStage) {
        // Création des deux scènes avec leur contenu respectif
        VBox ListeDesCours = new VBox(new Label("Liste des cours"));
        HBox FormulaireDInscription = new HBox(new Label("Formulaire d'inscription"));

        // Création d'un SplitPane horizontal
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);

        splitPane.getItems().addAll(ListeDesCours, FormulaireDInscription);
        ListeDesCours.setPrefWidth(200);
        FormulaireDInscription.setPrefWidth(200);

        Scene scene = new Scene(splitPane, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Inscription UdeM");
        primaryStage.show();
    }



}
