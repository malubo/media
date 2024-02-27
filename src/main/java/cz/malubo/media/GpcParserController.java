package cz.malubo.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GpcParserController {

    private GpcParserService gpcParserService;

    @Autowired
    public void setGpcParserService(GpcParserService gpcParserService) {
        this.gpcParserService = gpcParserService;
    }

    @GetMapping("/gpcParser/parseUrl")
    public GpcPayment parseUrl(@RequestParam String url) {
        try {
            return gpcParserService.parseUrl(url);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "GPC Payment could not be parsed", e);
        }
    }

}