package yelm.io.yelm.payment;

import android.util.Log;

import java.math.BigDecimal;

import io.reactivex.Observable;
import yelm.io.yelm.loader.controller.LoaderActivity;
import yelm.io.yelm.payment.models.PayRequestArgs;
import yelm.io.yelm.payment.models.Post3dsRequestArgs;
import yelm.io.yelm.payment.models.Transaction;
import yelm.io.yelm.payment.response.PayApiResponse;


public class PayApi {

    private static final String CONTENT_TYPE = "application/json";

    public static Observable<Transaction> charge(String cardCryptogramPacket, String cardHolderName, BigDecimal amount, String order) {

        // Параметры:
        PayRequestArgs args = new PayRequestArgs();
        args.setAmount(amount.toString());  // Сумма платежа (Обязательный)
        args.setCurrency("RUB"); // Валюта (Обязательный)
        args.setName(cardHolderName); // Имя держателя карты в латинице (Обязательный для всех платежей кроме Apple Pay и Google Pay)
        args.setCardCryptogramPacket(cardCryptogramPacket); // Криптограмма платежных данных (Обязательный)

        args.setInvoiceId(""); // Номер счета или заказа в вашей системе (необязательный)
        args.setDescription(""); // Описание оплаты в свободной форме (необязательный)
        args.setAccountId(""); // Идентификатор пользователя в вашей системе (необязательный)
        args.setJsonData(order); // Любые другие данные, которые будут связаны с транзакцией (необязательный)

        return PayApiFactory.getPayMethods()
                .charge(CONTENT_TYPE, args)
                .flatMap(PayApiResponse::handleError)
                .map(transactionPayApiResponse -> transactionPayApiResponse.getData());
    }

    public static Observable<Transaction> auth(String cardCryptogramPacket, String cardHolderName, BigDecimal amount, String order) {

        // Параметры:
        PayRequestArgs args = new PayRequestArgs();
        args.setAmount(amount.toString());  // Сумма платежа (Обязательный)
        args.setCurrency(LoaderActivity.settings.getString(LoaderActivity.CURRENCY, "RUB")); // Валюта (Обязательный)
        args.setName(cardHolderName); // Имя держателя карты в латинице (Обязательный для всех платежей кроме Apple Pay и Google Pay)
        args.setCardCryptogramPacket(cardCryptogramPacket); // Криптограмма платежных данных (Обязательный)

        args.setInvoiceId(""); // Номер счета или заказа в вашей системе (необязательный)
        args.setDescription(""); // Описание оплаты в свободной форме (необязательный)
        args.setAccountId(""); // Идентификатор пользователя в вашей системе (необязательный)
        args.setJsonData(order); // Любые другие данные, которые будут связаны с транзакцией (необязательный)
        Log.d("AlexDebug", "cardCryptogramPacket: " + cardCryptogramPacket);

        return PayApiFactory.getPayMethods()
                .auth(CONTENT_TYPE, args)
                .flatMap(transactionPayApiResponse -> transactionPayApiResponse.handleError())
                .map((PayApiResponse<Transaction> transactionPayApiResponse1) -> {
                    return transactionPayApiResponse1.getData();
                });
    }

    public static Observable<Transaction> post3ds(String transactionId, String paRes) {
        Log.d("AlexDebug", "post3ds");

        Post3dsRequestArgs args = new Post3dsRequestArgs();
        args.setTransactionId(transactionId);
        args.setPaRes(paRes);

        return PayApiFactory.getPayMethods()
                .post3ds(CONTENT_TYPE, args)
                .flatMap(PayApiResponse::handleError)
                .map(PayApiResponse::getData);
    }
}