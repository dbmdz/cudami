package de.digitalcollections.cudami.server.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.mapper.Mapper;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentBlocksContainer;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.content.BulletListImpl;
import de.digitalcollections.prosemirror.model.impl.content.EmbeddedCodeBlockImpl;
import de.digitalcollections.prosemirror.model.impl.content.HardBreakImpl;
import de.digitalcollections.prosemirror.model.impl.content.HeadingImpl;
import de.digitalcollections.prosemirror.model.impl.content.ListItemImpl;
import de.digitalcollections.prosemirror.model.impl.content.MarkImpl;
import de.digitalcollections.prosemirror.model.impl.content.OrderedListImpl;
import de.digitalcollections.prosemirror.model.impl.content.ParagraphImpl;
import de.digitalcollections.prosemirror.model.impl.content.TextImpl;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;

public class XmlHttpMessageConverter {

  public HttpMessageConverter<Object> createConverter() {
    MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();
    XStreamMarshaller xstreamMarshaller = new XStreamMarshaller();

    xstreamMarshaller.setStreamDriver(new CdataXppDriver());

    Map<String, Class> aliases = new HashMap<>();
    aliases.put("bulletList", BulletListImpl.class);
    aliases.put("contentBlocksContainer", ContentBlocksContainer.class);
    aliases.put("document", DocumentImpl.class);
    aliases.put("embeddedCodeBlock", EmbeddedCodeBlockImpl.class);
    aliases.put("hardBreak", HardBreakImpl.class);
    aliases.put("heading", HeadingImpl.class);
    aliases.put("listItem", ListItemImpl.class);
    aliases.put("mark", MarkImpl.class);
    aliases.put("orderedList", OrderedListImpl.class);
    aliases.put("paragraph", ParagraphImpl.class);
    aliases.put("text", TextImpl.class);
    aliases.put("webpage", WebpageImpl.class);
    xstreamMarshaller.setAliases(aliases);

    Map<Class, String> attributes = new HashMap<>();
    attributes.put(MarkImpl.class, "type");
    xstreamMarshaller.setUseAttributeFor(attributes);

    XStream xStream = xstreamMarshaller.getXStream();
    xStream.aliasSystemAttribute(null, "class");

    xStream.registerConverter(new MapToFlatConverter(xStream.getMapper()));
    xStream.aliasField("body", TextImpl.class, "text");

    xmlConverter.setMarshaller(xstreamMarshaller);
    xmlConverter.setUnmarshaller(xstreamMarshaller);
    return xmlConverter;
  }

  class MapToFlatConverter extends MapConverter {

    public MapToFlatConverter(Mapper mapper) {
      super(mapper);
    }


    @Override
    public boolean canConvert(Class type) {
      return Map.class.isAssignableFrom(type);
    }


    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
      Map<Object, Object> map = (Map<Object, Object>) source;

      Set<Object> keySet = map.keySet();
      if ( keySet != null && !keySet.isEmpty() && keySet.iterator().next() instanceof String) {
        for (Entry<Object, Object> entry :  map.entrySet()) {
          if ( entry.getValue() != null ) {
            writer.startNode(entry.getKey().toString());
            writer.setValue(entry.getValue().toString());
            writer.endNode();
          }
        }
      } else {
        super.marshal(source, writer, context);
      }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
      return null;
    }
  }


  class CdataXppDriver extends XppDriver {

    @Override
    public HierarchicalStreamWriter createWriter(Writer out) {
      return new PrettyPrintWriter(out) {

        boolean cdata = false;

        @SuppressWarnings("rawtypes")
        public void startNode(String name, Class clazz) {
          cdata = "code".equalsIgnoreCase(name) || "body".equalsIgnoreCase(name);
          super.startNode(name, clazz);
        }

        @Override
        protected void writeText(QuickWriter writer, String text) {
          if ( cdata ) {
            writer.write("<![CDATA[");
            writer.write(text);
            writer.write("]]>");
          } else {
            super.writeText(writer, text);
          }
        }
      };
    }
  }

}
