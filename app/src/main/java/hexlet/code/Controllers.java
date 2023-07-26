package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import io.javalin.http.Handler;
import hexlet.code.domain.query.QUrl;
import java.net.URL;
import java.util.List;


import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
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
        String urlName = urlToCheck.getName();
        List<UrlCheck> listOfChecks = urlToCheck.getUrlChecks();

        try {
            HttpResponse<String> response = Unirest.get(urlToCheck.getName()).asString();
            int statusCode = response.getStatus();
            UrlCheck urlCheckAdd = new UrlCheck(statusCode, "Title", "h1", "Desc", urlToCheck);
            urlCheckAdd.save();

        } catch (Exception e) {
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
