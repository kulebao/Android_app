package com.djc.logintest.handler;

import com.djc.logintest.push.info.PushEvent;

public interface PushEventObserver {
    public void handle(PushEvent action);
}
