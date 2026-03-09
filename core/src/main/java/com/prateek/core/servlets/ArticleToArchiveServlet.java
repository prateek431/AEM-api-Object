package com.prateek.core.servlets;


import com.google.gson.JsonObject;
import com.prateek.core.service.ArticleToArchive;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/newshub/archive",
                "sling.servlet.methods=POST"
        }
)
public class ArticleToArchiveServlet extends SlingAllMethodsServlet {

    @Reference
    private ArticleToArchive moveArticleToArchive;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String articlePath = request.getParameter("articlePath");

        JsonObject result = moveArticleToArchive.moveArticle(articlePath);

        response.getWriter().write(result.toString());
    }
}
