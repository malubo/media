package cz.malubo.media;

public record GpcPaymentItem(
        String assignedAccountNumber,
        String accountNumber,
        String itemNumber,
        String amount,
        String accountingCode
) {}