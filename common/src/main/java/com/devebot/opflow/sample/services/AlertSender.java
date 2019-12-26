package com.devebot.opflow.sample.services;

import com.devebot.opflow.annotation.OpflowSourceRoutine;
import com.devebot.opflow.sample.models.AlertMessage;

/**
 *
 * @author acegik
 */
public interface AlertSender {
    @OpflowSourceRoutine(isAsync = true)
    void notify(AlertMessage note);
}
