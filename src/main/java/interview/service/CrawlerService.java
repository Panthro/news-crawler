package interview.service;

import interview.domain.UrlEntry;
import interview.dto.UrlDTO;
import interview.repository.UrlEntryRepository;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Rafael Roman on 4/5/17.
 */
@Component
@Slf4j
public class CrawlerService {


    @Value("${crawler.matcher.selector}")
    private String selector;

    @Value("${crawler.matcher.pattern}")
    private String patternString;

    private Pattern pattern;

    private final UrlEntryRepository repository;

    @Autowired
    public CrawlerService(UrlEntryRepository repository) {
        this.repository = repository;
    }


    @PostConstruct
    private void init() {
        // to make sure any combination is case matched so the regex can be less complex
        pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
    }

    /**
     * TODO this can be easily enhanced with MapStruct
     * Builds a UrlEntry based on the UrlDTO
     *
     * @param urlDTO the DTO that represents the build
     * @return the built Entry
     */
    private UrlEntry buildUrlEntry(UrlDTO urlDTO) {
        return UrlEntry.builder().url(urlDTO.getUrl()).build();
    }

    /**
     * Submit a list of {@link UrlDTO} to be processed
     *
     * @param urls the list of {@link UrlDTO}
     */
    @Async // -> To show use of Async...
    public void submitUrls(List<UrlDTO> urls) {
        log.info("Received {} urls to crawl", urls.size());
        Flowable.fromIterable(urls)
                .observeOn(Schedulers.io()) // TODO ideally this should be parametrized to grow or shrink based o the capability of the service/infra
                .map(this::buildUrlEntry)
                .flatMap(entry ->
                        Flowable.just(entry)
                                .subscribeOn(Schedulers.computation())
                                .map(this::crawl)
                ).blockingSubscribe((entry -> log.info("Finished {}", entry)));


    }


    /**
     * Visits the url from the entry and try to determine if the url matches the configured pattern <br />
     * If an error occur, it is logged and saved in the database for further query
     *
     * @param entry the urlEntry to crawl
     * @return the Observable with the result entry
     */
    public UrlEntry crawl(UrlEntry entry) {

        log.info("Start crawler for {}", entry);

        entry.setPatternUsed(patternString);

        try {
            Document doc = Jsoup.connect(normalizeUrl(entry.getUrl())).get();
            Elements matchingElements = doc.select(selector);

            boolean match = matchingElements.stream().anyMatch(element -> {
                boolean matches = element.hasText() && pattern.matcher(element.ownText()).matches();
                if (matches) {
                    entry.setMessage("Matched Content: " + element.ownText());
                }
                return matches;
            });

            entry.setResult(match ? UrlEntry.Result.MATCHES : UrlEntry.Result.NOT_MATCHES);
        } catch (Exception e) {
            entry.setResult(UrlEntry.Result.ERROR);
            entry.setMessage(e.getMessage()); // TODO there should be some standard for error messages
        } finally {
            entry.setCrawledDate(LocalDateTime.now());
            repository.save(entry);
        }

        log.info("Crawl result {}", entry);
        return entry;
    }

    /**
     * Tries to fix malformed urls if possible, currently only protocol is verified as http or https are expected
     *
     * @param url the url to be normalized
     * @return the url if is already OK or the url with "http://" prepended
     */
    private String normalizeUrl(String url) {
        return url.matches(".*://.*$") ? url : "http://" + url;
    }


}
