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

		private ArrayList<ConnectionThread> clients = new ArrayList<ConnectionThread>();
		private ServerSocket server;
		private InputStream input;

		public ServerThread(int port){
			try {
				server = Gdx.net.newServerSocket(Net.Protocol.TCP, port, null);
				System.out.println("Server: Hosting server on port "+port);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			while(!this.done) {
				try {
					Socket client = server.accept(null);
					ConnectionThread connThread = new ConnectionThread(client) ;
					clients.add(connThread);
					Thread thread = new Thread(connThread);
					thread.start();
				} catch (Exception e) {
					e.printStackTrace();
					this.done = true;
				}
			}
		}
	}

	private static class ConnectionThread implements Runnable{
		Socket socket;
		DataInputStream dis;
		DataOutputStream dos;

		public ConnectionThread(Socket socket){
			System.out.println("Server: New client connected");
			this.socket = socket;
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());

			GH.Message mess = new GH.Message();
			mess.message = "Hi!";

			try {
				System.out.println("Sending 'Hi!' to new client");
				dos.write(BytesUtil.toByteArray(mess));
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

			while(socket.isConnected()) {
				try {
					System.out.println("Server: Server listening");
					byte[] data = new byte[16384];
					dis.readFully(data);
					GH.Message mess = null;
					try {
						mess = (GH.Message)BytesUtil.toObject(data);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					System.out.println("Server: Got message '"+mess.message+"'");
				} catch (IOException e) {
					socket.dispose();
				}
			}

			System.out.println("Server: Client disconnected");
		}
	}
}
