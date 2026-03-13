package com.prateek.core.service.impl;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.prateek.core.service.AutoPublishService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.HashMap;
import java.util.Map;

@Component(service=AutoPublishService.class)
public class AutoPublishServiceImpl implements AutoPublishService {
    private final Logger log= LoggerFactory.getLogger(AutoPublishServiceImpl.class);

    @Reference
    private Replicator replicator;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public void publishSchedulerPages (){
        Map<String,Object> params=new HashMap<>();
        params.put(ResourceResolverFactory.SUBSERVICE,"content-writer");


        try(ResourceResolver resolver=resolverFactory.getServiceResourceResolver(params)){
            String pagePath="/content/newshub/article/sample-page";
            Resource page = resolver.getResource(pagePath);
            log.info("Page exists: {}", page != null);

            Session session=resolver.adaptTo(Session.class);

            log.info("Attempting to publish page: {}", pagePath);

            replicator.replicate(session, ReplicationActionType.ACTIVATE, pagePath);

            log.info("Successfully published page: {}", pagePath);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
