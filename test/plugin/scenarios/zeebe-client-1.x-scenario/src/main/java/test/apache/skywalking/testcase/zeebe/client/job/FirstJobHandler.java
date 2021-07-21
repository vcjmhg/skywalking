package test.apache.skywalking.testcase.zeebe.client.job;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class FirstJobHandler implements JobHandler {

    @Override
    public void handle(final JobClient client, final ActivatedJob job) {
        //mock rpc
        mockCrossThreadRPC(client, job);
        //get process variable
        log.info(job.getVariablesAsMap().get("key1").toString());
    }

    private void mockCrossThreadRPC(final JobClient client, final ActivatedJob job) {
        final String rpcUrl = "http://localhost:8080/mock_rpc";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.getForEntity(rpcUrl, String.class);

        log.info(result.getBody());
        log.info(job.toString());
        client.newCompleteCommand(job.getKey()).send().join();
        JobWorkerCreator.setStop(true);
    }
}