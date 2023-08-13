package hexlet.code;

import io.ebean.Database;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;


import io.javalin.Javalin;
import io.ebean.DB;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public final class MainTest {
    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer server;
    private static String mockUrl;



    @BeforeAll
    public static void prepareAll() throws IOException {

        app = App.getApp();
        app.start();
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        Unirest.config().defaultBaseUrl(baseUrl);
        database = DB.getDefault();

        String dummyHtml = Files.readString(Path.of("src/test/resources/dummy.html"));

        server = new MockWebServer();
        server.enqueue(new MockResponse().setBody(dummyHtml));
        server.start();
        mockUrl = server.url("/").toString();

    }

    @BeforeEach
    public void fillDB() {
        // Fill [url] DB with 2 rows [#1 yandex.ru] and [#2 fontanka.ru]
        database.script().run("/seed.sql");
    }

    @AfterEach
    public void revertEach() {
        // DROP DB Tables IF EXISTS
        database.script().run("/clearseed.sql");
    }

    @AfterAll
    public static void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    public void getTests() {

        //Get Main Page
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Анализатор");

        //GET list of URLS (should be 2 rows according to seed.sql)
        response = Unirest.get("/urls").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Последняя");
        assertThat(response.getBody()).contains("yandex");
        assertThat(response.getBody()).contains("fontanka");

        //GET page of [id=1] should be [yandex.ru]
        response = Unirest.get("/urls/1").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("yandex.ru");

        //GET page of [id=3] should be error as only 2 rows were added be seed.sql
        response = Unirest.get("/urls/3").asString();
        assertThat(response.getStatus()).isEqualTo(404);

    }

    @Test
    public void addNewUrlTest() {
        HttpResponse response = Unirest.post("/urls")
                .field("url", "http://rambler.ru")
                .asEmpty();

        HttpResponse<String> getResponse = Unirest.get("/urls").asString();
        assertThat(getResponse.getBody()).contains("rambler.ru");
        assertThat(getResponse.getStatus()).isEqualTo(200);

        HttpResponse<String> getResponse2 = Unirest.get("/urls/3").asString();
        assertThat(getResponse2.getStatus()).isEqualTo(200);
        assertThat(getResponse2.getBody()).contains("rambler.ru");
    }

    @Test
    public void checksPostTest() {

        //Adding mockedURL to list before check
        HttpResponse response = Unirest.post("/urls")
                .field("url", mockUrl)
                .asEmpty();
        response = Unirest.post("/urls/3/checks")
                .asEmpty();
        HttpResponse<String> getResponse = Unirest.get("/urls/3").asString();
        assertThat(getResponse.getBody()).contains("dummy h1");
        assertThat(getResponse.getBody()).contains("dummy description");
    }
}
