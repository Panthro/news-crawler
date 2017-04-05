package interview.rest;

import interview.dto.UrlDTO;
import interview.service.CrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Rafael Roman on 4/5/17.
 */
@RestController
@RequestMapping("/url")
@Slf4j
public class CrawlerController {

    private final CrawlerService crawlerService;

    @Autowired
    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }


    @PostMapping
    public ResponseEntity createUrls(@RequestBody List<UrlDTO> urls) {
        log.info("Received request to process {} urls", urls.size());
        crawlerService.submitUrls(urls);
        return ResponseEntity.accepted().build();
    }

}
