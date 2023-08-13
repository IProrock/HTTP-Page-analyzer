package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import hexlet.code.domain.query.QUrl;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    //POST [/urls]
    public static Handler addUrl = ctx -> {


        String urlString = ctx.formParam("url");
        URL urlInstance = null;

        try {
            LOGGER.info("try to parse url string {}", urlString);
            urlInstance = new URL(urlString);

        } catch (Exception e) {
            LOGGER.error("Could not parse URL String: {}", urlString);
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect("/");
            return;
        }

        String urlToAdd = urlInstance.getProtocol() + "://" + urlInstance.getAuthority();

        LOGGER.info("Check if URL already exists {}", urlToAdd);

        if (isAlreadyExists(urlToAdd)) {
            LOGGER.info("URL Already exists: '{}'", urlToAdd);
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect("/");
            return;
        }

        LOGGER.info("URL is new, will try to add to DB: {}", urlToAdd);
        Url nurl = new Url(urlToAdd);
        nurl.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flashType", "success");

        ctx.redirect("/urls");
    };

    //GET [/urls]
    public static Handler printUrls = ctx -> {

        LOGGER.info("UrlController.printUrls entered");

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .id.isNotNull()
                .findPagedList();

        List<Url> printList = pagedUrls.getList();

        LOGGER.info("UrlController.printURLS url list:\n" + printList.toString());

        Map<Long, UrlCheck> urlChecks = new QUrlCheck()
                .url.id.asMapKey()
                        .orderBy().createdAt.desc()
                        .findMap();

        LOGGER.info("UrlController.printURLS urlChecks Map:\n" + urlChecks.toString());

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", printList);
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("pages", pages);

        LOGGER.info("UrlController.printURLS current page: " + currentPage);
        ctx.attribute("currentPage", currentPage);


        ctx.render("urls.html");
    };

    //GET [/urls/{id}]
    public static Handler showId = ctx -> {

        LOGGER.info("Controllers.showId entered");

        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url urlId = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (urlId == null) {
            throw new NotFoundResponse();
        }

        ctx.sessionAttribute("url", urlId);
        ctx.render("showId.html");
    };

    //POST [/urls/{id}/checks]
    public static Handler urlCheckRequest = ctx -> {

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

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flashType", "success");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flashType", "danger");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flashType", "danger");
        }


        ctx.redirect("/urls/" + idUrl);

    };

    private static boolean isAlreadyExists(String urlToAdd) {
        LOGGER.info("Controllers.ifAlreadyExists: Check if exists '{}'", urlToAdd);
        boolean isExists = new QUrl()
                .name.equalTo(urlToAdd)
                .exists();

        LOGGER.info("isExists result: {}", isExists);

        return isExists;
    }
}
