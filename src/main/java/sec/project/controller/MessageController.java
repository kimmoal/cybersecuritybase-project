package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Account;
import sec.project.domain.Message;
import sec.project.repository.AccountRepository;
import sec.project.repository.MessageRepository;
import sec.project.service.SanitizerService;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@Controller
public class MessageController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SanitizerService sanitizer;

    @Autowired
    private HttpSession session;

    @RequestMapping(value = "/message/{user}", method = RequestMethod.GET)
    public String userMessages(Principal principal, Model model, @PathVariable String user) {
        Account account = accountRepository.findByUsername(user);
        Account owner = accountRepository.findByUsername(principal.getName());
        model.addAttribute("account", account);
        model.addAttribute("owner", owner);
        System.out.println("Account username: " + account.getUsername());
        model.addAttribute("messages", messageRepository.findByReceiver(account));
        return "messages";
    }

    @RequestMapping(value = "/message/{user}", method = RequestMethod.POST)
    public String sendMessage(Principal principal, @PathVariable String user, @RequestParam String message) {
        String username = principal.getName();
        Account sender = accountRepository.findByUsername(username);
        Account receiver = accountRepository.findByUsername(user);
        if (receiver == null) {
            return "error";
        }

        message = sanitizer.sanitizeMessage(message);
        Message msg = new Message(sender, receiver, message);

        messageRepository.save(msg);

        return "redirect:/account/" + receiver.getUsername();
    }


}
