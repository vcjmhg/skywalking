package test.apache.skywalking.testcase.zeebe.client.job;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class FirstJobHandler implements JobHandler {

    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
        //mock rpc
        mockCrossThreadRPC(client, job);
//        mockCrossProgremRPC();
    }

    private void mockCrossThreadRPC(final JobClient client, final ActivatedJob job) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.getForEntity(
            "http://localhost:8080/zeebe-client-1.x-scenario/hello", String.class);

        System.out.println(result.getBody());
        System.out.println(job);
        client.newCompleteCommand(job.getKey()).send().join();
        JobWorkerCreator.setStop(true);
    }

    public void mockCrossProgremRPC() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://test.api.s.woa.com/public-service-id-generator/generateId";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"key\":\"zeebe-plugin-scenario\"}", headers);
        ResponseEntity<String> result = restTemplate.postForEntity(url, request, String.class);
        System.out.println(result);
    }
}