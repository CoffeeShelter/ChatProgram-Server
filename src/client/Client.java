package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

import room.Room;
import server.Server;

public class Client implements Runnable {

	private Socket client;
	private Vector<Room> roomVector = new Vector<Room>();
	private String nickName;

	public Client(Socket client) {
		this.client = client;
		this.nickName = "unknown";
	}

	public void run() {
		String msg = null;
		System.out.println(client.getInetAddress().toString() + " Ŭ���̾�Ʈ ������ ����");
		Server.roomList.add(roomVector); // ���� �븮��Ʈ�� �ش� Ŭ���̾�Ʈ �� ����Ʈ �߰�
		System.out.println("Ŭ���̾�Ʈ �� ����Ʈ �� : " + Server.roomList.size());
		while (true) {
			msg = recv(); // Ŭ���̾�Ʈ �� �䱸���� �ޱ�

			if (msg != null) {
				if (!msg.equals("refresh"))
					System.out.println(msg);
				Process(msg); // �䱸���� ó��
				if (msg.equals("exit")) {
					for (Room room : roomVector) {
						room.deleteClient(this);
						roomVector.addElement(room);
					}
					break;
				}
			} else {
				for (Room room : roomVector) {
					room.deleteClient(this);
				}
				break;
			}
		}

		Server.roomList.remove(roomVector); // ���� �� ����Ʈ���� �ش� Ŭ���̾�Ʈ �� ����Ʈ ����
		System.out.println(client.getInetAddress().toString() + " Ŭ���̾�Ʈ ������ ����");
	}

	public void Process(String msg) {
		String[] code = msg.split("/");

		switch (code[0]) {
		// �߰� ��û
		// add/room/<������>
		case "add":

			switch (code[1]) {
			case "room":
				Room room = new Room(code[2]);
				room.addClient(this);
				roomVector.add(room);

				send(code[2] + "/" + code[2] + " ���� �����Ǿ����ϴ�." + "/" + "SYSTEM");
				break;
			}

			break;

		// < �� ���� ��û >
		// in/<������>/<�г���>
		case "in": {
			boolean isRoom = false;
			for (Vector<Room> rList : Server.roomList) {
				if (rList.size() == 0)
					continue; // �ش� Ŭ���̾�Ʈ �� ����Ʈ�� 0���̸� �ǳʶ�.

				for (Room room : rList) {
					// �ش� ���� ã�� ���
					if (room.getTitle().equals(code[1])) { // code[1] = ������
						room.addClient(this);
						roomVector.add(room);
						isRoom = true;
						room.welcomePrint(code[2]);
						break;
					}
				}
				if (isRoom)
					break;
				// �ش� ���� �� ã���� ���
				else {
					// send(code[1] + "<- this room not found");
				}
			}
			break;
		}

		// < �� ������ ��û >
		// out/<������>/<�г���>
		case "out": {
			String roomName = code[1];
			String nickName = code[2];
			boolean isOk = false;
			for (Vector<Room> roomList : Server.roomList) {
				if (roomList.size() == 0)
					continue; // �ش� Ŭ���̾�Ʈ �� ������ ������ 0���̸� �ǳʶ�.

				for (Room rooms : roomList) {
					if (rooms.getTitle().equals(roomName)) {
						rooms.deleteClient(this); // �ش� ���� ���� �ִ� ��� Ŭ���̾�Ʈ���� �ش� Ŭ���̾�Ʈ ���� ����
						this.roomVector.remove(rooms); // �ش� Ŭ���̾�Ʈ �� ����Ʈ���� �������� �� ����

						// �ش� �濡 �����ִ� Ŭ���̾�Ʈ�� ���� ��� �ش� ������ ���� ���� ����
						if (rooms.getCountClient() != 0) {
							rooms.goodByePrint(nickName);
						}
						isOk = true;
						break;
					}
					if (isOk)
						break;
				}

			}

			break;
		}

		case "set":
			if (code[1].equals("nickname")) {
				setNickname(code[2]);
			}
			break;

		// ���� ��û
		case "exit":
			send("exit");
			closeClient();
			break;
			
		// �˻��� ä�ù� ����Ʈ ���
		// search/<������>
		case "search":
		{
			Vector<String> temp = new Vector<String>();
			String roomTitle = code[1];
			for (Vector<Room> vectorRoom : Server.roomList) {
				if (vectorRoom.size() == 0)
					continue;
				for (Room room : vectorRoom) {
					if(room.getTitle().contains(roomTitle))
					{
						if(!temp.contains(room.getTitle()))
						{
							temp.add(room.getTitle());
						}
					}
				}
			}
			
			String searchCommand = "search";
			for(String title : temp)
			{
				searchCommand = searchCommand + "/" + title;
			}
			
			send(searchCommand);
			
			break;
		}
		// �� ����Ʈ ���� ��û // �� ���� ���� ����
		case "refresh": {
			String titleList = "_refresh_";
			Vector<String> preventDup = new Vector<String>();
			for (Vector<Room> vectorRoom : Server.roomList) {
				if (vectorRoom.size() == 0)
					continue;
				for (Room room : vectorRoom) {
					if (!preventDup.contains(room.getTitle())) {
						preventDup.add(room.getTitle());
						titleList = titleList + "/" + room.getTitle();
					}
				}
			}

			// titleList => _refresh_/<������>/<������>/......./<������>
			send(titleList);

			break;
		}

		// �̸�Ƽ�� ����
		// emoticon/<�� ����>/<�̸�Ƽ�� ����>/<������ ��� �г���>
		case "emoticon": {
			if (roomVector != null) {
				for (Room room : roomVector) {
					if (room.getTitle().equals(code[1])) {
						room.sendEmoticon(code[1], code[2], code[3]);
					}
				}
			}
			break;
		}

		// ��ɾ �ƴ� �� �Ϲ� ���� ���ڷ� �ν�
		// ������/�޼���/�г���
		default:
			if (roomVector != null) {
				for (Room room : roomVector) {
					if (room.getTitle().equals(code[0])) {
						room.sendRoom(msg);
					}
				}
			}
			break;
		}
	}

	// ����
	public String recv() {
		String msg = null;

		InputStream is;
		BufferedReader br;

		try {
			is = client.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			msg = br.readLine();

			// br.close();
		} catch (Exception e) {
			//
		}

		return msg;
	}

	// ����
	public void send(String msg) {
		OutputStream os;
		BufferedWriter bw;
		PrintWriter pw;

		try {
			os = client.getOutputStream();
			bw = new BufferedWriter(new OutputStreamWriter(os));
			pw = new PrintWriter(bw, true);

			pw.println(msg);

			// pw.close();
		} catch (Exception e) {
			//
		}
	}

	public void sendObject(Object o) {
		try {
			OutputStream os = client.getOutputStream();
			ObjectOutputStream objOs = new ObjectOutputStream(os);

			objOs.writeObject(o);

			// objOs.close();
		} catch (IOException e) {
			//
		}
	}

	// Ŭ���̾�Ʈ ����
	public void closeClient() {
		try {
			client.close();
		} catch (Exception e) {
			//
		}
	}

	public void setNickname(String nickName) {
		this.nickName = nickName;
	}

	// getter
	public Socket getClient() {
		return client;
	}

	public Vector<Room> getRoomVector() {
		return roomVector;
	}

	public String getNickname() {
		return nickName;
	}
}
