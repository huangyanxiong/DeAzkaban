package azkaban.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * date format
 * <p>
 * by dataeye
 */
public class DateFormatLookup extends StrLookup {
    @Override
    public String lookup(String key) {
        Preconditions.checkNotNull(key, "The input can't be null");

        return calculate(key);
    }

    private static String calculate(String template) {
        List<String> fields = split(template);
        String year, month, day, hour, min;
        String yearTemplate = null, monthTemplate = null,
                dayTemplate = null, hourTemplate = null, minTemplate = null;

        DateTime now = new DateTime();

        for (String field : fields) {
            String originalField = field;
            if (field.startsWith("(") && field.endsWith(")")) {
                field = field.substring(1, field.length() - 1);
            }

            String[] itemFields = field.split("[-|+]");

            String dateField = itemFields[0];
            Integer count = itemFields.length == 2 ? Integer.valueOf(itemFields[1]) : 0;
            if (field.indexOf("-") >= 0) {
                count = -count;
            }
            if (dateField.equals("YYYY")) {
                now = now.plusYears(count);
                yearTemplate = originalField;
            } else if (dateField.equals("MM")) {
                now = now.plusMonths(count);
                monthTemplate = originalField;
            } else if (dateField.equals("DD")) {
                now = now.plusDays(count);
                dayTemplate = originalField;
            } else if (dateField.equals("HH")) {
                now = now.plusHours(count);
                hourTemplate = originalField;
            } else if (dateField.equals("mm")) {
                now = now.plusMinutes(count);
                minTemplate = originalField;
            } else {
                throw new RuntimeException("The format:DD:" + field + " not support yet!");
            }
            year = now.yearOfEra().getAsString();
            month = paddingZero(now.monthOfYear().getAsString(), 2);
            day = paddingZero(now.dayOfMonth().getAsString(), 2);
            hour = paddingZero(now.hourOfDay().getAsString(), 2);
            min = paddingZero(now.minuteOfHour().getAsString(), 2);


            if (StringUtils.isNotBlank(yearTemplate)) {
                template = template.replace(yearTemplate, year);
            }
            if (StringUtils.isNotBlank(monthTemplate)) {
                template = template.replace(monthTemplate, month);
            }
            if (StringUtils.isNotBlank(dayTemplate)) {
                template = template.replace(dayTemplate, day);

            }
            if (StringUtils.isNotBlank(hourTemplate)) {
                template = template.replace(hourTemplate, hour);

            }
            if (StringUtils.isNotBlank(minTemplate)) {
                template = template.replace(minTemplate, min);

            }
        }

        return template;
    }

    public static String paddingZero(String input, int length) {
        if (input.length() < length) {
            List<String> elements = new LinkedList<String>();
            for (int i = 0; i < length - input.length(); i++) {
                elements.add("0");
            }
            elements.add(input);

            return Joiner.on("").join(elements).toString();
        }
        return input;
    }

    private static List<String> split(String template) {
        List<String> fields = new LinkedList<String>();

        StringBuffer buffer = new StringBuffer();

        int initIndex = 0;
        while (initIndex < template.length()) {
            for (int index = initIndex; index < template.length(); index++) {
                buffer.append(template.charAt(index));

                if (isValidatePattern(buffer.toString())) {
                    fields.add(buffer.toString());

                    buffer = new StringBuffer();
                    initIndex = index + 1;
                }
            }
            buffer = new StringBuffer();
            initIndex = initIndex + 1;
        }
        if (fields.size() == 0) {
            throw new RuntimeException("The format:DD:" + template + " not support yet!");
        }
        return fields;
    }

    private static boolean isValidatePattern(String input) {
        boolean match = Pattern.compile(
                "\\(YYYY[-+0-9]*\\)|" +
                        "\\(MM[-+0-9]*\\)|" +
                        "\\(DD[-+0-9]*\\)|" +
                        "\\(HH[-+0-9]*\\)|" +
                        "\\(mm[-+0-9]*\\)")
                .matcher(input).matches();
        if (match) {
            return true;
        }

        return false;
    }
}