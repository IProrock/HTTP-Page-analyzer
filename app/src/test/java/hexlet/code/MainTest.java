package hexlet.code;

import io.ebean.Database;
import io.ebean.Transaction;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;


import io.javalin.Javalin;
import io.ebean.DB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

public class MainTest {
    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static Transaction transaction;

    @BeforeAll
    public static void prepareAll() {
        app = App.getApp();
        app.start();
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        Unirest.config().defaultBaseUrl(baseUrl);
        database = DB.getDefault();

    }

    @BeforeEach
    public void prepareEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    public void revertEach() {
        transaction.rollback();
    }



    @Test
    public void getTests() {

        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Анализатор");

        response = Unirest.get("/urls").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Последняя");

        response = Unirest.get("/urls/1").asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("yandexseed.ru");

        response = Unirest.get("/urls/4").asString();
        assertThat(response.getStatus()).isEqualTo(400);

    }

    @Test
    public void postTest() {
        HttpResponse response = Unirest.post("/urls")
                .field("url", "http://fontanka.ru")
                .asEmpty();

        HttpResponse<String> getResponse = Unirest.get("/urls").asString();
        assertThat(getResponse.getBody()).contains("fontanka.ru");

        HttpResponse<String> getResponse2 = Unirest.get("/urls/3").asString();
        assertThat(getResponse2.getStatus()).isEqualTo(200);
        assertThat(getResponse2.getBody()).contains("fontanka.ru");
    }
}
