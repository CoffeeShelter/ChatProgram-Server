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
		System.out.println(client.getInetAddress().toString() + " 클라이언트 쓰레드 실행");
		Server.roomList.add(roomVector); // 서버 룸리스트에 해당 클라이언트 룸 리스트 추가
		System.out.println("클라이언트 방 리스트 수 : " + Server.roomList.size());
		while (true) {
			msg = recv(); // 클라이언트 측 요구사항 받기

			if (msg != null) {
				if (!msg.equals("refresh"))
					System.out.println(msg);
				Process(msg); // 요구사항 처리
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

		Server.roomList.remove(roomVector); // 서버 룸 리스트에서 해당 클라이언트 룸 리스트 삭제
		System.out.println(client.getInetAddress().toString() + " 클라이언트 쓰레드 종료");
	}

	public void Process(String msg) {
		String[] code = msg.split("/");

		switch (code[0]) {
		// 추가 요청
		// add/room/<방제목>
		case "add":

			switch (code[1]) {
			case "room":
				Room room = new Room(code[2]);
				room.addClient(this);
				roomVector.add(room);

				send(code[2] + "/" + code[2] + " 방이 생성되었습니다." + "/" + "SYSTEM");
				break;
			}

			break;

		// < 방 입장 요청 >
		// in/<방제목>/<닉네임>
		case "in": {
			boolean isRoom = false;
			for (Vector<Room> rList : Server.roomList) {
				if (rList.size() == 0)
					continue; // 해당 클라이언트 방 리스트가 0개이면 건너뜀.

				for (Room room : rList) {
					// 해당 방을 찾은 경우
					if (room.getTitle().equals(code[1])) { // code[1] = 방제목
						room.addClient(this);
						roomVector.add(room);
						isRoom = true;
						room.welcomePrint(code[2]);
						break;
					}
				}
				if (isRoom)
					break;
				// 해당 방을 못 찾았을 경우
				else {
					// send(code[1] + "<- this room not found");
				}
			}
			break;
		}

		// < 방 나가기 요청 >
		// out/<방제목>/<닉네임>
		case "out": {
			String roomName = code[1];
			String nickName = code[2];
			boolean isOk = false;
			for (Vector<Room> roomList : Server.roomList) {
				if (roomList.size() == 0)
					continue; // 해당 클라이언트 방 리스르 갯수가 0개이면 건너뜀.

				for (Room rooms : roomList) {
					if (rooms.getTitle().equals(roomName)) {
						rooms.deleteClient(this); // 해당 방을 갖고 있는 모든 클라이언트에서 해당 클라이언트 정보 제거
						this.roomVector.remove(rooms); // 해당 클라이언트 방 리스트에서 나가려는 방 제거

						// 해당 방에 남아있는 클라이언트가 있을 경우 해당 방으로 퇴장 문자 전송
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

		// 종료 요청
		case "exit":
			send("exit");
			closeClient();
			break;
			
		// 검색한 채팅방 리스트 출력
		// search/<방제목>
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
		// 방 리스트 정보 요청 // 방 제목 벡터 전송
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

			// titleList => _refresh_/<방제목>/<방제목>/......./<방제목>
			send(titleList);

			break;
		}

		// 이모티콘 전송
		// emoticon/<방 제목>/<이모티콘 종류>/<보내는 사람 닉네임>
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

		// 명령어가 아닐 시 일반 전송 문자로 인식
		// 방제목/메세지/닉네임
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

	// 수신
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

	// 전송
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

	// 클라이언트 종료
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
