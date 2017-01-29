package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Account;
import sec.project.repository.AccountRepository;
import sec.project.service.MailService;

import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Controller
public class SignupController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpSession session;

    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String loadAccountForm() {
        return "signup";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public String registerAccount(Model model, @RequestParam String username, @RequestParam String password, @RequestParam String address) {
        if (accountRepository.findByUsername(username) != null) {
            return "redirect:/";
        }

        String encoded = passwordEncoder.encode(password);
        String code = generateOTAC(username);
        Account acc = new Account(username, encoded, address);
        acc.setCode(code);
        accountRepository.save(acc);

        System.out.println("One time code: " + code);
        mailService.sendMail(acc);
        return "redirect:/validate/" + username;
    }

    @RequestMapping(value = "/validate/{user}", method = RequestMethod.GET)
    public String validate(Model model, @PathVariable String user) {
        Account acc = accountRepository.findByUsername(user);
        if (acc == null) return "error";

        model.addAttribute("account", acc);
        return "done";
    }

    @RequestMapping(value = "/validate/{user}", method = RequestMethod.POST)
    public String validateCode(Model model, @PathVariable String user, @RequestParam String code, @RequestParam String url) {
        Account account = accountRepository.findByUsername(user);

        if (account == null) {
            System.out.println("Account not found " + user) ;
            return "redirect:/";
        }

        if (!code.equals(account.getCode())) {
            System.out.println("Code did not match " + code + " != " + account.getCode());
            return "redirect:/validate/" + user;
        }

        System.out.println("Account validated: " + account.getUsername());
        account.setValidated(true);

        System.out.println("Redirecting user to " + url);
        return "redirect:" + url;
    }

    public String generateOTAC(String username) {
        byte[] digest = {};
        try {
            MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
            digest = md5.digest(username.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return String.format("%032X", new BigInteger(1, digest)).substring(0, 5);
    }
}
