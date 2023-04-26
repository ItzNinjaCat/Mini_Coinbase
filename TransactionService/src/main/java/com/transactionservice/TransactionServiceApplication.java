package com.transactionservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
@SpringBootApplication
@EnableKafkaStreams
public class TransactionServiceApplication {
	private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(TransactionServiceApplication.class, args);
	}
	@Bean
	public NewTopic transactions() {
		return TopicBuilder.name("transactions")
				.partitions(3)
				.compact()
				.build();
	}

	@Bean
	public NewTopic cryptoTopic() {
		return TopicBuilder.name("crypto")
				.partitions(3)
				.compact()
				.build();
	}

	@Bean
	public NewTopic fiatTopic() {
		return TopicBuilder.name("fiat")
				.partitions(3)
				.compact()
				.build();
	}

	@Bean
	public NewTopic transactionCompletedTopic() {
		return TopicBuilder.name("transaction-completed")
				.partitions(3)
				.compact()
				.build();
	}

	@Autowired
	TransactionService transactionService;

	@Bean
	public KStream<Long, TransactionDto> stream(StreamsBuilder builder) {
		JsonSerde<TransactionDto> transactionDtoSerde = new JsonSerde<>(TransactionDto.class);
		KStream<Long, TransactionDto> stream = builder
				.stream("fiat", Consumed.with(Serdes.Long(), transactionDtoSerde));
		stream.join(
						builder.stream("crypto"),
						transactionService::confirm,
						JoinWindows.of(Duration.ofSeconds(10)),
						StreamJoined.with(Serdes.Long(), transactionDtoSerde, transactionDtoSerde))
				.peek((k, t) -> LOG.info("Output: {}"))
				.to("transactions");

		return stream;
	}

	@Bean
	public KTable<Long, TransactionDto> table(StreamsBuilder builder) {
		KeyValueBytesStoreSupplier store =
				Stores.persistentKeyValueStore("transactions");
		JsonSerde<TransactionDto> TransactionDtoSerde = new JsonSerde<>(TransactionDto.class);
		KStream<Long, TransactionDto> stream = builder
				.stream("transactions", Consumed.with(Serdes.Long(), TransactionDtoSerde));
		return stream.toTable(Materialized.<Long, TransactionDto>as(store)
				.withKeySerde(Serdes.Long())
				.withValueSerde(TransactionDtoSerde));
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
