package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.mygdx.game.helpers.BytesUtil;
import com.mygdx.game.helpers.GH;

import java.io.*;
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
				server = Gdx.net.newServerSocket(Net.Protocol.TCP, port, null);
				System.out.println("Hosting server on port "+port);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while(!this.done) {
				try {
					Socket client = server.accept(null);
					clients.add(client);
					System.out.println("New client connected");

					ConnectionThread connThread = new ConnectionThread(client) ;
					Thread thread = new Thread(connThread);
					thread.start();

					input = client.getInputStream();
					String inputString = inputStreamAsString(input);

					System.out.println(inputString);

					client.dispose();
					server.dispose();
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

	private static class ConnectionThread implements Runnable{
		Socket socket;

		public ConnectionThread(Socket socket){
			this.socket = socket;
		}

		@Override
		public void run() {
			// Again, probably better to store these objects references in the support class
			InputStream in = socket.getInputStream();
			DataInputStream dis = new DataInputStream(in);

			while(socket.isConnected()) {
				try {
					System.out.println("Server listening");
					byte[] data = new byte[8192];
					dis.readFully(data);
					GH.Message mess = null;
					try {
						mess = (GH.Message)BytesUtil.toObject(data);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					System.out.println("Got message '"+mess.message+"'");
				} catch (IOException e) {
					e.printStackTrace();
					socket.dispose();
				}
			}
		}
	}
}
