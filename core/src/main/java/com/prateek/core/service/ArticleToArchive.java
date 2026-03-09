package com.prateek.core.service;


import com.google.gson.JsonObject;
public interface ArticleToArchive {

    JsonObject moveArticle(String articlePath);

}
