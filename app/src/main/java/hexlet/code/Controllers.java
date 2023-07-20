package hexlet.code;

import hexlet.code.domain.Url;
import io.javalin.http.Handler;
import hexlet.code.domain.query.QUrl;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Controllers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controllers.class);

    public static Handler addUrl = ctx -> {

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

        if (ifExists(urlToAdd)) {
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
        LOGGER.info("Controllers.printUrls entered");

        List<Url> printList = new QUrl()
                .id.isNotNull()
                .findList();

        ctx.sessionAttribute("urls", printList);

        ctx.render("urls.html");


    };

    public static Handler showId = ctx -> {
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

    private static boolean ifExists(String urlToAdd) {
        LOGGER.info("Controllers.ifExists: Check if exists '{}'", urlToAdd);
        boolean ifExists = new QUrl()
                .name.equalTo(urlToAdd)
                .exists();

        LOGGER.info("ifExists result: {}", ifExists);

        return ifExists;
    }
}
