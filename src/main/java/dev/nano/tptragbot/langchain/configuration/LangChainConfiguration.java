// This class configures Langchain4j integration and beans for OnboardEase
package dev.nano.tptragbot.langchain.configuration;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.nano.tptragbot.langchain.agent.OnboardTrainingAssistant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static dev.nano.tptragbot.common.constant.Constant.LANGCHAIN_VECTOR_STORE_DATABASE_NAME;
import static dev.nano.tptragbot.common.constant.Constant.MODEL_NAME;


@Configuration
@RequiredArgsConstructor
public class LangChainConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(LangChainConfiguration.class);

    @Value("${langchain.timeout}")
    private Long timeout;

    @Value("${langchain.api-key}")
    private String apiKey;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.datasource.username}")
    private String databaseUsername;



    @Bean
    public OnboardTrainingAssistant chain(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            ChatMemory chatMemory
    ) {
        log.info("Creating OnboardTrainingAssistant bean");
        return AiServices.builder(OnboardTrainingAssistant.class)
                .chatLanguageModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .timeout(Duration.ofSeconds(timeout))
                        .build()
                )
                .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("Creating EmbeddingModel bean");
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("Creating PgVectorEmbeddingStore bean");
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5433)
                .database(LANGCHAIN_VECTOR_STORE_DATABASE_NAME)
                .user(databaseUsername)
                .password(databasePassword)
                .table(LANGCHAIN_VECTOR_STORE_DATABASE_NAME)
                .dimension(384)
                .build();
    }

    // chatMemory is a bean that is used to store the chat history
    // It allows the chat to remember the context of the conversation which enhance conversation context understanding
    @Bean
    public MessageWindowChatMemory chatMemory() {
        log.info("Creating MessageWindowChatMemory bean");
        return MessageWindowChatMemory.withMaxMessages(20);
    }

    @Bean
    Tokenizer tokenizer() {
        return new OpenAiTokenizer(MODEL_NAME);
    }
}
