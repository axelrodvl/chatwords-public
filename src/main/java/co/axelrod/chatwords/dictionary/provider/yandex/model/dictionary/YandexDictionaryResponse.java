package co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "head",
        "def"
})
@Generated("jsonschema2pojo")
public class YandexDictionaryResponse {

    @JsonProperty("head")
    private Head head;
    @JsonProperty("def")
    private List<Definition> definition = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("head")
    public Head getHead() {
        return head;
    }

    @JsonProperty("head")
    public void setHead(Head head) {
        this.head = head;
    }

    @JsonProperty("def")
    public List<Definition> getDef() {
        return definition;
    }

    @JsonProperty("def")
    public void setDef(List<Definition> definition) {
        this.definition = definition;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
