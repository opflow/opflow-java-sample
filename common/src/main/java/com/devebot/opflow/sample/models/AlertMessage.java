package com.devebot.opflow.sample.models;

import java.io.Serializable;

/**
 *
 * @author acegik
 */
public class AlertMessage implements Serializable {
    String type;
    String title;
    String body;

    public AlertMessage() {
    }
    
    public AlertMessage(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
