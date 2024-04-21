import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application{
	TextField usernameBox; // specifically used for when the user has to enter a username

	HashMap<String, Scene> sceneMap; // maps all scenes
	VBox clientBox; // user's window, which puts everything together
	VBox backBox1; // (wheat) all clients window
	HBox backBox; // (added by wheat) adds a back button to view all clients
	Client clientConnection; // connection between the client and server
	String tempUsername; // temporarily stores any username attempts here, mainly used for choosing the username
	ListView<String> listItems2; // lists out all messages from all clients
	ListView<Button> listClientNames; // stores all connected clients as a button

	String receiver = "all";

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		listItems2 = new ListView<>();
		listClientNames = new ListView<>();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){ // completely closes program when window is closed
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();System.exit(0);
			}
		});

		// create scenes and store in map
		sceneMap = new HashMap<String, Scene>();

		// initialize and store the scenes, ANY NEW SCENES SHOULD BE INITIALIZED AND ADDED HERE
		sceneMap.put("username", chooseUsernameScene());
		sceneMap.put("publicChatroom",  publicChatroom(primaryStage));
		sceneMap.put("allUsers", viewOnlineClients(primaryStage)); // (wheat)

		// clients will start in the chooseUsernameScene
		primaryStage.setScene(sceneMap.get("username"));
		primaryStage.setTitle("Discord 2: Choosing username...");

		primaryStage.show();

		clientConnection = new Client(data->{
				Platform.runLater(()->{
					Message serverMessage = (Message) data; //THIS is saying callback.accept() now takes in new messages as a Message object

					// USED FOR DIRECT MESSAGES
					if(serverMessage.getSender().equals("UPDATE")){
						listClientNames.getItems().clear();
						initializeUserListButtons(serverMessage.updateClients());
					}

					// SPECIFICALLY USED FOR CHOOSING A USERNAME
					else if(serverMessage.getMsg().equals("good")) { // server will send a message back saying whether the username is valid
						// moves the client to the chatroom once they properly set up a username
						clientConnection.clientUsername = tempUsername; // client's username is stored in Client.java that it's easier to reference to
						primaryStage.setScene(sceneMap.get("publicChatroom")); // changes the scene to the public chatroom (after username is made, default room will be this)
						primaryStage.setTitle("Discord 2: Connected as " + clientConnection.clientUsername); // changes the title of the window
					}
					else if(serverMessage.getMsg().equals("Username already taken...")) {
						//keeps client in the chooseUsername scene until they have a unique username
						usernameBox.setPromptText("Username already taken...");
					}
					// end of choosing username, basically never used again

					else {
						// adds new messages to the client's current chatroom, THIS MIGHT HAVE TO CHANGE FOR DM'S AND GROUPS
						listItems2.getItems().add(serverMessage.getSender() + ": " + serverMessage.getMsg());
					}
			});
		});
		//start client connection to the server
		clientConnection.start();
	}

	public Scene chooseUsernameScene() {

		Button send = new Button("Send"); // new send button for the choosing username (A NEW BUTTON NEEDS TO BE MADE FOR EVERY SCENE, OTHERWISE IT BREAKS)
		usernameBox = new TextField(); // new textfield for the entering username (SAME THING, NEW ONE FOR EACH SCENE)
		usernameBox.setPromptText("Enter a username");

		send.setOnAction(e->{
			tempUsername = usernameBox.getText().trim(); // takes the text in the textfield and "trims" off leading/trailing spaces
			if(!tempUsername.isEmpty()){ // makes sure the textField isn't empty
				clientConnection.setUsername(tempUsername);
				usernameBox.clear();
				System.out.println("username sent"); // checking is a name is sent, for testing purposes
			}
		}); // initially, the send button will be for setting up the username

		clientBox = new VBox(10, usernameBox, send); // sets up components for the scene
		clientBox.setStyle("-fx-background-color: blue;" + "-fx-font-family: 'serif';");
		return new Scene(clientBox, 400, 300);
	}

	public Scene publicChatroom(Stage theStage){
		Label titleLabel = new Label("Chat Room");
		Button send = new Button("Send"); // new send button for the public chat (A NEW BUTTON NEEDS TO BE MADE FOR EVERY SCENE, OTHERWISE IT BREAKS)
		Button backButton = new Button("Users"); // (wheat) back button to view online clients
		TextField msgbox = new TextField(); // new textfield for the public chat (SAME THING, NEW ONE FOR EACH SCENE)

		send.setOnAction(e->{ //because we're in the public chat, the send button will be sending to ALL clients
			String msg = msgbox.getText();
			if(!msg.isEmpty()){
				clientConnection.send(receiver, clientConnection.clientUsername, msg); // grabs message from textbox and wraps it in a Message Object in the send() function
				msgbox.clear();
			}
		});

		// (wheat) action event handling which proceeds to new scene viewOnlineClients(); which views all the clients online
		backButton.setOnAction(e-> {
			theStage.setScene(sceneMap.get("allUsers"));
		});

		backBox = new HBox(20, msgbox, send); // (wheat)
		backBox.setAlignment(Pos.CENTER); // (wheat) position center box center

		clientBox = new VBox(10, titleLabel, backButton, backBox,listItems2); // sets up components for the scene
		clientBox.setStyle("-fx-background-color: gray;"+"-fx-font-family: 'serif';");
		return new Scene(clientBox, 400, 500);
	}

	// (wheat) new view all clients scene
	public Scene viewOnlineClients(Stage theStage) {
		Label titleLabel = new Label("Online Clients");
		Button backButton = new Button("Back");

		backButton.setOnAction(e-> {
			theStage.setScene(sceneMap.get("publicChatroom"));
		});

		backBox1 = new VBox(10, titleLabel, backButton, listClientNames);

		return new Scene(backBox1, 400, 500);
	}

	private void initializeUserListButtons(ArrayList<String> newList){
		// initializes buttons for each user that is currently connected
		Button allButton = new Button("All");
		allButton.setOnAction(e->{
			receiver = "all";
		});
		listClientNames.getItems().addAll(allButton);

		for(String username : newList){
			Button userButton = new Button(username);
			userButton.setOnAction(e->{
				receiver = userButton.getText();
			});
			listClientNames.getItems().addAll(userButton);

		}
	}
}
