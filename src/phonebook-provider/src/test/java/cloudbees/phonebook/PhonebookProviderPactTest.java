package cloudbees.phonebook;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.SpringRestPactRunner;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.*;

/**
 * Created by mariaisabelmunozvilacides on 08/03/2018.
 */
@RunWith(SpringRestPactRunner.class)
@Provider("phonebook-provider")
@PactFolder("/Users/mariaisabelmunozvilacides/Code/phonebook/phonebook-consumer/target/pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = PhonebookController.class)
public class PhonebookProviderPactTest {

    @TestTarget
    public final Target target = new HttpTarget(8080);

}