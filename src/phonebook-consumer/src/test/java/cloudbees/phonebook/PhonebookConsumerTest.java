package cloudbees.phonebook;

import au.com.dius.pact.consumer.*;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import org.apache.http.HttpHeaders;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariaisabelmunozvilacides on 07/03/2018.
 */
public class PhonebookConsumerTest {

    @Rule
    // It spins up the provider in localhost and a random port
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