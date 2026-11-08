# Chat bi-direccional 

Proyecto de chat bi-direccional 



Autores:
Edgard Alvarado Angulo - ecaalvaradoa@poligran.edu.co


----
## Proyecto:

#### Antes de iniciar:

Antes de poder trabajar y poder ejecutar el proyecto de manera correcta, se debe de tener los siguientes componentes:


* Visual Studio Code
    * VS Code extension: [Extension pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
    * VS Code extension: [Debugger For Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-debug)
    * VS Code extension: [Maven For Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-maven)
* Java 25 LTS (JDK 25.0.5 or newer)
* [Maven 3.10.0](https://maven.apache.org/download.cgi#CurrentMaven)
* Docker


#### Correr proyecto

1. Desde `socket-communication-poli`, En una terminal, ejecutar el comando `mvn clean install` para validar que todo esté bien
2. Se hace el cambio al folder de `server` con el comando `cd server` y se ejecuta el comando `mvn exec:java`.
3. Con el servidor levantado se puede levantar el servidor de **UI**, en una nueva terminal desde `socket-communication-poli`, se hace el cambio al folder de `client` ejecutando `cd client` y luego ejecutando el comando `mvn exec:java`.
    * Para probar con multilpes usuarios, es necesario repetir el paso **3**. O sea, abrir una nueva terminal, cambiar al folder de `client` y ejecutar el comando de `mvn exec:java`.

