package co.axelrod.chatwords.dictionary.provider.yandex.model.dictionary;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "text",
        "pos",
        "syn",
        "mean",
        "ex"
})
@Generated("jsonschema2pojo")
public class Translation {

    @JsonProperty("text")
    private String text;
    @JsonProperty("pos")
    private String pos;
    @JsonProperty("syn")
    private List<Synonym> synonym = null;
    @JsonProperty("mean")
    private List<Mean> mean = null;
    @JsonProperty("ex")
    private List<Example> examples = null;
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

    @JsonProperty("pos")
    public String getPos() {
        return pos;
    }

    @JsonProperty("pos")
    public void setPos(String pos) {
        this.pos = pos;
    }

    @JsonProperty("syn")
    public List<Synonym> getSyn() {
        return synonym;
    }

    @JsonProperty("syn")
    public void setSyn(List<Synonym> synonym) {
        this.synonym = synonym;
    }

    @JsonProperty("mean")
    public List<Mean> getMean() {
        return mean;
    }

    @JsonProperty("mean")
    public void setMean(List<Mean> mean) {
        this.mean = mean;
    }

    @JsonProperty("ex")
    public List<Example> getEx() {
        return examples;
    }

    @JsonProperty("ex")
    public void setEx(List<Example> examples) {
        this.examples = examples;
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
