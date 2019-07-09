package com.example.kaixing.sipp2pdemo;

// the interface between TextClientConsole and SipLayerFacade
public interface SipMessageListener {
    public void processReceivedMessage(String sender, String message);
    public void processError(String errorMessage);
    public void processInfo(String infoMessage);
}
