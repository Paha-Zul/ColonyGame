package com.mygdx.game.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.Socket;
import com.mygdx.game.util.BytesUtil;
import com.mygdx.game.util.GH;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
		Socket socket;
		DataInputStream dis;
		DataOutputStream dos;

		public ClientThread(String host, int port){
			socket = Gdx.net.newClientSocket(Net.Protocol.TCP, host, port, null);
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());

			System.out.println("Started listening from "+host+" on port "+port);

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
				dos.flush();
				System.out.println("Client: I sent the data");
			}catch(IOException e){
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			while(socket.isConnected()) {
				try {
					System.out.println("Client: Client is listening");
					byte[] data = new byte[8192];
					dis.readFully(data);
					GH.Message mess = (GH.Message)BytesUtil.toObject(data);
					System.out.println("Client: Client received data: "+mess.message);

				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
