package be.ordina.msdashboard.converters;

import be.ordina.msdashboard.node.Node;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

public class NodeToJsonConverter implements Converter<Node, String> {

    private static final Logger logger = LoggerFactory.getLogger(NodeToJsonConverter.class);

    private ObjectWriter objectWriter;

    public NodeToJsonConverter() {
        this.objectWriter = new ObjectMapper().writer();
    }

    @Override
    public String convert(Node source) {
        if (source == null)
            return null;

        try {
            return objectWriter.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            logger.error("unable to create string value", e);
            throw new IllegalArgumentException("", e);
        }
    }
}
