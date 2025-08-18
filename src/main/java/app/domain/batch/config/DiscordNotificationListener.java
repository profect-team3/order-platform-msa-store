package app.domain.batch.config;

import org.bson.internal.BsonUtil;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class DiscordNotificationListener implements JobExecutionListener {

    // private static final String DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/1406861320732540938/105lGe9ox-M6r7kD0LXV4cs7QibndOGJdPV-9YIoWQ8-73sFHJtNXaFxYlor5lUTRw5k";
    @Value("${discord.webhook.url}")
    private String discordWebhookUrl;


    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().name();
        String exitMessage = jobExecution.getExitStatus().getExitDescription();

        String message = String.format(
            "**배치 잡 완료 알림**\n" +
            "**잡 이름**: `%s`\n" +
            "**상태**: `%s`\n" +
            "**종료 메시지**: `%s`\n" +
            "**시작 시간**: `%s`\n" +
            "**종료 시간**: `%s`",
            jobName, status, exitMessage,
            jobExecution.getStartTime(), jobExecution.getEndTime()
        );

        Map<String, String> payload = new HashMap<>();
        payload.put("content", message);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(discordWebhookUrl, payload, String.class);
            System.out.println("Discord 알림 전송 성공: " + message);
        } catch (Exception e) {
            System.err.println("Discord 알림 전송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
