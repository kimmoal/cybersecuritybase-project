package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Account;
import sec.project.repository.AccountRepository;
import sec.project.repository.MessageRepository;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@Controller
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private HttpSession session;

    @RequestMapping("*")
    public String defaultMapping() {
        return "redirect:/accounts";
    }

    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public String index(Principal principal, Model model) {
        model.addAttribute("owner", accountRepository.findByUsername(principal.getName()));
        model.addAttribute("accounts", accountRepository.findAll());
        return "accounts";
    }

    @RequestMapping(value = "/account/{user}", method = RequestMethod.GET)
    public String userAccount(Principal principal, Model model, @PathVariable String user) {
        String username = principal.getName();
        Account owner = accountRepository.findByUsername(username);

        model.addAttribute("canEdit", false);
        if (username.equals(user)) {
            model.addAttribute("canEdit", true);
        }

        Account acc = accountRepository.findByUsername(user);

        if (acc == null) {
            return "redirect:/";
        }

        model.addAttribute("account", acc);
        model.addAttribute("owner", owner);
        model.addAttribute("messages", messageRepository.findByReceiver(acc));
        return "account";
    }

    @RequestMapping(value = "/account/{user}", method = RequestMethod.POST)
    public String userSettings(@RequestParam(required = false) boolean visible, @RequestParam String name, @PathVariable String user) {
        Account acc = accountRepository.findByUsername(user);
        acc.setName(name);
        acc.setVisible(visible);
        accountRepository.save(acc);

        return "redirect:/accounts";
    }

}
