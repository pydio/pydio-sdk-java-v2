package io.pyd.sdk.client.utils;

import io.pyd.sdk.client.model.FileNode;
import io.pyd.sdk.client.model.RepositoryNode;
import io.pyd.sdk.client.model.ServerNode;

public class StateHolder {
	
	ServerNode currentServer;
	RepositoryNode currentRepository;
	FileNode currentDirectory;
	
	public RepositoryNode getRepository(){
		return currentRepository;
	}
	
	public FileNode getDirectory(){
		return currentDirectory;
	}
	
	public ServerNode getServer(){
		return currentServer;
	}
	
	public void setServer(ServerNode server){
		currentServer = server;
	}
	
	public void setDirectory(FileNode directory){
		currentDirectory = directory;
	}
	
	public void setRepository(RepositoryNode repository){
		currentRepository = repository;
	}
	
	boolean isServerSet(){
		return currentServer != null;
	}
	
	boolean isRepositorySet(){
		return currentRepository != null;
	}
	
}
