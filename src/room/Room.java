package room;

import java.util.Vector;

import client.Client;

public class Room {
	private Vector<Client> clientList = new Vector<Client>();
	private String title;

	public Room(String title) {
		this.title = title;
	}

	// 방에 클라이언트 추가
	public void addClient(Client client) {
		if (!clientList.contains(client))
			clientList.add(client);
	}

	// 방에서 클라이언트 제거
	public void deleteClient(Client client) {
		if (clientList.contains(client))
			clientList.remove(client);
		else
			System.out.println("해당 클라이언트 존재 x");
	}

	// 전송 약속 -> [방제목]/[내용] -> 해당 방 제목을 가진 채팅 방에만 메세지 출력

	// 방 전체 메세지 보내기
	// <방제목>/<메세지>/<닉네임>
	public void sendRoom(String msg) {
		// msg = <방제목>/<메세지>/<닉네임>
		String[] code = msg.split("/");
		for (Client client : clientList) {
			client.send(code[0] + "/" + code[1] + "/" + code[2]);
		}
	}

	// 방 전체 이모티콘 보내기
	// emoticon/<방제목>/<이모티콘 종류>/<닉네임>
	public void sendEmoticon(String roomTitle, String emoticonName, String senderName) {
		for (Client client : clientList) {
			client.send("emoticon/" + roomTitle + "/" + emoticonName + "/" + senderName);
		}
	}

	// 입장 메세지
	public void welcomePrint(String name) {
		for (Client client : clientList) {
			client.send(title + "/" + "[" + name + "]님이 입장 하셨습니다." + "/" + "SYSTEM");
		}
	}

	// 퇴장 메세지
	public void goodByePrint(String name) {
		for (Client client : clientList) {
			client.send(title + "/" + "[" + name + "]님이 퇴장 하셨습니다." + "/" + "SYSTEM");
		}
	}

	// getter
	public String getTitle() {
		return title;
	}

	public Vector<Client> getClientList() {
		return clientList;
	}

	// 현재 방에 접속중인 클라이언트 수 반환
	public int getCountClient() {
		return clientList.size();
	}
}