package com.mygdx.game.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.mygdx.game.helpers.BytesUtil;
import com.mygdx.game.helpers.GH;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
		private ServerSocket serverSocket;

        public final static String ADDRESS = "127.0.0.1";
        public static int PORT = 8511;
        public final static long TIMEOUT = 10000;

        private ServerSocketChannel serverChannel;
        private Selector selector;
        /**
         * This hashmap is important. It keeps track of the data that will be written to the clients.
         * This is needed because we read/write asynchronously and we might be reading while the server
         * wants to write. In other words, we tell the Selector we are ready to write (SelectionKey.OP_WRITE)
         * and when we get a key for writing, we then write from the Hashmap. The write() method explains this further.
         */
        private Map<SocketChannel,byte[]> dataTracking = new HashMap<SocketChannel, byte[]>();

		public ServerThread(int port){
			try {
                init();
                PORT = port;
				System.out.println("Server: Hosting server on port "+PORT);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}

        private void init(){
            System.out.println("initializing server");
            // We do not want to call init() twice and recreate the selector or the serverChannel.
            if (selector != null) return;
            if (serverChannel != null) return;

            try {
                // This is how you open a Selector
                selector = Selector.open();
                // This is how you open a ServerSocketChannel
                serverChannel = ServerSocketChannel.open();
                // You MUST configure as non-blocking or else you cannot register the serverChannel to the Selector.
                serverChannel.configureBlocking(false);
                // bind to the address that you will use to Serve.
                serverChannel.socket().bind(new InetSocketAddress(ADDRESS, PORT));

                /**
                 * Here you are registering the serverSocketChannel to accept connection, thus the OP_ACCEPT.
                 * This means that you just told your selector that this channel will be used to accept connections.
                 * We can change this operation later to read/write, more on this later.
                 */
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

		@Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()) {

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
