package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.relation.EntityToEntityRelationService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.WorkService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The WorkService")
class WorkServiceImplTest extends AbstractServiceImplTest {

  private WorkRepository workRepository;

  private HookProperties hookProperties;
  private EntityToEntityRelationService entityRelationService;
  private IdentifierService identifierService;
  private LocaleService localeService;
  private UrlAliasService urlAliasService;
  private WorkService workService;

  @BeforeEach
  public void beforeEach() throws Exception {
    super.beforeEach();
    hookProperties = mock(HookProperties.class);
    entityRelationService = mock(EntityToEntityRelationService.class);
    identifierService = mock(IdentifierService.class);
    localeService = mock(LocaleService.class);
    urlAliasService = mock(UrlAliasService.class);
    workRepository = mock(WorkRepository.class);
    workService =
        new WorkServiceImpl(
            workRepository,
            identifierService,
            urlAliasService,
            hookProperties,
            localeService,
            entityRelationService,
            cudamiConfig);
  }

  @DisplayName("can save a work without relations")
  @Test
  public void saveWithoutRelations()
      throws RepositoryException, ValidationException, ServiceException {
    Work workToSave = Work.builder().label(Locale.GERMAN, "Erstlingswerk").build();
    Work savedWork = Work.builder().label(Locale.GERMAN, "Erstlingswerk").randomUuid().build();

    doAnswer(
            invocation -> {
              Object[] args = invocation.getArguments();
              ((Work) args[0]).setUuid(savedWork.getUuid());
              return null;
            })
        .when(workRepository)
        .save(eq(workToSave));

    workService.save(workToSave);

    assertThat(workToSave).isEqualTo(savedWork);
  }

  @DisplayName("saves the relations to a work, too")
  @Test
  public void saveWithRelations()
      throws ValidationException, ServiceException, RepositoryException {
    UUID authorUuid = UUID.randomUUID();
    Person author =
        Person.builder()
            .label(Locale.GERMAN, "Karl Ranseier")
            .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .uuid(authorUuid)
            .build();

    Work workToSave =
        Work.builder()
            .label(Locale.GERMAN, "Erstlingswerk")
            .relation(EntityRelation.builder().subject(author).predicate("is_creator_of").build())
            .build();

    Work savedWork =
        Work.builder()
            .uuid(UUID.randomUUID())
            .label(Locale.GERMAN, "Erstlingswerk")
            .relation(EntityRelation.builder().subject(author).predicate("is_creator_of").build())
            .build();

    doAnswer(
            invocation -> {
              Object[] args = invocation.getArguments();
              ((Work) args[0]).setUuid(savedWork.getUuid());
              return null;
            })
        .when(workRepository)
        .save(eq(workToSave));

    workService.save(workToSave);

    verify(entityRelationService, times(1))
        .setEntityRelations(eq(savedWork), eq(workToSave.getRelations()), eq(true));
  }

  @DisplayName("can update a work without relations")
  @Test
  public void updateWithoutRelations()
      throws ValidationException, ServiceException, RepositoryException {
    UUID uuid = UUID.randomUUID();
    Work workToUpdate = Work.builder().uuid(uuid).label(Locale.GERMAN, "Erstlingswerk").build();

    doAnswer(
            invocation -> {
              return null;
            })
        .when(workRepository)
        .update(eq(workToUpdate));
    when(workRepository.getByExample(eq(workToUpdate))).thenReturn(workToUpdate);

    workService.update(workToUpdate);

    assertThat(workToUpdate).isEqualTo(workToUpdate);

    verify(entityRelationService, times(1))
        .setEntityRelations(eq(workToUpdate), eq(List.of()), eq(false));
  }

  @DisplayName("can update a work with relations")
  @Test
  public void updateWithRelations()
      throws RepositoryException, ServiceException, ValidationException {
    UUID authorUuid = UUID.randomUUID();
    Person author =
        Person.builder()
            .label(Locale.GERMAN, "Karl Ranseier")
            .name(new LocalizedText(Locale.GERMAN, "Karl Ranseier"))
            .uuid(authorUuid)
            .build();

    UUID uuid = UUID.randomUUID();
    Work workToUpdate =
        Work.builder()
            .uuid(uuid)
            .label(Locale.GERMAN, "Erstlingswerk")
            .relation(EntityRelation.builder().subject(author).predicate("is_creator_of").build())
            .build();

    doAnswer(
            invocation -> {
              return null;
            })
        .when(workRepository)
        .update(eq(workToUpdate));
    when(workRepository.getByExample(eq(workToUpdate))).thenReturn(workToUpdate);

    workService.update(workToUpdate);

    assertThat(workToUpdate).isEqualTo(workToUpdate);

    verify(entityRelationService, times(1))
        .setEntityRelations(eq(workToUpdate), eq(workToUpdate.getRelations()), eq(false));
  }
}
