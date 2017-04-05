package interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.domain.UrlEntry;
import interview.dto.UrlDTO;
import interview.repository.UrlEntryRepository;
import interview.service.CrawlerService;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static interview.domain.UrlEntry.Result.MATCHES;
import static interview.domain.UrlEntry.Result.NOT_MATCHES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * @author Rafael Roman on 4/5/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CrawlerServiceTests {

    private static final String INPUT_JSON = "sites.json";

    @Autowired
    private CrawlerService crawlerService;

    @Autowired
    private UrlEntryRepository repository;

    @Before
    public void setUp() {
        repository.deleteAll();
    }

    @Test
    public void testSubmitCrawlsAndSaveToDatabase() throws Exception {

        // -> Given a list of urls
        ObjectMapper mapper = new ObjectMapper();
        UrlDTO[] urls = mapper.readerFor(UrlDTO[].class).readValue(new ClassPathResource(INPUT_JSON).getFile());
        // -> When calling the submit in the crawlerService

        crawlerService.submitUrls(Stream.of(urls).collect(Collectors.toList()));


        await().atMost(Duration.TEN_SECONDS).until(() -> {
            List<UrlEntry> urlEntries = repository.findAll();
            // -> Then the url should be created


            assertThat(urlEntries.stream().map(UrlEntry::getUrl).collect(Collectors.toList()))
                    .containsAll(Stream.of(urls).map(UrlDTO::getUrl).collect(Collectors.toList()));

            // -> And status should be populated
            assertThat(urlEntries.stream().map(UrlEntry::getResult).collect(Collectors.toList()))
                    .hasSameSizeAs(urlEntries)
                    .doesNotContainNull();

        });


    }


    @Test
    public void testWebsiteContainingNewsAreMatched() throws Exception {

        // -> Given a url with "news" in any case
        String url = "news.ycombinator.com";

        UrlEntry urlEntry = UrlEntry.builder().url(url).build();

        // -> When the CrawlerService#crawl is called

        crawlerService.crawl(urlEntry);
        // -> Then the entry should be persisted in the database with result MATCHES

        await().atMost(Duration.ONE_SECOND).until(() -> assertThat(urlEntry.getResult()).isEqualTo(MATCHES));


    }

    @Test
    public void testWebsitesNotContainingThePatternAreNotMatched() throws Exception {

        // -> Given a url with "news" in any case
        String url = "google.es";

        UrlEntry urlEntry = UrlEntry.builder().url(url).build();

        // -> When the CrawlerService#crawl is called

        crawlerService.crawl(urlEntry);
        // -> Then the entry should be persisted in the database with result MATCHES

        await().atMost(Duration.ONE_SECOND).until(() -> assertThat(urlEntry.getResult()).isEqualTo(NOT_MATCHES));


    }


    @Test
    public void testWebsiteContainingNoticiasAreMatched() throws Exception {

        // -> Given a url with "news" in any case
        String url = "http://elpais.com/tag/fecha/ultimahora/";

        UrlEntry urlEntry = UrlEntry.builder().url(url).build();

        // -> When the CrawlerService#crawl is called

        crawlerService.crawl(urlEntry);
        // -> Then the entry should be persisted in the database with result MATCHES

        await().atMost(Duration.ONE_SECOND).until(() -> assertThat(urlEntry.getResult()).isEqualTo(MATCHES));


    }


}
