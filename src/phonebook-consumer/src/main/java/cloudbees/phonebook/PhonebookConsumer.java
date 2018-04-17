package cloudbees.phonebook;


import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by mariaisabelmunozvilacides on 07/03/2018.
 */
public class PhonebookConsumer {
    private String url;

    public PhonebookConsumer (String url){

        this.url = url;
    }

    public String getPhoneNumber(String contact) throws IOException {
        Request request = Request.Get(url+"/"+contact);
        Response executeRequest = request.execute();
        HttpResponse response = executeRequest.returnResponse();
        String responseToString = EntityUtils.toString(response.getEntity());
        return responseToString;
    }
}
