package server;

import java.net.ServerSocket;
import java.net.Socket;

import client.Client;

public class Accept implements Runnable {

	private ServerSocket server;
	
	public Accept(ServerSocket server) {
		this.server = server;
	}

	public void run() {
		Socket client;
		Client clientClass;
		try {
			while (true) {
				client = server.accept();
				clientClass = new Client(client);
				Server.clientList.add(clientClass);
				System.out.println(client.getInetAddress().toString()+"에서 접속하셨습니다.");
				new Thread(clientClass).start();
				if (Thread.interrupted()) {
					break;
				}
			}
		} catch (Exception e) {
			//
		}
		System.out.println("Accept 쓰레드 종료");
	}

	//getter

}
