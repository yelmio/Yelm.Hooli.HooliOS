package yelm.io.raccoon.payment.models;

import com.google.gson.annotations.SerializedName;

public class Post3dsRequestArgs {

    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("pares")
    private String paRes;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaRes() {
        return paRes;
    }

    public void setPaRes(String paRes) {
        this.paRes = paRes;
    }
}
