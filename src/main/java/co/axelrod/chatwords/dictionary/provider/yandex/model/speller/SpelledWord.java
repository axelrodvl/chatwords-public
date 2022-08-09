package co.axelrod.chatwords.dictionary.provider.yandex.model.speller;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "pos",
        "row",
        "col",
        "len",
        "word",
        "s"
})
@Generated("jsonschema2pojo")
public class SpelledWord {

    @JsonProperty("code")
    private Integer code;
    @JsonProperty("pos")
    private Integer pos;
    @JsonProperty("row")
    private Integer row;
    @JsonProperty("col")
    private Integer col;
    @JsonProperty("len")
    private Integer len;
    @JsonProperty("word")
    private String word;
    @JsonProperty("s")
    private List<String> s = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    @JsonProperty("code")
    public void setCode(Integer code) {
        this.code = code;
    }

    @JsonProperty("pos")
    public Integer getPos() {
        return pos;
    }

    @JsonProperty("pos")
    public void setPos(Integer pos) {
        this.pos = pos;
    }

    @JsonProperty("row")
    public Integer getRow() {
        return row;
    }

    @JsonProperty("row")
    public void setRow(Integer row) {
        this.row = row;
    }

    @JsonProperty("col")
    public Integer getCol() {
        return col;
    }

    @JsonProperty("col")
    public void setCol(Integer col) {
        this.col = col;
    }

    @JsonProperty("len")
    public Integer getLen() {
        return len;
    }

    @JsonProperty("len")
    public void setLen(Integer len) {
        this.len = len;
    }

    @JsonProperty("word")
    public String getWord() {
        return word;
    }

    @JsonProperty("word")
    public void setWord(String word) {
        this.word = word;
    }

    @JsonProperty("s")
    public List<String> getS() {
        return s;
    }

    @JsonProperty("s")
    public void setS(List<String> s) {
        this.s = s;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SpelledWord.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("code");
        sb.append('=');
        sb.append(((this.code == null) ? "<null>" : this.code));
        sb.append(',');
        sb.append("pos");
        sb.append('=');
        sb.append(((this.pos == null) ? "<null>" : this.pos));
        sb.append(',');
        sb.append("row");
        sb.append('=');
        sb.append(((this.row == null) ? "<null>" : this.row));
        sb.append(',');
        sb.append("col");
        sb.append('=');
        sb.append(((this.col == null) ? "<null>" : this.col));
        sb.append(',');
        sb.append("len");
        sb.append('=');
        sb.append(((this.len == null) ? "<null>" : this.len));
        sb.append(',');
        sb.append("word");
        sb.append('=');
        sb.append(((this.word == null) ? "<null>" : this.word));
        sb.append(',');
        sb.append("s");
        sb.append('=');
        sb.append(((this.s == null) ? "<null>" : this.s));
        sb.append(',');
        sb.append("additionalProperties");
        sb.append('=');
        sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}