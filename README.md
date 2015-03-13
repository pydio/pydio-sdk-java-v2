The pydio java sdk provides a Java implementation of Pydio features for communicating with a Pydio server. All the features are wrapped by the PydioClient class that contains simple methods to do the main file system operations.

The data model:
--------------
Pydio model is based on the notion of tree. In fact a server is the root node of a tree which has WorkspaceNode as children. Each WorkspaceNode has a list of FileNodes as children which have other FileNodes as children too.
So a hierarchy is defined by an abstract Node class and concrete classes like ServerNode, WorkspaceNode and FileNode. 


Configuring a Pydio server:
--------------------------
Confuguring a Pydio server is configuring a ServerNode. The SDK provides two method for configuring a serverNode. A simple one that consist of setting manully the server properties, and the other one is based on a resolution process.

* Simple configuration
``` java
	//step 1: create an empty instance of a ServerNode using the NodeFactory
	ServerNode server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);

	//step 2: set server info like host, protocol, path, has self-signed ssl certicicate
	server.setHost("hostname");
	server.setPath("path_of_your_pydio_install");
	server.setSelfSigned(boolvalue); //only if the target server use self-signed certificate
	server.setLegacy(PYDIO_SERVER_VERSION > 5);	//for version pydio version greater than 5
```


* Resolution-based configuration

this case depend on the context in which you use the sdk. For example, let's suppose you're building an application that connect to a server that has not a fixed domain name or ip. But you have a way to retrieve it using a special protocol. The resolution process allows you to handle this case. The SDK provides an interface that need to be implemented to spécify the way your app retrieve te properties of your serverNode.
here is an exmaple of using a Resolver.
``` java
	...
	//step 1: create an instance of ServerResolver

	String KEY = "key";
	ServerResolver resolver = new ServerResolver(){
		@override
		public void resolve(ServerNode server){
			// do your resolution stuff here
			// and set your serverNode properties
		}
	};

	//step 2: register your resolver like this
	Resolution.register(KEY, resolver);
	ServerNode server = NodeFactory.createNode(Node.TYPE_SERVER);

	// specify that your serverNode properties need to resolved
	// by setting the hostname with the same key you've just 
	// when registering your resolver	
	server.setHost(KEY);
```

Creation of a Pydio Client:
--------------------------
The creation of a PydioClient is based on an an instance of a ServerNode and an instance of a credentials provider.
A credential provider is a an object that carry/fetches credentials for pydio authentication.
Here is the simplest way to instantiate a PydioClient:
``` java
	...
	PydioClient client = PydioClient.configure(server, new CredentialsProvider(){
        @Override
        public UsernamePasswordCredentials requestForLoginPassword() {
            return new UsernamePasswordCredentials("login", "password");
        }
        @Override
        public X509Certificate requestForCertificate() {
            return null;
        }
	});
```

Performing simple actions:
-------------------------
Once your pydio client is configured, you can start calling the client methods to perform file operations on your server.

>Note that PydioClient is design to perform asynchrone operations. So for operation that requires result like file listing, you need to pass 
a callback to process one by one every parsed node.
The example below stores and displays the name of each child of a given node.

``` java
	...
	final ArrayList<Node> children = new ArrayList<>();
	client.listChildren(givenNode, new NodeHandler(){
		@Override
		public void processNode(Node node){
			//filter to exclude the recyclebin file
			if(!node.label().equals("Recycle Bin")){
				System.out.println(node.label());
				children.add(node);
			}
		}
	}, offset, max);
	//note that is the given node is null, the client automatically list the workspaces of the serverNode.
	// the offset and max arguments are both integer that allows you to list only subset of the givenNode children
```
