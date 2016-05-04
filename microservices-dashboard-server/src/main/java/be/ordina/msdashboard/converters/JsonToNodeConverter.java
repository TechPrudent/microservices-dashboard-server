package be.ordina.msdashboard.converters;

import be.ordina.msdashboard.node.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class JsonToNodeConverter implements Converter<String, Node> {

    private static final Logger logger = LoggerFactory.getLogger(JsonToNodeConverter.class);

    private ObjectReader objectReader;

    public JsonToNodeConverter() {
        this.objectReader = new ObjectMapper().reader();
    }

    @Override
    public Node convert(String source) {
        try {
            if (isEmpty(source)) {
                return null;
            }
            return objectReader.forType(Node.class).readValue(source);
        } catch (IOException e) {
            logger.error("unable to read value", e);
            throw new IllegalArgumentException(source, e);
        }
    }
}
