Springboot-Graceful-Shutdown
=
The Springboot-Graceful-Shutdown enables your springboot application to do a rolling deployment without any downtime.
We (SBB) test and use it in a dockerized Cloud environment based on Openshift. But it should easely work with any other system.
The description is made according to Openshift terminology of Pods (Containers) and Services (Clustered Service which refers to a bunch of containers)
Openshift needs to know when it needs to take a Pod off the Service. That is indicated by the readyness probe in Openshift.
As soon the readynessprobe fails, Openshift takes the Pod off the exposed service, so the Service-Router doesn't redirect to this Pod anymore.  

Graceful Shutdown Workflow
--
Here an example how the Gracefulshutdown Workflow works
1. **Openshift sends the POD the TERM Signal** which tells the Docker instance to shutdown all Processes. 
This can happen by scaling down a pod by hand or automatically by a rolling deplyoment from Openshift.
2. JVM receives the SIGTERM Signal and the Gracefulshutdownhook sets the **readyness Probes to false**
3. The **process waits for a defined time to initiate the shutdown of the spring context**. For example 20 seconds. 
This time is needed for the Readynessprobe to receive the false signal and remove the pod from the service. 
The readyness probe intervall must be configured less than 20s.
4. Openshift removes Pod from Service.
5. After the configured wait time, for example 20s, the springcontext will be shutdown. Open transactions will be finished properly. 
6. After Springbootapp is shutdown, the livenessprobe is false. 
7. Pod is shutdown and removed.

How to use it
--
Add the maven dependency for Springboot actuator and the graceful shutdown
```xml 
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
        <version>X.X.X</version>
    </dependency>
    <dependency>
        <groupId>ch.sbb</groupId>
        <artifactId>springboot-graceful-shutdown</artifactId>
        <version>X.X</version>
    </dependency>
```

Configure in your **Yaml, Properties or Systemproperty** the shutdown delay:
- **Yaml or Properties**: estaGracefulShutdownWaitSeconds: 30
- **System Property**: -DestaGracefulShutdownWaitSeconds=30


Start now your application and Point your **Readyness** check to http://yourhost:8282/health and set the check interval lower than the shutdown delay. For example 10s.
In your developement Environment you can exit the application, not stopping it, and see what happens with the healthcheck and the shutdown of the spring context.

If you want to implement **your own readyness check** implement your RestController with the 

**ch.sbb.esta.openshift.gracefullshutdown.IProbeController** Interface.


Other Graceful Shutdown implementation for Spring Boot
--
- This nice implementation can do a Graceful-Shutdown triggered by REST, JMX or the shell: https://github.com/corentin59/spring-boot-graceful-shutdown


Good to know
--
If you do a rolling deployment, the risk of having failing servicecalls is with this strategy minimized. 
And still it can happen that servicecalls fail because of the Openshift internal routing, networkproblems, 
or any other component between service caller and service host.

Releasing
--
If you want to create a new release, you need to do following steps:
1. Generate your PGP Keys with Gnugpg: gpg --gen-key
2. Publish your PGP Key to a public server: gpg --keyserver keyserver.ubuntu.com --send-key YOURKEYID
2. Copy the prepared Maven settings.xml from this repo to your Maven home directory
3. Adjust all the user and password configurations in the settings.xml
4. You find the sonatype credentials on the protected passwordpage of the SBB ESTA Team 
5. Adjust the version in the pom.xml -SNAPSHOT for a snapshot deployment, without the -SNAPSHOT for the release deployment.
6. Execute mvn clean deploy
7. If the build was successful, check oss.sonatype.org if your version was published
8. In case of troubles contact the Repoowner


**Be aware that releasing behind the proxy might not work because they blocked the URL.
                In SBB I needed to publish the release from outside of the Proxy. The SNAPSHOT publishing worked behind the proxy.**


Credits
--
This Shutdownhook was created, maintained and used by SBB (Schweizerische Bundesbahnen) team ESTA in Switzerland. 

