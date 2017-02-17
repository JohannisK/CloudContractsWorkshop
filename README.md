# Spring Cloud Contracts

## Introduction
Checking out this project will give you a very simple microservice architecture on your local environment.

All modules will inherit from spring-boot-starter-parent:1.4.3.RELEASE

Quick overview of the modules:

- **eureka-server** will act as a service discovery server. Microservices can register with the eureka-server to look up other microservice and/or to be discoverable by other microservices. <br>
-*application.yml* contains configuration regarding the port of the application. It also disables registration and registry fetching since the server itself does not need to discover other services, or be discoverable in this example.<br>
-*pom.xml* contains the only extra dependency we need to start the eureka server: `spring-cloud-starter-eureka-server`<br>
-*EurekaServerApplication* is the main class, all that is needed to start the Eureka Server including a complete working fronted is the annotation: `@EnableEurekaServer`
- **numbers-service** is the microservice in this example. <br>
-*application.yml* contains configuration value 0 for the port of the appliction. This means a random port will be assigned making it possible to run several instances of the microservice on your machine. It also contains some information regarding the EurekaServer<br>
-*pom.xml* contains two extra dependencies we need to enable RestControllers and the Discovery Client: `spring-boot-starter-web` and `spring-cloud-starter-eureka`<br>
-*NumberServiceApplication* is the main class, by adding the `@EnableDiscoveryClient` annotation the service will register itself with the Eureka discovery server using the name defined in *bootstrap.yml*
- **frontend** is the frontend of our application. It uses thymeleaf to serve a webinterface. It uses ribbon to implement clientside loadbalancing to the microservices listed by the Eureka Server.<br>
-*application.yml* contains configuration value 8080 for the port of the application. It also has some configuration for eureka, this application will not register itself. It will not be discoverable, but will be able to discover other service, for instance the numbers-service.<br>
-*pom.xml* contains some extra dependencies for the frontend. Regarding the microservice it's important to notice `spring-cloud-starter-eureka` and `spring-cloud-starter-ribbon`.<br>
-*FrontendApplication* is the main class, notice our RestTemplate is `@LoadBalanced` which implements clientside loadbalancing to the services listed by the Eureka Server.

You should have an understanding of this microservice architecture now. If you don't please do the following workshop by JDriven: [https://bitbucket.org/jdriven/spring-cloud-workshop]()
The modules in this project are a simplified version of the spring cloud workshop.



## Task 1: Adding support for cloud contracts to the producers code

**Task 1.1** Add the `spring-cloud-starter-contract-verifier` dependency to the numbers-service module's pom file under TODO 1.1.<br>
*(If necesary, refresh your maven dependencies!)*
		
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-contract-verifier</artifactId>
		<scope>test</scope>
	</dependency>
	
**Task 1.2** Add the `spring-cloud-contract-maven-plugin` build plugin to the numbers-service module's pom file under TODO 1.2.<br>
*(If necesary, refresh your maven dependencies!)*

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-contract-maven-plugin</artifactId>
				<version>1.0.3.RELEASE</version>
				<extensions>true</extensions>
				<configuration>
					<baseClassForTests>nl.johannisk.cloud.contract.BaseTest</baseClassForTests>
				</configuration>
			</plugin>
		</plugins>
	</build>
	
> This build plugin can generate unit tests based on contracts. Note the baseClassForTests property, we need to supply a base class for generated tests

**Task 1.3** Create a new test class in the numbers-service module in the package `nl.johannisk.cloud.contract` named `BaseTest`
	
	package nl.johannisk.cloud.contract;
	
	import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
	import nl.johannisk.cloud.numbersservice.NumbersServiceApplication;
	import org.junit.Before;
	import org.junit.runner.RunWith;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.boot.test.context.SpringBootTest;
	import org.springframework.test.context.junit4.SpringRunner;
	import org.springframework.web.context.WebApplicationContext;
	
	@RunWith(SpringRunner.class)
	@SpringBootTest(classes = NumbersServiceApplication.class, webEnvironment = 	SpringBootTest.WebEnvironment.RANDOM_PORT)
	public abstract class BaseTest {
	
	    @Autowired
	    private WebApplicationContext appContext;
	
	    @Before
	    public void setup() {
	        RestAssuredMockMvc.webAppContextSetup(appContext);
	    }
	}

**Task 1.4** Per default the build task looks for contracts in a test resource folder named `contracts`. Create this folder.
> The full path from the projecet root should be numbers-service -> src -> test -> resources -> contracts

**Task 1.5** Create a groovy contract in the numbers-service module for the prime numbers call in the test resources contracts folder named `returnPrimesFrom1to10.groovy`

	package contracts;
	
	org.springframework.cloud.contract.spec.Contract.make {
		request {
			method 'POST'
			url '/primenumbers'
			body([
				from:1,
				to:10
				])
			headers {
				header('Content-Type', 'application/json;charset=UTF-8')
			}
		}
		response {
			status 200
			body([
				primeNumbers: [2,3,5,7],
				instanceId: value(consumer('numbers-service-005887b1ff2a0c4cf0b94414d6e74a3a'),
                      producer(regex('numbers-service-[a-fA-F0-9]{32}')))
				])
			headers {
				header('Content-Type': 'application/json;charset=UTF-8')
			}
		}
	}
> Note the instanceId value. This value is declared for the consumer as a static value, and for the producer as a regular expression. The purpose of this is to illustrate the value of fields can be different for the producer generated tests, and the client generated stubs.

**Task 1.6** run `mvn clean install` in the numbers-service module

###Summary

The microservice now suppports cloud contracts. The `mvn clean install` command should have executed correctly giving a few errors on the Discovery Client. Since we don't have a eureka instance running the test complains about not being able te register itself. This can be solved with more specific and finegrained BaseTest(s) (yes, you can have more), but this is not in scope in this workshop.

Note the following line in the build log:
`[INFO] Installing /****/CloudContractsWorkshop/numbers-service/target/numbers-service-1.0-SNAPSHOT-stubs.jar to /****/.m2/repository/nl/johannisk/numbers-service/1.0-SNAPSHOT/numbers-service-1.0-SNAPSHOT-stubs.jar`
	
With the new dependency and build plugin, `mvn clean install` now generates a stubs jar, and installs it in the local maven repository.

## Task 2: Adding support for cloud contracts to the consumers code

**Task 2.1** Add the `spring-cloud-contract-wiremock` and `spring-cloud-starter-contract-stub-runner` dependencies to the frontend module's pom file under TODO 2.1. <br>
*(If necesary, refresh your maven dependencies!)*

	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-contract-wiremock</artifactId>
		<scope>test</scope>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
		<scope>test</scope>
	</dependency>

**Task 2.2** Add configuration for stubrunner to the frontend module's application.yml under TODO 2.2. 

	stubrunner:
	  ids: nl.johannisk:numbers-service:+:stubs:0
	  work-offline: true

> The "ids" property will download the stubs for the repository identifier en configure the port which it is run on. It's format is:<br> `<groupId>:<artifactId>:<version ... + means latest>:<submodule>:<port ... 0 means random>`<br>
> The "work-offline" property will tell stubrunner to download the artifact from your local repository.

**Task 2.3** Create a new test class in the frontend module in the package `nl.johannisk.cloud.frontend.client` named `NumberServiceClientTest`

	package nl.johannisk.cloud.frontend.client;
	
	import org.junit.Before;
	import org.junit.Test;
	import org.junit.runner.RunWith;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.boot.test.context.SpringBootTest;
	import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
	import org.springframework.test.context.junit4.SpringRunner;
	import org.springframework.web.client.RestTemplate;
	
	import java.util.ArrayList;
	import java.util.List;
	
	import static org.junit.Assert.*;
	
	@RunWith(SpringRunner.class)
	@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.NONE)
	@AutoConfigureStubRunner
	public class NumberServiceClientTest {
	
	    @Autowired
	    private RestTemplate restTemplate;
	    private NumberServiceClient client;
	
	    @Before
	    public void setUp() {
	        client = new NumberServiceClient(restTemplate);
	    }
	
	    @Test
	    public void getPrimeNumbersFrom1till10() {
	        PrimeNumbersResponse response = client.calculatePrimeNumbers(new PrimeNumbersRequest(1,10));
	
	        List<Integer> primes = new ArrayList<>();
	        primes.add(2);
	        primes.add(3);
	        primes.add(5);
	        primes.add(7);
	
	        assertTrue(response.getPrimeNumbers().equals(primes));
	    }
	
	    // TODO 3.3
	}

> Note the annotation `@AutoConfigureStubRunner`. This annotation will stub the discovery server as well as the stubs configured in the application.yml

**Task 2.4** Run the unit test, is should succeed. 

###Summary

The frontend now also suppports cloud contracts. The test uses known parameters which are also configured in the contract at the producers side. We can now test, through the cloud contract, that the producer and the consumer have agreed on the service api, and are both conforming to it.

A few lines in the log of the test might be interesting:

	2017-01-20 18:56:17.033  INFO 17948 --- [           main] o.s.c.c.stubrunner.AetherStubDownloader  : Remote repos not passed but the switch to work offline was set. Stubs will be used from your local Maven repository.
	2017-01-20 18:56:17.137  INFO 17948 --- [           main] o.s.c.c.stubrunner.AetherStubDownloader  : Desired version is [+] - will try to resolve the latest version
	2017-01-20 18:56:17.163  INFO 17948 --- [           main] o.s.c.c.stubrunner.AetherStubDownloader  : Resolved version is [1.0-SNAPSHOT]
	2017-01-20 18:56:17.163  INFO 17948 --- [           main] o.s.c.c.stubrunner.AetherStubDownloader  : Resolving artifact [nl.johannisk:numbers-service:jar:stubs:1.0-SNAPSHOT] using remote repositories []
	2017-01-20 18:56:17.174  INFO 17948 --- [           main] o.s.c.c.stubrunner.AetherStubDownloader  : Resolved artifact [nl.johannisk:numbers-service:jar:stubs:1.0-SNAPSHOT] to /Users/johankragt/.m2/repository/nl/johannisk/numbers-service/1.0-SNAPSHOT/numbers-service-1.0-SNAPSHOT-stubs.jar
	2017-01-20 18:56:17.181  INFO 17948 --- [           main] o.s.c.c.stubrunner.AetherStubDownloader  : Unpacking stub from JAR [URI: file:/Users/johankragt/.m2/repository/nl/johannisk/numbers-service/1.0-SNAPSHOT/numbers-service-1.0-SNAPSHOT-stubs.jar]
	2017-01-20 18:56:17.184  INFO 17948 --- [           main] o.s.c.c.stubrunner.AetherStubDownloader  : Unpacked file to [/var/folders/_6/4ztpb_rn0j77d1f3q692t9f40000gn/T/contracts3054482821481625751]
	...
	2017-01-20 18:56:28.800  INFO 17948 --- [           main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 64914 (http)
	2017-01-20 18:56:28.802  INFO 17948 --- [           main] o.s.c.contract.stubrunner.StubServer     : Started stub server for project [nl.johannisk:numbers-service:1.0-SNAPSHOT:stubs] on port 64914
	
These lines show the stubs are downloaded and instantiated on port 64914

## Task 3: Consuming a new service endpoint using cloud contracts
###Introduction
This task needs a small introduction. In task 1 support for cloud contracts was added to the producers code. By runnning `mvn clean install` the stubs jar was created and uploaded to the local mvn repository. In task 2 support for cloud contracts was added to the consumers code, which also used the local repository to download the stubs jar from.
We will be adding a new contract in this task, directly in the producers code. Normally this new contract would be added in a local checkout of the producers code. To have the contract implemented by the producer the contract would be submitted to the producer via a mergerequest. This is out of scope for the workshop, but it's important to remeber this, since we will be coming back to this point.

**Task 3.1** Add a new contract next to the `returnPrimesFrom1to10.groovy` file named `returnFibonacciUntil10.groovy`

	package contracts;

	org.springframework.cloud.contract.spec.Contract.make {
   		request {
   			method 'POST'
			url '/fibonaccinumbers'
			body(
				[until:10]
			)
			headers {
				header('Content-Type', 'application/json;charset=UTF-8')
			}
		}
		response {
			status 200
			body(
				[
					fibonacciNumbers: [0,1,1,2,3,5,8],
					instanceId: value(consumer('numbers-service-005887b1ff2a0c4cf0b94414d6e74a3a'),
                      producer(regex('numbers-service-[a-fA-F0-9]{32}')))
				]
			)
			headers {
				header('Content-Type': 'application/json;charset=UTF-8')
			}
		}
	}

**Task 3.2** Build the numbers-service without tests using `mvn clean install -DskipTests`
> Since there is no implementation in the producers code the generated tests would fail. As a consumer we are only interested in the generated test. That's why the parameter -DskipTests is added.

**Task 3.3** Go back to the frontend application. Find the NumberServiceClientTest file and add a test case for the Fibonacci service under TODO 3.3 (replace or add the TODO 5.3, you'll need it later)

	//TODO 5.3
	@Test
	public void getFibonacciNumbersUntill10() {
        FibonacciNumbersResponse response = client.calculateFibonacciNumbers(new FibonacciNumbersRequest(10));

        List<Integer> fibonacci = new ArrayList<>();
        fibonacci.add(0);
        fibonacci.add(1);
        fibonacci.add(1);
        fibonacci.add(2);
        fibonacci.add(3);
        fibonacci.add(5);
        fibonacci.add(8);

        assertTrue(response.getFibonacciNumbers().equals(fibonacci));
    }
    
This test should be full of compiler errors, so let's fix those.

**Task 3.4** Create a FibonacciNumbersRequest object in the frontend application

	package nl.johannisk.cloud.frontend.client;
	
	public class FibonacciNumbersRequest {

		//TODO 5.2
		private final Integer untill;
		
		public FibonacciNumbersRequest(Integer untill) {
			this.untill = untill;
		}

		public Integer getUntil() {
			return untill;
		}
	}
	
> Note that this is based on the request specified in our newly created fibonacci contract.

**Task 3.5** Create a FibonacciNumbersResponse object in the frontend application

    package nl.johannisk.cloud.frontend.client;

    import com.fasterxml.jackson.annotation.JsonCreator;
    import com.fasterxml.jackson.annotation.JsonProperty;

    import java.util.List;

    public class FibonacciNumbersResponse {

        private final List<Integer> fibonacciNumbers;
        private final String instanceId;

        @JsonCreator
        public FibonacciNumbersResponse(@JsonProperty("fibonacciNumbers") List<Integer> fibonacciNumbers, @JsonProperty("instanceId") String instanceId) {
            this.fibonacciNumbers = fibonacciNumbers;
            this.instanceId = instanceId;
        }

        public List<Integer> getFibonacciNumbers() {
            return fibonacciNumbers;
        }

        public String getInstanceId() {
            return instanceId;
        }
    }

**Task 3.6** Create the `calculateFibonacciNumbers` method in the NumbersServiceClient under TODO 3.6 in the fronted module.

	public FibonacciNumbersResponse calculateFibonacciNumbers(FibonacciNumbersRequest fibonacciNumbersRequest) {
        return restTemplate.postForObject("http://numbers-service/fibonaccinumbers",
                fibonacciNumbersRequest,
                FibonacciNumbersResponse.class);
    }
    
 > Please check the modified files to fix any import problems. They shouldn't occur if you followed this workshop, but might if you mixed up or changed packaging.
 
**Task 3.7** Run the newly created test in NumberServiceClientTest either from the IDE or with a `mvn clean install` in the frontend module.

## Task 4: Developing a new service endpoint using cloud contracs
### Introduction
Now that the client is working the producer needs to implement the service. We've build the numbers-service code on our consumer side with `mvn clean install -DskipTests`. The reason for this, is that we wanted the stubs to be generated by the build process. Cloud contracts also generated unit-tests based on the contract on the producer side. Normally the consumer would submit a new contract through a pull request. Let's assume you are now the implementor of the producer, and you've just checked out the pull request.

**Task 4.1** Start the eureka-server module

**Task 4.2** Run `mvn clean install` in the numbers-service module.
> Note that the build fails on the ContractVerifierTest
	
	org.junit.ComparisonFailure: expected:<[200]> but was:<[404]>
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at org.springframework.cloud.contract.verifier.tests.ContractVerifierTest.validate_returnFibonacciUntil10(ContractVerifierTest.java:28)

> The endpoint for Fibonacci is not found. 

**Task 4.3** As the producer we want the new endpoint to conform to the first one. Change the contract. Rename the contract file from `returnFibonacciUntil10` to `returnFibonacciFrom1to10` and replace it's contents.

    package contracts;
    
    org.springframework.cloud.contract.spec.Contract.make {
        request {
            method 'POST'
            url '/fibonaccinumbers'
            body(
                    [from:1,
                     to:10]
            )
            headers {
                header('Content-Type', 'application/json;charset=UTF-8')
            }
        }
        response {
            status 200
            body(
                    [
                            fibonacciNumbers: [1, 1, 2, 3, 5, 8],
                            instanceId  : value(consumer('numbers-service-005887b1ff2a0c4cf0b94414d6e74a3a'),
                                    producer(regex('numbers-service-[a-fA-F0-9]{32}')))
                    ]
            )
            headers {
                header('Content-Type': 'application/json;charset=UTF-8')
            }
        }
    }

    
**Task 4.4** Create a class FibonacciNumbersService in the numbers-service module in the service package.

    package nl.johannisk.cloud.numbersservice.service;

    import org.springframework.stereotype.Service;

    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;
    import java.util.stream.Collectors;

    @Service
    public class FibonacciNumbersService {

        public List<Integer> calculateFibonacciNumbers(int from, int to) {
            List<Integer> fibonacci = new ArrayList<>(Arrays.asList(0));
            int nextFibonacci = 1;
            while (nextFibonacci <= to) {
                fibonacci.add(nextFibonacci);
                nextFibonacci = fibonacci.get(fibonacci.size() - 1) + fibonacci.get(fibonacci.size() - 2);
            }
            return fibonacci.stream().filter(number -> number >= from).collect(Collectors.toList());
        }
    }

**Task 4.5** Create a class FibonacciNumbersController in the numbers-service module in the controller package.

    package nl.johannisk.cloud.numbersservice.controller;
    
    import com.netflix.appinfo.EurekaInstanceConfig;
    import nl.johannisk.cloud.numbersservice.service.FibonacciNumbersService;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.RestController;
    
    import java.util.List;
    
    @RestController
    @RequestMapping("/fibonaccinumbers")
    public class FibonacciNumbersController {
    
        private final FibonacciNumbersService fibonacciNumbersService;
    
        private final EurekaInstanceConfig instanceConfig;
    
        public FibonacciNumbersController(FibonacciNumbersService fibonacciNumbersService, EurekaInstanceConfig instanceConfig) {
            this.fibonacciNumbersService = fibonacciNumbersService;
            this.instanceConfig = instanceConfig;
        }
    
        @RequestMapping(method = RequestMethod.POST)
        public FibonacciNumbersResponse calculateFibonacciNumbers(@RequestBody @Validated FibonacciNumbersRequest fibonacciNumbersRequest) {
            List<Integer> fibonacciNumbers = fibonacciNumbersService.calculateFibonacciNumbers(fibonacciNumbersRequest.getFrom(), fibonacciNumbersRequest.getTo());
            return new FibonacciNumbersResponse(fibonacciNumbers, instanceConfig.getInstanceId());
        }
    
    }

**Task 4.6** Create a class FibonacciNumbersRequest in the numbers-service module in the controller package.

    package nl.johannisk.cloud.numbersservice.controller;
    
    import com.fasterxml.jackson.annotation.JsonCreator;
    import com.fasterxml.jackson.annotation.JsonProperty;
    
    public class FibonacciNumbersRequest {
    
        private final Integer from;
        private final Integer to;
    
        @JsonCreator
        public FibonacciNumbersRequest(@JsonProperty("from") Integer from,
                                       @JsonProperty("to") Integer to) {
            this.from = from;
            this.to = to;
        }
    
        public Integer getFrom() {
            return from;
        }
    
        public Integer getTo() {
            return to;
        }
    }


**Task 4.7** Create a class FibonacciNumbersResponse in the numbers-service module in the controller package.

    package nl.johannisk.cloud.numbersservice.controller;
    
    import com.fasterxml.jackson.annotation.JsonCreator;
    import com.fasterxml.jackson.annotation.JsonProperty;
    
    import java.util.List;
    
    public class FibonacciNumbersResponse {
    
    
        private final List<Integer> fibonacciNumbers;
        private final String instanceId;
    
        @JsonCreator
        public FibonacciNumbersResponse(@JsonProperty("fibonacciNumbers") List<Integer> fibonacciNumbers,
                                        @JsonProperty("instanceId") String instanceId) {
            this.fibonacciNumbers = fibonacciNumbers;
            this.instanceId = instanceId;
        }
    
        public List<Integer> getFibonacciNumbers() {
            return fibonacciNumbers;
        }
    
        public String getInstanceId() {
            return instanceId;
        }
    
    }

**Task 4.8** Run `mvn clean install` in the numbers-service module. It should compile without errors.


## Task 5: Verifying our client using cloud contracs
### Introduction
The producer has notified the consumer that the service is implemented and will be availlable soon. In the following steps we're still talking to our local m2 repository. Normally, at this point you would change the properties to work online and provide a url to the maven repository. This way you would verify against the stubs generated by the producer based on the code they will deploy to production, which passed the contracts availlable to this producer.

**Task 5.1** Open the frontend module and navigate to the NumbersServiceClientTest. Execute the tests. The `getFibonacciNumbersUntill10` should fail.

> Note the following lines in the log:

	2017-02-17 11:36:56.982 ERROR 30072 --- [o-auto-1-exec-5] WireMock                                 : 	Request was not matched:
	{
	  	"url" : "/fibonaccinumbers",
	  	"absoluteUrl" : "http://localhost:49277/fibonaccinumbers",
	  	"method" : "POST",
	  	"clientIp" : "127.0.0.1",
	  	"headers" : {
	     "accept" : "application/json, application/*+json",
	     "content-type" : "application/json;charset=UTF-8",
	     "user-agent" : "Java/1.8.0_111",
	     "host" : "localhost:49277",
	     "connection" : "keep-alive",
	     "content-length" : "12"
	   },
	   "cookies" : { },
	   "browserProxyRequest" : false,
	   "loggedDate" : 1487327816953,
	   "bodyAsBase64" : "eyJ1bnRpbCI6MTB9",
	   "body" : "{\"until\":10}",
	   "loggedDateString" : "2017-02-17T10:36:56Z"
	}
	Closest match:
	{
	  "url" : "/fibonaccinumbers",
	  "method" : "POST",
	  "headers" : {
	    "Content-Type" : {
	      "equalTo" : "application/json;charset=UTF-8"
	    }
	  },
	  "bodyPatterns" : [ {
	    "matchesJsonPath" : "$[?(@.to == 10)]"
	  }, {
	    "matchesJsonPath" : "$[?(@.from == 1)]"
	  } ]
	}

> Stubrunner finds the change the producer has made to the code. The call our consumer executes doesn't match, but Stubrunner finds a very close match. In the next tasks we'll conform to the new contract.

**Task 5.2** Replace the complete implementation of the FibonacciNumbersRequest in the frontend module under TODO 5.2.

	private final Integer from;
    private final Integer to;

    public FibonacciNumbersRequest(Integer from, Integer to) {
        this.from = from;
        this.to = to;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }
    
**Task 5.3** Replace the complete test funcion getFibonacciNumbersUntill10 with the new test in the frontend module's test class under TODO 5.3.

    @Test
    public void getFibonacciNumbersFrom1till10() {
    
        FibonacciNumbersResponse response = client.calculateFibonacciNumbers(new FibonacciNumbersRequest(1,10));
    
        List<Integer> fibonacci = new ArrayList<>();
        fibonacci.add(1);
        fibonacci.add(1);
        fibonacci.add(2);
        fibonacci.add(3);
        fibonacci.add(5);
        fibonacci.add(8);
    
        assertTrue(response.getFibonacciNumbers().equals(fibonacci));
    }
    
##Conclusion
Cloud Contracts provide a very usefull mechanism to generate extra tests and stubs to verify the integration between services. In my opinion one of the greatest features is that the producer and the consumer al talking to the same contract and are both obligated to maintain the contract when making changes to the client or the producer. The consumer needs the changes to generate the correct stubs, while the producer needs the changes because otherwise the generated tests will fail. A possible pittfall is that the contract only needs to be changed when either side wants to make breaking changes or add functionality. When functionality or data is removed from the client this wont be breaking change to the stubs. This might clutter the code.

The final task:
**Task 6** Create a nice frontend for the fibonacci service so we can verify manually that it works!