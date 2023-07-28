package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import io.javalin.http.Handler;
import hexlet.code.domain.query.QUrl;
import java.net.URL;
import java.util.List;


import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controllers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controllers.class);

    public static Handler addUrl = ctx -> {
        //POST [/urls]

        String urlString = ctx.formParam("url");
        URL urlInstance = null;
        String urlToAdd = null;

        try {
            LOGGER.info("try to parse url string {}", urlString);
            urlInstance = new URL(urlString);
            urlToAdd = urlInstance.getProtocol() + "://" + urlInstance.getAuthority();
        } catch (Exception e) {
            LOGGER.error("Could not parse URL String: {}", urlString);
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.redirect("/");
            return;
        }

        LOGGER.info("Check if URL already exists {}", urlToAdd);

        if (ifAlreadyExists(urlToAdd)) {
            LOGGER.info("URL Already exists: '{}'", urlToAdd);
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.redirect("/");
            return;
        }

        LOGGER.info("URL is new, will try to add to DB: {}", urlToAdd);
        Url nurl = new Url(urlToAdd);
        nurl.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");

        ctx.redirect("/urls");
    };

    public static Handler printUrls = ctx -> {
        //GET [/urls]
        LOGGER.info("Controllers.printUrls entered");

        List<Url> printList = new QUrl()
                .id.isNotNull()
                .findList();

        ctx.sessionAttribute("urls", printList);

        ctx.render("urls.html");
    };

    public static Handler showId = ctx -> {
        //GET [/urls/{id}]
        LOGGER.info("Controllers.showId entered");

        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url urlId = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (urlId == null) {
            ctx.status(400);
            ctx.result("URL is not correct, id: '" + id.toString() + "' does not exist");
            return;
        }

        ctx.sessionAttribute("url", urlId);
        ctx.render("showId.html");
    };

    public static Handler urlCheckRequest = ctx -> {
        //POST [/urls/{id}/checks]
        LOGGER.info("Controllers.urlCheckRequest entered");

        long idUrl = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url urlToCheck = new QUrl()
                .id.equalTo(idUrl)
                .findOne();


        try {
            LOGGER.info("Controllers.urlChechReques - HTTP request by Unirest started");
            HttpResponse<String> response = Unirest.get(urlToCheck.getName()).asString();
            int statusCode = response.getStatus();
            String body = response.getBody();
            Document doc = Jsoup.parse(body);

            String title = doc.title();
            LOGGER.info("Controllers.urlCheckRequest - parsed title: " + title);

            Element h1Elm = doc.select("h1").first();
            String h1str = (h1Elm == null ? "h1 tag missing" : h1Elm.text());
            LOGGER.info("Controllers.urlCheckRequest - parsed h1 tag: " + h1str);

            Element descElm = doc.select("meta[name=description]").first();
            String descStr = (descElm == null ? "No description" : descElm.attr("content"));
            LOGGER.info("Controllers.urlCheckRequest - parsed description: " + descStr);

            UrlCheck urlCheckAdd = new UrlCheck(statusCode, title, h1str, descStr, urlToCheck);
            urlCheckAdd.save();

        } catch (Exception e) {
            LOGGER.info("Controllers.urlCheckRequest - Exception catched: " + e.toString());
            ctx.sessionAttribute("flash", "Не удалось проверить страницу, проверьте правильность URL");
        }


        ctx.redirect("/urls/" + idUrl);

    };

    private static boolean ifAlreadyExists(String urlToAdd) {
        LOGGER.info("Controllers.ifAlreadyExists: Check if exists '{}'", urlToAdd);
        boolean ifExists = new QUrl()
                .name.equalTo(urlToAdd)
                .exists();

        LOGGER.info("ifExists result: {}", ifExists);

        return ifExists;
    }
}
