package interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.dto.UrlDTO;
import interview.rest.CrawlerController;
import interview.service.CrawlerService;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CrawlerControllerTests {

    private static final String INPUT_JSON = "sites.json";

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CrawlerService crawlerService;

    @Autowired
    private CrawlerController crawlerController;

    @Before
    public void setUp() {
        initMocks(this);
        ReflectionTestUtils.setField(crawlerController, "crawlerService", crawlerService);
    }


    @Test
    public void contextLoads() {
    }


    @Test
    public void testCanReceiveJSON() throws Exception {
        // -> Given a list of websites in json


        ObjectMapper mapper = new ObjectMapper();
        UrlDTO[] urls = mapper.readerFor(UrlDTO[].class).readValue(new ClassPathResource(INPUT_JSON).getFile());


        // -> When api receives a list of JSON

        mockMvc.perform(post("/url").contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsBytes(urls)))
                .andExpect(status().isAccepted());


        // -> Then the same list of sites has to be passed to the service

        await().atMost(Duration.ONE_SECOND).until(() -> verify(crawlerService).submitUrls(Stream.of(urls).collect(Collectors.toList())));


    }

}
