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
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import de.digitalcollections.cudami.model.api.identifiable.resource.parts.ContentBlock;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TranslationImpl;
import de.digitalcollections.cudami.model.impl.identifiable.resource.WebpageImpl;
import de.digitalcollections.prosemirror.model.impl.DocumentImpl;
import de.digitalcollections.prosemirror.model.impl.MarkImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.BlockquoteImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.BulletListImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.CodeBlockImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.IFrameImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HardBreakImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.HeadingImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ListItemImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.OrderedListImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.ParagraphImpl;
import de.digitalcollections.prosemirror.model.impl.contentblocks.TextImpl;
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
    XStreamMarshaller xstreamMarshaller = new XStreamMarshaller();

    xstreamMarshaller.setStreamDriver(new CdataXppDriver());

    Map<String, Class> aliases = new HashMap<>();
    aliases.put("blockquote", BlockquoteImpl.class);
    aliases.put("bulletList", BulletListImpl.class);
    aliases.put("codeBlock", CodeBlockImpl.class);
    aliases.put("content", ContentBlock.class);
    aliases.put("contentBlocksContainer", MultilanguageDocument.class);
    aliases.put("document", DocumentImpl.class);
    aliases.put("embeddedCode", IFrameImpl.class);
    aliases.put("hardBreak", HardBreakImpl.class);
    aliases.put("heading", HeadingImpl.class);
    aliases.put("listItem", ListItemImpl.class);
    aliases.put("mark", MarkImpl.class);
    aliases.put("orderedList", OrderedListImpl.class);
    aliases.put("paragraph", ParagraphImpl.class);
    aliases.put("text", TextImpl.class);
    aliases.put("translation", TranslationImpl.class);
    aliases.put("webpage", WebpageImpl.class);
    xstreamMarshaller.setAliases(aliases);

    Map<Class, String> attributes = new HashMap<>();
    attributes.put(MarkImpl.class, "type");
    xstreamMarshaller.setUseAttributeFor(attributes);

    XStream xStream = xstreamMarshaller.getXStream();
    xStream.aliasSystemAttribute(null, "class");

    xStream.registerConverter(new MapToFlatConverter(xStream.getMapper()));
    xStream.aliasField("body", TextImpl.class, "text");
    xStream.aliasField("content", BlockquoteImpl.class, "contentBlocks");
    xStream.aliasField("content", BulletListImpl.class, "contentBlocks");
    xStream.aliasField("content", CodeBlockImpl.class, "contentBlocks");
    xStream.aliasField("content", DocumentImpl.class, "contentBlocks");
    xStream.aliasField("content", HeadingImpl.class, "contentBlocks");
    xStream.aliasField("content", ListItemImpl.class, "contentBlocks");
    xStream.aliasField("content", OrderedListImpl.class, "contentBlocks");
    xStream.aliasField("content", ParagraphImpl.class, "contentBlocks");

    xStream.setMode(XStream.NO_REFERENCES);

    MarshallingHttpMessageConverter xmlConverter = new MarshallingHttpMessageConverter();
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
      if (keySet != null && !keySet.isEmpty() && keySet.iterator().next() instanceof String) {
        for (Entry<Object, Object> entry : map.entrySet()) {
          if (entry.getValue() != null) {
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
          cdata = "body".equalsIgnoreCase(name);
          super.startNode(name, clazz);
        }

        @Override
        protected void writeText(QuickWriter writer, String text) {
          if (cdata) {
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
