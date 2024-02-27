package cz.malubo.media;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class GpcParserService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public GpcPayment parseUrl(String url) throws IOException {

        // validate url
        GpcValidator.validateUrl(url);

        Document document = null;
        TextNode textFile = null;

        try {
            Connection connection = Jsoup.connect(url);
            connection.ignoreContentType(true);
            document = connection.get();
        } catch (IOException ioException) {
            log.error("Could not connect to or extract text from given URL");
            throw ioException;
        }

        try {
            textFile = document.select("body").first().textNodes().getFirst();
        } catch (Exception e) {
            throw new GpcFormatValidationException(e.getMessage());
        }

        ArrayList<String> resultByLines = null;

        if (textFile != null) {
            resultByLines = new ArrayList<>(Arrays.stream(textFile.getWholeText().split(System.lineSeparator())).toList());
            for (String s : resultByLines) {
                System.out.println(s);
            }
        }

        // validate gpc format
        GpcValidator.validateGpcFormat(resultByLines);

        // parse the payment information
        GpcPayment parsedResult = parseGpcPaymentFile(resultByLines);

        return parsedResult;
    }

    private GpcPayment parseGpcPaymentFile(ArrayList<String> fileLines) {
        GpcPayment result = new GpcPayment();

        for (String line : fileLines) {
            // 1 - 3
            String operationType = line.substring(0, 3);

            if (operationType.equals("074")) {
                parseDataLine74(result, line);
            } else {
                parseDataLine75(result, line);
            }
        }

        return result;
    }

    private GpcPayment parseDataLine74(GpcPayment gpcPayment, String line) {
        // 4 - 19 přidělené č. účtu s vodícími nulami
        gpcPayment.accountNumber = Utils.removeLeadingZeroes(line.substring(4, 18));

        // 20 - 39 20 alfanumerických znaků zkráceného názvu účtu, doplněných případně mezerami zprava
        gpcPayment.accountName = line.substring(19, 38).trim();

        // 40 - 45 datum starého zůstatku ve formátu DDMMRR
        gpcPayment.dateOfOldBalance = line.substring(39, 44).trim();

        // 46 - 59 starý zůstatek v haléřích 14 numerických znaků s vodícími nulami
        gpcPayment.oldBalance = Utils.removeLeadingZeroes(line.substring(45, 58));

        // 60 znaménko starého zůstatku, 1 znak "+" či "-"
        gpcPayment.oldBalanceSign = String.valueOf(line.charAt(59));

        // 61 - 74 nový zůstatek v haléřích 14 numerických znaků s vodícími nulami
        gpcPayment.newBalance = Utils.removeLeadingZeroes(line.substring(60, 73));

        // 75 znaménko nového zůstatku, 1 znak "+" či "-"
        gpcPayment.newBalanceSign = String.valueOf(line.charAt(74));

        // 76 - 89 obraty debet (MD) v haléřích 14 numerických znaků s vodícími nulami
        // 90 znaménko obratů debet (MD), 1 znak "0" či "-"
        // 91 -104 obraty kredit (D) v haléřích 14 numerických znaků s vodícími nulami
        // 105 znaménko obratů kredit (D), 1 znak "0" či "-"
        // 106-108 pořadové číslo výpisu
        // 109-114 datum účtování ve formátu DDMMRR
        // 115-128 (vyplněno 14 znaky mezera z důvodu sjednocení délky záznamů)
        // 129-130 ukončovací znaky CR a LF
        return gpcPayment;
    }

    private GpcPayment parseDataLine75(GpcPayment gpcPayment, String line) {
        // 4 - 19 přidělené číslo účtu 16 numerických znaků s vodícími nulami
        String assignedAccountNumber = line.substring(3, 18);

        // 20 – 35 číslo účtu 16 numerických znaků s vodícími nulami (případně v pořadí předčíslí + číslo účtu)
        String accountNumber = line.substring(19, 34);

        // 36 – 48 číslo dokladu 13 numerických znaků
        String itemNumber = line.substring(35, 47);

        // 49 – 60 částka v haléřích 12 numerických znaků s vodícími nulami
        String amount = line.substring(48, 59);

        // 61 kód účtování vztažený k číslu účtu:
        /* 1 = položka debet
        2 = položka kredit
        4 = storno položky debet
        5 = storno položky kredit */
        String accountingCode = String.valueOf(line.charAt(60));

        // 62 – 71 variabilní symbol 10 numerických znaků s vodícími nulami
        // 72 – 81 konstantní symbol 10 numerických znaků s vodícími nulami ve tvaru BBBBKSYM, kde:
        /*BBBB = kód banky,
                KSYM = konstantní symbol
        */

        // 82 – 91 specifický symbol 10 numerických znaků s vodícími nulami
        // 92 – 97 "000000" = valuta
        // 98 –117 20 alfanumerických znaků zkráceného názvu klienta, doplněno případně mezerami zprava
        // 118 "0"
        // 119-122 "0203" = kód měny pro Kč
        // 123-128 datum splatnosti ve formátu DDMMRR
        // 129-130 ukončovací znaky CR a

        GpcPaymentItem item = new GpcPaymentItem(
                assignedAccountNumber,
                accountNumber,
                itemNumber,
                amount,
                accountingCode
        );

        gpcPayment.items.add(item);
        return gpcPayment;
    }

}
