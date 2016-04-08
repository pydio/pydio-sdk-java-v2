# Getting started
The pydio java sdk provides a Java implementation of Pydio features for communicating with a Pydio server. Most of the functions are wrapped into the <em>PydioClient</em> class that contains simple methods to easily manage your files on a Pydio server.

## <em>PydioClient</em> instantiation:

### 1 - Quick instantiation

Here is the simple way to instantiate a <em>PydioClient</em>
``` java
    PydioClient client = new PydioClient("serverAddress", "username", "password");    
```


### 2 - Advanced instantiation:

When a <em>PydioClient</em> is instantiated like it's done above, an <em>AuthenticationHelper</em> object is created to carry the user credentials. An Authentication helper is a delegate with a method that return the user credentials. 
``` java 
    public abstract String[] getCredentials();
```

So when performing a login operation, the PydioClient calls that method to get the user credentials. You can set your own helper as the following example shows:
``` java    
    PydioClient client = new PydioClient("https://demo.pyd.io");    
    client.setAuthenticationHelper(new AuthenticationHelper(){
        public String[] getCredentials(){        
            String[] credentials = new String[2];            
            System.out.println("AUTHENTICATION:");            
            System.out.print("username : ");
            credentials[0] = new Scanner(System.in).nextString();
            System.out.print("password : ");
            credentials[1] = new Scanner(System.in).nextString();
            return credentials;
        }
    });
```
The user is prompted every time the users credentials are needed.

Now we know how to configure a client we can manage files on the servers.


## Calling PydioClient methods:

Before using our configured client let's have a quick look at some data objects defined in the SDK. The Pydio SDK data objects are simple representation of resource that are sent/received when communicating with a Pydio Server.

### 1 - Data objects

* PydioMessage
The PydioMessage is a wrap of Pydio server response. It contains the result code, the message and others specific information.
 
* Node
The Node is an abstract representation of a file tree on Pydio a server. The most common subclasses of Node are ServerNode, WorkspaceNode and FileNode which are wraps of Server, Workspace and files.
We have also the SearchNode that wrap parameters when searching on a file tree.
 
 
### 2 - Using PydioClient

The snippet code below does :
 
+ create a directory "Test" 
+ upload a file "text.txt" in the "Test" directory
+ list the "Test" directory
+ download the "text.txt" file.


``` java
      //Setting up the client
      final PydioClient client = new PydioClient("yourServerAddress", "username", "password");

      // the ID of the workspace you work on. "1" is refereing to "My Files"
      String workspaceID = "1";

      //Create the MessageHandler to process the creation resposne
      final MessageHandler messageHandler = new MessageHandler(){
          public void onMessage(PydioMessage m){
              System.out.println(m.text());
          }
      };

      //Create the NodeHandler to process every parsed node
      final NodeHandler nodeHandler = new NodeHandler(){
          public void onNode(Node node){
              System.out.println(node.label());
          }
      };


      try {
          //create "Test" directory at the root of the workspace
          client.mkdir(workspaceID, "/", "Test", messageHandler);

          //create text file inside "Test" directory and set its content
          client.upload("1", "/Test", "Content of the file".getBytes(), "text.txt", true, null, null);

          //list "/Test" folder
          client.ls(workspaceID, "/Test", nodeHandler);

          //download the text.txt file content
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          client.download(workspaceID, new String[]{"/Test/text.txt"}, out, null);
          System.out.println("Downloaded content : " + new String(out.toByteArray()));

      } catch (IOException e) {
          e.printStackTrace();
      }
```


### 3 - Error handling

When an operation failed an IOException is thrown. The  <em>PydioClient</em> <em>responseStatus()</em> method can tell more about the reasons. That method return a constant integer that can be one of these following values:

+ Pydio.ERROR_NOT_A_SERVER 
There is no Pydio server configured on the specified address

+ Pydio.ERROR_CON_FAILED
The connection failed often due to no active connection

+ Pydio.ERROR_CON_UNREACHABLE_HOST
The server is unreachable

+ Pydio.ERROR_CON_FAILED_SSL
This error is internally handled by the client

+ Pydio.ERROR_UNVERIFIED_CERTIFICATE
The certificate chain verification process failed
Get the certificate chain this way:

``` java
    X509Certificate[] chain = CertificateTrustManager.mLastUnverifiedCertificateChain[];
```
+ Pydio.ERROR_AUTHENTICATION
The login failed. The credentials provided by the authentication helper are wrong

+ Pydio.ERROR_AUTHENTICATION_WITH_CAPTCHA
The login failed after many tries. Here is how to get the captcha data:

``` java
    ByteArrayOutputStream data = client.captchaData();
```


+ Pydio.ERROR_OLD_AUTHENTICATION_TOKEN
This error is internally handled by the client.

+ Pydio.ERROR_ACCESS_REFUSED
The workspace access is refused or some specific rights are required to perform the action.

+ Pydio.ERROR_OTHER
Error other than all cited before.

