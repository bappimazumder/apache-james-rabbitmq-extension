package com.mail.ext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.apache.james.core.Username;
import org.apache.james.mailbox.*;
import org.apache.james.mailbox.model.*;

import javax.mail.Flags;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class MailboxActionHandler {

    private final MailboxManager mailboxManager;
    private final MessageIdManager messageIdManager;
    private final Channel publishChannel;
    private final String resultExchange;

    public MailboxActionHandler(MailboxManager mailboxManager, MessageIdManager messageIdManager, Channel publishChannel, String resultExchange) {
        this.mailboxManager = mailboxManager;
        this.messageIdManager = messageIdManager;
        this.publishChannel = publishChannel;
        this.resultExchange = resultExchange;
    }

    public boolean handle(MailboxActionPayload payload) {
        try {
            // MailboxSession session = mailboxManager.createSystemSession("action-handler");
            Username user = Username.of("action-handler");
            MailboxSession session = mailboxManager.createSystemSession(user);
            // MailboxPath sourcePath = MailboxPath.forUser(session.getUser().asString(), payload.getSourceMailboxID());
            MailboxPath sourcePath = MailboxPath.forUser(session.getUser(), payload.getSourceMailboxID());
            MessageId messageId = mailboxManager.getMessageIdFactory().fromString(payload.getSourceMessageID());

            List<MessageResult> messages = messageIdManager.getMessages(
                    List.of(messageId),
                    FetchGroup.FULL_CONTENT,
                    session
            );

            switch (payload.getAction()) {
                case Trash -> {
                   //  MailboxPath trashPath = MailboxPath.forUser(session.getUser().asString(), "Trash");
                    MailboxPath trashPath = MailboxPath.forUser(session.getUser(), "Trash");
                    return moveMessage(session, sourcePath, trashPath, messageId);
                }
                case Move -> {
                    // MailboxPath destPath = MailboxPath.forUser(session.getUser().asString(), payload.getDestinationMailboxID());
                    MailboxPath destPath = MailboxPath.forUser(session.getUser(), payload.getDestinationMailboxID());
                    return moveMessage(session, sourcePath, destPath, messageId);
                }
                default -> throw new IllegalArgumentException("Unknown action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean moveMessage(MailboxSession session, MailboxPath source, MailboxPath dest, MessageId msgId) {
        try {
            MessageManager sourceManager = mailboxManager.getMailbox(source, session);
            MessageManager destManager = mailboxManager.getMailbox(dest, session);
            MessageResult message = sourceManager.getMessages(MessageRange.one(msgId), FetchGroup.MINIMAL, session).next();

            destManager.appendMessage(
                    message.getFullContent().getInputStream(),
                    message.getInternalDate(),
                    session,
                    false,
                    new Flags(Flags.Flag.SEEN)
            );
            sourceManager.deleteMessages(MessageRange.one(msgId), session);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void publishResult(String hashID, boolean success) {
        try {
            Map<String, String> result = Map.of("hashID", hashID, "status", success ? "success" : "failed");
            String json = new ObjectMapper().writeValueAsString(result);
            publishChannel.basicPublish(resultExchange, "", null, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
