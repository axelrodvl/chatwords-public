package co.axelrod.chatwords.image.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "preview",
        "small_thumb",
        "large_thumb",
        "huge_thumb",
        "preview_1000",
        "preview_1500"
})
@Generated("jsonschema2pojo")
public class Assets {

    @JsonProperty("preview")
    private Preview preview;
    @JsonProperty("small_thumb")
    private SmallThumb smallThumb;
    @JsonProperty("large_thumb")
    private LargeThumb largeThumb;
    @JsonProperty("huge_thumb")
    private HugeThumb hugeThumb;
    @JsonProperty("preview_1000")
    private Preview1000 preview1000;
    @JsonProperty("preview_1500")
    private Preview1500 preview1500;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("preview")
    public Preview getPreview() {
        return preview;
    }

    @JsonProperty("preview")
    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    @JsonProperty("small_thumb")
    public SmallThumb getSmallThumb() {
        return smallThumb;
    }

    @JsonProperty("small_thumb")
    public void setSmallThumb(SmallThumb smallThumb) {
        this.smallThumb = smallThumb;
    }

    @JsonProperty("large_thumb")
    public LargeThumb getLargeThumb() {
        return largeThumb;
    }

    @JsonProperty("large_thumb")
    public void setLargeThumb(LargeThumb largeThumb) {
        this.largeThumb = largeThumb;
    }

    @JsonProperty("huge_thumb")
    public HugeThumb getHugeThumb() {
        return hugeThumb;
    }

    @JsonProperty("huge_thumb")
    public void setHugeThumb(HugeThumb hugeThumb) {
        this.hugeThumb = hugeThumb;
    }

    @JsonProperty("preview_1000")
    public Preview1000 getPreview1000() {
        return preview1000;
    }

    @JsonProperty("preview_1000")
    public void setPreview1000(Preview1000 preview1000) {
        this.preview1000 = preview1000;
    }

    @JsonProperty("preview_1500")
    public Preview1500 getPreview1500() {
        return preview1500;
    }

    @JsonProperty("preview_1500")
    public void setPreview1500(Preview1500 preview1500) {
        this.preview1500 = preview1500;
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
