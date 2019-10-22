# Getting started
The pydio java sdk provides a Java implementation of features for communicating with a Pydio server Cells and Pydio 8+ versions. Most of the functions are wrapped into the <em>com.pydio.sdk.core.Client</em> class that contains methods to easily manage your files on a Pydio server.

## Configure and resolve a pydio server:

Given an URL, we create a ServerNode object to load the server info
``` java
String url = "https://demo.pydio.com";
ServerNode node = new ServerNode();
Error error = node.resolve();
if (error != null) {
    // error.code could be
    //Code.ssl_error or
    //Code.pydio_server_not_supported or
    //Code.pydio_server_not_supported or
    //Code.con_failed or
    //Code.ssl_certificate_not_signed or
    System.out.println("failed to resolve server");
    return;
}

System.out.println("version: " + server.getVersion());
System.out.println("version name: " + server.getVersionName());
```

## Working with the pydio client:

### Instantiate a Pydio Client

To create a client pass the resolved server to the client factory

``` java
Client client = Client.get(node);
```


### Setting user credentials

``` java
Credentials credentials = new DefaultCredentials("login", "password");
client.setCredentials(credentials);
```


### Performing folder list

``` java
//listing root of "My Files" workspace
try {
    client.ls("my-files", "/", (n) -> {
        System.out.println(n.label());
    });
} catch(SDKexception e) {
    e.printStackTrace();
    Error error = Error.fromException(e)
    // error.code could be
    //Code.ssl_error or
    //Code.pydio_server_not_supported or
    //Code.pydio_server_not_supported or
    //Code.con_failed or
    //Code.ssl_certificate_not_signed or
}
```

## Examples
Find more examples [here](https://github.com/pydio/pydio-sdk-java-v2/tree/master/src/main/java/com/pydio/sdk/examples)
