package com.mygdx.game.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.mygdx.game.helpers.BytesUtil;
import com.mygdx.game.helpers.GH;

import java.io.*;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class Client {

	public static void StartClient(String host, int port){
		ClientThread client = new ClientThread(host, port);
		Thread thread = new Thread(client);
		thread.start();

	}

	private static class ClientThread implements Runnable{
		public boolean done;
		Socket socket;
		InputStream in;
		DataInputStream dis;
		OutputStream out;
		DataOutputStream dos;

		public ClientThread(String host, int port){
			socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, null);
			in = socket.getInputStream();
			dis = new DataInputStream(in);
			out = socket.getOutputStream();
			dos = new DataOutputStream(out);

			GH.Message message = new GH.Message();
			message.message = "Hello";
			byte[] obj = null;
			try {
				obj = BytesUtil.toByteArray(message);
			}catch(IOException e) {
				e.printStackTrace();
			}

			try {
				dos.write(obj);
				System.out.println("Client: I sent the data");
			}catch(IOException e){
				e.printStackTrace();
			}

			System.out.println("Started listening from "+host+" on port "+port);
		}

		@Override
		public void run() {
			while(socket.isConnected()) {
				try {
					byte[] data = new byte[8192];
					dis.readFully(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
