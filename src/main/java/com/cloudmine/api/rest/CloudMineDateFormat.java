package com.cloudmine.api.rest;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Map;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/31/12, 10:55 AM
 */
public class CloudMineDateFormat extends DateFormat {


    @Override
    public StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        String formattedDate = JsonUtilities.dateToJsonClass(date);
        stringBuffer.append(formattedDate);
        return stringBuffer;
    }

    @Override
    public Date parse(String s, ParsePosition parsePosition) {
        String dateString = s.substring(parsePosition.getIndex());
        Map<String, Object> asMap = JsonUtilities.jsonToMap(dateString);
        asMap.get(JsonUtilities.TIME_KEY);
        boolean isNotDateClass = !JsonUtilities.DATE_CLASS.equals(asMap.get(JsonUtilities.CLASS_KEY));
        if(isNotDateClass) {
            return null;
        }
        Object time = asMap.get(JsonUtilities.TIME_KEY);
        if(time == null) {
            return null;
        }
        try {
            Long timeInSeconds = Long.parseLong(time.toString());
            long timeInMillis = timeInSeconds * 1000;

            int newPosition = parsePosition.getIndex() + dateString.length();
            parsePosition.setIndex(newPosition);
            return new Date(timeInMillis);
        }catch(NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Object clone() {
        return new CloudMineDateFormat();
    }
}
