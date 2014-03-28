package io.pyd.sdk.client;

import io.pyd.sdk.client.auth.CommandlineCredentialsProvider;
import io.pyd.sdk.client.http.CountingMultipartRequestEntity;
import io.pyd.sdk.client.model.FileNode;
import io.pyd.sdk.client.model.Message;
import io.pyd.sdk.client.model.Node;
import io.pyd.sdk.client.model.NodeFactory;
import io.pyd.sdk.client.model.RepositoryNode;
import io.pyd.sdk.client.model.ServerNode;
import io.pyd.sdk.client.transport.Transport;
import io.pyd.sdk.client.utils.StateHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Test {
	public static void main(String[] args) {

		ServerNode server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
		server.setHost("192.168.0.181");
		server.setPath("/test");
		server.setProtocol("http");
		server.setLegacy(false);

		StateHolder.getInstance().setServer(server);

		PydioClient pydio = new PydioClient(Transport.MODE_SESSION,	new CommandlineCredentialsProvider());

		ArrayList<Node> repos = pydio.listChildren(server, null, null);
		System.out.println("\n--Repository list :\nID - TYPE");
		for (int i = 0; i < repos.size(); i++) {
			RepositoryNode repo = (RepositoryNode) repos.get(i);
			System.out.println(repo.getId() + " - " + repo.getAccesType());
		}

		System.out.print("\n--> repository ID :  ");
		String repoName = new Scanner(System.in).nextLine();
		ArrayList<Node> files = null;
		for (int i = 0; i < repos.size(); i++) {
			RepositoryNode repo = (RepositoryNode) repos.get(i);
			if (repo.getId().equals(repoName)) {
				StateHolder.getInstance().setRepository(repo);
				files = pydio.listChildren(repo, null, null);
			}
		}

		if (files == null)
			return;

		ArrayList<Node> nds;

		for (;;) {

			System.out
					.println("\n\n-------------------------COMMAND LINE-------------------------");
			System.out.println("\n--Repository list file :\n");
			for (int i = 0; i < files.size(); i++) {
				FileNode file = (FileNode) files.get(i);
				System.out.println(i + " - " + file.path());
			}

			System.out.println("\n\nACTION lIST : ");
			System.out.println("1 list");
			System.out.println("2 delete");
			System.out.println("3 upload");
			System.out.println("4 download");
			System.out.print("--> ");
			int action = new Scanner(System.in).nextInt();

			if (action == 1) {
				System.out.print("File number : ");
				int folder = new Scanner(System.in).nextInt();
				FileNode node = (FileNode) files.get(folder);
				if (!node.isFile()) {
					nds = pydio.listChildren(node, null, null);
					System.out.println("\n-- :" + node.getProperty("text")
							+ "\n");
					for (int i = 0; i < nds.size(); i++) {
						FileNode file = (FileNode) nds.get(i);
						System.out.println(i + " - " + file.path());
					}
				}
				
				
			} else if (action == 2) {
				System.out.print("File number : ");
				int folder = new Scanner(System.in).nextInt();
				FileNode node = (FileNode) files.get(folder);
				Node[] arr = new Node[1];
				arr[0] = node;
				pydio.remove(arr);
				files = pydio.listChildren(StateHolder.getInstance()
						.getRepository(), null, null);
				
				
			} else if (action == 3) {
				System.out.print("Local file name : ");
				String filename = new Scanner(System.in).nextLine();
				
				File file = new File("C:\\Users\\pydio\\Desktop\\SDK_test_results\\"+filename);
				pydio.write(StateHolder.getInstance().getRepository(), file,
						new CountingMultipartRequestEntity.ProgressListener() {
							public void transferred(long num)throws IOException {
								//System.out.print("progress : "+num);
							}
							public void partTransferred(int part, int total) {
								System.out.println("part "+ part +" on "+total);
							}
						}, false, null);
				
				
			} else if (action == 4) {
				
				System.out.print("File number : ");
				int folder = new Scanner(System.in).nextInt();
				final FileNode node = (FileNode) files.get(folder);
				Node[] arr = new Node[1];
				arr[0] = node;
				final long nodeSize = node.size();
				final File file = new File("C:\\Users\\pydio\\Desktop\\SDK_test_results\\"+node.name());
				if(!file.exists() )
					try {file.createNewFile();} catch (IOException e) {}
				try {
					pydio.read(arr, new FileOutputStream(file), new ProgressHandler() {						
						public void onProgress(int progress) {							
							System.out.print("\rprogress : "+ (progress / nodeSize)+"%");
						}
					});
				}catch (Exception e) {}				
			} else {

			}
		}

		/*
		 * final Node node = files.get(0); Node[] ns = new Node[1];
		 * 
		 * ns[0] = files.get(1);
		 * 
		 * try{ FileOutputStream fos = new FileOutputStream(file);
		 * pydio.read(ns, fos, null); }catch(Exception e){
		 * System.out.println("Download error"); }
		 */
	}
}
