package com.prateek.core.schedulers;


import com.prateek.core.service.AutoPublishService;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


@Component(service = Runnable.class)
@Designate(ocd=DemoScheduler.abc.class)
public class DemoScheduler implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(DemoScheduler.class);

    @Reference
    private Scheduler scheduler;

    @Reference
    private AutoPublishService autoPublishService;

    private String schedulerName="Demo Scheduler";

    @ObjectClassDefinition(name="Demo Scheduler")
    public @interface abc{
        @AttributeDefinition(
                name="Scheduler Enabled",
                description = "Enabled or Disable Scheduler"
        )
        boolean scheduler_enabled() default true;

        @AttributeDefinition(
                name = "Cron Expression",
                description = "Scheduler Cron Expression"
        )
        String scheduler_expression() default "0 0/5 * * * ?";
    };

    @Activate
    protected void activate(abc config){
        if(config.scheduler_enabled()){
            String cronExpression=config.scheduler_expression();
            ScheduleOptions options=scheduler.EXPR(cronExpression);
            options.name(schedulerName);
            options.canRunConcurrently(false);
            scheduler.schedule(this,options);
            logger.info("Auto Publish Scheduler started with cron: {}", cronExpression);
        }

    }

    @Deactivate
    protected void deactivate(){
        scheduler.unschedule(schedulerName);
        logger.info("Auto Publish Scheduler stopped");

    }
    @Override
    public void run(){
        logger.info("Publish scheduler running");
        autoPublishService.publishSchedulerPages();
    }
}
