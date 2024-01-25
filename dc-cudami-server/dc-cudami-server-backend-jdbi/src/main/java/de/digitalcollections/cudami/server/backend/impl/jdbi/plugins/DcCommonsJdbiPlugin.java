package de.digitalcollections.cudami.server.backend.impl.jdbi.plugins;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;

public class DcCommonsJdbiPlugin implements JdbiPlugin {

  @Override
  public void customizeJdbi(Jdbi db) {
    db.registerArgument(new LocaleArgumentFactory());
    db.registerColumnMapper(new LocaleColumnMapperFactory());
    db.registerArgument(new MimeTypeArgumentFactory());
    db.registerColumnMapper(new MimeTypeColumnMapperFactory());
    db.registerArgument(new UrlArgumentFactory());
    db.registerColumnMapper(new UrlColumnMapperFactory());
    db.registerArgument(new LocaleSetArgumentFactory());
    db.registerColumnMapper(new LocaleSetColumnMapperFactory());
    db.registerArgument(new LocaleLinkedHashSetArgumentFactory());
    db.registerColumnMapper(new LocaleLinkedHashSetColumnMapperFactory());
  }
}
