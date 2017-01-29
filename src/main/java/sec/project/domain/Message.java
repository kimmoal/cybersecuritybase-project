package sec.project.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "MESSAGES")
public class Message extends AbstractPersistable<Long> {

    @ManyToOne
    private Account sender;
    @ManyToOne
    private Account receiver;
    private String content;

    public Message() {
        super();
    }

    public Message(Account sender, Account receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = message;
    }

    public Account getSender() {
        return sender;
    }

    public void setSender(Account sender) {
        this.sender = sender;
    }

    public Account getReceiver() {
        return receiver;
    }

    public void setReceiver(Account receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
