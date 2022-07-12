package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ItemRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.DigitalObjectService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("The item service")
class ItemServiceImplTest {

  private DigitalObjectService digitalObjectService;
  private IdentifierService identifierService;
  private ItemService itemService;
  private ItemRepository itemRepository;
  private UrlAliasService urlAliasService;
  private HookProperties hookProperties;
  private LocaleService localeService;
  private CudamiConfig cudamiConfig;

  @BeforeEach
  public void beforeEach() {
    digitalObjectService = mock(DigitalObjectService.class);
    itemRepository = mock(ItemRepository.class);
    urlAliasService = mock(UrlAliasService.class);
    hookProperties = mock(HookProperties.class);
    localeService = mock(LocaleService.class);
    cudamiConfig = mock(CudamiConfig.class);
    itemService =
        new ItemServiceImpl(
            itemRepository,
            digitalObjectService,
            identifierService,
            urlAliasService,
            hookProperties,
            localeService,
            cudamiConfig);
  }

  @Test
  @DisplayName("returns false when connecting an existing item to a nonexisting digital object")
  public void connectExistingItemToNonexistingDigitalObject()
      throws ValidationException, ConflictException, IdentifiableServiceException {
    when(itemRepository.getByUuidAndFiltering(any(UUID.class), eq(null)))
        .thenReturn(Item.builder().build());

    assertThat(itemService.addDigitalObject(UUID.randomUUID(), UUID.randomUUID())).isFalse();
  }

  @Test
  @DisplayName("returns false when connecting a nonexisting item to an xisting digital object")
  public void connectNoneistingItemToExistingDigitalObject()
      throws ValidationException, ConflictException, IdentifiableServiceException {
    when(itemRepository.getByUuidAndFiltering(any(UUID.class), eq(null))).thenReturn(null);
    when(digitalObjectService.getByUuid(any(UUID.class)))
        .thenReturn(DigitalObject.builder().build());

    assertThat(itemService.addDigitalObject(UUID.randomUUID(), UUID.randomUUID())).isFalse();
  }

  @Test
  @DisplayName(
      "returns true and updates the item field in DigitalObject when the item field was emmpty")
  public void connectForTheFirstTime()
      throws ValidationException, ConflictException, IdentifiableServiceException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject = DigitalObject.builder().uuid(UUID.randomUUID()).build();
    when(itemRepository.getByUuidAndFiltering(eq(item.getUuid()), eq(null))).thenReturn(item);
    when(digitalObjectService.getByUuid(eq(digitalObject.getUuid()))).thenReturn(digitalObject);

    boolean actual = itemService.addDigitalObject(item.getUuid(), digitalObject.getUuid());
    assertThat(actual).isTrue();

    ArgumentCaptor<DigitalObject> digitalObjectArgumentCaptor =
        ArgumentCaptor.forClass(DigitalObject.class);
    verify(digitalObjectService, times(1)).update(digitalObjectArgumentCaptor.capture());

    assertThat(digitalObjectArgumentCaptor.getValue().getItem()).isEqualTo(item);
  }

  @Test
  @DisplayName(
      "returns true but does not update the DigitalObject, when it was already connected to the item")
  public void isAlreadyConnected()
      throws ValidationException, ConflictException, IdentifiableServiceException {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder().uuid(UUID.randomUUID()).item(item).build();
    when(itemRepository.getByUuidAndFiltering(eq(item.getUuid()), eq(null))).thenReturn(item);
    when(digitalObjectService.getByUuid(eq(digitalObject.getUuid()))).thenReturn(digitalObject);

    boolean actual = itemService.addDigitalObject(item.getUuid(), digitalObject.getUuid());
    assertThat(actual).isTrue();

    verify(digitalObjectService, never()).update(any(DigitalObject.class));
  }

  @Test
  @DisplayName("throws an exception, then the DigitalObject already belongs to another item")
  public void digitalObjectBelongsToOtherItem() {
    Item item = Item.builder().uuid(UUID.randomUUID()).build();
    DigitalObject digitalObject =
        DigitalObject.builder()
            .uuid(UUID.randomUUID())
            .item(Item.builder().uuid(UUID.randomUUID()).build())
            .build();
    when(itemRepository.getByUuidAndFiltering(eq(item.getUuid()), eq(null))).thenReturn(item);
    when(digitalObjectService.getByUuid(eq(digitalObject.getUuid()))).thenReturn(digitalObject);

    assertThrows(
        ConflictException.class,
        () -> itemService.addDigitalObject(item.getUuid(), digitalObject.getUuid()));
  }
}
