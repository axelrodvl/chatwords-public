package co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "text",
        "tr"
})
@Generated("jsonschema2pojo")
public class Example {

    @JsonProperty("text")
    private String text;
    @JsonProperty("tr")
    private List<TranslationInside> tr = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("tr")
    public List<TranslationInside> getTr() {
        return tr;
    }

    @JsonProperty("tr")
    public void setTr(List<TranslationInside> tr) {
        this.tr = tr;
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
