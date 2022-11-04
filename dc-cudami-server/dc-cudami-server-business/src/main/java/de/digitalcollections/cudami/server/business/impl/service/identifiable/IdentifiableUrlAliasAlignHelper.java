package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
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
    if (checkIdentifiableExcluded()) {
      return;
    }
    if (identifiableInDatabase == null) {
      return;
    }
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
        newAlias.setTargetIdentifiableObjectType(actualIdentifiable.getIdentifiableObjectType());

        if (websiteUuid != null) {
          Website ws = new Website();
          ws.setUuid(websiteUuid);
          newAlias.setWebsite(ws);
        }
        newAlias.setPrimary(true);

        actualIdentifiable.getLocalizedUrlAliases().add(newAlias);
      }
    }

    // finally we set primary flag of current primary aliases to false
    unsetConflictingPrimaries(
        getPrimaryUrlAliases(identifiableInDatabase.getLocalizedUrlAliases(), null),
        getPrimaryUrlAliases(actualIdentifiable.getLocalizedUrlAliases(), null));
  }

  private boolean checkIdentifiableExcluded() {
    return cudamiConfig
            .getUrlAlias()
            .getGenerationExcludes()
            .contains(actualIdentifiable.getIdentifiableObjectType().toString())
        || actualIdentifiable.getLabel() == null;
  }

  public static <I extends Identifiable> boolean checkIdentifiableExcluded(
      I actualIdentifiable, CudamiConfig cudamiConfig) throws CudamiServiceException {

    if (actualIdentifiable == null || cudamiConfig == null) {
      throw new CudamiServiceException("Actual Identifiable and cudami config must not be null!");
    }

    IdentifiableUrlAliasAlignHelper<I> inst =
        new IdentifiableUrlAliasAlignHelper<>(actualIdentifiable, null, cudamiConfig, null);
    return inst.checkIdentifiableExcluded();
  }

  private void ensureDefaultAliasesExist() throws CudamiServiceException {
    if (checkIdentifiableExcluded()) {
      return;
    }
    LocalizedUrlAliases urlAliases = actualIdentifiable.getLocalizedUrlAliases();
    if (urlAliases == null) {
      urlAliases = new LocalizedUrlAliases();
      actualIdentifiable.setLocalizedUrlAliases(urlAliases);
    }
    for (Locale labelLang : actualIdentifiable.getLabel().getLocales()) {
      Locale urlAliasLang =
          Locale.forLanguageTag(
              labelLang.toLanguageTag().replaceFirst("-.*$", "")); // removes the script

      if (!urlAliases.containsKey(urlAliasLang)) {
        // We don't yet have an UrlAlias for that language (w/o script), so we must create one.

        // not for webpages
        if (!(actualIdentifiable instanceof Webpage)
            && (!urlAliases.containsKey(urlAliasLang)
                || urlAliases.get(urlAliasLang).stream()
                    .allMatch(alias -> alias.getWebsite() != null))) {
          // there is not any default alias (w/o website); create one.
          UrlAlias defaultAlias = new UrlAlias();
          defaultAlias.setTargetIdentifiableType(actualIdentifiable.getType());
          defaultAlias.setTargetLanguage(urlAliasLang);
          defaultAlias.setTargetUuid(actualIdentifiable.getUuid());
          defaultAlias.setTargetIdentifiableObjectType(
              actualIdentifiable.getIdentifiableObjectType());
          defaultAlias.setPrimary(!urlAliases.containsKey(urlAliasLang));
          try {
            String labelText = actualIdentifiable.getLabel().getText(labelLang);
            String slug = slugGeneratorService.apply(labelLang, labelText, null);
            defaultAlias.setSlug(slug);
          } catch (CudamiServiceException e) {
            throw new CudamiServiceException("An error occured during slug generation.", e);
          }
          urlAliases.add(defaultAlias);
        }
      }

      // check that a primary alias exists for this language (again w/o script), even for webpages
      if (urlAliases.get(urlAliasLang) == null
          || !urlAliases.get(urlAliasLang).stream().anyMatch(alias -> alias.isPrimary())) {
        throw new CudamiServiceException(
            String.format(
                "There is not any primary alias for language '%s' (%s) of identifiable '%s'.",
                urlAliasLang, labelLang, actualIdentifiable.getUuid()));
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
          // if new one is equal to alias from DB -> ignore
          // we must filter by primaries again since l. 285 can change it
          .filter(ua -> !ua.equals(primaryFromDb) && ua.isPrimary())
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
      // complete the aliases of the identifiable
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
