package cz.malubo.media;

import java.net.MalformedURLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GpcValidator {

    private static final String URL_REGEX =
            "^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))" +
                    "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)" +
                    "([).!';/?:,][[:blank:]])?$";

    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    public static void validateUrl(String url) throws MalformedURLException {
        if (url == null) {
            return;
        }

        Matcher matcher = URL_PATTERN.matcher(url);

        if (!matcher.matches()) {
            throw new MalformedURLException("Provided URL is not valid");
        }
    }
    
    public static void validateGpcFormat(List<String> gpcMessage) {
        if (gpcMessage.isEmpty()) {
            throw new GpcFormatValidationException("GPC message was empty.");
        }

        for (String line: gpcMessage) {
            if (line == null ||
                line.length() != 130 ||
                (!line.substring(0,3).contains("074") && !line.substring(0,3).contains("075"))
            ) {
                throw new GpcFormatValidationException("Gpc format validation failed for this line: " + line);
            }
        }
    }

}
