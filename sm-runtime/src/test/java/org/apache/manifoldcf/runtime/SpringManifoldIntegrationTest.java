package org.apache.manifoldcf.runtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.apache.manifoldcf.core.connector.OutputConnector;
import org.apache.manifoldcf.core.connector.RepositoryConnector;
import org.apache.manifoldcf.runtime.orchestrator.JobOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SpringManifoldIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(SpringManifoldIntegrationTest.class);

    @Autowired
    private JobOrchestrator jobOrchestrator;

    @Autowired
    private RepositoryConnector repositoryConnector;

    @Autowired
    private OutputConnector outputConnector;

    @Autowired
    private VectorStore vectorStore;

    @TempDir
    Path tempDir;

    @Test
    void testEndToEndCrawlAndVectorSearch() throws IOException, InterruptedException {
        // 1. Create a sample file in the temporary directory
        Path testFile = tempDir.resolve("test-document.txt");
        String content = "Spring-Manifold Next-Gen is a powerful tool for document orchestration and vectorization. It supports high-intensity I/O operations and advanced Java 25 features.";
        Files.writeString(testFile, content);
        log.info("Created test file: {}", testFile.toAbsolutePath());

        // 2. Run the job orchestrator on the temporary directory
        jobOrchestrator.runJob(repositoryConnector, outputConnector, tempDir.toString());

        // Give some time for the persistence (if any async logic involved)
        Thread.sleep(1000);

        // 3. Verify the content in the Vector Store using a similarity search
        log.info("Performing similarity search for 'document orchestration'...");
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query("document orchestration")
                        .topK(5)
                        .similarityThreshold(0.1) // Lower threshold to ensure we find it
                        .build()
        );

        log.info("Found {} results in Vector Store.", results.size());
        results.forEach(doc -> log.info("Result Match: [Score: {}] Content: {}", doc.getMetadata().get("similarity_score"), doc.getText()));

        // 4. Assertions
        assertThat(results).withFailMessage("Expected at least one result in Vector Store search").isNotEmpty();
        assertThat(results.get(0).getText()).contains("Spring-Manifold");
        assertThat(results.get(0).getMetadata()).containsKey("uri");
    }
}
