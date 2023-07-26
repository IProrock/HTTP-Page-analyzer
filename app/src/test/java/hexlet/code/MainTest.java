package hexlet.code;

import io.ebean.Database;
//import io.ebean.Transaction;
//import io.ebean.annotation.Transactional;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;


import io.javalin.Javalin;
import io.ebean.DB;


import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class MainTest {
    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static MockWebServer server;
    private static Instant today;



    @BeforeAll
    public static void prepareAll() throws IOException {
        app = App.getApp();
        app.start();
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        Unirest.config().defaultBaseUrl(baseUrl);
        database = DB.getDefault();
        server = new MockWebServer();
        server.enqueue(new MockResponse()
                .setBody("mocked yandex.ru"));
        server.start();

        // Fill [url] DB with 2 rows [id=1] and [id=2]
        database.script().run("/seed.sql");

    }

    @AfterEach
    public void revertEach() {
        // Remove all data with [id>2]
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

        //GET page of [id=1] should be [yandex.ru]
        response = Unirest.get("/urls/1").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("yandex.ru");

        //GET page of [id=3] should be error as only 2 rows were added be seed.sql
        response = Unirest.get("/urls/3").asString();
        assertThat(response.getStatus()).isEqualTo(400);

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
        // POST [/urls/1/checks] - should start checking [http://yandex.ru] with mock
        HttpResponse response = Unirest.post("/urls/1/checks")
                .asEmpty();

        //GET [/urls] - list of URL verification. Table will have 2 rows.
        HttpResponse<String> getResponse = Unirest.get("/urls").asString();
        //Checking that [200] is present. (Should be added after POST request)
        assertThat(getResponse.getBody()).contains("200");
        assertThat(getResponse.getBody()).contains("yandex");
        assertThat(getResponse.getStatus()).isEqualTo(200);

        //GET [/urls/1] - personal page for [yandex.ru] record
        getResponse = Unirest.get("/urls/1").asString();
        assertThat(getResponse.getBody()).contains("yandex");
        //Check that [200] is present (should be added after POST request)
        assertThat(getResponse.getBody()).contains("200");
        assertThat(getResponse.getStatus()).isEqualTo(200);

        //GET [/urls/2] - personal page for [fontanka.ru] record
        getResponse = Unirest.get("/urls/2").asString();
        assertThat(getResponse.getBody()).contains("fontanka");
        //Check that page DO NOT contain [200] (POST request was not done)
        assertThat(getResponse.getBody()).doesNotContain("200");
        assertThat(getResponse.getStatus()).isEqualTo(200);
    }
}
