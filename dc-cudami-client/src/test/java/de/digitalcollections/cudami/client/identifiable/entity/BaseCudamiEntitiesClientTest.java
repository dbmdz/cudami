package de.digitalcollections.cudami.client.identifiable.entity;

import de.digitalcollections.cudami.client.identifiable.BaseCudamiIdentifiablesClientTest;
import de.digitalcollections.model.identifiable.entity.Entity;

public abstract class BaseCudamiEntitiesClientTest<
        E extends Entity, C extends CudamiEntitiesClient<E>>
    extends BaseCudamiIdentifiablesClientTest<E, C> {}
