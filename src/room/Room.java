package room;

import java.util.Vector;

import client.Client;

public class Room {
	private Vector<Client> clientList = new Vector<Client>();
	private String title;

	public Room(String title) {
		this.title = title;
	}

	// �濡 Ŭ���̾�Ʈ �߰�
	public void addClient(Client client) {
		if (!clientList.contains(client))
			clientList.add(client);
	}

	// �濡�� Ŭ���̾�Ʈ ����
	public void deleteClient(Client client) {
		if (clientList.contains(client))
			clientList.remove(client);
		else
			System.out.println("�ش� Ŭ���̾�Ʈ ���� x");
	}

	// ���� ��� -> [������]/[����] -> �ش� �� ������ ���� ä�� �濡�� �޼��� ���

	// �� ��ü �޼��� ������
	public void sendRoom(String msg) {
		// msg = ������/<�޼���>/�г���
		String[] code = msg.split("/");
		for (Client client : clientList) {
			client.send(code[0] + "/" + "[" + code[2] + "] " + code[1]); // ������/<�г���:�޼���>
		}
	}

	// ���� �޼���
	public void welcomePrint(String name) {
		for (Client client : clientList) {
			client.send(title + "/[" + name + "] ���� �����ϼ̽��ϴ�.");
		}
	}

	// ���� �޼���
	public void goodByePrint(String name) {
		for (Client client : clientList) {
			client.send(title + "/[" + name + "] ���� �����ϼ̽��ϴ�.");
		}
	}

	// getter
	public String getTitle() {
		return title;
	}

	public Vector<Client> getClientList() {
		return clientList;
	}

	// ���� �濡 �������� Ŭ���̾�Ʈ �� ��ȯ
	public int getCountClient() {
		return clientList.size();
	}
}