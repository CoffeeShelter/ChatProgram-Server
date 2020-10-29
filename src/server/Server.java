package server;

import java.net.ServerSocket;
import java.util.Scanner;
import java.util.Vector;

import client.Client;
import room.Room;

public class Server {

	// 채팅 방
	public static Vector<Vector<Room>> roomList = new Vector<Vector<Room>>();
	public static Vector<Client> clientList = new Vector<Client>();

	private ServerSocket serverSocket; // 서버 소켓
	private Accept accept;

	public Server(int port) {
		try {
			this.serverSocket = new ServerSocket(7777);
			System.out.println("Open Server!");
			this.accept = new Accept(serverSocket);

		} catch (Exception e) {
			System.out.println("서버 생성 실패");
			System.exit(0);
		}

	}

	public void runServer() {
		String code = null;
		Scanner scan = new Scanner(System.in);
		Thread acceptThread = new Thread(accept);
		acceptThread.start(); // accept 쓰레드 실행

		try {
			while (code != "exit") {
				code = scan.next();
				if (code.equals("print")) {
					int i = 0;
					int count = 0;
					for (Vector<Room> room : Server.roomList) {
						System.out.println("< " + (i+1) + " 번째 >");
						for (Room r : room) {
							count++;
							System.out.println(
									count + " : " + r.getTitle() + " ( 클라이언트 수: " + r.getCountClient() + " 개 )");
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

	// 서버 종료
	public void closeServer() {
		try {
			serverSocket.close();
		} catch (Exception e) {
			System.out.println("서버 닫기 실패");
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
