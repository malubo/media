package cz.malubo.media;

import java.util.ArrayList;

public class GpcPayment {
    public String accountNumber;
    public String accountName;
    public String dateOfOldBalance;
    public String oldBalance;
    public String oldBalanceSign;
    public String newBalance;
    public String newBalanceSign;
    public ArrayList<GpcPaymentItem> items = new ArrayList<>();
}

