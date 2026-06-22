package org.apache.manifoldcf.runtime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.apache.manifoldcf.core.connector.RepositoryConnector;
import org.apache.manifoldcf.core.connector.OutputConnector;
import org.apache.manifoldcf.runtime.orchestrator.JobOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication(
    scanBasePackages = "org.apache.manifoldcf",
    exclude = { PgVectorStoreAutoConfiguration.class }
)
public class SpringManifoldNextGenApplication {

    private static final Logger log = LoggerFactory.getLogger(SpringManifoldNextGenApplication.class);

    @Bean
    @Profile("!test")
    public CommandLineRunner runSampleJob(
            JobOrchestrator orchestrator,
            RepositoryConnector repositoryConnector,
            OutputConnector outputConnector) {
        return args -> {
            log.info("--- Spring-Manifold Next-Gen Bootstrap ---");
            log.info("Detected Repository Connector: {}", repositoryConnector.getName());
            log.info("Detected Output Connector: {}", outputConnector.getName());

            // Example path to scan - can be overridden by application properties
            String scanPath = System.getProperty("user.home") + "/Desktop/mcf-test"; 
            
            log.info("Triggering sample crawl job on path: {}", scanPath);
            
            // Run the job using the Virtual Thread-based orchestrator
            orchestrator.runJob(repositoryConnector, outputConnector, scanPath);
            
            log.info("--- Bootstrap sequence completed ---");
        };
    }
}
