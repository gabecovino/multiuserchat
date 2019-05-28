package com.muc;

public interface MessageListener {
    public void onMessage(String fromLogin, String msgBody);

    void add(MessageListener messageListener);

    void remove(MessageListener messageListener);
}
