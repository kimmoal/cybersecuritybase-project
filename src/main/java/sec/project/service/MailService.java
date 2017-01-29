package sec.project.service;

import org.springframework.stereotype.Service;
import sec.project.domain.Account;

import java.io.IOException;

@Service
public class MailService {

    public void sendMail(Account account) {
        // TODO: send mail to user
        System.out.println("Sent mail to: " + account.getEmail());
        try {
            String[] cmd = {"/bin/sh", "-c", "echo " + account.getEmail()};
            String[] winCmd = new String[] {"cmd.exe", "/c", "echo " + account.getEmail()};

            if (System.getProperty("os.name").contains("Windows")) {
                cmd = winCmd;
            }

            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
