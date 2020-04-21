package de.digitalcollections.cudami.server.controller.converter;

import de.digitalcollections.model.api.filter.FilterCriteria;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.stereotype.Component;

@Component
public class StringToFilterCriteriaGenericConverter implements GenericConverter {

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    Set<ConvertiblePair> convertibleTypes = new HashSet<>();
//    convertibleTypes.add(new ConvertiblePair(String.class, FilterCriteria.class));
    return convertibleTypes;
  }

  @Override
  public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
