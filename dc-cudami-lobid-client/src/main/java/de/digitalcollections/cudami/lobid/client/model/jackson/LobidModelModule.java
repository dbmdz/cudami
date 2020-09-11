package de.digitalcollections.cudami.lobid.client.model.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class LobidModelModule extends SimpleModule {

  public LobidModelModule() {
    super();
  }

  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);

    // context.setMixInAnnotations(Agent.class, AgentMixIn.class);
  }
}
