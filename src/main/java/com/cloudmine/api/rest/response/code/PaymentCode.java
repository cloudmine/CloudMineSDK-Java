package com.cloudmine.api.rest.response.code;

/**
 * <br>Copyright CloudMine LLC. All rights reserved
 * <br> See LICENSE file included with SDK for details.
 */
public enum PaymentCode {
    SUCCESS(200, 299), FAILED(300, 500);

    public static PaymentCode codeForStatus(int status) {
        for(PaymentCode code : PaymentCode.values()) {
            if(code.startCode <= status && status <= code.finishCode) return code;
        }
        return FAILED;
    }

    private final int startCode, finishCode;

    private PaymentCode(int code) {
        this(code, code);
    }
    private PaymentCode(int startCode, int finishCode) {
        this.startCode = startCode;
        this.finishCode = finishCode;
    }
}
