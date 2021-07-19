package com.modusbox.client.jaxrs;

import com.modusbox.client.api.SendmoneyApi;
//import com.modusbox.client.model.SendmoneyRequest;
import com.modusbox.client.model.TransferContinuationAccept;
import com.modusbox.client.model.TransferResponse;

public class SendmoneyApiImpl implements SendmoneyApi {

//    @Override
//    public TransferResponse postSendmoney(TransferRequest transferRequest) {
//        return null;
//    }

    @Override
    public TransferResponse postSendmoney() {
        return null;
    }

    @Override
    public TransferResponse putSendmoneyById(String transferId, TransferContinuationAccept transferContinuationAccept) {
        return null;
    }
}
