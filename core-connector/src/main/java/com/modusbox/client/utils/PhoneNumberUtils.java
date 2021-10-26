package com.modusbox.client.utils;

public class PhoneNumberUtils {

    public static boolean isPhoneNumberMatch(String walletPhoneNumber, String mfiPhoneNumber) {
        return walletPhoneNumber.equals(mfiPhoneNumber) ||
                walletPhoneNumber.substring(1).equals(mfiPhoneNumber) ||
                walletPhoneNumber.substring(2).equals(mfiPhoneNumber);
    }

    public static String stripCode(String number) {

        if(number.contains(" ")) {
            number = number.replaceAll(" ","");
        }
        if(number.startsWith("+")) {
            number = number.substring(1);
        }
        if(number.startsWith("95")) {
            number = number.substring(2);
        }
        if(number.startsWith("9")) {
            number = "0" + number;
        }

        return number;
    }
}
