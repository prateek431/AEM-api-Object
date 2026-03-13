package com.prateek.core.service;

import com.google.gson.JsonObject;
import com.prateek.core.service.ArticleToArchive;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

@Component(service = ArticleToArchive.class)
public class ArticleToArchiveServiceImpl implements ArticleToArchive {

    private static final Logger LOG = LoggerFactory.getLogger(ArticleToArchiveServiceImpl.class);

    private static final String NEWS_PATH = "/content/newshub/en/news";
    private static final String ARCHIVE_PATH = "/content/newshub/en/archive";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public JsonObject moveArticle(String articlePath) {
        JsonObject response = new JsonObject();

        if (articlePath == null || articlePath.isEmpty()) {
            response.addProperty("success", false);
            response.addProperty("message", "articlePath is required");
            return response;
        }
        if (!articlePath.startsWith(NEWS_PATH)) {
            response.addProperty("success", false);
            response.addProperty("message", "Invalid article path. Must start with " + NEWS_PATH);
            return response;
        }

        Map<String, Object> params = new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE, "content-writer");

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(params)) {

            PageManager pageManager = resolver.adaptTo(PageManager.class);
            if (pageManager == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Could not obtain PageManager");
                return response;
            }

            Page articlePage = pageManager.getPage(articlePath);
            if (articlePage == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Article not found at: " + articlePath);
                return response;
            }

            String articleName    = articlePage.getName();
            String destinationPath = ARCHIVE_PATH + "/" + articleName;
            Session session = resolver.adaptTo(Session.class);
            if (session == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Could not obtain JCR Session");
                return response;
            }

            session.move(articlePath, destinationPath);
            session.save();

            response.addProperty("success", true);
            response.addProperty("message", "Article archived successfully");
            response.addProperty("sourcePath", articlePath);
            response.addProperty("destinationPath", destinationPath);

        } catch (Exception e) {
            LOG.error("Error archiving article at path: {}", articlePath, e);
            response.addProperty("success", false);
            response.addProperty("message", "Error while archiving article: " + e.getMessage());
        }

        return response;
    }
}

