package server;

import java.net.ServerSocket;
import java.util.Scanner;
import java.util.Vector;

import client.Client;
import room.Room;

public class Server {

	// ä�� ��
	public static Vector<Vector<Room>> roomList = new Vector<Vector<Room>>();
	public static Vector<Client> clientList = new Vector<Client>();

	private ServerSocket serverSocket; // ���� ����
	private Accept accept;

	public Server(int port) {
		try {
			this.serverSocket = new ServerSocket(7777);
			System.out.println("Open Server!");
			this.accept = new Accept(serverSocket);

		} catch (Exception e) {
			System.out.println("���� ���� ����");
			System.exit(0);
		}

	}

	public void runServer() {
		String code = null;
		Scanner scan = new Scanner(System.in);
		Thread acceptThread = new Thread(accept);
		acceptThread.start(); // accept ������ ����

		try {
			while (code != "exit") {
				code = scan.next();
				if (code.equals("print")) {
					int i = 0;
					int count = 0;
					for (Vector<Room> room : Server.roomList) {
						System.out.println("< " + (i+1) + " ��° >");
						for (Room r : room) {
							count++;
							System.out.println(
									count + " : " + r.getTitle() + " ( Ŭ���̾�Ʈ ��: " + r.getCountClient() + " �� )");
						}
						i++;
						count = 0;
					}
				}
			}
		} catch (Exception e) {
			//
		}

		scan.close();
		closeServer();
	}

	// ���� ����
	public void closeServer() {
		try {
			serverSocket.close();
		} catch (Exception e) {
			System.out.println("���� �ݱ� ����");
			System.exit(0);
		}
	}

	// getter
	public ServerSocket getServer() {
		return serverSocket;
	}

	public Vector<Vector<Room>> getRoom() {
		return roomList;
	}

	public Vector<Client> getClientList() {
		return clientList;
	}
}
