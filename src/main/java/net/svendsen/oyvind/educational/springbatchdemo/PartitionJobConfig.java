package net.svendsen.oyvind.educational.springbatchdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.StepExecutionSplitter;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Configuration
@EnableBatchProcessing
public class PartitionJobConfig {

    @Autowired
    private JobBuilderFactory  jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job ParitionJob(Step partitionStep) {
        return jobBuilderFactory.get("partitionJob")
                .start(partitionStep)
                .build();
    }

    @Bean
    public Step partitionStep(Step demoTaskletStep, Partitioner partitioner) {
        return stepBuilderFactory.get("partitionStep")
                .partitioner(demoTaskletStep)
                .gridSize(4)
                .partitioner("partitionStepSlave", partitioner)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Partitioner partitioner() {
        return new Partitioner() {
            @Override public Map<String, ExecutionContext> partition(int gridSize) {
                HashMap<String, ExecutionContext> map = new HashMap<>();
                for (int i = 0; i < gridSize; i++) {
                    ExecutionContext executionContext = new ExecutionContext();
                    map.put("step"+i, executionContext);
                }
                return map;
            }
        };
    }
}
