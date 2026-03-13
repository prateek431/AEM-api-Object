package com.prateek.core.servlets;

import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(
        service = Servlet.class,
        immediate = true,
        property = {
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "sling.servlet.paths=/bin/newshub/search"
        }
)
public class ArticleconfigSearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private QueryBuilder queryBuilder;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("q");

        ResourceResolver resolver = null;

        try {

            Map<String, Object> params = new HashMap<>();
            params.put(ResourceResolverFactory.SUBSERVICE, "content-reader");

            resolver = resolverFactory.getServiceResourceResolver(params);

            Session session = resolver.adaptTo(Session.class);

            Map<String, String> predicate = new HashMap<>();

            predicate.put("path", "/content/newshub/en/news");
            predicate.put("type", "cq:Page");

            predicate.put("group.p.or", "true");

            predicate.put("group.1_property", "jcr:content/jcr:title");
            predicate.put("group.1_property.operation", "like");
            predicate.put("group.1_property.value", "%" + keyword + "%");

            predicate.put("group.2_property", "jcr:content/subtitle");
            predicate.put("group.2_property.operation", "like");
            predicate.put("group.2_property.value", "%" + keyword + "%");

            predicate.put("p.limit", "10");

            predicate.put("orderby", "@jcr:content/publishDate");
            predicate.put("orderby.sort", "desc");

            Query query = queryBuilder.createQuery(
                    com.day.cq.search.PredicateGroup.create(predicate),
                    session
            );

            SearchResult result = query.getResult();

            response.setContentType("application/json");

            StringBuilder json = new StringBuilder();
            json.append("[");

            result.getHits().forEach(hit -> {
                try {

                    String path = hit.getPath();

                    json.append("{");
                    json.append("\"path\":\"").append(path).append("\"");
                    json.append("},");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (json.length() > 1) {
                json.deleteCharAt(json.length() - 1);
            }

            json.append("]");

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (resolver != null && resolver.isLive()) {
                resolver.close();
            }
        }
    }
}