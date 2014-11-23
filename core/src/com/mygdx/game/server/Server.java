package com.mygdx.game.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/22/2014.
 */
public class Server{

	private static ServerThread serverThread;
	private static Thread thread;

	public static void start(int port){
		if(serverThread == null){
			serverThread = new ServerThread(port);
			thread = new Thread(serverThread);
			thread.start();
		}
	}

	private static class ServerThread implements Runnable{
		public boolean done = false;

		private ArrayList<Socket> clients = new ArrayList<Socket>();
		private ServerSocket server;
		private InputStream input;

		public ServerThread(int port){
			try {
				server = new ServerSocket(port);
				System.out.println("Hosting server on port "+port);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while(!this.done) {
				try {
					Socket client = server.accept();
					clients.add(client);

					input = client.getInputStream();
					String inputString = inputStreamAsString(input);

					System.out.println(inputString);

					client.close();
					server.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public static String inputStreamAsString(InputStream stream) throws IOException {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}

			br.close();
			return sb.toString();
		}
	}
}
