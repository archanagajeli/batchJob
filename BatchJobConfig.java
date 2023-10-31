package gov.hi.dhs.bes.besint.batch_config;
import gov.hi.dhs.bes.mongodb.repository.batch_repo.BatchJobRep;
import gov.hi.dhs.bes.besint.config.JobCompletionNotificationListener;
import gov.hi.dhs.bes.mongodb.model.dto.BatchDTO;
import gov.hi.dhs.bes.db.entities.Client;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.transaction.PlatformTransactionManager;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchJobConfig {
    @Autowired
    private StepBuilderFactory builderFactory;
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private BatchJobRep interfaceRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Bean
    public Job batchJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("batchJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(batchJobStep()).build();

    }

    @Bean
    public Step batchJobStep() {
        return builderFactory.get("batchJobStep")
                .transactionManager(transactionManager)
                .<BatchDTO, BatchDTO>chunk(5).
                reader(dataReaderOracle())
                .writer(mongoWriter())
                .build();
    }
    @Bean
    @StepScope
    public FlatFileItemReader<BatchDTO> dataReaderOracle() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Client> criteriaQuery = builder.createQuery(Client.class);
        Root<Client> root = criteriaQuery.from(Client.class);
        criteriaQuery.multiselect(root.get("clientId"),
                root.get("firstName"),
                root.get("middleName"),
                root.get("lastName"),
                root.get("birthDate"));
        List<Client> oracleDataList = entityManager.createQuery(criteriaQuery).getResultList();
        var sb = new StringBuilder();
        oracleDataList.stream().limit(10).forEach((Client client) ->
        {
            sb.append(client.getClientId()).append(",")
                    .append(client.getFirstName()).append(",")
                    .append(client.getMiddleName()).append(",")
                    .append(client.getLastName()).append(",")
                    .append(client.getBirthDate());
            if (oracleDataList.size() > 1)
                sb.append(System.lineSeparator());
        });

        BeanWrapperFieldSetMapper<BatchDTO> mapper =
                new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(BatchDTO.class);
        return new FlatFileItemReaderBuilder<BatchDTO>().name("batchReader")
                .resource(new ByteArrayResource(sb.toString().getBytes())).delimited()
                .names("id", "name", "middleName", "surname", "birthDate")
                .fieldSetMapper(mapper).build();
    }

    @Bean
    @StepScope
    public ItemWriter<BatchDTO> mongoWriter() {
        return (List<? extends BatchDTO> batchInterface) -> {
            interfaceRepository.saveAll(batchInterface);
        };
    }

    private static BatchDTO getBatchInterface(BatchDTO batch) {
        BatchDTO anInterface = new BatchDTO
                (batch.getId(),
                        batch.getName(),
                        batch.getMiddleName(),
                        batch.getSurname(),
                        batch.getBirthDate());
        anInterface.setId(batch.getId());
        anInterface.setName(batch.getName());
        anInterface.setMiddleName(batch.getMiddleName());
        anInterface.setSurname(batch.getSurname());
        anInterface.setBirthDate(batch.getBirthDate());
        return anInterface;
    }


}
