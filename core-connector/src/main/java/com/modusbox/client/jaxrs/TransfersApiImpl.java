package com.modusbox.client.jaxrs;

import com.modusbox.client.api.TransfersApi;
import com.modusbox.client.model.FulfilNotification;
import com.modusbox.client.model.TransferRequestInbound;
import com.modusbox.client.model.TransferResponseInbound;

import javax.validation.Valid;

public class TransfersApiImpl implements TransfersApi {


    @Override
    public TransferResponseInbound postTransfers(TransferRequestInbound transferRequestInbound) {
        return null;
    }

    @Override
    public TransferResponseInbound putTransfersByTransferId(String transferId, FulfilNotification fulfilNotification) {
        return null;
    }


}
