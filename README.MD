# Contract testing example

This is the simplest example you will ever find about how to do Consumer Driven Contract Testing with PACT
using Pact JVM JUnit 2.11 and Maven. The simplicity of the provider and the consumer are intentional so that
you are not lost on implementation details but learn the basic skeleton and basic concepts of Consumer Driven
Contract testing. During this example repo we will follow the following steps:

1. Create two simple services, phonebook-consumer and phonebook-provider with Spring Boot.
2. Write a simple test in the Consumer side using the Junit rule
3. We will run the tests in the Consumer side and see the pact file created
4. We will run the tests in the Provider side
5. We will go through a breaking change and see what happens

## 0. Pre-requisites
This is a project that requires Java 8 and Maven 3.3.9.

## 1. Example applications: phonebook-consumer and phonebook provider
Clone the codes to your local, then you can find two applications that behave as per the following diagram:
![](/images/consumer-provider-diagram.png)

### phonebook-provider
This is an API service serves at http://localhost:8080/{name}. The consumer
can retrieve the telephone number of a given name which is passed as a parameter.

If you want to run it execute `mvn spring-boot:run` in the phonebook-provider folder.

### phonebook-consumer

This consumer retrieves phone numbers for given names. It is a VERY simple app that expects the provider to return
the **phone number** for my **mum**.


## 2. Simple consumer test

If you look at the code, the test is not so different from a unit test, but there are some key differences:
* We are using a mock server (mockProvider) as opposed to a regular mock. This assures that on testing
a mock provider server is spun up in a random port of localhost thanks to the **PactProviderRuleMk2** class.
When using a regular mock you have to fake requests but using a mock server the requests during testing are real HTTP
requests, no weird fake wiring. That is the @Rule part.
Then you define the interactions in the @Pact section which defines the contract between the consumer and the provider.
Finally you have your actual test, which is a verification of the contract between the consumer and the provider with
regular assertions.

```java
public class PhonebookConsumerTest {

    @Rule
    public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("phonebook-provider", this);

    @Pact(provider="phonebook-provider", consumer="phonebook-consumer")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        return builder
                .uponReceiving("A contact request for mum")
                .path("/mum")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8")
                .body("684088275")
                .toPact();
    }

    @Test
    @PactVerification("phonebook-provider")
    public void getPhoneNumber() throws Exception {
        PhonebookConsumer pc = new PhonebookConsumer(mockProvider.getUrl());
        String mum = pc.getPhoneNumber("mum");
        assertEquals("684088275", mum);
    }

}
```
This is not the only way to create the tests, you can also use basic unit testing but it requires additional wiring,
and you can also use the PACT Junit DSL. For simplicity we will use the JUnit Rule but be aware that the Junit Pact DSL
allows you to check response's attributes with matchers in a more accurate way. More information in
Comparing with Basic Junit and Junit Rule usage, the DSL provides the ability to create multiple Pact files
in one test class.



## 3. Running the tests in the consumer side

Both the consumer and provider can be built and tested with maven. Remember, these are simple JUnit tests, so to run
them in in the console execute in the /contractTestingExample/phonebook-consumer folder:

`mvn clean install`

What has happened now? A couple of things:
1. The PhonebookConsumerTest has been executed successfully (with exit code 0)
2. This has generated a pact file in /target/pacts/phonebook-consumer-phonebook-provider.json which is the contract
between the consumer and the provider:

```json
{
    "provider": {
        "name": "phonebook-provider"
    },
    "consumer": {
        "name": "phonebook-consumer"
    },
    "interactions": [
        {
            "description": "A contact request for mum",
            "request": {
                "method": "GET",
                "path": "/mum"
            },
            "response": {
                "status": 200,
                "headers": {
                    "Content-Type": "text/plain;charset=UTF-8"
                },
                "body": "684088275",
                "matchingRules": {
                    "header": {
                        "Content-Type": {
                            "matchers": [
                                {
                                    "match": "regex",
                                    "regex": "text/plain;charset=UTF-8"
                                }
                            ],
                            "combine": "AND"
                        }
                    }
                }
            }
        }
    ],
    "metadata": {
        "pact-specification": {
            "version": "3.0.0"
        },
        "pact-jvm": {
            "version": "3.5.13"
        }
    }
}
```

This Pact file is what will be used by the provider later on to replay the requests and compare if what it is returning
matches with the consumer expectations.

## 4. Running the tests in the provider side

Oh wait! We have not talked about the PhonebookProviderPactTest. This is how it looks like:

```java
@RunWith(SpringRestPactRunner.class)
@Provider("phonebook-provider")
@PactFolder("../phonebook-consumer/target/pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = PhonebookController.class)
public class PhonebookProviderPactTest {

    @TestTarget
    public final Target target = new HttpTarget(8080);

}
```

What this is really doing is replaying the interactions that are described in the pact file and comparing them with
the expected response of the pact file. It's like going throught the interactions twice, from the consumer point of view
and to proove that it matches with what the provider is offering. This can be achieved thanks to the SprintRestPactRunner
that we use with @RunWith.

@PactFolder is where we define where the pact file of the consumer lives.

Just like with normal tests, you can execute this command in your console to run the tests in a clean environment
in the /contractTestingExample/phonebook-provider folder:

`mvn clean install`

See how it looks like in the console:

```commandline
Verifying a pact between phonebook-consumer and phonebook-provider
  A contact request for mum
2019-03-26 09:57:59.395  INFO 15149 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring FrameworkServlet 'dispatcherServlet'
2019-03-26 09:57:59.395  INFO 15149 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : FrameworkServlet 'dispatcherServlet': initialization started
2019-03-26 09:57:59.417  INFO 15149 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : FrameworkServlet 'dispatcherServlet': initialization completed in 22 ms
    returns a response which
      has status code 200 (OK)
      includes headers
        "Content-Type" with value "text/plain;charset=UTF-8" (OK)
      has a matching body (OK)
Results :

Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

## 5. Breaking changes

So far we have not seen the good on writing consumer driven contract tests. The promise is that, unlike normal unit tests,
this contract tests will prevent a provider for making a breaking change. How does it work?

Imagine that the provider is introducing a new feature: support for international phone numbers. Instead of returning
**684088275** it will start returning **+34684088275**.

```java
public class PhonebookController {

    private final Map<String, String> phoneDirectory;

    public PhonebookController() {
        phoneDirectory = new HashMap<>();
        phoneDirectory.put("mum", "+34684088275");
    }
```

But the consumer expects something different, let's see what happens when running the tests on the provider side
after making that change:


```commandline
Verifying a pact between phonebook-consumer and phonebook-provider
  A contact request for mum
2019-03-26 09:56:26.962  INFO 15105 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring FrameworkServlet 'dispatcherServlet'
2019-03-26 09:56:26.962  INFO 15105 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : FrameworkServlet 'dispatcherServlet': initialization started
2019-03-26 09:56:26.983  INFO 15105 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : FrameworkServlet 'dispatcherServlet': initialization completed in 21 ms
    returns a response which
      has status code 200 (OK)
      includes headers
        "Content-Type" with value "text/plain;charset=UTF-8" (OK)
      has a matching body (FAILED)

Failures:

0) A contact request for mum returns a response which has a matching body
      / -> Expected body '684088275' to match '+34684088275' using equality but did not match


java.lang.AssertionError:
0 - / -> [{mismatch=Expected body '684088275' to match '+34684088275' using equality but did not match, diff=}]
```

Which is exactly the expected error at origin.
