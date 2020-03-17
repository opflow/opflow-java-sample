package com.devebot.opflow.sample.services;

import com.devebot.opflow.supports.OpflowJsonTool;
import com.devebot.opflow.sample.models.AlertMessage;

/**
 *
 * @author acegik
 */
public class AlertSenderImpl implements AlertSender {

    @Override
    public void notify(AlertMessage note) {
        System.out.println("[-] AlertMessage: " + OpflowJsonTool.toString(note));
    }
}
