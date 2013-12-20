package com.cloudmine.api.rest.response;

import com.cloudmine.api.rest.response.code.PaymentCode;
import org.apache.http.HttpResponse;

/**
 * <br>Copyright CloudMine LLC. All rights reserved
 * <br> See LICENSE file included with SDK for details.
 */
public class PaymentResponse extends ResponseBase<PaymentCode> {
    protected PaymentResponse(HttpResponse response) {
        super(response);
    }

    public PaymentResponse(String messageBody, int statusCode) {
        super(messageBody, statusCode);
    }

    @Override
    public PaymentCode getResponseCode() {
        return PaymentCode.codeForStatus(getStatusCode());
    }
}
