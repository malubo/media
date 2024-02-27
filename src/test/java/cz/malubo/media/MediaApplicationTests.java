package cz.malubo.media;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MediaApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private MockedStatic<Jsoup> mockedJsoup;

    private final String[] testFileNames = {"fio1.gpc", "fio2.gpc", "fio3.gpc", "fio4.gpc", "custom5.gpc"};

    private void mockUrlResource(String fileName) throws Exception {

        if (mockedJsoup != null) {
            mockedJsoup.close();
        }

        // load test file
        Resource resource = new ClassPathResource(fileName);
        Document mockDocument = Jsoup.parse(resource.getFile(), Charset.defaultCharset().name());

        // mock Jsoup and Connection
        mockedJsoup = Mockito.mockStatic(Jsoup.class);
        Connection mockedConnection = Mockito.mock(Connection.class);

        // set up mocked return value for Jsoup.connect('url').get()
        mockedJsoup.when(() -> Jsoup.connect("http://localhost:8080/" + fileName)).thenReturn(mockedConnection);
        Mockito.when(mockedConnection.get()).thenReturn(mockDocument);

    }

    @Test
    void testGpcParserEndpoint() throws Exception {

        // go through all test files, mock them and test the controller method
        for (String fileName : testFileNames) {
            mockUrlResource(fileName);

            // call the controller and check the payment info has been parsed correctly
            this.mockMvc
                    .perform(get("/gpcParser/parseUrl").param("url", "http://localhost:8080/" + fileName)).andDo(print())
                    .andExpect(status().isOk());
        }

    }

}