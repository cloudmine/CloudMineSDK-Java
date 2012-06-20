package com.cloudmine.api.rest;

import com.cloudmine.api.exceptions.JsonConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * DateFormat for converting json dates to java Dates
 * Copyright CloudMine LLC
 */
public class CMDateFormat extends DateFormat {

    private static final Logger LOG = LoggerFactory.getLogger(CMDateFormat.class);

    /**
     * Converts a Number of seconds since the unix epoch to a Java date
     * @param toConvertInSeconds seconds since the unix epoch
     * @return java date
     */
    public static Date fromNumber(Number toConvertInSeconds) {
        return new Date(toConvertInSeconds.longValue() * 1000);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        String formattedDate = JsonUtilities.convertDateToJsonClass(date);
        stringBuffer.append(formattedDate);
        return stringBuffer;
    }

    @Override
    public Date parse(String s, ParsePosition parsePosition) {
        String dateString = s.substring(parsePosition.getIndex());
        Map<String, Object> asMap = null;
        try {
            asMap = JsonUtilities.jsonToMap(dateString);
        } catch (JsonConversionException e) {
            LOG.error("Unable to parse string as json");
            asMap = Collections.emptyMap();
        }

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

            int newPosition = parsePosition.getIndex() + dateString.length();
            parsePosition.setIndex(newPosition);
            return fromNumber(timeInSeconds);
        }catch(NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Object clone() {
        return new CMDateFormat();
    }
}
