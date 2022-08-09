package co.axelrod.chatwords.image.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "aspect",
        "assets",
        "contributor",
        "description",
        "image_type",
        "has_model_release",
        "media_type"
})
@Generated("jsonschema2pojo")
public class Datum {

    @JsonProperty("id")
    private String id;
    @JsonProperty("aspect")
    private Double aspect;
    @JsonProperty("assets")
    private Assets assets;
    @JsonProperty("contributor")
    private Contributor contributor;
    @JsonProperty("description")
    private String description;
    @JsonProperty("image_type")
    private String imageType;
    @JsonProperty("has_model_release")
    private Boolean hasModelRelease;
    @JsonProperty("media_type")
    private String mediaType;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("aspect")
    public Double getAspect() {
        return aspect;
    }

    @JsonProperty("aspect")
    public void setAspect(Double aspect) {
        this.aspect = aspect;
    }

    @JsonProperty("assets")
    public Assets getAssets() {
        return assets;
    }

    @JsonProperty("assets")
    public void setAssets(Assets assets) {
        this.assets = assets;
    }

    @JsonProperty("contributor")
    public Contributor getContributor() {
        return contributor;
    }

    @JsonProperty("contributor")
    public void setContributor(Contributor contributor) {
        this.contributor = contributor;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("image_type")
    public String getImageType() {
        return imageType;
    }

    @JsonProperty("image_type")
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    @JsonProperty("has_model_release")
    public Boolean getHasModelRelease() {
        return hasModelRelease;
    }

    @JsonProperty("has_model_release")
    public void setHasModelRelease(Boolean hasModelRelease) {
        this.hasModelRelease = hasModelRelease;
    }

    @JsonProperty("media_type")
    public String getMediaType() {
        return mediaType;
    }

    @JsonProperty("media_type")
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
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
