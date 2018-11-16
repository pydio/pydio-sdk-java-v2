# Getting started
The pydio java sdk provides a Java implementation of features for communicating with a Pydio server. Most of the functions are wrapped into the <em>com.pydio.sdk.core.Pydio8</em> class that contains methods to easily manage your files on a Pydio server.

## <em>com.pydio.sdk.core.Pydio8</em> instantiation:


Here is the simple way to instantiate a <em>com.pydio.sdk.core.Pydio8</em>
``` java
    com.pydio.sdk.core.Pydio8 client = new com.pydio.sdk.core.Pydio8("serverAddress", "username", "password");    
```

## Calling com.pydio.sdk.core.Pydio8 methods:

Before showing how to use the client let's have a quick look at some important classes defined in the SDK. A pydio server response contains different sort of data. To easily deal with that data the SDk provides classes to easily

### 1 - Useful classes

+ Data wrap : definition of resource that are sent/received when communicating with a Pydio Server.
+ Delegates : classes that help in data wrap handling. Theses classes are useful for handling event and better memory usage. 

#### Data wrap

* PydioMessage
The PydioMessage is a wrap of Pydio server response. It contains the result code, the message and others specific information.
 
* Node
The Node is an abstract representation of a file tree on Pydio a server. The most common subclasses of Node are ServerNode, WorkspaceNode and FileNode which are wraps of Server, Workspace and files.
We have also the SearchNode that wrap parameters when searching on a file tree.

#### Delegates

The advanced com.pydio.sdk.core.Pydio8 instantiation shows how delegate can be useful to provide data when a login event occurs. Delegates are also used to retrieve data. For example when listing a folder node delegates are used to retrieve children. Delegates are useful for a better memory usage. For example when listing a folder node loading the whole response data into memory is not a good practice. Instead the response is parsed as stream and each parsed node is passed to a delegate. Here are all delegates defined in the SDK:

+ <em>AuthenticationHelper</em> : Helps in login
+ <em>NodeHandler</em> : Helps in folder node listing.
+ <em>ProgressListener</em> : Helps in task progress handling.
+ <em>UploadStopNotifierProgressListener</em> : Helps only in Upload progress handling. 
+ <em>MessageHandler</em> : Helps to get requests response.
+ <em>WorkspaceNodeSaxHandler, RegistrySaxHandler, registryItemHandler, </em> : help in registry parsing. 
 
 
### 2 - Using com.pydio.sdk.core.Pydio8

The snippet code below does :
 
+ create a directory "Test" 
+ upload a file "text.txt" in the "Test" directory
+ list the "Test" directory
+ download the "text.txt" file.


``` java
      //Setting up the client
      final com.pydio.sdk.core.Pydio8 client = new com.pydio.sdk.core.Pydio8("yourServerAddress", "username", "password");

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
            //Log.e("IO", e.getMessage());
      }
```


### 3 - Error handling

When an operation failed an IOException is thrown. The  <em>com.pydio.sdk.core.Pydio8</em> <em>responseStatus()</em> method can tell more about the reasons. That method return a constant integer that can be one of these following values:

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


### 4 - Examples
Find more com.pydio.sdk.examples [here](https://github.com/pydio/pydio-sdk-java-v2/tree/master/src/main/java/examples) 