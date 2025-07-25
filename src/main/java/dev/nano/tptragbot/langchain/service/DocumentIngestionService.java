// Service for ingesting documents and storing embeddings in OnboardEase
package dev.nano.tptragbot.langchain.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.nano.tptragbot.common.model.Progress;
import dev.nano.tptragbot.langchain.configuration.DocumentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class DocumentIngestionService {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DocumentConfiguration documentConfiguration;
    private final Tokenizer tokenizer;
    
    public DocumentIngestionService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            DocumentConfiguration documentConfiguration,
            Tokenizer tokenizer) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.documentConfiguration = documentConfiguration;
        this.tokenizer = tokenizer;
    }

    public void ingestDocuments(List<String> urls, List<String> paths, Progress progress) {
        log.info("Starting document ingestion");

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(
                        DocumentSplitters.recursive(500, 100, tokenizer))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        List<Document> documents = documentConfiguration.documents(urls, paths);

        int totalDocuments = documents.size();
        log.info("Total documents: {}", totalDocuments);
        progress.setTotal(totalDocuments);

        for (Document document : documents) {
            ingestor.ingest(Collections.singletonList(document));
            progress.increment();
            log.info("Progress: {}", progress.getPercentage());
        }

        log.info("Document ingestion completed");
    }
}
