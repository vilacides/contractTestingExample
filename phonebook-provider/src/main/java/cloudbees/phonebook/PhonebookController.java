package cloudbees.phonebook;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariaisabelmunozvilacides on 08/03/2018.
 */

@Controller
@EnableAutoConfiguration
public class PhonebookController {

    private final Map<String, String> phoneDirectory;

    public PhonebookController() {
        phoneDirectory = new HashMap<>();
        phoneDirectory.put("mum", "684088275");
    }

    @RequestMapping("/{name}")
    @ResponseBody
    String giveAPhone(@PathVariable String name) {
        return phoneDirectory.get(name);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(PhonebookController.class, args);
    }
}
