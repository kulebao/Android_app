package com.cocobabys.handler;

import com.cocobabys.push.info.PushEvent;

public interface PushEventObserver {
    public void handle(PushEvent action);
}
