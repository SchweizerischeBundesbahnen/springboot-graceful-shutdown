Springboot-Graceful-Shutdown for Openshift/Kubernetes
=
The Springboot-Graceful-Shutdown enables your spring boot application to do a rolling deployment without any downtime on Openshift.
We (SBB) use this in a dockerized Cloud environment on Openshift - it should also work on Kubernetes.
We use here the terminology of Openshift/Kubernetes: Pods (roughly containers) and Services (logical load balancers that combine N containers).
Openshift needs to know when a Pod is ready to respond to requests. That is done via the readyness probe.
As soon as the readyness probe of a Pod fails, Openshift takes the Pod off the service, so user requests are no longer sent to this Pod.  

Graceful Shutdown Workflow
--
Here is an example of how the graceful shutdown workflow works:
1. **Openshift sends the Pod the SIGTERM signal** which tells the Docker instance to shutdown all its processes. 
This can happen when scaling down a Pod by hand or automatically during a rolling deployment of Openshift.
2. The JVM receives the SIGTERM signal and the graceful shutdown hook sets the **readyness probe to false**
3. The **process waits for a defined time to initiate the shutdown of the spring context**, e.g. for 20 seconds. 
This time is needed for Openshift to detect that the Pod is no longer ready and to remove the pod from the service. 
The readyness probe check interval must in this case be configured to be less than 20 seconds.
4. Openshift removes the Pod from the Service.
5. After the configured wait time, for example 20 seconds, the spring context will be shut down. Open transactions will be properly finished. 
6. After the Spring Boot application is shutdown, the liveness probe will automatically be set to false. 
7. Pod is shutdown and removed.

How to use it
--
1. Add the maven dependency for Spring Boot actuator and this graceful shutdown starters: 
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

2. Start your application with the alternative method ```GracefulshutdownSpringApplication.run``` instead of ```SpringApplication.run```, e.g. 

```
@SpringBootApplication
public class EstaGracefullshutdownTesterApp {

    public static void main(String[] args) {
        GracefulshutdownSpringApplication.run(EstaGracefullshutdownTesterApp.class, args);
    }
}
```


3. Optionally, adapt the shutdown delay (in your yaml, properties or system property) - the default is 20 seconds:
- **Yaml or Properties**: estaGracefulShutdownWaitSeconds: 30  
- **System Property**: -DestaGracefulShutdownWaitSeconds=30


Start now your application and point your readyness probe to ```http://yourhost:8282/health``` and set the check interval lower than the shutdown delay, for example, to 10 seconds.
In your developement environment (Eclipse or IntelliJ) you can simulate a SIGTERM: For this use the _exit_ operation (do not terminate it via the stop icon). Then check what 
happens with the health check and the shutdown of the spring context.

If you want to implement your own readyness check implement your RestController with the **ch.sbb.esta.openshift.gracefullshutdown.IProbeController** interface.


Other Graceful Shutdown implementation for Spring Boot:
--
- This nice implementation can do a Graceful-Shutdown triggered by REST, JMX or the shell: https://github.com/corentin59/spring-boot-graceful-shutdown


Good to know
--
If you do a rolling deployment, the risk of having failing service calls is minimized with this strategy. 
It is still possible that service calls fail because of the Openshift internal routing, network issues, 
or other components between service caller and service host.

Releasing (internal)
--
If you want to create a new release, you need to do following steps:
1. Generate your PGP Keys with Gnugpg: ```gpg --gen-key```
2. Publish your PGP Key to a public server: ```gpg --keyserver keyserver.ubuntu.com --send-key YOURKEYID```
2. Copy the prepared Maven ```settings.xml``` from this repo to your Maven home directory
3. Adjust all the user and password configurations in the ```settings.xml```
4. You find the sonatype credentials on the protected password page of the SBB ESTA Team 
5. Adjust the version in the pom.xml -SNAPSHOT for a snapshot deployment, without the -SNAPSHOT for a release deployment.
6. Execute ```mvn clean deploy```
7. If the build was successful, check on oss.sonatype.org whether your version was published
8. In case of troubles contact the owner of the repository


**Be aware that releasing behind the proxy might not work as the URL is blocked.
 Publishing a SNAPSHOT publishing worked fine from behind the proxy.**


Credits
--
This Shutdownhook was created, maintained and used by the SBB ESTA team in Switzerland. 

