package com.modusbox.client.utils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataFormatUtils {

	public static boolean isJSONValid(String inputData) {
		try {
			new JSONObject(inputData);
		} catch (JSONException ex) {
			try {
				new JSONArray(inputData);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	public static boolean isOnlyDigits (String str)
	{
		String regex = "[0-9]+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		return m.matches();
	}

}
