package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class IdentifiableUrlAliasAlignHelper<I extends Identifiable> {

  private CudamiConfig cudamiConfig;
  private SlugGeneratorService slugGeneratorService;

  // the old one that is already in database
  private I identifiableInDatabase;
  // the one that should be saved later
  private I actualIdentifiable;

  private IdentifiableUrlAliasAlignHelper(
      I actualIdentifiable,
      I identifiableInDatabase,
      CudamiConfig cudamiConfig,
      SlugGeneratorService slugGeneratorService) {
    this.actualIdentifiable = actualIdentifiable;
    this.identifiableInDatabase = identifiableInDatabase;
    this.cudamiConfig = cudamiConfig;
    this.slugGeneratorService = slugGeneratorService;
  }

  /**
   * Align all UrlAliases to fit in with the updating/updated identifiable. It is also ensured that
   * default aliases exist and missing ones are created.
   *
   * @param actualIdentifiable the object that is/was updated
   * @param identifiableInDatabase the existing object saved in storage prior to any update
   * @param cudamiConfig
   * @param slugGeneratorService slug generator method
   * @throws CudamiServiceException
   */
  public static <I extends Identifiable> void alignForUpdate(
      I actualIdentifiable,
      I identifiableInDatabase,
      CudamiConfig cudamiConfig,
      SlugGeneratorService slugGeneratorService)
      throws CudamiServiceException {

    if (actualIdentifiable == null
        || identifiableInDatabase == null
        || cudamiConfig == null
        || slugGeneratorService == null) {
      throw new CudamiServiceException(
          "Missing argument. Every parameter must be passed (not null).");
    }

    IdentifiableUrlAliasAlignHelper<I> inst =
        new IdentifiableUrlAliasAlignHelper<>(
            actualIdentifiable, identifiableInDatabase, cudamiConfig, slugGeneratorService);
    try {
      inst.fixMissingLocalizedUrlAliases();
      inst.alignLabelUpdate();
      inst.ensureDefaultAliasesExist();
    } catch (RuntimeException e) {
      throw new CudamiServiceException(
          "Uncaught error in IdentifiableUrlAliasAlignHelper::alignForUpdate.", e);
    }
  }

  /**
   * Ensure that default aliases exist (new ones can be created). For an update of an existing
   * identifiable {@link #alignForUpdate(Identifiable, Identifiable, CudamiConfig,
   * SlugGeneratorService)} should be used instead.
   *
   * @param actualIdentifiable the (new) identifiable
   * @param cudamiConfig
   * @param slugGeneratorService slug generator method
   * @throws CudamiServiceException
   */
  public static <I extends Identifiable> void checkDefaultAliases(
      I actualIdentifiable, CudamiConfig cudamiConfig, SlugGeneratorService slugGeneratorService)
      throws CudamiServiceException {

    if (actualIdentifiable == null || cudamiConfig == null || slugGeneratorService == null) {
      throw new CudamiServiceException(
          "Missing argument. Every parameter must be passed (not null).");
    }

    IdentifiableUrlAliasAlignHelper<I> inst =
        new IdentifiableUrlAliasAlignHelper<>(
            actualIdentifiable, null, cudamiConfig, slugGeneratorService);
    try {
      inst.ensureDefaultAliasesExist();
    } catch (RuntimeException e) {
      throw new CudamiServiceException(
          "Uncaught error in IdentifiableUrlAliasAlignHelper::checkDefaultAliases.", e);
    }
  }

  private void alignLabelUpdate() throws CudamiServiceException {
    if (actualIdentifiable == null
        || (actualIdentifiable instanceof Entity)
            && cudamiConfig
                .getUrlAlias()
                .getGenerationExcludes()
                .contains(((Entity) actualIdentifiable).getEntityType())
        || actualIdentifiable.getLabel() == null) {
      return;
    }
    if (identifiableInDatabase == null) {
      return;
    }
    List<UrlAlias> newAliases = new ArrayList<>();
    for (Locale langFromDb : identifiableInDatabase.getLabel().getLocales()) {
      // check all in db EXISTING label languages; we are into label changes ONLY
      String labelSavedInDb = identifiableInDatabase.getLabel().get(langFromDb);
      String labelInIdentifiable = actualIdentifiable.getLabel().get(langFromDb);
      if (Objects.equals(labelSavedInDb, labelInIdentifiable) || labelInIdentifiable == null) {
        continue;
      }
      // if we get any NPE here then a former safe or update operation went wrong already; should
      // never happen
      List<UrlAlias> primariesFromDb =
          getPrimaryUrlAliases(identifiableInDatabase.getLocalizedUrlAliases(), langFromDb);
      if (actualIdentifiable.getLocalizedUrlAliases() == null) {
        actualIdentifiable.setLocalizedUrlAliases(new LocalizedUrlAliases());
      }
      for (UrlAlias primaryFromDb : primariesFromDb) {
        final UUID websiteUuid =
            primaryFromDb.getWebsite() != null ? primaryFromDb.getWebsite().getUuid() : null;
        String newSlug = slugGeneratorService.apply(langFromDb, labelInIdentifiable, websiteUuid);
        // if this slug already exists in the identifiable then we must silently go on, otherwise we
        // will add it
        if (actualIdentifiable.getLocalizedUrlAliases().flatten().stream()
            .anyMatch(
                ua ->
                    Objects.equals(ua.getSlug(), newSlug)
                        && Objects.equals(ua.getTargetLanguage(), langFromDb)
                        && Objects.equals(
                            ua.getWebsite() != null ? ua.getWebsite().getUuid() : null,
                            websiteUuid))) {
          continue;
        }
        UrlAlias newAlias = new UrlAlias();
        newAlias.setSlug(newSlug);
        newAlias.setTargetIdentifiableType(actualIdentifiable.getType());
        newAlias.setTargetLanguage(langFromDb);
        newAlias.setTargetUuid(actualIdentifiable.getUuid());
        if (actualIdentifiable instanceof Entity) {
          newAlias.setTargetEntityType(((Entity) actualIdentifiable).getEntityType());
        }
        if (websiteUuid != null) {
          Website ws = new Website();
          ws.setUuid(websiteUuid);
          newAlias.setWebsite(ws);
        }
        newAlias.setPrimary(true);

        actualIdentifiable.getLocalizedUrlAliases().add(newAlias);
        newAliases.add(newAlias);
      }
    }

    // finally we set primary flag of current primary aliases to false
    unsetConflictingPrimaries(
        getPrimaryUrlAliases(identifiableInDatabase.getLocalizedUrlAliases(), null), newAliases);
  }

  private void ensureDefaultAliasesExist() throws CudamiServiceException {
    if ((actualIdentifiable instanceof Entity)
        && cudamiConfig
            .getUrlAlias()
            .getGenerationExcludes()
            .contains(((Entity) actualIdentifiable).getEntityType())) {
      return;
    }
    LocalizedUrlAliases urlAliases = actualIdentifiable.getLocalizedUrlAliases();
    if (urlAliases == null) {
      urlAliases = new LocalizedUrlAliases();
      actualIdentifiable.setLocalizedUrlAliases(urlAliases);
    }
    for (Locale lang : actualIdentifiable.getLabel().getLocales()) {
      // not for webpages
      if (!(actualIdentifiable instanceof Webpage)
          && (!urlAliases.containsKey(lang)
              || urlAliases.get(lang).stream().allMatch(alias -> alias.getWebsite() != null))) {
        // there is not any default alias (w/o website); create one.
        UrlAlias defaultAlias = new UrlAlias();
        defaultAlias.setTargetIdentifiableType(actualIdentifiable.getType());
        defaultAlias.setTargetLanguage(lang);
        defaultAlias.setTargetUuid(actualIdentifiable.getUuid());
        if (actualIdentifiable instanceof Entity) {
          defaultAlias.setTargetEntityType(((Entity) actualIdentifiable).getEntityType());
        }
        defaultAlias.setPrimary(!urlAliases.containsKey(lang));
        try {
          defaultAlias.setSlug(
              slugGeneratorService.apply(lang, actualIdentifiable.getLabel().getText(lang), null));
        } catch (CudamiServiceException e) {
          throw new CudamiServiceException("An error occured during slug generation.", e);
        }
        urlAliases.add(defaultAlias);
      }

      // check that a primary alias exists for this language, even for webpages
      if (urlAliases.get(lang) == null
          || !urlAliases.get(lang).stream().anyMatch(alias -> alias.isPrimary())) {
        throw new CudamiServiceException(
            String.format(
                "There is not any primary alias for language '%s' of identifiable '%s'.",
                lang, actualIdentifiable.getUuid()));
      }
    }
  }

  private void fixMissingLocalizedUrlAliases() {
    if (actualIdentifiable.getLocalizedUrlAliases() == null
        || actualIdentifiable.getLocalizedUrlAliases().isEmpty()) {
      // if this field is unset then we check the DB
      LocalizedUrlAliases aliasesFromDb = identifiableInDatabase.getLocalizedUrlAliases();
      actualIdentifiable.setLocalizedUrlAliases(aliasesFromDb);
    } else if (actualIdentifiable.getLocalizedUrlAliases().flatten().stream()
        .allMatch(ua -> ua.isPrimary())) {
      // there are only primary aliases: conflicting ones in DB must be unset
      // conflicting aliases: equal websiteUuid & targetLanguage
      LocalizedUrlAliases urlAliasesToUpdate = actualIdentifiable.getLocalizedUrlAliases();
      // only primary aliases (as the var name suggests)
      List<UrlAlias> allPrimariesFromDb =
          getPrimaryUrlAliases(identifiableInDatabase.getLocalizedUrlAliases(), null);
      // now we check whether any primary from the DB conflict with the new ones
      unsetConflictingPrimaries(allPrimariesFromDb, urlAliasesToUpdate.flatten());
    }
  }

  private List<UrlAlias> getPrimaryUrlAliases(
      LocalizedUrlAliases localizedUrlAliases, Locale lang) {
    if (localizedUrlAliases == null) {
      return new ArrayList<>();
    }
    return localizedUrlAliases.flatten().stream()
        .filter(ua -> ua.isPrimary() && (lang != null ? ua.getTargetLanguage().equals(lang) : true))
        .collect(Collectors.toList());
  }

  private void unsetConflictingPrimaries(
      List<UrlAlias> primariesFromDb, List<UrlAlias> newPrimaryAliases) {
    if (primariesFromDb == null || newPrimaryAliases == null) {
      return;
    }
    for (UrlAlias primaryFromDb : primariesFromDb) {
      if (newPrimaryAliases.stream()
          .filter(ua -> !ua.equals(primaryFromDb)) // if new one is equal to alias from DB -> ignore
          .anyMatch(
              ua ->
                  (ua.getWebsite() != null
                              && primaryFromDb.getWebsite() != null
                              && ua.getWebsite()
                                  .getUuid()
                                  .equals(primaryFromDb.getWebsite().getUuid())
                          || ua.getWebsite() == primaryFromDb.getWebsite())
                      && Objects.equals(
                          ua.getTargetLanguage(), primaryFromDb.getTargetLanguage()))) {
        // UrlAlias in the database conflicts with a new created one
        // so we set primary flag of the old one to false
        primaryFromDb.setPrimary(false);
      }
      // must be outside preceding `if` to avoid creation of new aliases in
      // `ensureDefaultAliasesExist`
      // either set primary of the alias in the identifiable or add the primary from database to
      // complete
      // tha aliases of the identifiable
      Optional<UrlAlias> oldPrimary =
          actualIdentifiable.getLocalizedUrlAliases().flatten().stream()
              .filter(ua -> Objects.equals(ua.getUuid(), primaryFromDb.getUuid()))
              .findFirst();
      if (oldPrimary.isPresent()) {
        oldPrimary.get().setPrimary(primaryFromDb.isPrimary());
      } else {
        actualIdentifiable.getLocalizedUrlAliases().add(primaryFromDb);
      }
    }
  }

  // We do not want to have any services in this class but we need the slug generator.
  // It can easyly be passed into a parameter of this functional interface type.
  public interface SlugGeneratorService {
    String apply(Locale locale, String label, UUID website) throws CudamiServiceException;
  }
}
