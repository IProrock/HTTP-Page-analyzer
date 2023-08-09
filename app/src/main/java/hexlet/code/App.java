package hexlet.code;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import org.thymeleaf.TemplateEngine;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        LOGGER.info("App.main entered");
        int port = getPort();
        LOGGER.info("App.main port received: " + port);
        Javalin app = getApp();

        app.start(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    public static int getPort() {
        LOGGER.info("App.getPort entered");
        String port = System.getenv().getOrDefault("PORT", "8000");
        return Integer.valueOf(port);
    }

    private static boolean isProduction() {
        LOGGER.info("App.isProduction entered");
        return getMode().equals("production");
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.plugins.enableDevLogging();
            }
            config.staticFiles.enableWebjars();
            JavalinThymeleaf.init(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    };

    private static void addRoutes(Javalin app) {
        app.get("/", ctx -> {
            ctx.render("index.html");
        });

        app.get("/urls", UrlController.printUrls);
        app.post("/urls", UrlController.addUrl);
        app.get("/urls/{id}", UrlController.showId);
        app.post("/urls/{id}/checks", UrlController.urlCheckRequest);
    }


    private static TemplateEngine getTemplateEngine() {

        TemplateEngine templateEngine = new TemplateEngine();

        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }
}
