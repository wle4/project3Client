import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;



public class Client extends Thread{

	// Declaring member variables for socket, input/output streams, and a callback function
	Socket socketClient;
	ObjectOutputStream out; // java library
	ObjectInputStream in; // java library

	String clientUsername;

	private Consumer<Serializable> callback; // be able to print this out in front end

	// Constructor for the Client class, which takes a Consumer<Serializable> callback
	// Consumer<Serializable> is part of Java's STANDARD LIBRARY, import java.io.Serializable;...

		// <Serializable> means it can be any object or type which implements serializable
	// Consumer has two functions:
	// accept()- which takes <Serializable> as parameter
	// then takes that parameter into Consumer
	Client(Consumer<Serializable> call){
		clientUsername = "temp";
		callback = call;
	}

	public void run() {

		try {
			socketClient= new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {}

		// Infinite loop to keep receiving messages from the server
		while(true) {

			try {
			// Reading a Serializable object from the ObjectInputStream and converting it to a String
				Message message = (Message) in.readObject();

				if(message.getSender().equals("Server")){
					String serverMessage = message.getMsg();
					if(serverMessage.equals("good")){
						callback.accept(new Message("","",""));
					}
					else if(serverMessage.equals("Username already taken...")){
						callback.accept(new Message("","","Username already taken..."));
					}
				}
			// Passing the received message to the callback function
				callback.accept(message);
			}
			catch(Exception e) {}
		}

	}

	public void setUsername(String username){
		// specifically used for setting the client's username, it sends a Message object to the server for the server to check if it's valid
		try {
			Message message = new Message("", "", username); // make a new message object containing the username
			out.writeObject(message); // send the message object to the server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method to send data to the server
	public void send(String receiver, String sender, String msg) {

		try {
			// Writing the provided data as a Serializable object to the ObjectOutputStream
			// sends to server
			// everything that gets sent has to be a message object
			Message message = new Message(receiver, sender, msg); // make a new message object
			out.writeObject(message); // send the message object to the server
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}