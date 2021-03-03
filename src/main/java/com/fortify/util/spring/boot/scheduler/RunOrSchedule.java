package com.fortify.util.spring.boot.scheduler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class RunOrSchedule implements CommandLineRunner {
	private static final Logger LOG = LoggerFactory.getLogger(RunOrSchedule.class);
	@Value("${runOnce:false}") private boolean runOnce;
	@Autowired private TaskScheduler scheduler;
	@Autowired(required=false) private List<ISchedulableRunnerFactory> runnerFactories;
	@Autowired ApplicationContext context;

	@Override
	public void run(String... args) throws Exception {
		// TODO Check if there are any enabled runner factories, throw exception otherwise
		if ( runnerFactories==null || runnerFactories.isEmpty() ) {
			throw new RuntimeException("No runner factories are available; please check the classpath");
		} else {
			if ( isRunOnce(runnerFactories) ) {
				runOnce(runnerFactories);
			} else {
				schedule(runnerFactories);
			}
		}
	}
	
	private boolean isRunOnce(List<ISchedulableRunnerFactory> runners) {
		return runOnce || !runners.stream().anyMatch(this::hasSchedule);
	}
	
	private boolean hasSchedule(ISchedulableRunnerFactory runnerFactory) {
		return StringUtils.isNotBlank(runnerFactory.getCronSchedule())
				&& !"-".equals(runnerFactory.getCronSchedule());
	}

	private void runOnce(List<ISchedulableRunnerFactory> runnerFactories) {
		for ( ISchedulableRunnerFactory runnerFactory : runnerFactories ) {
			if ( runnerFactory.isEnabled() ) {
				ISchedulableRunner runner = runnerFactory.getRunner();
				LOG.debug("Running {}", runner);
				runner.run();
			}
		}
		SpringApplication.exit(context);
	}
	
	private void schedule(List<ISchedulableRunnerFactory> runnerFactories) {
		for ( ISchedulableRunnerFactory runnerFactory : runnerFactories ) {
			if ( runnerFactory.isEnabled() && hasSchedule(runnerFactory) ) {
				ISchedulableRunner runner = runnerFactory.getRunner();
				String cronSchedule = runnerFactory.getCronSchedule();
				LOG.debug("Scheduling {} with cron schedule {}", runner, cronSchedule);
				scheduler.schedule(runner, new CronTrigger(cronSchedule));
			}
		}
	}
	
}
