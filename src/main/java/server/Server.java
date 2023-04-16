package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    /**
     * Crée le serveur avec le port mit en argument, Utilisé dans la classe serverlauncher pour demarré le serveur
     *
     * @param port
     * @throws IOException
     */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    /**
     *
     * @param h
     */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     @exception FileNotFoundException, si jamais on trouve pas le fichier
     */

    public void handleLoadCourses(String arg) {
        final String fichierCours = "src/main/java/server/data/cours.txt";
        try {
            FileReader cours = new FileReader(fichierCours);
            BufferedReader reader = new BufferedReader(cours);
            String ligne;
            ArrayList<Course> listeDeCours = new ArrayList<>();
            while ((ligne = reader.readLine()) != null) {
                String[] partie = ligne.split("\t");
                if (partie[2].equalsIgnoreCase(arg)) {
                    Course cour = new Course(partie[1], partie[0], partie[2]);
                    listeDeCours.add(cour);
                }
            }
            reader.close();


            objectOutputStream.writeObject(listeDeCours);

        } catch (FileNotFoundException e) {
            System.out.println("Path au fichier incorrect");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gére les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        System.out.println("testing");
        String path2 = "src/main/java/server/data/inscription.txt";
        try {
            RegistrationForm registrationForm = (RegistrationForm) objectInputStream.readObject();
            FileWriter fileWriter = new FileWriter(path2, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            Course regCourse = registrationForm.getCourse();
            String content = "\n" + regCourse.getSession() + " " + regCourse.getCode() + " " + registrationForm.getMatricule() + "\t" + registrationForm.getNom() + "\t" + registrationForm.getPrenom()+ "\t" + registrationForm.getEmail() ;
            bufferedWriter.write(content);
            bufferedWriter.close();
            fileWriter.close();
            System.out.println(content);
            String success = "Enregistrement fait avec succès";
            objectOutputStream.writeObject(success);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
    }


