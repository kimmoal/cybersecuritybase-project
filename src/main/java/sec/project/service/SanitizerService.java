package sec.project.service;

import org.springframework.stereotype.Service;
import sec.project.domain.Account;

import java.io.IOException;

@Service
public class SanitizerService {

    // TODO: check whether we need more sanitization!
    public String sanitizeMessage(String message) {
        message = message.replaceAll("<script>", "");
        message = message.replaceAll("</script>", "");
        message = message.replaceAll("<marquee>", "");
        message = message.replaceAll("<style>", "");
        message = message.replaceAll(".js", "");
        return message;
    }

}
