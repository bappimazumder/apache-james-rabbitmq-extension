package com.mail.ext;

public class MailboxActionPayload {
    private Action action;
    private String sourceMailboxID;
    private String sourceMessageID;
    private String destinationMailboxID; // Nullable for Trash
    private String hashID;

    // Getters and setters
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public String getSourceMailboxID() { return sourceMailboxID; }
    public void setSourceMailboxID(String sourceMailboxID) { this.sourceMailboxID = sourceMailboxID; }

    public String getSourceMessageID() { return sourceMessageID; }
    public void setSourceMessageID(String sourceMessageID) { this.sourceMessageID = sourceMessageID; }

    public String getDestinationMailboxID() { return destinationMailboxID; }
    public void setDestinationMailboxID(String destinationMailboxID) { this.destinationMailboxID = destinationMailboxID; }

    public String getHashID() { return hashID; }
    public void setHashID(String hashID) { this.hashID = hashID; }
}
