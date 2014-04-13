package com.lcf.server;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;

public class ServerMain {

	
	private static final int PORT = 10022;
	
	public static void main(String[] args) throws IOException {
		try {
			IBeacon.newIBeacon();
			DBConnection.readProperties();
			DBConnection.openConnection();
			
			ServerSocket serverSocket = new ServerSocket(PORT);

			Socket clientSocket;

			while (true) {
				clientSocket = serverSocket.accept();

				ClientConnection connection = new ClientConnection(clientSocket);
				Thread t = new Thread(connection);
				t.start();
			}
		} catch (IOException | SQLException e) {
			System.err.println("Accept failed.");
			e.printStackTrace();
		}
	}
}
